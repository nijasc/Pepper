package com.buhlergroup.pepper.action.admin;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.raffle.RaffleRepository;
import com.buhlergroup.pepper.robot.RobotContext;
import com.buhlergroup.pepper.action.raffle.WinnerController;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntity;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntryEntity;
import com.buhlergroup.pepper.action.raffle.data.RaffleStatus;
import com.buhlergroup.pepper.action.selfie.data.SelfieEntity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

final class RaffleAdminController {

    private final View root;
    private final Executor executor;
    private final PanelNavigator panelNav;
    private final SelfieGalleryController gallery;
    private final Runnable onClose;
    private final RaffleEntryRowFactory entryRowFactory;
    private final RaffleEmailSender emailSender;

    private final EditText raffleTitle;
    private final EditText raffleDescription;
    private final Button raffleEndDateButton;
    private final CheckBox raffleRequiresSelfie;
    private final CheckBox raffleRequiresPhone;
    private final TextView raffleCreateError;
    private final Button raffleCreateSave;
    private final TextView raffleOverviewTitle;
    private final TextView raffleOverviewStatus;
    private final LinearLayout raffleEntries;
    private final Button raffleCloseButton;
    private final Button raffleFinishButton;
    private final Button raffleDeleteButton;
    private final Button raffleDrawButton;
    private final Button raffleRedrawButton;
    private final Button raffleEmailButton;
    private final TextView raffleWinnerView;
    private long raffleEndDateMillis;
    private long currentRaffleId;
    private String currentRaffleTitle;
    private RaffleEntryEntity currentWinner;

    RaffleAdminController(View root, Executor executor, PanelNavigator panelNav,
                          SelfieGalleryController gallery, Runnable onClose) {
        this.root = root;
        this.executor = executor;
        this.panelNav = panelNav;
        this.gallery = gallery;
        this.onClose = onClose;
        this.entryRowFactory = new RaffleEntryRowFactory(root, executor,
                new RaffleEntryRowFactory.Host() {
                    @Override
                    public void onDeleteEntry(RaffleEntryEntity entry) {
                        confirmDeleteEntry(entry);
                    }

                    @Override
                    public void onShowSelfie(SelfieEntity selfie) {
                        gallery.showDetail(selfie);
                    }
                });
        this.emailSender = new RaffleEmailSender(root, executor);

        raffleTitle = root.findViewById(R.id.raffleTitle);
        raffleDescription = root.findViewById(R.id.raffleDescription);
        raffleEndDateButton = root.findViewById(R.id.raffleEndDate);
        raffleRequiresSelfie = root.findViewById(R.id.raffleRequiresSelfie);
        raffleRequiresPhone = root.findViewById(R.id.raffleRequiresPhone);
        raffleCreateError = root.findViewById(R.id.raffleCreateError);
        raffleCreateSave = root.findViewById(R.id.raffleCreateSave);
        raffleOverviewTitle = root.findViewById(R.id.adminRaffleTitle);
        raffleOverviewStatus = root.findViewById(R.id.adminRaffleStatus);
        raffleEntries = root.findViewById(R.id.adminRaffleEntries);
        raffleCloseButton = root.findViewById(R.id.adminRaffleClose);
        raffleFinishButton = root.findViewById(R.id.adminRaffleFinish);
        raffleDeleteButton = root.findViewById(R.id.adminRaffleDelete);
        raffleDrawButton = root.findViewById(R.id.adminRaffleDraw);
        raffleRedrawButton = root.findViewById(R.id.adminRaffleRedraw);
        raffleEmailButton = root.findViewById(R.id.adminRaffleEmail);
        raffleWinnerView = root.findViewById(R.id.adminRaffleWinner);

        root.findViewById(R.id.raffleDescriptionDone).setOnClickListener(this::hideKeyboard);
        raffleEndDateButton.setOnClickListener(v -> pickEndDate());
        raffleCreateSave.setOnClickListener(v -> onSaveRaffle());
        raffleFinishButton.setOnClickListener(v -> finishCurrentRaffle());
        raffleCloseButton.setOnClickListener(v -> closeCurrentRaffle());
        raffleDeleteButton.setOnClickListener(v -> confirmDeleteRaffle());
        raffleDrawButton.setOnClickListener(v -> drawWinner());
        raffleRedrawButton.setOnClickListener(v -> redrawWinner());
        raffleEmailButton.setOnClickListener(v -> sendWinnerEmail());
    }

    private Context ctx() {
        return root.getContext();
    }

    private int dp(int value) {
        return Math.round(value * root.getResources().getDisplayMetrics().density);
    }

    void openRaffle() {
        executor.execute(() -> {
            RaffleRepository repo = RaffleRepository.get(ctx());
            RaffleEntity raffle = repo.getCurrentRaffle();
            if (raffle == null) {
                root.post(this::showRaffleCreate);
                return;
            }
            List<RaffleEntryEntity> entries = repo.getEntries(raffle.id);
            root.post(() -> showRaffleOverview(raffle, entries));
        });
    }

    private void showRaffleCreate() {
        raffleTitle.setText("");
        raffleDescription.setText("");
        raffleRequiresSelfie.setChecked(false);
        raffleRequiresPhone.setChecked(false);
        raffleEndDateMillis = 0L;
        raffleEndDateButton.setText(R.string.raffle_end_date);
        raffleCreateError.setVisibility(View.GONE);
        raffleCreateSave.setEnabled(true);
        raffleCreateSave.setAlpha(1f);
        panelNav.show(PanelNavigator.PANEL_RAFFLE_CREATE);
    }

    private void hideKeyboard(View anchor) {
        InputMethodManager imm =
                (InputMethodManager) ctx().getSystemService(Context.INPUT_METHOD_SERVICE);
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
        DatePickerDialog dateDialog = new DatePickerDialog(ctx(), (view, year, month, day) -> {
            Calendar picked = Calendar.getInstance();
            if (raffleEndDateMillis > 0) {
                picked.setTimeInMillis(raffleEndDateMillis);
            }
            picked.set(Calendar.YEAR, year);
            picked.set(Calendar.MONTH, month);
            picked.set(Calendar.DAY_OF_MONTH, day);
            new TimePickerDialog(ctx(), (timeView, hour, minute) -> {
                picked.set(Calendar.HOUR_OF_DAY, hour);
                picked.set(Calendar.MINUTE, minute);
                picked.set(Calendar.SECOND, 0);
                raffleEndDateMillis = picked.getTimeInMillis();
                raffleEndDateButton.setText(AdminFormats.dateTime()
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
            raffleCreateError.setVisibility(View.VISIBLE);
            return;
        }
        boolean requiresSelfie = raffleRequiresSelfie.isChecked();
        boolean requiresPhone = raffleRequiresPhone.isChecked();
        long endDate = raffleEndDateMillis;
        raffleCreateSave.setEnabled(false);
        executor.execute(() -> {
            long id = RaffleRepository.get(ctx()).createRaffle(
                    title, description, requiresSelfie, requiresPhone, endDate);
            root.post(() -> {
                if (id < 0) {
                    raffleCreateError.setText(R.string.raffle_create_active_exists);
                    raffleCreateError.setVisibility(View.VISIBLE);
                    raffleCreateSave.setEnabled(true);
                } else {
                    Toast.makeText(ctx(), R.string.raffle_created, Toast.LENGTH_SHORT).show();
                    openRaffle();
                }
            });
        });
    }

    private void showRaffleOverview(RaffleEntity raffle, List<RaffleEntryEntity> entries) {
        currentRaffleId = raffle.id;
        currentRaffleTitle = raffle.title;
        raffleOverviewTitle.setText(raffle.title);
        if (raffle.status == RaffleStatus.ACTIVE) {
            String end = AdminFormats.dateTime()
                    .format(new Date(raffle.endDate));
            raffleOverviewStatus.setText(
                    ctx().getString(R.string.raffle_status_active, end, entries.size()));
        } else {
            raffleOverviewStatus.setText(
                    ctx().getString(R.string.raffle_status_ended, entries.size()));
        }
        boolean ended = raffle.status == RaffleStatus.ENDED;
        raffleCloseButton.setVisibility(raffle.status == RaffleStatus.ACTIVE ? View.VISIBLE : View.GONE);
        raffleFinishButton.setVisibility(ended ? View.VISIBLE : View.GONE);
        raffleDeleteButton.setVisibility(ended ? View.VISIBLE : View.GONE);

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
                    ctx().getString(R.string.raffle_winner_label, winner.name, winner.email));
            raffleWinnerView.setVisibility(View.VISIBLE);
        } else {
            raffleWinnerView.setVisibility(View.GONE);
        }
        raffleDrawButton.setVisibility(ended && winner == null && !entries.isEmpty() ? View.VISIBLE : View.GONE);
        raffleRedrawButton.setVisibility(ended && winner != null ? View.VISIBLE : View.GONE);
        raffleEmailButton.setVisibility(ended && winner != null ? View.VISIBLE : View.GONE);

        raffleEntries.removeAllViews();
        if (entries.isEmpty()) {
            TextView empty = new TextView(ctx());
            empty.setText(R.string.raffle_no_entries);
            empty.setTextColor(0xCCFFFFFF);
            int pad = dp(8);
            empty.setPadding(pad, pad, pad, pad);
            raffleEntries.addView(empty);
        } else {
            for (RaffleEntryEntity entry : entries) {
                boolean isWinner = raffle.winnerId != null && raffle.winnerId == entry.id;
                raffleEntries.addView(entryRowFactory.createEntryRow(entry, isWinner));
            }
        }
        panelNav.show(PanelNavigator.PANEL_RAFFLE);
    }

    private void confirmDeleteRaffle() {
        if (currentRaffleId <= 0) {
            return;
        }
        new AlertDialog.Builder(ctx())
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
        executor.execute(() -> {
            RaffleRepository.get(ctx()).deleteRaffleCompletely(id);
            root.post(this::openRaffle);
        });
    }

    private void confirmDeleteEntry(RaffleEntryEntity entry) {
        new AlertDialog.Builder(ctx())
                .setTitle(R.string.raffle_entry_delete_title)
                .setMessage(ctx().getString(R.string.raffle_entry_delete_message, entry.name))
                .setNegativeButton(R.string.admin_back, null)
                .setPositiveButton(R.string.raffle_entry_delete, (d, w) -> deleteEntry(entry))
                .show();
    }

    private void deleteEntry(RaffleEntryEntity entry) {
        executor.execute(() -> {
            RaffleRepository.get(ctx()).deleteEntry(entry);
            root.post(this::openRaffle);
        });
    }

    private void finishCurrentRaffle() {
        long id = currentRaffleId;
        if (id <= 0) {
            return;
        }
        executor.execute(() -> {
            RaffleRepository.get(ctx()).finishRaffle(id);
            root.post(this::openRaffle);
        });
    }

    private void closeCurrentRaffle() {
        long id = currentRaffleId;
        if (id <= 0) {
            return;
        }
        executor.execute(() -> {
            RaffleRepository.get(ctx()).endRaffle(id);
            root.post(this::openRaffle);
        });
    }

    private void drawWinner() {
        long id = currentRaffleId;
        if (id <= 0) {
            return;
        }
        executor.execute(() -> {
            RaffleEntryEntity winner = RaffleRepository.get(ctx()).pickWinner(id);
            root.post(() -> celebrateWinner(winner));
        });
    }

    private void redrawWinner() {
        long id = currentRaffleId;
        if (id <= 0) {
            return;
        }
        executor.execute(() -> {
            RaffleEntryEntity winner = RaffleRepository.get(ctx()).pickReplacementWinner(id);
            root.post(() -> {
                if (winner == null) {
                    Toast.makeText(ctx(), R.string.raffle_no_replacement,
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
        onClose.run();
        QiContext qc = RobotContext.get();
        if (qc != null) {
            WinnerController.get().celebrate(qc, winner.name);
        }
    }

    private void sendWinnerEmail() {
        emailSender.send(currentWinner, currentRaffleTitle);
    }
}
