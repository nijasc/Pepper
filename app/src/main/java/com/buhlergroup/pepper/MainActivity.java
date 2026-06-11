package com.buhlergroup.pepper;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import com.buhlergroup.pepper.action.memory.MemoryGameController;
import com.buhlergroup.pepper.action.memory.MemoryGameView;
import com.buhlergroup.pepper.action.raffle.RaffleJoinController;
import com.buhlergroup.pepper.action.raffle.RaffleJoinView;
import com.buhlergroup.pepper.action.selfie.SelfieController;
import com.buhlergroup.pepper.action.selfie.SelfieView;
import com.buhlergroup.pepper.lang.LanguageManager;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.lang.SupportedLanguage;
import com.buhlergroup.pepper.openai.OpenAIService;
import com.buhlergroup.pepper.openai.history.HistoryManager;

import java.util.ArrayList;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {
    private static final int SPEECH_EVENT = 10;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        QiSDK.register(this, this);
        Log.e("Mainactivity", "ACreate");

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

        AdminView adminView = findViewById(R.id.adminView);
        AdminController.get().attachView(adminView);
        adminButton = findViewById(R.id.adminButton);
        adminButton.setOnClickListener(v -> AdminController.get().open());

        AdminController.get().setAdminStateListener(open -> updateHomeControls());
        SelfieController.get().setStateListener(active -> updateHomeControls());
        RaffleJoinController.get().setStateListener(active -> updateHomeControls());

        initSpeech();
    }

    @Override
    protected void onDestroy() {
        Log.e("Mainactivity", "ADestroyed");

        MemoryGameController.get().abort();
        MemoryGameController.get().detachView();
        SelfieController.get().setStateListener(null);
        SelfieController.get().detachView();
        SelfieController.get().stopServer();
        RaffleJoinController.get().setStateListener(null);
        RaffleJoinController.get().detachView();
        AdminController.get().setAdminStateListener(null);
        AdminController.get().detachView();
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
        Log.e("Mainactivity", "AFocus Gained");
        holdBackgroundMovement(qiContext);
        FollowController.get().onFocusGained(qiContext);
        FollowController.get().setFollowStateListener(following ->
                runOnUiThread(() ->
                        stopFollowButton.setVisibility(following ? View.VISIBLE : View.GONE)));

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
                executionHandler.handleInput(qiContext, said);
                said = "";
            }
        } catch (Exception e) {
            Log.e("Mainactivity", "OnFocuseGainedError: " + e.getMessage());
        }
        listenToSpeech();
    }

    @Override
    public void onRobotFocusLost() {
        MemoryGameController.get().abort();
        FollowController.get().setFollowStateListener(null);
        FollowController.get().onFocusLost();
        releaseBackgroundMovement();
        Log.e("Mainactivity", "AFocus Lost");
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
        Log.e("Mainactivity", "AFocus Refused");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("Mainactivity", "APermission Result");

        if (requestCode == 1 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Log.i("MainActivity_request", "Permission Granted");
        }
        Log.i("MainActivity", "Permission");
    }

    private void updateHomeControls() {
        boolean overlayOpen = AdminController.get().isOpen()
                || SelfieController.get().isRunning()
                || RaffleJoinController.get().isBusy();
        runOnUiThread(() -> {
            int visibility = overlayOpen ? View.GONE : View.VISIBLE;
            adminButton.setVisibility(visibility);
            languageLabel.setVisibility(visibility);
        });
    }

    private void updateLanguageLabel(SupportedLanguage lang) {
        if (languageLabel != null) {
            runOnUiThread(() -> languageLabel.setText(lang.getDisplayName()));
        }
    }

    private void listenToSpeech() {
        while (FollowController.get().isFollowing() || AdminController.get().isOpen()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, SPEECH_EVENT);
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("Mainactivity", "AActivity result");

        if (requestCode == SPEECH_EVENT) {
            if (resultCode == RESULT_OK && data != null) {
                said = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
            }
        }
    }

}