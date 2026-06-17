package com.buhlergroup.pepper.action.admin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.dance.DanceLibraryController;
import com.buhlergroup.pepper.action.navigation.NavigationController;
import com.buhlergroup.pepper.action.raffle.RaffleRepository;
import com.buhlergroup.pepper.databinding.ViewAdminBinding;

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
import static com.buhlergroup.pepper.action.admin.PanelNavigator.PANEL_DANCE;

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
    private View attractPanel;
    private View dancePanel;
    private View adminHeader;
    private TextView adminHeaderTitle;
    private PanelNavigator panelNav;
    private SelfieGalleryController galleryController;
    private RaffleAdminController raffleAdmin;
    private LanguagePanelController language;
    private HistoryPanelController history;
    private AttractPanelController attract;
    private DancePanelController dance;
    private CameraPanelController camera;
    private DiagnosticsController diagnostics;

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
        ViewAdminBinding binding = ViewAdminBinding.inflate(LayoutInflater.from(context), this);
        setBackgroundColor(ContextCompat.getColor(context, R.color.game_overlay));
        setClickable(true);
        setFocusable(true);

        pinPanel = binding.adminPinPanel;
        menuPanel = binding.adminMenuPanel;
        devLogPanel = binding.adminDevLogPanel;
        galleryPanel = binding.adminGalleryPanel;
        detailPanel = binding.adminDetailPanel;
        langPanel = binding.adminLangPanel;
        historyPanel = binding.adminHistoryPanel;
        raffleCreatePanel = binding.adminRaffleCreatePanel;
        rafflePanel = binding.adminRafflePanel;
        cameraPanel = binding.adminCameraPanel;
        statusPanel = binding.adminStatusPanel;
        statsPanel = binding.adminStatsPanel;
        debugPanel = binding.adminDebugPanel;
        attractPanel = binding.adminAttractPanel;
        dancePanel = binding.adminDancePanel;

        adminHeader = binding.adminHeader;
        adminHeaderTitle = binding.adminHeaderTitle;
        binding.adminHeaderBack.setOnClickListener(v -> goBack());
        binding.adminHeaderClose.setOnClickListener(v -> hide());

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
        panelNav.register(PANEL_DANCE, dancePanel);

        pinController = new PinController(this, () -> panelNav.show(PANEL_MENU));
        dashboard = new DashboardController(this, dbExecutor);
        galleryController = new SelfieGalleryController(this, dbExecutor, panelNav);
        raffleAdmin = new RaffleAdminController(this, dbExecutor, panelNav, galleryController, this::hide);
        language = new LanguagePanelController(this, panelNav);
        history = new HistoryPanelController(this, panelNav);
        attract = new AttractPanelController(this, panelNav);
        dance = new DancePanelController(this, dbExecutor, panelNav);
        camera = new CameraPanelController(this, dbExecutor, panelNav);
        diagnostics = new DiagnosticsController(this, panelNav);
        binding.adminPinCancel.setOnClickListener(v -> hide());
        binding.adminClose.setOnClickListener(v -> hide());
        binding.adminDevLogs.setOnClickListener(v -> diagnostics.showDevLog());
        binding.adminSelfies.setOnClickListener(v -> galleryController.showGallery());
        binding.adminLanguage.setOnClickListener(v -> language.showLanguage());
        binding.adminHistory.setOnClickListener(v -> history.showHistory());
        binding.adminRaffle.setOnClickListener(v -> raffleAdmin.openRaffle());
        binding.adminCamera.setOnClickListener(v -> camera.showCamera());
        binding.adminStatus.setOnClickListener(v -> showStatus());
        binding.statusRefresh.setOnClickListener(v -> showStatus());
        binding.adminStats.setOnClickListener(v -> showStats());
        binding.adminStatsExport.setOnClickListener(v -> dashboard.exportStats());
        binding.adminDebug.setOnClickListener(v -> diagnostics.showDebug());
        binding.adminAttract.setOnClickListener(v -> attract.showAttract());
        binding.adminDanceSettings.setOnClickListener(v -> dance.showDance());
        binding.adminNavigation.setOnClickListener(v -> openNavigation());
        binding.adminDances.setOnClickListener(v -> openDanceLibrary());
        binding.adminDsgvo.setOnClickListener(v -> showDsgvoAccessDialog());
        binding.adminChangePin.setOnClickListener(v -> showChangePinDialog());
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

    private void showStats() {
        dashboard.refreshStats();
        panelNav.show(PANEL_STATS);
    }

    private void showStatus() {
        panelNav.show(PANEL_STATUS);
        dashboard.refreshStatus();
    }
}
