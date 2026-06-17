package com.buhlergroup.pepper.action.admin;

import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.camera.CameraSettings;
import com.buhlergroup.pepper.action.camera.WifiCameraManager;

import java.util.concurrent.Executor;

final class CameraPanelController {

    private final View root;
    private final Executor executor;
    private final PanelNavigator panelNav;
    private final EditText cameraIp;
    private final EditText cameraPort;
    private final CheckBox cameraEnabled;
    private final TextView cameraStatus;

    CameraPanelController(View root, Executor executor, PanelNavigator panelNav) {
        this.root = root;
        this.executor = executor;
        this.panelNav = panelNav;
        this.cameraIp = root.findViewById(R.id.cameraIp);
        this.cameraPort = root.findViewById(R.id.cameraPort);
        this.cameraEnabled = root.findViewById(R.id.cameraEnabled);
        this.cameraStatus = root.findViewById(R.id.cameraStatus);
        root.findViewById(R.id.cameraTest).setOnClickListener(v -> testCamera());
        root.findViewById(R.id.cameraSave).setOnClickListener(v -> saveCamera());
    }

    void showCamera() {
        cameraIp.setText(CameraSettings.getIp(root.getContext()));
        cameraPort.setText(String.valueOf(CameraSettings.getPort(root.getContext())));
        cameraEnabled.setChecked(CameraSettings.isEnabled(root.getContext()));
        cameraStatus.setText("");
        panelNav.show(PanelNavigator.PANEL_CAMERA);
    }

    private int readCameraPort() {
        try {
            int port = Integer.parseInt(cameraPort.getText().toString().trim());
            return port > 0 ? port : CameraSettings.DEFAULT_PORT;
        } catch (NumberFormatException e) {
            return CameraSettings.DEFAULT_PORT;
        }
    }

    private void testCamera() {
        String ip = cameraIp.getText().toString().trim();
        if (ip.isEmpty()) {
            cameraStatus.setText(R.string.camera_status_no_ip);
            return;
        }
        int port = readCameraPort();
        cameraStatus.setText(R.string.camera_status_testing);
        executor.execute(() -> {
            boolean reachable = new WifiCameraManager().testConnection(ip, port);
            root.post(() -> cameraStatus.setText(
                    reachable ? R.string.camera_status_ok : R.string.camera_status_fail));
        });
    }

    private void saveCamera() {
        CameraSettings.save(root.getContext(), cameraIp.getText().toString().trim(),
                readCameraPort(), cameraEnabled.isChecked());
        Toast.makeText(root.getContext(), R.string.camera_saved, Toast.LENGTH_SHORT).show();
    }
}
