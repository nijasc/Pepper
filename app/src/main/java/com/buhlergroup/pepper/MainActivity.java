package com.buhlergroup.pepper;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.HolderBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.holder.AutonomousAbilitiesType;
import com.aldebaran.qi.sdk.object.holder.Holder;
import com.buhlergroup.pepper.action.ActionHandler;
import com.buhlergroup.pepper.action.follow.FollowController;
import com.buhlergroup.pepper.action.admin.AdminController;
import com.buhlergroup.pepper.action.admin.AdminView;
import com.buhlergroup.pepper.action.dance.DanceLibraryController;
import com.buhlergroup.pepper.action.dance.DanceLibraryView;
import com.buhlergroup.pepper.action.dance.RobotContext;
import com.buhlergroup.pepper.action.dialogue.DialogueController;
import com.buhlergroup.pepper.action.dialogue.DialogueView;
import com.buhlergroup.pepper.action.hold.HoldController;
import com.buhlergroup.pepper.action.hold.HoldView;
import com.buhlergroup.pepper.action.navigation.NavigationController;
import com.buhlergroup.pepper.action.navigation.NavigationManager;
import com.buhlergroup.pepper.action.navigation.NavigationView;
import com.buhlergroup.pepper.action.memory.MemoryGameController;
import com.buhlergroup.pepper.action.memory.MemoryGameView;
import com.buhlergroup.pepper.action.raffle.RaffleJoinController;
import com.buhlergroup.pepper.action.raffle.RaffleJoinView;
import com.buhlergroup.pepper.action.raffle.RaffleRepository;
import com.buhlergroup.pepper.action.raffle.WinnerController;
import com.buhlergroup.pepper.action.raffle.WinnerView;
import com.buhlergroup.pepper.action.selfie.SelfieController;
import com.buhlergroup.pepper.action.selfie.SelfieRepository;
import com.buhlergroup.pepper.action.selfie.SelfieView;
import com.buhlergroup.pepper.lang.LanguageManager;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.lang.SupportedLanguage;
import com.buhlergroup.pepper.openai.OpenAIService;
import com.buhlergroup.pepper.openai.history.HistoryManager;

import java.util.ArrayList;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {
    private static final int SPEECH_EVENT = 10;
    private static final int DANCE_EDIT_SPEECH_EVENT = 11;
    private static final long SPEECH_WATCHDOG_INTERVAL_MS = 5000;
    private static final long SPEECH_WATCHDOG_IDLE_MS = 15000;
    private SpeechRecognizer recognizer;
    private Intent intent;
    private String said = "";
    private ActionHandler executionHandler;
    private LanguageManager languageManager;
    private HistoryManager historyManager;
    private Button stopFollowButton;
    private Button adminButton;
    private Holder backgroundMovementHolder;
    private TextView languageLabel;
    private volatile boolean listening;
    private volatile boolean listenPending;
    private volatile boolean processing;
    private volatile long lastListenStartMs;
    private final Handler watchdogHandler = new Handler(Looper.getMainLooper());
    private final Runnable speechWatchdog = new Runnable() {
        @Override
        public void run() {
            checkSpeechWatchdog();
            watchdogHandler.postDelayed(this, SPEECH_WATCHDOG_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        QiSDK.register(this, this);
        Log.d("Mainactivity", "ACreate");

        setContentView(R.layout.activity_main);
        stopFollowButton = findViewById(R.id.stopFollowButton);
        stopFollowButton.setOnClickListener(v -> FollowController.get().stop());
        languageLabel = findViewById(R.id.languageLabel);

        MemoryGameView memoryGame = findViewById(R.id.memoryGame);
        MemoryGameController.get().attachView(memoryGame);

        SelfieView selfieView = findViewById(R.id.selfieView);
        SelfieController.get().attachView(selfieView);

        RaffleJoinView raffleJoinView = findViewById(R.id.raffleJoinView);
        RaffleJoinController.get().attachView(raffleJoinView);

        DialogueView dialogueView = findViewById(R.id.dialogueView);
        DialogueController.get().attachView(dialogueView);

        AdminView adminView = findViewById(R.id.adminView);
        AdminController.get().attachView(adminView);
        adminButton = findViewById(R.id.adminButton);
        adminButton.setOnClickListener(v -> AdminController.get().open());

        NavigationView navigationView = findViewById(R.id.navigationView);
        NavigationController.get().attachView(navigationView);

        DanceLibraryView danceLibraryView = findViewById(R.id.danceLibraryView);
        DanceLibraryController.get().attachView(danceLibraryView);

        HoldView holdView = findViewById(R.id.holdView);
        HoldController.get().attachView(holdView);

        WinnerView winnerView = findViewById(R.id.winnerView);
        WinnerController.get().attachView(winnerView);

        AdminController.get().setAdminStateListener(open -> updateHomeControls());
        SelfieController.get().setStateListener(active -> updateHomeControls());
        RaffleJoinController.get().setStateListener(active -> updateHomeControls());
        NavigationController.get().setStateListener(open -> updateHomeControls());
        DanceLibraryController.get().setStateListener(open -> updateHomeControls());
        HoldController.get().setStateListener(active -> updateHomeControls());
        WinnerController.get().setStateListener(active -> updateHomeControls());

        initSpeech();
        lastListenStartMs = SystemClock.elapsedRealtime();
        watchdogHandler.postDelayed(speechWatchdog, SPEECH_WATCHDOG_INTERVAL_MS);
        RaffleRepository.purgeExpiredAsync(this);
        SelfieRepository.purgeExpiredAsync(this);

        DanceLibraryController.get().setVoiceRequester(() -> runOnUiThread(() -> {
            if (intent != null && intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, DANCE_EDIT_SPEECH_EVENT);
            }
        }));
    }

    @Override
    protected void onDestroy() {
        Log.d("Mainactivity", "ADestroyed");

        MemoryGameController.get().abort();
        MemoryGameController.get().detachView();
        SelfieController.get().setStateListener(null);
        SelfieController.get().detachView();
        SelfieController.get().stopServer();
        RaffleJoinController.get().setStateListener(null);
        RaffleJoinController.get().detachView();
        AdminController.get().setAdminStateListener(null);
        AdminController.get().detachView();
        DialogueController.get().detachView();
        NavigationController.get().setStateListener(null);
        NavigationController.get().detachView();
        NavigationManager.get().onFocusLost();
        DanceLibraryController.get().setStateListener(null);
        DanceLibraryController.get().detachView();
        HoldController.get().setStateListener(null);
        HoldController.get().detachView();
        WinnerController.get().setStateListener(null);
        WinnerController.get().detachView();
        watchdogHandler.removeCallbacks(speechWatchdog);
        QiSDK.unregister(this);
        recognizer.cancel();
        recognizer.destroy();
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {

        OpenAIService s = new OpenAIService(new ArrayList<>());
        s.setC(qiContext);
        s.getAuthToken(qiContext);
        Log.d("Mainactivity", "AFocus Gained");
        RobotContext.set(qiContext);
        holdBackgroundMovement(qiContext);
        NavigationManager.get().setQiContext(qiContext);
        FollowController.get().onFocusGained(qiContext);
        FollowController.get().setFollowStateListener(following -> {
            runOnUiThread(() ->
                    stopFollowButton.setVisibility(following ? View.VISIBLE : View.GONE));
            if (!following) {
                maybeResumeListening();
            }
        });

        if (languageManager == null) {
            languageManager = new LanguageManager(intent);
            languageManager.setLanguageChangeListener(this::updateLanguageLabel);
            SpeechManager.getInstance().setLanguageManager(languageManager);
        }
        updateLanguageLabel(languageManager.getCurrent());
        if (historyManager == null) {
            historyManager = new HistoryManager();
        }
        AdminController.get().setHistoryManager(historyManager);
        AdminController.get().setLanguageManager(languageManager);
        try {
            if (executionHandler == null) {
                executionHandler = new ActionHandler(languageManager, historyManager);
            }
            if (!said.isEmpty()) {
                processing = true;
                try {
                    executionHandler.handleInput(qiContext, said);
                } finally {
                    processing = false;
                }
                said = "";
            }
        } catch (Exception e) {
            Log.e("Mainactivity", "OnFocuseGainedError: " + e.getMessage());
        }
        listenToSpeech();
    }

    @Override
    public void onRobotFocusLost() {
        RobotContext.clear();
        MemoryGameController.get().abort();
        FollowController.get().setFollowStateListener(null);
        FollowController.get().onFocusLost();
        HoldController.get().onFocusLost();
        NavigationManager.get().onFocusLost();
        releaseBackgroundMovement();
        Log.d("Mainactivity", "AFocus Lost");
    }

    private void holdBackgroundMovement(QiContext qiContext) {
        releaseBackgroundMovement();
        try {
            backgroundMovementHolder = HolderBuilder.with(qiContext)
                    .withAutonomousAbilities(AutonomousAbilitiesType.BACKGROUND_MOVEMENT)
                    .build();
            backgroundMovementHolder.hold();
        } catch (Exception e) {
            Log.e("Mainactivity", "holdBackgroundMovement failed: " + e.getMessage());
        }
    }

    private void releaseBackgroundMovement() {
        if (backgroundMovementHolder != null) {
            try {
                backgroundMovementHolder.release();
            } catch (Exception ignored) {
            }
            backgroundMovementHolder = null;
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.d("Mainactivity", "AFocus Refused");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("Mainactivity", "APermission Result");

        if (requestCode == 1 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Log.i("MainActivity_request", "Permission Granted");
        }
        Log.i("MainActivity", "Permission");
    }

    private void updateHomeControls() {
        boolean overlayOpen = AdminController.get().isOpen()
                || SelfieController.get().isRunning()
                || RaffleJoinController.get().isBusy()
                || NavigationController.get().isOpen()
                || DanceLibraryController.get().isOpen()
                || HoldController.get().isActive()
                || WinnerController.get().isActive();
        DialogueController.get().setSuppressed(overlayOpen);
        runOnUiThread(() -> {
            int visibility = overlayOpen ? View.GONE : View.VISIBLE;
            adminButton.setVisibility(visibility);
            languageLabel.setVisibility(visibility);
        });
        maybeResumeListening();
    }

    private void updateLanguageLabel(SupportedLanguage lang) {
        if (languageLabel != null) {
            runOnUiThread(() -> languageLabel.setText(lang.getDisplayName()));
        }
    }

    private void listenToSpeech() {
        if (isOverlayOpen()) {
            listenPending = true;
            return;
        }
        listenPending = false;
        startSpeechRecognition();
    }

    private boolean isOverlayOpen() {
        return FollowController.get().isFollowing()
                || HoldController.get().isActive()
                || AdminController.get().isOpen()
                || NavigationController.get().isOpen()
                || DanceLibraryController.get().isOpen()
                || WinnerController.get().isActive();
    }

    private void startSpeechRecognition() {
        runOnUiThread(() -> {
            if (listening) {
                return;
            }
            if (intent != null && intent.resolveActivity(getPackageManager()) != null) {
                listening = true;
                lastListenStartMs = SystemClock.elapsedRealtime();
                startActivityForResult(intent, SPEECH_EVENT);
            }
        });
    }

    private void maybeResumeListening() {
        if (listenPending && !listening && !isOverlayOpen()) {
            listenToSpeech();
        }
    }

    private void checkSpeechWatchdog() {
        if (listening || processing || isOverlayOpen()) {
            return;
        }
        long idle = SystemClock.elapsedRealtime() - lastListenStartMs;
        if (idle > SPEECH_WATCHDOG_IDLE_MS) {
            Log.w("Mainactivity", "Speech watchdog restarting recognition after " + idle + "ms idle");
            listenToSpeech();
        }
    }

    private void initSpeech() {
        checkPermission();
        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }

    private void checkPermission() {
        String[] required = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        java.util.List<String> missing = new java.util.ArrayList<>();
        for (String permission : required) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                missing.add(permission);
            }
        }
        if (!missing.isEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toArray(new String[0]), 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Mainactivity", "AActivity result");

        if (requestCode == SPEECH_EVENT) {
            listening = false;
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> results =
                        data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (results != null && !results.isEmpty()) {
                    said = results.get(0);
                }
            }
        } else if (requestCode == DANCE_EDIT_SPEECH_EVENT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> results =
                        data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (results != null && !results.isEmpty()) {
                    DanceLibraryController.get().onVoiceEditResult(results.get(0));
                }
            }
        }
    }

}