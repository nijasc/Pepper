package com.buhlergroup.pepper.action.navigation;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
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

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.navigation.data.RoomScanEntity;
import com.buhlergroup.pepper.action.navigation.data.WaypointEntity;
import com.buhlergroup.pepper.debug.DebugLog;

import java.util.List;

public class NavigationView extends FrameLayout {

    private TextView statusView;
    private EditText scanName;
    private EditText waypointName;
    private CheckBox fotostand;
    private LinearLayout scanList;
    private LinearLayout waypointList;
    private ImageView mapImage;
    private WaypointMapView waypointMap;
    private View scanStartButton;
    private View scanStopButton;
    private View waypointSaveButton;
    private ScrollView scrollRoot;
    private View scanFullscreen;
    private ImageView scanMap;

    public NavigationView(Context context) {
        super(context);
        init(context);
    }

    public NavigationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NavigationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_navigation, this, true);
        setBackgroundColor(ContextCompat.getColor(context, R.color.game_overlay));
        setClickable(true);
        setFocusable(true);

        scrollRoot = findViewById(R.id.navScrollRoot);
        statusView = findViewById(R.id.navStatus);
        scanName = findViewById(R.id.navScanName);
        waypointName = findViewById(R.id.navWpName);
        fotostand = findViewById(R.id.navWpFotostand);
        scanList = findViewById(R.id.navScanList);
        waypointList = findViewById(R.id.navWpList);
        mapImage = findViewById(R.id.navMapImage);
        waypointMap = findViewById(R.id.navWaypointMap);
        scanStartButton = findViewById(R.id.navScanStart);
        scanStopButton = findViewById(R.id.navScanStop);
        waypointSaveButton = findViewById(R.id.navWpSave);
        scanFullscreen = findViewById(R.id.navScanFullscreen);
        scanMap = findViewById(R.id.navScanMap);

        findViewById(R.id.navMapRefresh).setOnClickListener(v -> loadMap());
        scanStartButton.setOnClickListener(v -> startScan());
        scanStopButton.setOnClickListener(v -> requestScanStop());
        findViewById(R.id.navScanStopBig).setOnClickListener(v -> requestScanStop());
        findViewById(R.id.navScanJogForward).setOnClickListener(v -> jog(0.3, 0.0, 0.0));
        findViewById(R.id.navScanJogBack).setOnClickListener(v -> jog(-0.3, 0.0, 0.0));
        findViewById(R.id.navScanJogLeft).setOnClickListener(v -> jog(0.0, 0.0, 0.5));
        findViewById(R.id.navScanJogRight).setOnClickListener(v -> jog(0.0, 0.0, -0.5));
        findViewById(R.id.navScanRotate).setOnClickListener(v -> rotateInPlace());
        waypointSaveButton.setOnClickListener(v -> saveWaypoint());
        findViewById(R.id.navJogForward).setOnClickListener(v -> jog(0.3, 0.0, 0.0));
        findViewById(R.id.navJogBack).setOnClickListener(v -> jog(-0.3, 0.0, 0.0));
        findViewById(R.id.navJogLeft).setOnClickListener(v -> jog(0.0, 0.0, 0.5));
        findViewById(R.id.navJogRight).setOnClickListener(v -> jog(0.0, 0.0, -0.5));
        findViewById(R.id.navClose).setOnClickListener(v -> NavigationController.get().close());

        NavigationManager.get().setScanStopCallback(() -> post(this::requestScanStop));
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            applyDebugInset();
        }
    }

    private void applyDebugInset() {
        if (scrollRoot == null) {
            return;
        }
        int side = scrollRoot.getPaddingLeft();
        int top = DebugLog.get().isEnabled()
                ? getResources().getDimensionPixelSize(R.dimen.debug_overlay_inset)
                : side;
        scrollRoot.setPadding(side, top, side, scrollRoot.getPaddingBottom());
    }

    public void open() {
        NavigationManager.get().setMapUpdateListener(bitmap -> post(() -> {
            mapImage.setImageBitmap(bitmap);
            mapImage.setVisibility(VISIBLE);
            scanMap.setImageBitmap(bitmap);
        }));
        post(() -> {
            setVisibility(VISIBLE);
            bringToFront();
            refreshAll();
        });
    }

    public void hide() {
        NavigationManager.get().setMapUpdateListener(null);
        post(() -> setVisibility(GONE));
    }

    private void refreshAll() {
        updateStatus();
        loadScans();
        loadWaypoints();
        loadRobotPose();
    }

    private void loadRobotPose() {
        NavigationManager.get().getRobotPose(new NavigationManager.Callback<double[]>() {
            @Override
            public void onResult(double[] value) {
                post(() -> waypointMap.setRobotPose(value));
            }

            @Override
            public void onError(String error) {
                post(() -> waypointMap.setRobotPose(null));
            }
        });
    }

    private void updateStatus() {
        NavigationManager nav = NavigationManager.get();
        String text;
        if (nav.isScanning()) {
            text = getContext().getString(R.string.nav_status_scanning);
        } else if (nav.isLocalized() && nav.getActiveScan() != null) {
            text = getContext().getString(R.string.nav_status_localized, nav.getActiveScan().name);
        } else if (nav.getActiveScan() != null) {
            text = getContext().getString(R.string.nav_status_not_localized, nav.getActiveScan().name);
        } else {
            text = getContext().getString(R.string.nav_status_idle);
        }
        statusView.setText(text);

        boolean scanning = nav.isScanning();
        boolean localized = nav.isLocalized();
        setActionEnabled(scanStartButton, !scanning);
        setActionEnabled(scanStopButton, scanning);
        setActionEnabled(waypointSaveButton, localized);
    }

    private void setActionEnabled(View view, boolean enabled) {
        if (view != null) {
            view.setEnabled(enabled);
            view.setAlpha(enabled ? 1f : 0.4f);
        }
    }

    private void startScan() {
        NavigationManager.get().startScan(new NavigationManager.Callback<Void>() {
            @Override
            public void onResult(Void value) {
                post(() -> {
                    toast(R.string.nav_scan_started);
                    enterScanFullscreen();
                    updateStatus();
                });
            }

            @Override
            public void onError(String error) {
                post(() -> toastText(error));
            }
        });
    }

    private void requestScanStop() {
        if (!NavigationManager.get().isScanning()) {
            return;
        }
        exitScanFullscreen();
        stopScan();
    }

    private void enterScanFullscreen() {
        if (scanFullscreen != null) {
            scanFullscreen.setVisibility(VISIBLE);
            scanFullscreen.bringToFront();
        }
    }

    private void exitScanFullscreen() {
        if (scanFullscreen != null) {
            scanFullscreen.setVisibility(GONE);
        }
    }

    private void stopScan() {
        String name = scanName.getText().toString().trim();
        if (name.isEmpty()) {
            name = getContext().getString(R.string.nav_default_scan_name);
        }
        NavigationManager.get().stopAndSaveScan(name, new NavigationManager.Callback<RoomScanEntity>() {
            @Override
            public void onResult(RoomScanEntity value) {
                post(() -> {
                    scanName.setText("");
                    toast(R.string.nav_scan_saved);
                    refreshAll();
                });
            }

            @Override
            public void onError(String error) {
                post(() -> toastText(error));
            }
        });
    }

    private void jog(double dx, double dy, double dTheta) {
        NavigationManager.get().jog(dx, dy, dTheta, new NavigationManager.Callback<Void>() {
            @Override
            public void onResult(Void value) {
                post(() -> loadRobotPose());
            }

            @Override
            public void onError(String error) {
                post(() -> toastText(error));
            }
        });
    }

    private void rotateInPlace() {
        NavigationManager.get().rotateInPlace(new NavigationManager.Callback<Void>() {
            @Override
            public void onResult(Void value) {
                post(() -> loadRobotPose());
            }

            @Override
            public void onError(String error) {
                post(() -> toastText(error));
            }
        });
    }

    private void saveWaypoint() {
        String name = waypointName.getText().toString().trim();
        if (name.isEmpty()) {
            name = getContext().getString(R.string.nav_default_wp_name);
        }
        String type = fotostand.isChecked()
                ? WaypointEntity.TYPE_FOTOSTAND : WaypointEntity.TYPE_GENERAL;
        NavigationManager.get().saveWaypoint(name, type, new NavigationManager.Callback<WaypointEntity>() {
            @Override
            public void onResult(WaypointEntity value) {
                post(() -> {
                    waypointName.setText("");
                    fotostand.setChecked(false);
                    toast(R.string.nav_wp_saved);
                    loadWaypoints();
                });
            }

            @Override
            public void onError(String error) {
                post(() -> toastText(error));
            }
        });
    }

    private void loadMap() {
        NavigationManager.get().getMapBitmap(new NavigationManager.Callback<Bitmap>() {
            @Override
            public void onResult(Bitmap value) {
                post(() -> {
                    mapImage.setImageBitmap(value);
                    mapImage.setVisibility(VISIBLE);
                });
            }

            @Override
            public void onError(String error) {
                post(() -> toastText(error));
            }
        });
    }

    private void loadScans() {
        NavigationManager.get().listScans(new NavigationManager.Callback<List<RoomScanEntity>>() {
            @Override
            public void onResult(List<RoomScanEntity> value) {
                post(() -> renderScans(value));
            }

            @Override
            public void onError(String error) {
                post(() -> toastText(error));
            }
        });
    }

    private void loadWaypoints() {
        NavigationManager.get().listWaypoints(new NavigationManager.Callback<List<WaypointEntity>>() {
            @Override
            public void onResult(List<WaypointEntity> value) {
                post(() -> renderWaypoints(value));
            }

            @Override
            public void onError(String error) {
                post(() -> renderWaypoints(null));
            }
        });
    }

    private void renderScans(List<RoomScanEntity> scans) {
        scanList.removeAllViews();
        if (scans == null || scans.isEmpty()) {
            scanList.addView(emptyLabel(R.string.nav_no_scans));
            return;
        }
        for (RoomScanEntity scan : scans) {
            LinearLayout row = row(scan.name);
            row.addView(pill(getContext().getString(R.string.nav_activate),
                    R.drawable.bg_pill_teal, v -> activate(scan)));
            row.addView(pill(getContext().getString(R.string.nav_delete),
                    R.drawable.bg_pill_red, v -> deleteScan(scan)));
            scanList.addView(row);
        }
    }

    private void renderWaypoints(List<WaypointEntity> waypoints) {
        waypointMap.setWaypoints(waypoints);
        waypointList.removeAllViews();
        if (waypoints == null || waypoints.isEmpty()) {
            waypointList.addView(emptyLabel(R.string.nav_no_waypoints));
            return;
        }
        for (WaypointEntity wp : waypoints) {
            String label = wp.name;
            if (WaypointEntity.TYPE_FOTOSTAND.equals(wp.type)) {
                label = label + " (" + getContext().getString(R.string.nav_wp_fotostand) + ")";
            }
            LinearLayout row = row(label);
            row.addView(pill(getContext().getString(R.string.nav_drive_here),
                    R.drawable.bg_pill_teal, v -> driveTo(wp)));
            row.addView(pill(getContext().getString(R.string.nav_delete),
                    R.drawable.bg_pill_red, v -> deleteWaypoint(wp)));
            waypointList.addView(row);
        }
    }

    private void activate(RoomScanEntity scan) {
        toast(R.string.nav_localizing);
        NavigationManager.get().localize(scan, new NavigationManager.Callback<Boolean>() {
            @Override
            public void onResult(Boolean value) {
                post(() -> {
                    toast(R.string.nav_localized);
                    updateStatus();
                    loadWaypoints();
                    loadRobotPose();
                });
            }

            @Override
            public void onError(String error) {
                post(() -> {
                    toastText(error);
                    updateStatus();
                });
            }
        });
    }

    private void deleteScan(RoomScanEntity scan) {
        NavigationManager.get().deleteScan(scan, new NavigationManager.Callback<Void>() {
            @Override
            public void onResult(Void value) {
                post(() -> refreshAll());
            }

            @Override
            public void onError(String error) {
                post(() -> toastText(error));
            }
        });
    }

    private void driveTo(WaypointEntity wp) {
        toast(R.string.nav_driving);
        NavigationManager.get().goToWaypoint(wp, new NavigationManager.Callback<Void>() {
            @Override
            public void onResult(Void value) {
                post(() -> {
                    toast(R.string.nav_arrived);
                    loadRobotPose();
                });
            }

            @Override
            public void onError(String error) {
                post(() -> toastText(error));
            }
        });
    }

    private void deleteWaypoint(WaypointEntity wp) {
        NavigationManager.get().deleteWaypoint(wp, new NavigationManager.Callback<Void>() {
            @Override
            public void onResult(Void value) {
                post(() -> loadWaypoints());
            }

            @Override
            public void onError(String error) {
                post(() -> toastText(error));
            }
        });
    }

    private LinearLayout row(String label) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.topMargin = dp(6);
        row.setLayoutParams(rowParams);

        TextView text = new TextView(getContext());
        text.setText(label);
        text.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        text.setTextSize(18);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        text.setLayoutParams(textParams);
        row.addView(text);
        return row;
    }

    private TextView pill(String text, int bgRes, OnClickListener onClick) {
        TextView pill = new TextView(getContext());
        pill.setText(text);
        pill.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        pill.setTextSize(15);
        pill.setGravity(Gravity.CENTER);
        pill.setBackgroundResource(bgRes);
        pill.setPadding(dp(20), dp(10), dp(20), dp(10));
        pill.setClickable(true);
        pill.setFocusable(true);
        pill.setOnClickListener(onClick);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMarginStart(dp(8));
        pill.setLayoutParams(params);
        return pill;
    }

    private TextView emptyLabel(int resId) {
        TextView text = new TextView(getContext());
        text.setText(resId);
        text.setTextColor(0xCCFFFFFF);
        text.setTextSize(16);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(6);
        text.setLayoutParams(params);
        return text;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void toast(int resId) {
        Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT).show();
    }

    private void toastText(String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }
}
