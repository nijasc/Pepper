package com.buhlergroup.pepper.action.admin;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.buhlergroup.pepper.PepperApplication;
import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.attract.AttractController;
import com.buhlergroup.pepper.action.attract.AttractSettings;
import com.buhlergroup.pepper.action.camera.CameraSettings;
import com.buhlergroup.pepper.action.camera.WifiCameraManager;
import com.buhlergroup.pepper.action.dance.DanceLibraryController;
import com.buhlergroup.pepper.action.navigation.NavigationController;
import com.buhlergroup.pepper.action.raffle.RaffleRepository;
import com.buhlergroup.pepper.action.raffle.RaffleSettings;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntity;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntryEntity;
import com.buhlergroup.pepper.action.raffle.data.RaffleStatus;
import com.buhlergroup.pepper.action.selfie.NetworkUtils;
import com.buhlergroup.pepper.action.selfie.QrGenerator;
import com.buhlergroup.pepper.action.selfie.SelfieController;
import com.buhlergroup.pepper.action.selfie.SelfieRepository;
import com.buhlergroup.pepper.action.selfie.SelfieSettings;
import com.buhlergroup.pepper.action.selfie.data.SelfieEntity;
import com.buhlergroup.pepper.lang.SupportedLanguage;
import com.buhlergroup.pepper.net.Connectivity;
import com.buhlergroup.pepper.stats.Stats;
import com.buhlergroup.pepper.openai.history.HistoryEntry;
import com.buhlergroup.pepper.openai.history.HistoryRole;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AdminView extends FrameLayout {

    private static final String TAG = "AdminView";
    private static final int MAX_PIN_ATTEMPTS = 5;
    private static final long PIN_LOCKOUT_MS = 60000;
    private static final int PANEL_PIN = 0;
    private static final int PANEL_MENU = 1;
    private static final int PANEL_DEVLOG = 2;
    private static final int PANEL_GALLERY = 3;
    private static final int PANEL_DETAIL = 4;
    private static final int PANEL_LANG = 5;
    private static final int PANEL_HISTORY = 6;
    private static final int PANEL_RAFFLE_CREATE = 7;
    private static final int PANEL_RAFFLE = 8;
    private static final int PANEL_CAMERA = 9;
    private static final int PANEL_STATUS = 10;
    private static final int PANEL_STATS = 11;
    private static final int PANEL_ATTRACT = 12;

    private static final long DASH_REFRESH_MS = 15000;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private final Handler dashHandler = new Handler(Looper.getMainLooper());
    private final Runnable dashRefresh = new Runnable() {
        @Override
        public void run() {
            refreshDashboardStatus();
            dashHandler.postDelayed(this, DASH_REFRESH_MS);
        }
    };
    private final StringBuilder entered = new StringBuilder();
    private int pinAttempts = 0;
    private long pinLockoutUntil = 0;

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
    private TextView statsText;
    private View attractPanel;
    private View adminHeader;
    private TextView adminHeaderTitle;
    private int currentPanel = PANEL_PIN;
    private CheckBox attractEnabled;
    private EditText attractIdle;
    private EditText attractGreet;
    private TextView statusWifi;
    private TextView statusOpenAi;
    private TextView statusBattery;
    private TextView statusUptime;
    private TextView dashWifi;
    private TextView dashOpenAi;
    private TextView dashBattery;
    private TextView dashUptime;
    private ScrollView devLogScroll;
    private ScrollView historyScroll;
    private LinearLayout historyContainer;

    private TextView pinDots;
    private TextView pinError;
    private TextView devLogText;
    private TextView galleryEmpty;
    private TextView langCurrent;
    private Button exportAllButton;
    private RecyclerView selfieGrid;
    private SelfieAdapter selfieAdapter;

    private ImageView detailImage;
    private ImageView detailQr;
    private TextView detailQrHint;
    private TextView detailNumber;
    private TextView detailDate;
    private Button detailFavorite;
    private SelfieEntity currentDetail;
    private boolean detailServerHeld = false;

    private EditText raffleTitle;
    private EditText raffleDescription;
    private Button raffleEndDateButton;
    private CheckBox raffleRequiresSelfie;
    private CheckBox raffleRequiresPhone;
    private EditText raffleRetentionDays;
    private TextView raffleCreateError;
    private Button raffleCreateSave;
    private long raffleEndDateMillis;

    private TextView raffleOverviewTitle;
    private TextView raffleOverviewStatus;
    private LinearLayout raffleEntries;
    private Button raffleCloseButton;
    private Button raffleFinishButton;
    private Button raffleDeleteButton;
    private Button raffleDrawButton;
    private Button raffleRedrawButton;
    private Button raffleEmailButton;
    private TextView raffleWinnerView;
    private long currentRaffleId;
    private String currentRaffleTitle;
    private RaffleEntryEntity currentWinner;

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
        statsText = findViewById(R.id.adminStatsText);
        attractPanel = findViewById(R.id.adminAttractPanel);
        attractEnabled = findViewById(R.id.attractEnabled);
        attractIdle = findViewById(R.id.attractIdle);
        attractGreet = findViewById(R.id.attractGreet);
        statusWifi = findViewById(R.id.statusWifi);
        statusOpenAi = findViewById(R.id.statusOpenAi);
        statusBattery = findViewById(R.id.statusBattery);
        statusUptime = findViewById(R.id.statusUptime);
        dashWifi = findViewById(R.id.dashWifi);
        dashOpenAi = findViewById(R.id.dashOpenAi);
        dashBattery = findViewById(R.id.dashBattery);
        dashUptime = findViewById(R.id.dashUptime);
        devLogScroll = findViewById(R.id.adminDevLogScroll);
        historyScroll = findViewById(R.id.adminHistoryScroll);
        historyContainer = findViewById(R.id.adminHistoryContainer);

        pinDots = findViewById(R.id.adminPinDots);
        pinError = findViewById(R.id.adminPinError);
        devLogText = findViewById(R.id.adminDevLogText);
        galleryEmpty = findViewById(R.id.adminGalleryEmpty);
        langCurrent = findViewById(R.id.adminLangCurrent);
        exportAllButton = findViewById(R.id.adminExportAll);
        selfieGrid = findViewById(R.id.adminSelfieGrid);
        detailImage = findViewById(R.id.adminDetailImage);
        detailQr = findViewById(R.id.adminDetailQr);
        detailQrHint = findViewById(R.id.adminDetailQrHint);
        detailNumber = findViewById(R.id.adminDetailNumber);
        detailDate = findViewById(R.id.adminDetailDate);
        detailFavorite = findViewById(R.id.adminDetailFavorite);

        raffleTitle = findViewById(R.id.raffleTitle);
        raffleDescription = findViewById(R.id.raffleDescription);
        raffleEndDateButton = findViewById(R.id.raffleEndDate);
        raffleRequiresSelfie = findViewById(R.id.raffleRequiresSelfie);
        raffleRequiresPhone = findViewById(R.id.raffleRequiresPhone);
        raffleRetentionDays = findViewById(R.id.raffleRetentionDays);
        raffleCreateError = findViewById(R.id.raffleCreateError);
        raffleCreateSave = findViewById(R.id.raffleCreateSave);
        raffleOverviewTitle = findViewById(R.id.adminRaffleTitle);
        raffleOverviewStatus = findViewById(R.id.adminRaffleStatus);
        raffleEntries = findViewById(R.id.adminRaffleEntries);
        raffleCloseButton = findViewById(R.id.adminRaffleClose);
        raffleFinishButton = findViewById(R.id.adminRaffleFinish);
        raffleDeleteButton = findViewById(R.id.adminRaffleDelete);
        raffleDrawButton = findViewById(R.id.adminRaffleDraw);
        raffleRedrawButton = findViewById(R.id.adminRaffleRedraw);
        raffleEmailButton = findViewById(R.id.adminRaffleEmail);
        raffleWinnerView = findViewById(R.id.adminRaffleWinner);
        cameraIp = findViewById(R.id.cameraIp);
        cameraPort = findViewById(R.id.cameraPort);
        cameraEnabled = findViewById(R.id.cameraEnabled);
        cameraStatus = findViewById(R.id.cameraStatus);

        adminHeader = findViewById(R.id.adminHeader);
        adminHeaderTitle = findViewById(R.id.adminHeaderTitle);
        findViewById(R.id.adminHeaderBack).setOnClickListener(v -> goBack());
        findViewById(R.id.adminHeaderClose).setOnClickListener(v -> hide());

        wireKeypad();
        findViewById(R.id.adminPinCancel).setOnClickListener(v -> hide());
        findViewById(R.id.adminClose).setOnClickListener(v -> hide());
        findViewById(R.id.adminClearHistory).setOnClickListener(v -> onClearHistory());
        findViewById(R.id.adminDevLogs).setOnClickListener(v -> showDevLog());
        findViewById(R.id.adminSelfies).setOnClickListener(v -> showGallery());
        exportAllButton.setOnClickListener(v -> onExportAll());
        findViewById(R.id.adminSelfieRetention).setOnClickListener(v -> showSelfieRetentionDialog());
        detailFavorite.setOnClickListener(v -> toggleFavorite());
        findViewById(R.id.adminDetailDelete).setOnClickListener(v -> deleteCurrent());
        findViewById(R.id.adminLanguage).setOnClickListener(v -> showLanguage());
        findViewById(R.id.adminLangDe).setOnClickListener(v -> setLanguage(SupportedLanguage.GERMAN));
        findViewById(R.id.adminLangEn).setOnClickListener(v -> setLanguage(SupportedLanguage.ENGLISH));
        findViewById(R.id.adminHistory).setOnClickListener(v -> showHistory());
        findViewById(R.id.adminRaffle).setOnClickListener(v -> openRaffle());
        findViewById(R.id.raffleDescriptionDone).setOnClickListener(this::hideKeyboard);
        raffleEndDateButton.setOnClickListener(v -> pickEndDate());
        raffleCreateSave.setOnClickListener(v -> onSaveRaffle());
        raffleFinishButton.setOnClickListener(v -> finishCurrentRaffle());
        raffleCloseButton.setOnClickListener(v -> closeCurrentRaffle());
        raffleDeleteButton.setOnClickListener(v -> confirmDeleteRaffle());
        raffleDrawButton.setOnClickListener(v -> drawWinner());
        raffleRedrawButton.setOnClickListener(v -> redrawWinner());
        raffleEmailButton.setOnClickListener(v -> sendWinnerEmail());
        findViewById(R.id.adminCamera).setOnClickListener(v -> showCamera());
        findViewById(R.id.adminStatus).setOnClickListener(v -> showStatus());
        findViewById(R.id.statusRefresh).setOnClickListener(v -> showStatus());
        findViewById(R.id.adminStats).setOnClickListener(v -> showStats());
        findViewById(R.id.adminStatsExport).setOnClickListener(v -> exportStats());
        findViewById(R.id.adminAttract).setOnClickListener(v -> showAttract());
        findViewById(R.id.attractSave).setOnClickListener(v -> saveAttract());
        findViewById(R.id.attractTest).setOnClickListener(v -> testAttract());
        findViewById(R.id.cameraTest).setOnClickListener(v -> testCamera());
        findViewById(R.id.cameraSave).setOnClickListener(v -> saveCamera());
        findViewById(R.id.adminNavigation).setOnClickListener(v -> openNavigation());
        findViewById(R.id.adminDances).setOnClickListener(v -> openDanceLibrary());
        findViewById(R.id.adminDsgvo).setOnClickListener(v -> showDsgvoAccessDialog());
        findViewById(R.id.adminChangePin).setOnClickListener(v -> showChangePinDialog());

        selfieAdapter = new SelfieAdapter(this::showDetail);
        selfieGrid.setLayoutManager(new GridLayoutManager(context, 3));
        selfieGrid.setAdapter(selfieAdapter);
    }

    private void wireKeypad() {
        int[] ids = {
                R.id.adminKey0, R.id.adminKey1, R.id.adminKey2, R.id.adminKey3, R.id.adminKey4,
                R.id.adminKey5, R.id.adminKey6, R.id.adminKey7, R.id.adminKey8, R.id.adminKey9
        };
        for (int d = 0; d <= 9; d++) {
            final int digit = d;
            findViewById(ids[d]).setOnClickListener(v -> onDigit(digit));
        }
        findViewById(R.id.adminKeyClear).setOnClickListener(v -> resetEntry());
        findViewById(R.id.adminKeyBackspace).setOnClickListener(v -> backspace());
    }

    public void open() {
        post(() -> {
            resetEntry();
            if (isPinLocked()) {
                showLockoutMessage();
            } else {
                pinError.setVisibility(INVISIBLE);
            }
            showPanel(PANEL_PIN);
            setVisibility(VISIBLE);
            bringToFront();
        });
    }

    public void hide() {
        AdminController.get().markClosed();
        releaseDetailServer();
        dashHandler.removeCallbacks(dashRefresh);
        post(() -> setVisibility(GONE));
    }

    private void openNavigation() {
        hide();
        NavigationController.get().open();
    }

    private void openDanceLibrary() {
        hide();
        DanceLibraryController.get().open();
    }

    private void onDigit(int digit) {
        if (isPinLocked()) {
            showLockoutMessage();
            return;
        }
        if (entered.length() >= 4) {
            return;
        }
        pinError.setVisibility(INVISIBLE);
        entered.append(digit);
        updateDots();
        if (entered.length() == 4) {
            checkPin();
        }
    }

    private void backspace() {
        if (entered.length() > 0) {
            entered.deleteCharAt(entered.length() - 1);
            updateDots();
        }
    }

    private void resetEntry() {
        entered.setLength(0);
        updateDots();
    }

    private void checkPin() {
        if (AdminSettings.getPin(getContext()).contentEquals(entered)) {
            pinAttempts = 0;
            pinLockoutUntil = 0;
            resetEntry();
            showPanel(PANEL_MENU);
        } else {
            resetEntry();
            pinAttempts++;
            if (pinAttempts >= MAX_PIN_ATTEMPTS) {
                pinAttempts = 0;
                pinLockoutUntil = SystemClock.elapsedRealtime() + PIN_LOCKOUT_MS;
                showLockoutMessage();
            } else {
                pinError.setText(R.string.admin_pin_error);
                pinError.setVisibility(VISIBLE);
            }
        }
    }

    private boolean isPinLocked() {
        return SystemClock.elapsedRealtime() < pinLockoutUntil;
    }

    private void showLockoutMessage() {
        long remaining = Math.max(0, pinLockoutUntil - SystemClock.elapsedRealtime());
        int seconds = (int) Math.ceil(remaining / 1000.0);
        pinError.setText(getContext().getString(R.string.admin_pin_locked, seconds));
        pinError.setVisibility(VISIBLE);
    }

    private void updateDots() {
        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            dots.append(i < entered.length() ? "●" : "○");
            if (i < 3) {
                dots.append(' ');
            }
        }
        pinDots.setText(dots.toString());
    }

    private void showPanel(int which) {
        currentPanel = which;
        pinPanel.setVisibility(which == PANEL_PIN ? VISIBLE : GONE);
        menuPanel.setVisibility(which == PANEL_MENU ? VISIBLE : GONE);
        devLogPanel.setVisibility(which == PANEL_DEVLOG ? VISIBLE : GONE);
        galleryPanel.setVisibility(which == PANEL_GALLERY ? VISIBLE : GONE);
        detailPanel.setVisibility(which == PANEL_DETAIL ? VISIBLE : GONE);
        langPanel.setVisibility(which == PANEL_LANG ? VISIBLE : GONE);
        historyPanel.setVisibility(which == PANEL_HISTORY ? VISIBLE : GONE);
        raffleCreatePanel.setVisibility(which == PANEL_RAFFLE_CREATE ? VISIBLE : GONE);
        rafflePanel.setVisibility(which == PANEL_RAFFLE ? VISIBLE : GONE);
        cameraPanel.setVisibility(which == PANEL_CAMERA ? VISIBLE : GONE);
        statusPanel.setVisibility(which == PANEL_STATUS ? VISIBLE : GONE);
        statsPanel.setVisibility(which == PANEL_STATS ? VISIBLE : GONE);
        attractPanel.setVisibility(which == PANEL_ATTRACT ? VISIBLE : GONE);
        dashHandler.removeCallbacks(dashRefresh);
        if (which == PANEL_MENU) {
            dashHandler.post(dashRefresh);
        }
        updateHeader(which);
    }

    private void updateHeader(int which) {
        boolean show = which != PANEL_PIN && which != PANEL_MENU;
        adminHeader.setVisibility(show ? VISIBLE : GONE);
        if (show) {
            adminHeaderTitle.setText(titleFor(which));
            adminHeader.bringToFront();
        }
    }

    private int titleFor(int which) {
        switch (which) {
            case PANEL_DEVLOG:
                return R.string.admin_dev_logs;
            case PANEL_GALLERY:
            case PANEL_DETAIL:
                return R.string.admin_selfies;
            case PANEL_LANG:
                return R.string.admin_language;
            case PANEL_HISTORY:
                return R.string.admin_history_view;
            case PANEL_RAFFLE_CREATE:
                return R.string.raffle_create_title;
            case PANEL_RAFFLE:
                return R.string.admin_raffle;
            case PANEL_CAMERA:
                return R.string.admin_camera_title;
            case PANEL_STATUS:
                return R.string.admin_status;
            case PANEL_STATS:
                return R.string.admin_stats;
            case PANEL_ATTRACT:
                return R.string.admin_attract;
            default:
                return R.string.admin_menu_title;
        }
    }

    private void goBack() {
        if (currentPanel == PANEL_DETAIL) {
            showGallery();
        } else {
            showPanel(PANEL_MENU);
        }
    }

    private void refreshDashboardStatus() {
        boolean online = Connectivity.isOnline(getContext());
        dashWifi.setText(online ? R.string.status_dash_connected : R.string.status_dash_disconnected);
        dashWifi.setTextColor(ContextCompat.getColor(getContext(),
                online ? R.color.status_ok : R.color.status_bad));

        int pct = batteryPercent();
        if (pct < 0) {
            dashBattery.setText("–");
            dashBattery.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
        } else {
            dashBattery.setText(pct + "%");
            int color = pct >= 30 ? R.color.status_ok : pct >= 15 ? R.color.status_warn : R.color.status_bad;
            dashBattery.setTextColor(ContextCompat.getColor(getContext(), color));
        }

        dashUptime.setText(uptimeText());
        dashUptime.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));

        dashOpenAi.setText("…");
        dashOpenAi.setTextColor(ContextCompat.getColor(getContext(), R.color.text_muted));
        dbExecutor.submit(() -> {
            boolean reachable = isOpenAiReachable();
            post(() -> {
                dashOpenAi.setText(reachable ? R.string.status_dash_ok : R.string.status_dash_fail);
                dashOpenAi.setTextColor(ContextCompat.getColor(getContext(),
                        reachable ? R.color.status_ok : R.color.status_bad));
            });
        });
    }

    private int batteryPercent() {
        Intent battery = getContext().registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (battery == null) {
            return -1;
        }
        int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level < 0 || scale <= 0) {
            return -1;
        }
        return Math.round(level * 100f / scale);
    }

    private void showLanguage() {
        updateLanguageLabel();
        showPanel(PANEL_LANG);
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
        showPanel(PANEL_HISTORY);
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

    private void showRaffleCreate() {
        raffleTitle.setText("");
        raffleDescription.setText("");
        raffleRequiresSelfie.setChecked(false);
        raffleRequiresPhone.setChecked(false);
        raffleRetentionDays.setText(String.valueOf(RaffleSettings.getRetentionDays(getContext())));
        raffleEndDateMillis = 0L;
        raffleEndDateButton.setText(R.string.raffle_end_date);
        raffleCreateError.setVisibility(GONE);
        raffleCreateSave.setEnabled(true);
        raffleCreateSave.setAlpha(1f);
        showPanel(PANEL_RAFFLE_CREATE);
    }

    private void hideKeyboard(View anchor) {
        InputMethodManager imm =
                (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(anchor.getWindowToken(), 0);
        }
        raffleDescription.clearFocus();
    }

    private void pickEndDate() {
        Calendar initial = Calendar.getInstance();
        if (raffleEndDateMillis > 0) {
            initial.setTimeInMillis(raffleEndDateMillis);
        }
        DatePickerDialog dateDialog = new DatePickerDialog(getContext(), (view, year, month, day) -> {
            Calendar picked = Calendar.getInstance();
            if (raffleEndDateMillis > 0) {
                picked.setTimeInMillis(raffleEndDateMillis);
            }
            picked.set(Calendar.YEAR, year);
            picked.set(Calendar.MONTH, month);
            picked.set(Calendar.DAY_OF_MONTH, day);
            new TimePickerDialog(getContext(), (timeView, hour, minute) -> {
                picked.set(Calendar.HOUR_OF_DAY, hour);
                picked.set(Calendar.MINUTE, minute);
                picked.set(Calendar.SECOND, 0);
                raffleEndDateMillis = picked.getTimeInMillis();
                raffleEndDateButton.setText(new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY)
                        .format(new Date(raffleEndDateMillis)));
            }, picked.get(Calendar.HOUR_OF_DAY), picked.get(Calendar.MINUTE), true).show();
        }, initial.get(Calendar.YEAR), initial.get(Calendar.MONTH), initial.get(Calendar.DAY_OF_MONTH));
        dateDialog.show();
    }

    private void onSaveRaffle() {
        String title = raffleTitle.getText().toString().trim();
        String description = raffleDescription.getText().toString().trim();
        if (title.isEmpty() || raffleEndDateMillis <= 0) {
            raffleCreateError.setText(R.string.raffle_create_invalid);
            raffleCreateError.setVisibility(VISIBLE);
            return;
        }
        boolean requiresSelfie = raffleRequiresSelfie.isChecked();
        boolean requiresPhone = raffleRequiresPhone.isChecked();
        long endDate = raffleEndDateMillis;
        RaffleSettings.setRetentionDays(getContext(), parseRetentionDays());
        raffleCreateSave.setEnabled(false);
        dbExecutor.submit(() -> {
            long id = RaffleRepository.get(getContext()).createRaffle(
                    title, description, requiresSelfie, requiresPhone, endDate);
            post(() -> {
                if (id < 0) {
                    raffleCreateError.setText(R.string.raffle_create_active_exists);
                    raffleCreateError.setVisibility(VISIBLE);
                    raffleCreateSave.setEnabled(true);
                } else {
                    Toast.makeText(getContext(), R.string.raffle_created, Toast.LENGTH_SHORT).show();
                    openRaffle();
                }
            });
        });
    }

    private int parseRetentionDays() {
        try {
            return Math.max(0, Integer.parseInt(raffleRetentionDays.getText().toString().trim()));
        } catch (NumberFormatException e) {
            return RaffleSettings.DEFAULT_RETENTION_DAYS;
        }
    }

    private void openRaffle() {
        dbExecutor.submit(() -> {
            RaffleRepository repo = RaffleRepository.get(getContext());
            RaffleEntity raffle = repo.getCurrentRaffle();
            if (raffle == null) {
                post(this::showRaffleCreate);
                return;
            }
            List<RaffleEntryEntity> entries = repo.getEntries(raffle.id);
            post(() -> showRaffleOverview(raffle, entries));
        });
    }

    private void showRaffleOverview(RaffleEntity raffle, List<RaffleEntryEntity> entries) {
        currentRaffleId = raffle.id;
        currentRaffleTitle = raffle.title;
        raffleOverviewTitle.setText(raffle.title);
        if (raffle.status == RaffleStatus.ACTIVE) {
            String end = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY)
                    .format(new Date(raffle.endDate));
            raffleOverviewStatus.setText(
                    getContext().getString(R.string.raffle_status_active, end, entries.size()));
        } else {
            raffleOverviewStatus.setText(
                    getContext().getString(R.string.raffle_status_ended, entries.size()));
        }
        boolean ended = raffle.status == RaffleStatus.ENDED;
        raffleCloseButton.setVisibility(raffle.status == RaffleStatus.ACTIVE ? VISIBLE : GONE);
        raffleFinishButton.setVisibility(ended ? VISIBLE : GONE);
        raffleDeleteButton.setVisibility(ended ? VISIBLE : GONE);

        RaffleEntryEntity winner = null;
        if (raffle.winnerId != null) {
            for (RaffleEntryEntity entry : entries) {
                if (entry.id == raffle.winnerId) {
                    winner = entry;
                    break;
                }
            }
        }
        currentWinner = winner;
        if (winner != null) {
            raffleWinnerView.setText(
                    getContext().getString(R.string.raffle_winner_label, winner.name, winner.email));
            raffleWinnerView.setVisibility(VISIBLE);
        } else {
            raffleWinnerView.setVisibility(GONE);
        }
        raffleDrawButton.setVisibility(ended && winner == null && !entries.isEmpty() ? VISIBLE : GONE);
        raffleRedrawButton.setVisibility(ended && winner != null ? VISIBLE : GONE);
        raffleEmailButton.setVisibility(ended && winner != null ? VISIBLE : GONE);

        raffleEntries.removeAllViews();
        if (entries.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText(R.string.raffle_no_entries);
            empty.setTextColor(0xCCFFFFFF);
            int pad = dp(8);
            empty.setPadding(pad, pad, pad, pad);
            raffleEntries.addView(empty);
        } else {
            for (RaffleEntryEntity entry : entries) {
                boolean isWinner = raffle.winnerId != null && raffle.winnerId == entry.id;
                raffleEntries.addView(createEntryRow(entry, isWinner));
            }
        }
        showPanel(PANEL_RAFFLE);
    }

    private View createEntryRow(RaffleEntryEntity entry, boolean isWinner) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int pv = dp(6);
        row.setPadding(0, pv, 0, pv);

        ImageView thumb = new ImageView(getContext());
        LinearLayout.LayoutParams thumbParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        thumbParams.setMarginEnd(dp(12));
        thumb.setLayoutParams(thumbParams);
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        thumb.setBackgroundColor(0xFF000000);
        thumb.setVisibility(GONE);
        row.addView(thumb);

        StringBuilder sb = new StringBuilder();
        if (isWinner) {
            sb.append("🏆 ");
        }
        sb.append(entry.name).append(" · ").append(entry.email);
        if (entry.phone != null && !entry.phone.isEmpty()) {
            sb.append(" · ").append(entry.phone);
        }
        TextView text = new TextView(getContext());
        text.setText(sb.toString());
        text.setTextColor(isWinner ? 0xFFFFD54F : 0xFFFFFFFF);
        if (isWinner) {
            text.setTypeface(text.getTypeface(), android.graphics.Typeface.BOLD);
        }
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        text.setLayoutParams(textParams);
        row.addView(text);

        Button deleteButton = new Button(getContext());
        deleteButton.setText(R.string.raffle_entry_delete);
        deleteButton.setTextColor(0xFFFFFFFF);
        deleteButton.setBackgroundResource(R.drawable.bg_pill_red);
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        deleteParams.setMarginStart(dp(8));
        deleteButton.setLayoutParams(deleteParams);
        deleteButton.setOnClickListener(v -> confirmDeleteEntry(entry));
        row.addView(deleteButton);

        if (entry.selfieId != null && !entry.selfieId.isEmpty()) {
            dbExecutor.submit(() -> {
                SelfieRepository repository = SelfieRepository.get(getContext());
                SelfieEntity selfie = repository.findById(entry.selfieId);
                if (selfie == null) {
                    return;
                }
                Bitmap bitmap = SelfieAdapter.decodeThumb(
                        new File(repository.imagesDir(), selfie.filename), 160);
                post(() -> {
                    if (bitmap != null) {
                        thumb.setImageBitmap(bitmap);
                    }
                    thumb.setVisibility(VISIBLE);
                    row.setOnClickListener(v -> showDetail(selfie));
                });
            });
        }
        return row;
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

    private void showSelfieRetentionDialog() {
        EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(R.string.selfie_retention_hint);
        input.setText(String.valueOf(SelfieSettings.getRetentionDays(getContext())));
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.selfie_retention_title)
                .setView(input)
                .setNegativeButton(R.string.admin_back, null)
                .setPositiveButton(R.string.raffle_save, (d, w) -> {
                    int days;
                    try {
                        days = Math.max(0, Integer.parseInt(input.getText().toString().trim()));
                    } catch (NumberFormatException e) {
                        days = SelfieSettings.DEFAULT_RETENTION_DAYS;
                    }
                    SelfieSettings.setRetentionDays(getContext(), days);
                    Toast.makeText(getContext(), R.string.selfie_retention_saved,
                            Toast.LENGTH_SHORT).show();
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

    private void confirmDeleteRaffle() {
        if (currentRaffleId <= 0) {
            return;
        }
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.raffle_delete_title)
                .setMessage(R.string.raffle_delete_message)
                .setNegativeButton(R.string.admin_back, null)
                .setPositiveButton(R.string.raffle_delete, (d, w) -> deleteCurrentRaffle())
                .show();
    }

    private void deleteCurrentRaffle() {
        long id = currentRaffleId;
        if (id <= 0) {
            return;
        }
        dbExecutor.submit(() -> {
            RaffleRepository.get(getContext()).deleteRaffleCompletely(id);
            post(this::openRaffle);
        });
    }

    private void confirmDeleteEntry(RaffleEntryEntity entry) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.raffle_entry_delete_title)
                .setMessage(getContext().getString(R.string.raffle_entry_delete_message, entry.name))
                .setNegativeButton(R.string.admin_back, null)
                .setPositiveButton(R.string.raffle_entry_delete, (d, w) -> deleteEntry(entry))
                .show();
    }

    private void deleteEntry(RaffleEntryEntity entry) {
        dbExecutor.submit(() -> {
            RaffleRepository.get(getContext()).deleteEntry(entry);
            post(this::openRaffle);
        });
    }

    private void finishCurrentRaffle() {
        long id = currentRaffleId;
        if (id <= 0) {
            return;
        }
        dbExecutor.submit(() -> {
            RaffleRepository.get(getContext()).finishRaffle(id);
            post(this::openRaffle);
        });
    }

    private void closeCurrentRaffle() {
        long id = currentRaffleId;
        if (id <= 0) {
            return;
        }
        dbExecutor.submit(() -> {
            RaffleRepository.get(getContext()).endRaffle(id);
            post(this::openRaffle);
        });
    }

    private void drawWinner() {
        long id = currentRaffleId;
        if (id <= 0) {
            return;
        }
        dbExecutor.submit(() -> {
            RaffleEntryEntity winner = RaffleRepository.get(getContext()).pickWinner(id);
            post(() -> celebrateWinner(winner));
        });
    }

    private void redrawWinner() {
        long id = currentRaffleId;
        if (id <= 0) {
            return;
        }
        dbExecutor.submit(() -> {
            RaffleEntryEntity winner = RaffleRepository.get(getContext()).pickReplacementWinner(id);
            post(() -> {
                if (winner == null) {
                    Toast.makeText(getContext(), R.string.raffle_no_replacement,
                            Toast.LENGTH_SHORT).show();
                    openRaffle();
                } else {
                    celebrateWinner(winner);
                }
            });
        });
    }

    private void celebrateWinner(RaffleEntryEntity winner) {
        if (winner == null) {
            openRaffle();
            return;
        }
        hide();
        com.aldebaran.qi.sdk.QiContext qc =
                com.buhlergroup.pepper.action.dance.RobotContext.get();
        if (qc != null) {
            com.buhlergroup.pepper.action.raffle.WinnerController.get().celebrate(qc, winner.name);
        }
    }

    private void sendWinnerEmail() {
        RaffleEntryEntity winner = currentWinner;
        String title = currentRaffleTitle;
        if (winner == null) {
            return;
        }
        dbExecutor.submit(() -> {
            Uri selfieUri = null;
            if (winner.selfieId != null && !winner.selfieId.isEmpty()) {
                SelfieRepository repository = SelfieRepository.get(getContext());
                SelfieEntity selfie = repository.findById(winner.selfieId);
                if (selfie != null) {
                    File file = new File(repository.imagesDir(), selfie.filename);
                    if (file.exists()) {
                        try {
                            selfieUri = FileProvider.getUriForFile(getContext(),
                                    getContext().getPackageName() + ".fileprovider", file);
                        } catch (Exception e) {
                            Log.w(TAG, "Selfie attachment failed: " + e.getMessage());
                        }
                    }
                }
            }
            Uri attachment = selfieUri;
            post(() -> launchEmail(winner, title, attachment));
        });
    }

    private void launchEmail(RaffleEntryEntity winner, String title, Uri selfieUri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{winner.email});
        intent.putExtra(Intent.EXTRA_SUBJECT, getContext().getString(R.string.raffle_email_subject, title));
        intent.putExtra(Intent.EXTRA_TEXT,
                getContext().getString(R.string.raffle_email_body, winner.name, title));
        if (selfieUri != null) {
            intent.putExtra(Intent.EXTRA_STREAM, selfieUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.setPackage("com.google.android.gm");
        try {
            getContext().startActivity(intent);
            return;
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "Gmail not available, falling back to chooser");
        }
        Intent fallback = new Intent(intent);
        fallback.setPackage(null);
        Intent chooser = Intent.createChooser(fallback, getContext().getString(R.string.raffle_email_chooser));
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            getContext().startActivity(chooser);
        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.raffle_email_chooser, Toast.LENGTH_SHORT).show();
        }
    }

    private void showAttract() {
        attractEnabled.setChecked(AttractSettings.isEnabled(getContext()));
        attractIdle.setText(String.valueOf(AttractSettings.getIdleMinutes(getContext())));
        attractGreet.setText(String.valueOf(AttractSettings.getGreetSeconds(getContext())));
        showPanel(PANEL_ATTRACT);
    }

    private void saveAttract() {
        int idle = parseIntOr(attractIdle, AttractSettings.DEFAULT_IDLE_MINUTES);
        int greet = parseIntOr(attractGreet, AttractSettings.DEFAULT_GREET_SECONDS);
        AttractSettings.save(getContext(), attractEnabled.isChecked(), idle, greet);
        Toast.makeText(getContext(), R.string.attract_saved, Toast.LENGTH_SHORT).show();
    }

    private void testAttract() {
        saveAttract();
        hide();
        AttractController.get().forceStart();
    }

    private int parseIntOr(EditText field, int fallback) {
        try {
            return Integer.parseInt(field.getText().toString().trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private void showStats() {
        statsText.setText(buildStatsReport());
        showPanel(PANEL_STATS);
    }

    private String buildStatsReport() {
        String date = Stats.today();
        Map<String, Integer> day = Stats.forDay(getContext(), date);
        StringBuilder sb = new StringBuilder("Tagesreport " + date + "\n\n");
        sb.append("Interaktionen: ").append(value(day, Stats.INTERACTIONS)).append('\n');
        sb.append("Selfies: ").append(value(day, Stats.SELFIES)).append('\n');
        sb.append("Verlosungs-Beitritte: ").append(value(day, Stats.RAFFLE_JOINS)).append('\n');
        sb.append("Fehler: ").append(value(day, Stats.ERRORS)).append("\n\n");
        sb.append("Aktionen:\n");
        boolean anyAction = false;
        for (Map.Entry<String, Integer> entry : day.entrySet()) {
            if (entry.getKey().startsWith(Stats.ACTION_PREFIX)) {
                anyAction = true;
                sb.append("  ").append(entry.getKey().substring(Stats.ACTION_PREFIX.length()))
                        .append(": ").append(entry.getValue()).append('\n');
            }
        }
        if (!anyAction) {
            sb.append("  –\n");
        }
        return sb.toString();
    }

    private int value(Map<String, Integer> day, String key) {
        Integer v = day.get(key);
        return v != null ? v : 0;
    }

    private void exportStats() {
        String report = buildStatsReport();
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, getContext().getString(R.string.stats_share_title));
        share.putExtra(Intent.EXTRA_TEXT, report);
        Intent chooser = Intent.createChooser(share, getContext().getString(R.string.stats_export));
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            getContext().startActivity(chooser);
        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.admin_export_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void showStatus() {
        showPanel(PANEL_STATUS);
        String ip = NetworkUtils.localIp(getContext());
        boolean online = Connectivity.isOnline(getContext());
        statusWifi.setText(online && ip != null
                ? getContext().getString(R.string.status_wifi_connected, ip)
                : getContext().getString(R.string.status_wifi_disconnected));
        statusBattery.setText(batteryStatusText());
        statusUptime.setText(getContext().getString(R.string.status_uptime, uptimeText()));
        statusOpenAi.setText(R.string.status_openai_checking);
        dbExecutor.submit(() -> {
            boolean reachable = isOpenAiReachable();
            post(() -> statusOpenAi.setText(
                    reachable ? R.string.status_openai_ok : R.string.status_openai_fail));
        });
    }

    private String batteryStatusText() {
        Intent battery = getContext().registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (battery == null) {
            return getContext().getString(R.string.status_battery_unknown);
        }
        int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level < 0 || scale <= 0) {
            return getContext().getString(R.string.status_battery_unknown);
        }
        return getContext().getString(R.string.status_battery, Math.round(level * 100f / scale));
    }

    private String uptimeText() {
        long elapsedMs = SystemClock.elapsedRealtime() - PepperApplication.startElapsedMs();
        long totalMinutes = Math.max(0, elapsedMs / 60000);
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return hours + " h " + minutes + " min";
    }

    private boolean isOpenAiReachable() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("api.openai.com", 443), 4000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void showCamera() {
        cameraIp.setText(CameraSettings.getIp(getContext()));
        cameraPort.setText(String.valueOf(CameraSettings.getPort(getContext())));
        cameraEnabled.setChecked(CameraSettings.isEnabled(getContext()));
        cameraStatus.setText("");
        showPanel(PANEL_CAMERA);
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
        showPanel(PANEL_MENU);
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
        showPanel(PANEL_DEVLOG);
        devLogScroll.post(() -> devLogScroll.fullScroll(View.FOCUS_DOWN));
    }

    private void releaseDetailServer() {
        if (detailServerHeld) {
            SelfieController.get().releaseServer();
            detailServerHeld = false;
        }
    }

    private void showGallery() {
        releaseDetailServer();
        showPanel(PANEL_GALLERY);
        galleryEmpty.setVisibility(GONE);
        setExportEnabled(false);
        dbExecutor.submit(() -> {
            SelfieRepository repository = SelfieRepository.get(getContext());
            List<SelfieEntity> items = repository.getAll();
            File dir = repository.imagesDir();
            Set<String> linked = new HashSet<>(RaffleRepository.get(getContext()).linkedSelfieIds());
            post(() -> {
                selfieAdapter.setData(items, dir, linked);
                galleryEmpty.setVisibility(items.isEmpty() ? VISIBLE : GONE);
                setExportEnabled(!items.isEmpty());
            });
        });
    }

    private void setExportEnabled(boolean enabled) {
        exportAllButton.setEnabled(enabled);
        exportAllButton.setAlpha(enabled ? 1f : 0.4f);
    }

    private void onExportAll() {
        setExportEnabled(false);
        exportAllButton.setText(R.string.admin_export_preparing);
        dbExecutor.submit(() -> {
            File zip = createSelfiesZip();
            post(() -> {
                exportAllButton.setText(R.string.admin_export_all);
                setExportEnabled(true);
                if (zip != null) {
                    shareZip(zip);
                } else {
                    Toast.makeText(getContext(), R.string.admin_export_failed, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void shareZip(File zip) {
        try {
            Uri uri = FileProvider.getUriForFile(getContext(),
                    getContext().getPackageName() + ".fileprovider", zip);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("application/zip");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent chooser = Intent.createChooser(share,
                    getContext().getString(R.string.admin_export_share_title));
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(chooser);
        } catch (Exception e) {
            Log.w(TAG, "Teilen fehlgeschlagen: " + e.getMessage());
            Toast.makeText(getContext(), R.string.admin_export_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private File createSelfiesZip() {
        File imagesDir = SelfieRepository.get(getContext()).imagesDir();
        File[] files = imagesDir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        File zipFile = new File(getContext().getCacheDir(), "selfies_export.zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            byte[] buffer = new byte[8192];
            for (File file : files) {
                if (!file.isFile()) {
                    continue;
                }
                try (FileInputStream fis = new FileInputStream(file)) {
                    zos.putNextEntry(new ZipEntry(file.getName()));
                    int read;
                    while ((read = fis.read(buffer)) != -1) {
                        zos.write(buffer, 0, read);
                    }
                    zos.closeEntry();
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "Selfie-Export fehlgeschlagen: " + e.getMessage());
            return null;
        }
        return zipFile;
    }

    private void showDetail(SelfieEntity selfie) {
        if (!detailServerHeld) {
            SelfieController.get().acquireServer(getContext());
            detailServerHeld = true;
        }
        currentDetail = selfie;
        showPanel(PANEL_DETAIL);
        detailNumber.setText("#" + selfie.number);
        detailDate.setText(new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY)
                .format(new Date(selfie.createdAt)));
        updateFavoriteButton();
        detailImage.setImageBitmap(null);
        detailQr.setImageBitmap(null);
        detailQr.setVisibility(GONE);
        detailQrHint.setVisibility(GONE);

        File file = new File(SelfieRepository.get(getContext()).imagesDir(), selfie.filename);
        dbExecutor.submit(() -> {
            Bitmap bitmap = SelfieAdapter.decodeThumb(file, 1000);
            Bitmap qr = buildSelfieQr(selfie);
            post(() -> {
                if (currentDetail == selfie) {
                    detailImage.setImageBitmap(bitmap);
                    if (qr != null) {
                        detailQr.setImageBitmap(qr);
                        detailQr.setVisibility(VISIBLE);
                        detailQrHint.setVisibility(GONE);
                    } else {
                        detailQr.setImageBitmap(null);
                        detailQr.setVisibility(GONE);
                        detailQrHint.setVisibility(VISIBLE);
                    }
                }
            });
        });
    }

    private Bitmap buildSelfieQr(SelfieEntity selfie) {
        String url = SelfieController.get().downloadUrl(getContext(), selfie.filename);
        if (url == null) {
            return null;
        }
        try {
            return QrGenerator.encode(url, 500);
        } catch (Exception e) {
            Log.w(TAG, "Selfie-QR fehlgeschlagen: " + e.getMessage());
            return null;
        }
    }

    private void updateFavoriteButton() {
        detailFavorite.setText(currentDetail != null && currentDetail.favorite
                ? R.string.admin_favorite_remove
                : R.string.admin_favorite_add);
    }

    private void toggleFavorite() {
        SelfieEntity selfie = currentDetail;
        if (selfie == null) {
            return;
        }
        boolean newFavorite = !selfie.favorite;
        dbExecutor.submit(() -> {
            SelfieRepository.get(getContext()).setFavorite(selfie.id, newFavorite);
            post(() -> {
                selfie.favorite = newFavorite;
                updateFavoriteButton();
            });
        });
    }

    private void deleteCurrent() {
        SelfieEntity selfie = currentDetail;
        if (selfie == null) {
            return;
        }
        dbExecutor.submit(() -> {
            SelfieRepository.get(getContext()).delete(selfie.id);
            post(this::showGallery);
        });
    }
}
