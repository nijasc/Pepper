package com.buhlergroup.pepper.action.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public final class WifiCameraManager {

    private static final String TAG = "WifiCamera";

    private static final int CONNECT_TIMEOUT_MS = 4000;
    private static final int READ_TIMEOUT_MS = 8000;
    private static final int EVENT_POLL_ATTEMPTS = 40;
    private static final long EVENT_POLL_INTERVAL_MS = 250;

    private static final int PKT_INIT_COMMAND_REQUEST = 1;
    private static final int PKT_INIT_COMMAND_ACK = 2;
    private static final int PKT_INIT_EVENT_REQUEST = 3;
    private static final int PKT_INIT_EVENT_ACK = 4;
    private static final int PKT_OPERATION_REQUEST = 6;
    private static final int PKT_OPERATION_RESPONSE = 7;
    private static final int PKT_START_DATA = 9;
    private static final int PKT_DATA = 10;
    private static final int PKT_END_DATA = 12;

    private static final int DATA_PHASE_NONE = 1;
    private static final int DATA_PHASE_IN = 2;

    private static final int OP_OPEN_SESSION = 0x1002;
    private static final int OP_GET_OBJECT = 0x1009;
    private static final int OP_EOS_SET_REMOTE_MODE = 0x9114;
    private static final int OP_EOS_SET_EVENT_MODE = 0x9115;
    private static final int OP_EOS_GET_EVENT = 0x9116;
    private static final int OP_EOS_REMOTE_RELEASE_ON = 0x9128;
    private static final int OP_EOS_REMOTE_RELEASE_OFF = 0x9129;

    private static final int RESPONSE_OK = 0x2001;
    private static final int EOS_EVENT_OBJECT_ADDED = 0xc181;
    private static final int EOS_EVENT_OBJECT_ADDED_EX = 0xc18a;

    private static final int PROTOCOL_VERSION = 0x00010000;
    private static final String HOST_NAME = "Pepper";

    private final byte[] clientGuid = {
            (byte) 0x42, (byte) 0x55, (byte) 0x45, (byte) 0x48, (byte) 0x4c, (byte) 0x45, (byte) 0x52, 0x00,
            0x50, 0x45, 0x50, 0x50, 0x45, 0x52, 0x00, 0x01
    };

    private Socket commandSocket;
    private Socket eventSocket;
    private DataInputStream commandIn;
    private OutputStream commandOut;
    private int transactionId = 0;

    public boolean testConnection(String ip, int port) {
        try {
            openCommandChannel(ip, port);
            return true;
        } catch (IOException e) {
            Log.w(TAG, "testConnection failed: " + e.getMessage());
            return false;
        } finally {
            close();
        }
    }

    public Bitmap capture(String ip, int port) {
        try {
            int connectionNumber = openCommandChannel(ip, port);
            openEventChannel(ip, port, connectionNumber);

            executeCommand(OP_OPEN_SESSION, 1);
            executeCommand(OP_EOS_SET_REMOTE_MODE, 1);
            executeCommand(OP_EOS_SET_EVENT_MODE, 1);

            executeCommand(OP_EOS_REMOTE_RELEASE_ON, 3, 0);
            executeCommand(OP_EOS_REMOTE_RELEASE_OFF, 3, 0);

            int objectHandle = pollForObjectHandle();
            if (objectHandle == 0) {
                Log.w(TAG, "No object handle received from camera");
                return null;
            }

            byte[] jpeg = executeCommandWithDataIn(OP_GET_OBJECT, objectHandle);
            if (jpeg == null || jpeg.length == 0) {
                return null;
            }
            return BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
        } catch (IOException e) {
            Log.w(TAG, "capture failed: " + e.getMessage());
            return null;
        } finally {
            close();
        }
    }

    private int openCommandChannel(String ip, int port) throws IOException {
        commandSocket = new Socket();
        commandSocket.connect(new InetSocketAddress(ip, port), CONNECT_TIMEOUT_MS);
        commandSocket.setSoTimeout(READ_TIMEOUT_MS);
        commandIn = new DataInputStream(commandSocket.getInputStream());
        commandOut = commandSocket.getOutputStream();

        writePacket(commandOut, PKT_INIT_COMMAND_REQUEST, buildInitCommandRequest());
        Packet ack = readPacket(commandIn);
        if (ack.type != PKT_INIT_COMMAND_ACK || ack.payload.length < 4) {
            throw new IOException("Unexpected init command ack: type " + ack.type);
        }
        return ByteBuffer.wrap(ack.payload).order(ByteOrder.LITTLE_ENDIAN).getInt(0);
    }

    private void openEventChannel(String ip, int port, int connectionNumber) throws IOException {
        eventSocket = new Socket();
        eventSocket.connect(new InetSocketAddress(ip, port), CONNECT_TIMEOUT_MS);
        eventSocket.setSoTimeout(READ_TIMEOUT_MS);
        DataInputStream eventIn = new DataInputStream(eventSocket.getInputStream());
        OutputStream eventOut = eventSocket.getOutputStream();

        ByteBuffer payload = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        payload.putInt(connectionNumber);
        writePacket(eventOut, PKT_INIT_EVENT_REQUEST, payload.array());

        Packet ack = readPacket(eventIn);
        if (ack.type != PKT_INIT_EVENT_ACK) {
            throw new IOException("Unexpected init event ack: type " + ack.type);
        }
    }

    private byte[] buildInitCommandRequest() {
        byte[] host = (HOST_NAME).getBytes(StandardCharsets.UTF_16LE);
        ByteBuffer buffer = ByteBuffer.allocate(16 + host.length + 2 + 4).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(clientGuid);
        buffer.put(host);
        buffer.putShort((short) 0);
        buffer.putInt(PROTOCOL_VERSION);
        return buffer.array();
    }

    private void executeCommand(int opCode, int... params) throws IOException {
        int tid = ++transactionId;
        writePacket(commandOut, PKT_OPERATION_REQUEST, buildOperationRequest(DATA_PHASE_NONE, opCode, tid, params));
        Packet response = readOperationResponse();
        int code = ByteBuffer.wrap(response.payload).order(ByteOrder.LITTLE_ENDIAN).getShort(0) & 0xffff;
        if (code != RESPONSE_OK) {
            Log.w(TAG, "Operation 0x" + Integer.toHexString(opCode) + " response 0x" + Integer.toHexString(code));
        }
    }

    private byte[] executeCommandWithDataIn(int opCode, int... params) throws IOException {
        int tid = ++transactionId;
        writePacket(commandOut, PKT_OPERATION_REQUEST, buildOperationRequest(DATA_PHASE_IN, opCode, tid, params));

        ByteArrayOutputStream data = new ByteArrayOutputStream();
        while (true) {
            Packet packet = readPacket(commandIn);
            if (packet.type == PKT_START_DATA) {
                continue;
            }
            if (packet.type == PKT_DATA) {
                data.write(packet.payload, 4, packet.payload.length - 4);
                continue;
            }
            if (packet.type == PKT_END_DATA) {
                if (packet.payload.length > 4) {
                    data.write(packet.payload, 4, packet.payload.length - 4);
                }
                break;
            }
            if (packet.type == PKT_OPERATION_RESPONSE) {
                return data.toByteArray();
            }
        }
        readOperationResponse();
        return data.toByteArray();
    }

    private Packet readOperationResponse() throws IOException {
        while (true) {
            Packet packet = readPacket(commandIn);
            if (packet.type == PKT_OPERATION_RESPONSE) {
                return packet;
            }
        }
    }

    private byte[] buildOperationRequest(int dataPhase, int opCode, int tid, int[] params) {
        ByteBuffer buffer = ByteBuffer.allocate(4 + 2 + 4 + params.length * 4).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(dataPhase);
        buffer.putShort((short) opCode);
        buffer.putInt(tid);
        for (int param : params) {
            buffer.putInt(param);
        }
        return buffer.array();
    }

    private int pollForObjectHandle() throws IOException {
        for (int attempt = 0; attempt < EVENT_POLL_ATTEMPTS; attempt++) {
            byte[] events = executeCommandWithDataIn(OP_EOS_GET_EVENT);
            int handle = parseObjectHandle(events);
            if (handle != 0) {
                return handle;
            }
            try {
                Thread.sleep(EVENT_POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return 0;
            }
        }
        return 0;
    }

    private int parseObjectHandle(byte[] events) {
        if (events == null || events.length < 8) {
            return 0;
        }
        ByteBuffer buffer = ByteBuffer.wrap(events).order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.remaining() >= 8) {
            int start = buffer.position();
            long size = buffer.getInt() & 0xffffffffL;
            int type = buffer.getInt();
            if (size < 8 || start + size > events.length) {
                break;
            }
            if ((type == EOS_EVENT_OBJECT_ADDED || type == EOS_EVENT_OBJECT_ADDED_EX) && size >= 12) {
                return buffer.getInt();
            }
            buffer.position((int) (start + size));
        }
        return 0;
    }

    private void writePacket(OutputStream out, int type, byte[] payload) throws IOException {
        int length = 8 + payload.length;
        ByteBuffer header = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        header.putInt(length);
        header.putInt(type);
        out.write(header.array());
        out.write(payload);
        out.flush();
    }

    private Packet readPacket(DataInputStream in) throws IOException {
        byte[] header = new byte[8];
        in.readFully(header);
        ByteBuffer buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
        int length = buffer.getInt();
        int type = buffer.getInt();
        int payloadLength = length - 8;
        if (payloadLength < 0 || payloadLength > 64 * 1024 * 1024) {
            throw new IOException("Invalid packet length " + length);
        }
        byte[] payload = new byte[payloadLength];
        readFully(in, payload);
        return new Packet(type, payload);
    }

    private void readFully(InputStream in, byte[] buffer) throws IOException {
        int offset = 0;
        while (offset < buffer.length) {
            int read = in.read(buffer, offset, buffer.length - offset);
            if (read < 0) {
                throw new IOException("Stream closed while reading packet");
            }
            offset += read;
        }
    }

    private void close() {
        closeQuietly(commandSocket);
        closeQuietly(eventSocket);
        commandSocket = null;
        eventSocket = null;
        commandIn = null;
        commandOut = null;
    }

    private void closeQuietly(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static final class Packet {
        final int type;
        final byte[] payload;

        Packet(int type, byte[] payload) {
            this.type = type;
            this.payload = payload;
        }
    }
}
