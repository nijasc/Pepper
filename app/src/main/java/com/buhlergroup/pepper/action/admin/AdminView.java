package com.buhlergroup.pepper.action.admin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.attract.AttractSettings;
import com.buhlergroup.pepper.action.camera.CameraSettings;
import com.buhlergroup.pepper.action.camera.WifiCameraManager;
import com.buhlergroup.pepper.action.dance.DanceLibraryController;
import com.buhlergroup.pepper.action.navigation.NavigationController;
import com.buhlergroup.pepper.action.raffle.RaffleRepository;
import com.buhlergroup.pepper.debug.DebugLog;
import com.buhlergroup.pepper.lang.SupportedLanguage;
import com.buhlergroup.pepper.openai.history.HistoryEntry;
import com.buhlergroup.pepper.openai.history.HistoryRole;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.buhlergroup.pepper.action.admin.PanelNavigator.PANEL_PIN;
import static com.buhlergroup.pepper.action.admin.PanelNavigator.PANEL_MENU;
import static com.buhlergroup.pepper.action.admin.PanelNavigator.PANEL_DEVLOG;
import static com.buhlergroup.pepper.action.admin.PanelNavigator.PANEL_GALLERY;
import static com.buhlergroup.pepper.action.admin.PanelNavigator.PANEL_DETAIL;
import static com.buhlergroup.pepper.action.admin.PanelNavigator.PANEL_LANG;
import static com.buhlergroup.pepper.action.admin.PanelNavigator.PANEL_HISTORY;
import static com.buhlergroup.pepper.action.admin.PanelNavigator.PANEL_RAFFLE_CREATE;
import static com.buhlergroup.pepper.action.admin.PanelNavigator.PANEL_RAFFLE;
import static com.buhlergroup.pepper.action.admin.PanelNavigator.PANEL_CAMERA;
import static com.buhlergroup.pepper.action.admin.PanelNavigator.PANEL_STATUS;
import static com.buhlergroup.pepper.action.admin.PanelNavigator.PANEL_STATS;
import static com.buhlergroup.pepper.action.admin.PanelNavigator.PANEL_ATTRACT;
import static com.buhlergroup.pepper.action.admin.PanelNavigator.PANEL_DEBUG;

public class AdminView extends FrameLayout {

    private static final String TAG = "AdminView";

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private PinController pinController;
    private DashboardController dashboard;

    private View pinPanel;
    private View menuPanel;
    private View devLogPanel;
    private View galleryPanel;
    private View detailPanel;
    private View langPanel;
    private View historyPanel;
    private View raffleCreatePanel;
    private View rafflePanel;
    private View cameraPanel;
    private View statusPanel;
    private View statsPanel;
    private View debugPanel;
    private CheckBox debugEnabled;
    private TextView debugStatus;
    private TextView debugLogText;
    private ScrollView debugLogScroll;
    private View attractPanel;
    private View adminHeader;
    private TextView adminHeaderTitle;
    private PanelNavigator panelNav;
    private CheckBox attractEnabled;
    private EditText attractIdle;
    private EditText attractGreet;
    private ScrollView devLogScroll;
    private ScrollView historyScroll;
    private LinearLayout historyContainer;

    private TextView devLogText;
    private TextView langCurrent;
    private SelfieGalleryController galleryController;
    private RaffleAdminController raffleAdmin;

    private EditText cameraIp;
    private EditText cameraPort;
    private CheckBox cameraEnabled;
    private TextView cameraStatus;

    public AdminView(Context context) {
        super(context);
        init(context);
    }

    public AdminView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AdminView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_admin, this, true);
        setBackgroundColor(ContextCompat.getColor(context, R.color.game_overlay));
        setClickable(true);
        setFocusable(true);

        pinPanel = findViewById(R.id.adminPinPanel);
        menuPanel = findViewById(R.id.adminMenuPanel);
        devLogPanel = findViewById(R.id.adminDevLogPanel);
        galleryPanel = findViewById(R.id.adminGalleryPanel);
        detailPanel = findViewById(R.id.adminDetailPanel);
        langPanel = findViewById(R.id.adminLangPanel);
        historyPanel = findViewById(R.id.adminHistoryPanel);
        raffleCreatePanel = findViewById(R.id.adminRaffleCreatePanel);
        rafflePanel = findViewById(R.id.adminRafflePanel);
        cameraPanel = findViewById(R.id.adminCameraPanel);
        statusPanel = findViewById(R.id.adminStatusPanel);
        statsPanel = findViewById(R.id.adminStatsPanel);
        debugPanel = findViewById(R.id.adminDebugPanel);
        debugEnabled = findViewById(R.id.debugEnabled);
        debugStatus = findViewById(R.id.debugStatus);
        debugLogText = findViewById(R.id.adminDebugText);
        debugLogScroll = findViewById(R.id.adminDebugScroll);
        attractPanel = findViewById(R.id.adminAttractPanel);
        attractEnabled = findViewById(R.id.attractEnabled);
        attractIdle = findViewById(R.id.attractIdle);
        attractGreet = findViewById(R.id.attractGreet);
        devLogScroll = findViewById(R.id.adminDevLogScroll);
        historyScroll = findViewById(R.id.adminHistoryScroll);
        historyContainer = findViewById(R.id.adminHistoryContainer);

        devLogText = findViewById(R.id.adminDevLogText);
        langCurrent = findViewById(R.id.adminLangCurrent);

        cameraIp = findViewById(R.id.cameraIp);
        cameraPort = findViewById(R.id.cameraPort);
        cameraEnabled = findViewById(R.id.cameraEnabled);
        cameraStatus = findViewById(R.id.cameraStatus);

        adminHeader = findViewById(R.id.adminHeader);
        adminHeaderTitle = findViewById(R.id.adminHeaderTitle);
        findViewById(R.id.adminHeaderBack).setOnClickListener(v -> goBack());
        findViewById(R.id.adminHeaderClose).setOnClickListener(v -> hide());

        panelNav = new PanelNavigator(adminHeader, adminHeaderTitle, this::onPanelShown);
        panelNav.register(PANEL_PIN, pinPanel);
        panelNav.register(PANEL_MENU, menuPanel);
        panelNav.register(PANEL_DEVLOG, devLogPanel);
        panelNav.register(PANEL_GALLERY, galleryPanel);
        panelNav.register(PANEL_DETAIL, detailPanel);
        panelNav.register(PANEL_LANG, langPanel);
        panelNav.register(PANEL_HISTORY, historyPanel);
        panelNav.register(PANEL_RAFFLE_CREATE, raffleCreatePanel);
        panelNav.register(PANEL_RAFFLE, rafflePanel);
        panelNav.register(PANEL_CAMERA, cameraPanel);
        panelNav.register(PANEL_STATUS, statusPanel);
        panelNav.register(PANEL_STATS, statsPanel);
        panelNav.register(PANEL_DEBUG, debugPanel);
        panelNav.register(PANEL_ATTRACT, attractPanel);

        pinController = new PinController(this, () -> panelNav.show(PANEL_MENU));
        dashboard = new DashboardController(this, dbExecutor);
        galleryController = new SelfieGalleryController(this, dbExecutor, panelNav);
        raffleAdmin = new RaffleAdminController(this, dbExecutor, panelNav, galleryController, this::hide);
        findViewById(R.id.adminPinCancel).setOnClickListener(v -> hide());
        findViewById(R.id.adminClose).setOnClickListener(v -> hide());
        findViewById(R.id.adminClearHistory).setOnClickListener(v -> onClearHistory());
        findViewById(R.id.adminDevLogs).setOnClickListener(v -> showDevLog());
        findViewById(R.id.adminSelfies).setOnClickListener(v -> galleryController.showGallery());
        findViewById(R.id.adminLanguage).setOnClickListener(v -> showLanguage());
        findViewById(R.id.adminLangDe).setOnClickListener(v -> setLanguage(SupportedLanguage.GERMAN));
        findViewById(R.id.adminLangEn).setOnClickListener(v -> setLanguage(SupportedLanguage.ENGLISH));
        findViewById(R.id.adminLangIt).setOnClickListener(v -> setLanguage(SupportedLanguage.ITALIAN));
        findViewById(R.id.adminLangEs).setOnClickListener(v -> setLanguage(SupportedLanguage.SPANISH));
        findViewById(R.id.adminLangFr).setOnClickListener(v -> setLanguage(SupportedLanguage.FRENCH));
        findViewById(R.id.adminHistory).setOnClickListener(v -> showHistory());
        findViewById(R.id.adminRaffle).setOnClickListener(v -> raffleAdmin.openRaffle());
        findViewById(R.id.adminCamera).setOnClickListener(v -> showCamera());
        findViewById(R.id.adminStatus).setOnClickListener(v -> showStatus());
        findViewById(R.id.statusRefresh).setOnClickListener(v -> showStatus());
        findViewById(R.id.adminStats).setOnClickListener(v -> showStats());
        findViewById(R.id.adminStatsExport).setOnClickListener(v -> dashboard.exportStats());
        findViewById(R.id.adminDebug).setOnClickListener(v -> showDebug());
        debugEnabled.setOnClickListener(v -> DebugLog.get().setEnabled(getContext(), debugEnabled.isChecked()));
        findViewById(R.id.debugRefresh).setOnClickListener(v -> renderDebugLog());
        findViewById(R.id.debugExport).setOnClickListener(v -> exportDebugLog());
        findViewById(R.id.debugClear).setOnClickListener(v -> clearDebugLog());
        findViewById(R.id.adminAttract).setOnClickListener(v -> showAttract());
        findViewById(R.id.attractSave).setOnClickListener(v -> saveAttract());
        findViewById(R.id.cameraTest).setOnClickListener(v -> testCamera());
        findViewById(R.id.cameraSave).setOnClickListener(v -> saveCamera());
        findViewById(R.id.adminNavigation).setOnClickListener(v -> openNavigation());
        findViewById(R.id.adminDances).setOnClickListener(v -> openDanceLibrary());
        findViewById(R.id.adminDsgvo).setOnClickListener(v -> showDsgvoAccessDialog());
        findViewById(R.id.adminChangePin).setOnClickListener(v -> showChangePinDialog());
    }

    public void open() {
        post(() -> {
            pinController.resetEntry();
            if (pinController.isLocked()) {
                pinController.showLockoutMessage();
            } else {
                pinController.clearError();
            }
            panelNav.show(PANEL_PIN);
            setVisibility(VISIBLE);
            bringToFront();
        });
    }

    public void hide() {
        AdminController.get().markClosed();
        galleryController.releaseDetailServer();
        dashboard.stopRefresh();
        post(() -> setVisibility(GONE));
    }

    private void openNavigation() {
        NavigationController.get().open();
        hide();
    }

    private void openDanceLibrary() {
        DanceLibraryController.get().open();
        hide();
    }

    private void onPanelShown(int which) {
        dashboard.stopRefresh();
        if (which == PANEL_MENU) {
            dashboard.startRefresh();
        }
    }

    private void goBack() {
        if (panelNav.current() == PANEL_DETAIL) {
            galleryController.showGallery();
        } else {
            panelNav.show(PANEL_MENU);
        }
    }

    private void showLanguage() {
        updateLanguageLabel();
        panelNav.show(PANEL_LANG);
    }

    private void setLanguage(SupportedLanguage language) {
        AdminController.get().setLanguage(language);
        updateLanguageLabel();
    }

    private void updateLanguageLabel() {
        SupportedLanguage current = AdminController.get().getCurrentLanguage();
        langCurrent.setText(current != null ? current.getDisplayName() : "");
    }

    private void showHistory() {
        historyContainer.removeAllViews();
        List<HistoryEntry> entries = AdminController.get().getConversation();
        if (entries.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText(R.string.admin_history_empty);
            empty.setTextColor(0xCCFFFFFF);
            int pad = dp(16);
            empty.setPadding(pad, pad, pad, pad);
            historyContainer.addView(empty);
        } else {
            for (HistoryEntry entry : entries) {
                historyContainer.addView(createBubble(entry));
            }
        }
        panelNav.show(PANEL_HISTORY);
        historyScroll.post(() -> historyScroll.fullScroll(View.FOCUS_DOWN));
    }

    private TextView createBubble(HistoryEntry entry) {
        boolean user = entry.getRole() == HistoryRole.USER;
        TextView bubble = new TextView(getContext());
        bubble.setText(entry.getContent());
        bubble.setTextColor(0xFFFFFFFF);
        bubble.setBackgroundResource(user ? R.drawable.bg_bubble_user : R.drawable.bg_bubble_assistant);
        bubble.setMaxWidth(dp(560));
        int ph = dp(16);
        int pv = dp(10);
        bubble.setPadding(ph, pv, ph, pv);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = user ? Gravity.END : Gravity.START;
        int margin = dp(6);
        params.setMargins(margin, margin, margin, margin);
        bubble.setLayoutParams(params);
        return bubble;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void showChangePinDialog() {
        EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setHint(R.string.admin_change_pin_hint);
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.admin_change_pin)
                .setView(input)
                .setNegativeButton(R.string.admin_back, null)
                .setPositiveButton(R.string.raffle_save, (d, w) -> {
                    String pin = input.getText().toString().trim();
                    if (pin.matches("\\d{4}")) {
                        AdminSettings.setPin(getContext(), pin);
                        Toast.makeText(getContext(), R.string.admin_change_pin_saved,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), R.string.admin_change_pin_invalid,
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void showDsgvoAccessDialog() {
        EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setHint(R.string.dsgvo_access_hint);
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.dsgvo_access_title)
                .setView(input)
                .setNegativeButton(R.string.admin_back, null)
                .setPositiveButton(R.string.dsgvo_access_search,
                        (d, w) -> runDsgvoAccess(input.getText().toString().trim()))
                .show();
    }

    private void runDsgvoAccess(String email) {
        if (email.isEmpty()) {
            return;
        }
        dbExecutor.submit(() -> {
            String report = RaffleRepository.get(getContext()).buildAccessReport(email);
            post(() -> showDsgvoResult(email, report));
        });
    }

    private void showDsgvoResult(String email, String report) {
        if (report == null) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.dsgvo_access_title)
                    .setMessage(R.string.dsgvo_access_none)
                    .setPositiveButton(R.string.admin_back, null)
                    .show();
            return;
        }
        new AlertDialog.Builder(getContext())
                .setTitle(getContext().getString(R.string.dsgvo_report_title, email))
                .setMessage(report)
                .setNeutralButton(R.string.dsgvo_access_share, (d, w) -> shareDsgvoReport(email, report))
                .setPositiveButton(R.string.admin_back, null)
                .show();
    }

    private void shareDsgvoReport(String email, String report) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, getContext().getString(R.string.dsgvo_report_title, email));
        share.putExtra(Intent.EXTRA_TEXT, report);
        Intent chooser = Intent.createChooser(share, getContext().getString(R.string.dsgvo_access_share));
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            getContext().startActivity(chooser);
        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.admin_export_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void showAttract() {
        attractEnabled.setChecked(AttractSettings.isEnabled(getContext()));
        attractIdle.setText(String.valueOf(AttractSettings.getIdleMinutes(getContext())));
        attractGreet.setText(String.valueOf(AttractSettings.getGreetSeconds(getContext())));
        panelNav.show(PANEL_ATTRACT);
    }

    private void saveAttract() {
        int idle = parseIntOr(attractIdle, AttractSettings.DEFAULT_IDLE_MINUTES);
        int greet = parseIntOr(attractGreet, AttractSettings.DEFAULT_GREET_SECONDS);
        AttractSettings.save(getContext(), attractEnabled.isChecked(), idle, greet);
        Toast.makeText(getContext(), R.string.attract_saved, Toast.LENGTH_SHORT).show();
    }

    private int parseIntOr(EditText field, int fallback) {
        try {
            return Integer.parseInt(field.getText().toString().trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private void showStats() {
        dashboard.refreshStats();
        panelNav.show(PANEL_STATS);
    }

    private void showDebug() {
        debugEnabled.setChecked(DebugLog.get().isEnabled());
        renderDebugLog();
        panelNav.show(PANEL_DEBUG);
    }

    private void renderDebugLog() {
        String status = DebugLog.get().getStatus();
        debugStatus.setText(getContext().getString(R.string.debug_status_label) + " "
                + (status.isEmpty() ? "—" : status));
        List<String> log = DebugLog.get().snapshot();
        if (log.isEmpty()) {
            debugLogText.setText(R.string.debug_empty);
        } else {
            StringBuilder text = new StringBuilder();
            for (String entry : log) {
                text.append(entry).append('\n');
            }
            debugLogText.setText(text.toString());
        }
        debugLogScroll.post(() -> debugLogScroll.fullScroll(View.FOCUS_DOWN));
    }

    private void clearDebugLog() {
        DebugLog.get().clear();
        renderDebugLog();
    }

    private void exportDebugLog() {
        String content = DebugLog.get().export();
        File file = new File(getContext().getCacheDir(), "pepper_debug_log.txt");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            Log.w(TAG, "Debug-Export fehlgeschlagen: " + e.getMessage());
            Toast.makeText(getContext(), R.string.admin_export_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        shareDebugLog(file);
    }

    private void shareDebugLog(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(getContext(),
                    getContext().getPackageName() + ".fileprovider", file);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_SUBJECT, getContext().getString(R.string.debug_export_title));
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent chooser = Intent.createChooser(share, getContext().getString(R.string.debug_export));
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(chooser);
        } catch (Exception e) {
            Log.w(TAG, "Debug-Log teilen fehlgeschlagen: " + e.getMessage());
            Toast.makeText(getContext(), R.string.admin_export_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void showStatus() {
        panelNav.show(PANEL_STATUS);
        dashboard.refreshStatus();
    }

    private void showCamera() {
        cameraIp.setText(CameraSettings.getIp(getContext()));
        cameraPort.setText(String.valueOf(CameraSettings.getPort(getContext())));
        cameraEnabled.setChecked(CameraSettings.isEnabled(getContext()));
        cameraStatus.setText("");
        panelNav.show(PANEL_CAMERA);
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
        dbExecutor.submit(() -> {
            boolean reachable = new WifiCameraManager().testConnection(ip, port);
            post(() -> cameraStatus.setText(
                    reachable ? R.string.camera_status_ok : R.string.camera_status_fail));
        });
    }

    private void saveCamera() {
        CameraSettings.save(getContext(), cameraIp.getText().toString().trim(),
                readCameraPort(), cameraEnabled.isChecked());
        Toast.makeText(getContext(), R.string.camera_saved, Toast.LENGTH_SHORT).show();
    }

    private void onClearHistory() {
        boolean cleared = AdminController.get().clearHistory();
        Toast.makeText(getContext(),
                cleared ? R.string.admin_history_cleared : R.string.admin_pin_error,
                Toast.LENGTH_SHORT).show();
        panelNav.show(PANEL_MENU);
    }

    private void showDevLog() {
        List<String> log = AdminController.get().getDevLog();
        StringBuilder text = new StringBuilder();
        if (log.isEmpty()) {
            text.append(getContext().getString(R.string.admin_devlog_empty));
        } else {
            for (String entry : log) {
                text.append(entry).append('\n');
            }
        }
        devLogText.setText(text.toString());
        panelNav.show(PANEL_DEVLOG);
        devLogScroll.post(() -> devLogScroll.fullScroll(View.FOCUS_DOWN));
    }

}
