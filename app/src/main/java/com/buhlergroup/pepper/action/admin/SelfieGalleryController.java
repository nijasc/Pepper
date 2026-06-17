package com.buhlergroup.pepper.action.admin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.raffle.RaffleRepository;
import com.buhlergroup.pepper.action.selfie.QrGenerator;
import com.buhlergroup.pepper.action.selfie.SelfieController;
import com.buhlergroup.pepper.action.selfie.SelfieRepository;
import com.buhlergroup.pepper.action.selfie.SelfieSettings;
import com.buhlergroup.pepper.action.selfie.data.SelfieEntity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final class SelfieGalleryController {

    private static final String TAG = "AdminView";

    private final View root;
    private final Executor executor;
    private final PanelNavigator panelNav;

    private final TextView galleryEmpty;
    private final Button exportAllButton;
    private final RecyclerView selfieGrid;
    private final SelfieAdapter selfieAdapter;

    private final ImageView detailImage;
    private final ImageView detailQr;
    private final TextView detailQrHint;
    private final TextView detailNumber;
    private final TextView detailDate;
    private final Button detailFavorite;
    private SelfieEntity currentDetail;
    private boolean detailServerHeld = false;

    SelfieGalleryController(View root, Executor executor, PanelNavigator panelNav) {
        this.root = root;
        this.executor = executor;
        this.panelNav = panelNav;

        galleryEmpty = root.findViewById(R.id.adminGalleryEmpty);
        exportAllButton = root.findViewById(R.id.adminExportAll);
        selfieGrid = root.findViewById(R.id.adminSelfieGrid);
        detailImage = root.findViewById(R.id.adminDetailImage);
        detailQr = root.findViewById(R.id.adminDetailQr);
        detailQrHint = root.findViewById(R.id.adminDetailQrHint);
        detailNumber = root.findViewById(R.id.adminDetailNumber);
        detailDate = root.findViewById(R.id.adminDetailDate);
        detailFavorite = root.findViewById(R.id.adminDetailFavorite);

        selfieAdapter = new SelfieAdapter(this::showDetail);
        selfieGrid.setLayoutManager(new GridLayoutManager(ctx(), 3));
        selfieGrid.setAdapter(selfieAdapter);

        exportAllButton.setOnClickListener(v -> onExportAll());
        detailFavorite.setOnClickListener(v -> toggleFavorite());
        root.findViewById(R.id.adminDetailDelete).setOnClickListener(v -> deleteCurrent());
        root.findViewById(R.id.adminSelfieRetention).setOnClickListener(v -> showSelfieRetentionDialog());
    }

    private Context ctx() {
        return root.getContext();
    }

    void releaseDetailServer() {
        if (detailServerHeld) {
            SelfieController.get().releaseServer();
            detailServerHeld = false;
        }
    }

    void showGallery() {
        releaseDetailServer();
        panelNav.show(PanelNavigator.PANEL_GALLERY);
        galleryEmpty.setVisibility(View.GONE);
        setExportEnabled(false);
        executor.execute(() -> {
            SelfieRepository repository = SelfieRepository.get(ctx());
            List<SelfieEntity> items = repository.getAll();
            File dir = repository.imagesDir();
            Set<String> linked = new HashSet<>(RaffleRepository.get(ctx()).linkedSelfieIds());
            root.post(() -> {
                selfieAdapter.setData(items, dir, linked);
                galleryEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
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
        executor.execute(() -> {
            File zip = createSelfiesZip();
            root.post(() -> {
                exportAllButton.setText(R.string.admin_export_all);
                setExportEnabled(true);
                if (zip != null) {
                    shareZip(zip);
                } else {
                    Toast.makeText(ctx(), R.string.admin_export_failed, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void shareZip(File zip) {
        try {
            Uri uri = FileProvider.getUriForFile(ctx(),
                    ctx().getPackageName() + ".fileprovider", zip);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("application/zip");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent chooser = Intent.createChooser(share,
                    ctx().getString(R.string.admin_export_share_title));
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx().startActivity(chooser);
        } catch (Exception e) {
            Log.w(TAG, "Teilen fehlgeschlagen: " + e.getMessage());
            Toast.makeText(ctx(), R.string.admin_export_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private File createSelfiesZip() {
        File imagesDir = SelfieRepository.get(ctx()).imagesDir();
        File[] files = imagesDir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        File zipFile = new File(ctx().getCacheDir(), "selfies_export.zip");
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

    void showDetail(SelfieEntity selfie) {
        if (!detailServerHeld) {
            SelfieController.get().acquireServer(ctx());
            detailServerHeld = true;
        }
        currentDetail = selfie;
        panelNav.show(PanelNavigator.PANEL_DETAIL);
        detailNumber.setText("#" + selfie.number);
        detailDate.setText(new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY)
                .format(new Date(selfie.createdAt)));
        updateFavoriteButton();
        detailImage.setImageBitmap(null);
        detailQr.setImageBitmap(null);
        detailQr.setVisibility(View.GONE);
        detailQrHint.setVisibility(View.GONE);

        File file = new File(SelfieRepository.get(ctx()).imagesDir(), selfie.filename);
        executor.execute(() -> {
            Bitmap bitmap = SelfieAdapter.decodeThumb(file, 1000);
            Bitmap qr = buildSelfieQr(selfie);
            root.post(() -> {
                if (currentDetail == selfie) {
                    detailImage.setImageBitmap(bitmap);
                    if (qr != null) {
                        detailQr.setImageBitmap(qr);
                        detailQr.setVisibility(View.VISIBLE);
                        detailQrHint.setVisibility(View.GONE);
                    } else {
                        detailQr.setImageBitmap(null);
                        detailQr.setVisibility(View.GONE);
                        detailQrHint.setVisibility(View.VISIBLE);
                    }
                }
            });
        });
    }

    private Bitmap buildSelfieQr(SelfieEntity selfie) {
        String url = SelfieController.get().downloadUrl(ctx(), selfie.filename);
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
        executor.execute(() -> {
            SelfieRepository.get(ctx()).setFavorite(selfie.id, newFavorite);
            root.post(() -> {
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
        executor.execute(() -> {
            SelfieRepository.get(ctx()).delete(selfie.id);
            root.post(this::showGallery);
        });
    }

    private void showSelfieRetentionDialog() {
        EditText input = new EditText(ctx());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(R.string.selfie_retention_hint);
        input.setText(String.valueOf(SelfieSettings.getRetentionDays(ctx())));
        new AlertDialog.Builder(ctx())
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
                    SelfieSettings.setRetentionDays(ctx(), days);
                    Toast.makeText(ctx(), R.string.selfie_retention_saved,
                            Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}
