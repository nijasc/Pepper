package com.buhlergroup.pepper;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.HolderBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.holder.AutonomousAbilitiesType;
import com.aldebaran.qi.sdk.object.holder.Holder;
import com.buhlergroup.pepper.action.ActionHandler;
import com.buhlergroup.pepper.action.attract.AttractController;
import com.buhlergroup.pepper.action.career.CareerController;
import com.buhlergroup.pepper.action.career.CareerView;
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
import com.buhlergroup.pepper.action.quiz.QuizController;
import com.buhlergroup.pepper.action.quiz.QuizView;
import com.buhlergroup.pepper.action.raffle.RaffleJoinController;
import com.buhlergroup.pepper.action.raffle.RaffleJoinView;
import com.buhlergroup.pepper.action.raffle.RaffleRepository;
import com.buhlergroup.pepper.action.raffle.WinnerController;
import com.buhlergroup.pepper.action.raffle.WinnerView;
import com.buhlergroup.pepper.action.selfie.SelfieController;
import com.buhlergroup.pepper.action.selfie.SelfieRepository;
import com.buhlergroup.pepper.action.selfie.SelfieView;
import com.buhlergroup.pepper.debug.DebugLog;
import com.buhlergroup.pepper.debug.DebugOverlayView;
import com.buhlergroup.pepper.lang.LanguageManager;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.lang.SupportedLanguage;
import com.buhlergroup.pepper.openai.OpenAIService;
import com.buhlergroup.pepper.openai.history.HistoryManager;

import java.util.ArrayList;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {
    private static final int SPEECH_EVENT = 10;
    private static final int DANCE_EDIT_SPEECH_EVENT = 11;
    private String said = "";
    private ActionHandler executionHandler;
    private LanguageManager languageManager;
    private HistoryManager historyManager;
    private Button stopFollowButton;
    private Button adminButton;
    private Holder backgroundMovementHolder;
    private TextView languageLabel;
    private DebugOverlayView debugOverlay;
    private volatile boolean processing;
    private SpeechSession speech;

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

        QuizView quizView = findViewById(R.id.quizView);
        QuizController.get().attachView(quizView);

        CareerView careerView = findViewById(R.id.careerView);
        CareerController.get().attachView(careerView);

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

        debugOverlay = findViewById(R.id.debugOverlay);

        AdminController.get().setAdminStateListener(open -> updateHomeControls());
        SelfieController.get().setStateListener(active -> updateHomeControls());
        RaffleJoinController.get().setStateListener(active -> updateHomeControls());
        NavigationController.get().setStateListener(open -> updateHomeControls());
        DanceLibraryController.get().setStateListener(open -> updateHomeControls());
        HoldController.get().setStateListener(active -> updateHomeControls());
        WinnerController.get().setStateListener(active -> updateHomeControls());

        speech = new SpeechSession(this, SPEECH_EVENT, new SpeechSession.Gate() {
            @Override
            public boolean isOverlayOpen() {
                return MainActivity.this.isOverlayOpen();
            }

            @Override
            public boolean isBusy() {
                return processing;
            }

            @Override
            public void onTick() {
                tickAttract();
            }
        });
        speech.start();
        RaffleRepository.purgeExpiredAsync(this);
        SelfieRepository.purgeExpiredAsync(this);

        DanceLibraryController.get().setVoiceRequester(() ->
                runOnUiThread(() -> speech.requestDanceEdit(DANCE_EDIT_SPEECH_EVENT)));
    }

    @Override
    protected void onDestroy() {
        Log.d("Mainactivity", "ADestroyed");

        MemoryGameController.get().abort();
        MemoryGameController.get().detachView();
        QuizController.get().detachView();
        CareerController.get().detachView();
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
        speech.destroy();
        QiSDK.unregister(this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {

        OpenAIService s = OpenAIService.shared();
        s.setC(qiContext);
        s.getAuthToken(qiContext);
        Log.d("Mainactivity", "AFocus Gained");
        DebugLog.get().setStatus("Roboter-Fokus erhalten");
        DebugLog.get().i("MainActivity", "Roboter-Fokus erhalten");
        RobotContext.set(qiContext);
        holdBackgroundMovement(qiContext);
        NavigationManager.get().setQiContext(qiContext);
        NavigationManager.get().maybeAutoLocalize();
        FollowController.get().onFocusGained(qiContext);
        FollowController.get().setFollowStateListener(following -> {
            runOnUiThread(() ->
                    stopFollowButton.setVisibility(following ? View.VISIBLE : View.GONE));
            if (!following) {
                speech.resumeIfPending();
            }
        });

        if (languageManager == null) {
            languageManager = new LanguageManager(speech.recognitionIntent());
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
                AttractController.get().notifyInteraction();
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
        speech.listen();
    }

    @Override
    public void onRobotFocusLost() {
        RobotContext.clear();
        AttractController.get().stop();
        MemoryGameController.get().abort();
        FollowController.get().setFollowStateListener(null);
        FollowController.get().onFocusLost();
        HoldController.get().onFocusLost();
        NavigationManager.get().onFocusLost();
        releaseBackgroundMovement();
        Log.d("Mainactivity", "AFocus Lost");
        DebugLog.get().setStatus("Roboter-Fokus verloren");
        DebugLog.get().i("MainActivity", "Roboter-Fokus verloren");
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

    private boolean homeOverlayOpen() {
        return AdminController.get().isOpen()
                || SelfieController.get().isRunning()
                || RaffleJoinController.get().isBusy()
                || NavigationController.get().isOpen()
                || DanceLibraryController.get().isOpen()
                || HoldController.get().isActive()
                || WinnerController.get().isActive();
    }

    private void updateHomeControls() {
        boolean overlayOpen = homeOverlayOpen();
        DialogueController.get().setSuppressed(overlayOpen);
        if (debugOverlay != null) {
            debugOverlay.setSuppressed(AdminController.get().isOpen());
        }
        runOnUiThread(() -> {
            int visibility = overlayOpen ? View.GONE : View.VISIBLE;
            adminButton.setVisibility(visibility);
            languageLabel.setVisibility(visibility);
        });
        if (overlayOpen) {
            speech.pause();
        } else {
            speech.resumeIfPending();
        }
    }

    private void updateLanguageLabel(SupportedLanguage lang) {
        if (languageLabel != null) {
            runOnUiThread(() -> languageLabel.setText(lang.getDisplayName()));
        }
    }

    private boolean isOverlayOpen() {
        return FollowController.get().isFollowing()
                || HoldController.get().isActive()
                || AdminController.get().isOpen()
                || NavigationController.get().isOpen()
                || DanceLibraryController.get().isOpen()
                || WinnerController.get().isActive();
    }

    private void tickAttract() {
        AttractController.get().tick(RobotContext.get(), homeOverlayOpen(), processing);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Mainactivity", "AActivity result");

        if (requestCode == SPEECH_EVENT) {
            String recognized = speech.handleSpeechResult(resultCode, data);
            if (recognized != null) {
                said = recognized;
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