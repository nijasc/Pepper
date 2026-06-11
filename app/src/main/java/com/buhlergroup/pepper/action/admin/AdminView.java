package com.buhlergroup.pepper.action.admin;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.selfie.SelfieRepository;
import com.buhlergroup.pepper.action.selfie.data.SelfieEntity;
import com.buhlergroup.pepper.lang.SupportedLanguage;
import com.buhlergroup.pepper.openai.history.HistoryEntry;
import com.buhlergroup.pepper.openai.history.HistoryRole;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AdminView extends FrameLayout {

    private static final String TAG = "AdminView";
    private static final String PIN = "1019";
    private static final int PANEL_PIN = 0;
    private static final int PANEL_MENU = 1;
    private static final int PANEL_DEVLOG = 2;
    private static final int PANEL_GALLERY = 3;
    private static final int PANEL_DETAIL = 4;
    private static final int PANEL_LANG = 5;
    private static final int PANEL_HISTORY = 6;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private final StringBuilder entered = new StringBuilder();

    private View pinPanel;
    private View menuPanel;
    private View devLogPanel;
    private View galleryPanel;
    private View detailPanel;
    private View langPanel;
    private View historyPanel;
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
    private TextView detailNumber;
    private TextView detailDate;
    private Button detailFavorite;
    private SelfieEntity currentDetail;

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
        detailNumber = findViewById(R.id.adminDetailNumber);
        detailDate = findViewById(R.id.adminDetailDate);
        detailFavorite = findViewById(R.id.adminDetailFavorite);

        wireKeypad();
        findViewById(R.id.adminPinCancel).setOnClickListener(v -> hide());
        findViewById(R.id.adminClose).setOnClickListener(v -> hide());
        findViewById(R.id.adminClearHistory).setOnClickListener(v -> onClearHistory());
        findViewById(R.id.adminDevLogs).setOnClickListener(v -> showDevLog());
        findViewById(R.id.adminDevLogBack).setOnClickListener(v -> showPanel(PANEL_MENU));
        findViewById(R.id.adminSelfies).setOnClickListener(v -> showGallery());
        findViewById(R.id.adminGalleryBack).setOnClickListener(v -> showPanel(PANEL_MENU));
        exportAllButton.setOnClickListener(v -> onExportAll());
        findViewById(R.id.adminDetailBack).setOnClickListener(v -> showGallery());
        detailFavorite.setOnClickListener(v -> toggleFavorite());
        findViewById(R.id.adminDetailDelete).setOnClickListener(v -> deleteCurrent());
        findViewById(R.id.adminLanguage).setOnClickListener(v -> showLanguage());
        findViewById(R.id.adminLangDe).setOnClickListener(v -> setLanguage(SupportedLanguage.GERMAN));
        findViewById(R.id.adminLangEn).setOnClickListener(v -> setLanguage(SupportedLanguage.ENGLISH));
        findViewById(R.id.adminLangBack).setOnClickListener(v -> showPanel(PANEL_MENU));
        findViewById(R.id.adminHistory).setOnClickListener(v -> showHistory());
        findViewById(R.id.adminHistoryBack).setOnClickListener(v -> showPanel(PANEL_MENU));

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
            pinError.setVisibility(INVISIBLE);
            showPanel(PANEL_PIN);
            setVisibility(VISIBLE);
            bringToFront();
        });
    }

    public void hide() {
        AdminController.get().markClosed();
        post(() -> setVisibility(GONE));
    }

    private void onDigit(int digit) {
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
        if (PIN.contentEquals(entered)) {
            resetEntry();
            showPanel(PANEL_MENU);
        } else {
            pinError.setVisibility(VISIBLE);
            resetEntry();
        }
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
        pinPanel.setVisibility(which == PANEL_PIN ? VISIBLE : GONE);
        menuPanel.setVisibility(which == PANEL_MENU ? VISIBLE : GONE);
        devLogPanel.setVisibility(which == PANEL_DEVLOG ? VISIBLE : GONE);
        galleryPanel.setVisibility(which == PANEL_GALLERY ? VISIBLE : GONE);
        detailPanel.setVisibility(which == PANEL_DETAIL ? VISIBLE : GONE);
        langPanel.setVisibility(which == PANEL_LANG ? VISIBLE : GONE);
        historyPanel.setVisibility(which == PANEL_HISTORY ? VISIBLE : GONE);
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

    private void showGallery() {
        showPanel(PANEL_GALLERY);
        galleryEmpty.setVisibility(GONE);
        setExportEnabled(false);
        dbExecutor.submit(() -> {
            SelfieRepository repository = SelfieRepository.get(getContext());
            List<SelfieEntity> items = repository.getAll();
            File dir = repository.imagesDir();
            post(() -> {
                selfieAdapter.setData(items, dir);
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
                    Toast.makeText(getContext(), R.string.admin_export_done, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), R.string.admin_export_failed, Toast.LENGTH_SHORT).show();
                }
            });
        });
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
        currentDetail = selfie;
        showPanel(PANEL_DETAIL);
        detailNumber.setText("#" + selfie.number);
        detailDate.setText(new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY)
                .format(new Date(selfie.createdAt)));
        updateFavoriteButton();
        detailImage.setImageBitmap(null);

        File file = new File(SelfieRepository.get(getContext()).imagesDir(), selfie.filename);
        dbExecutor.submit(() -> {
            Bitmap bitmap = SelfieAdapter.decodeThumb(file, 1000);
            post(() -> {
                if (currentDetail == selfie) {
                    detailImage.setImageBitmap(bitmap);
                }
            });
        });
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
