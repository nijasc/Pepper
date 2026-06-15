package com.buhlergroup.pepper.action.navigation;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.action.navigation.data.WaypointEntity;
import com.buhlergroup.pepper.lang.SpeechManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class GuideAction extends Action {

    private static final long WAYPOINT_TIMEOUT_MS = 8000;

    private static final Set<String> FILLERS = new HashSet<>(Arrays.asList(
            "bring", "bringe", "bringst", "mich", "mir", "uns", "zum", "zur", "zu", "den", "dem",
            "die", "der", "das", "wo", "finde", "findest", "ich", "ist", "sich", "befindet",
            "bitte", "mal", "kannst", "du", "einen", "eine", "ein", "nach", "hin", "fahr", "fahre",
            "fuhr", "geh", "gehe", "gehen", "fuhre", "fuhren", "lotse", "fuehre", "fuhrst",
            "take", "me", "us", "to", "the", "a", "an", "where", "is", "find", "please", "go",
            "lead", "guide", "show", "of"));

    @Override
    public void execute(QiContext context, String input) {
        NavigationManager nav = NavigationManager.get();
        if (!nav.isLocalized() || nav.getActiveScan() == null) {
            SpeechManager.getInstance().systemSay(context,
                    "Ich habe gerade keine Orientierung im Raum. Ein Betreuer muss zuerst einen "
                            + "Raum-Scan aktivieren, dann kann ich dich herumführen.");
            return;
        }
        List<WaypointEntity> waypoints = fetchWaypoints(nav);
        if (waypoints == null || waypoints.isEmpty()) {
            SpeechManager.getInstance().systemSay(context,
                    "Es sind noch keine Wegpunkte gespeichert, zu denen ich dich bringen könnte.");
            return;
        }
        WaypointEntity target = match(input, waypoints);
        if (target == null) {
            SpeechManager.getInstance().systemSay(context,
                    "Dieses Ziel kenne ich leider nicht. " + availableList(waypoints));
            return;
        }
        SpeechManager.getInstance().systemSay(context,
                "Folge mir, ich bringe dich zu " + target.name + ".");

        NavigationManager.GuideOutcome outcome = drive(nav, target);
        announceOutcome(context, target, outcome);
    }

    private NavigationManager.GuideOutcome drive(NavigationManager nav, WaypointEntity target) {
        AtomicReference<NavigationManager.GuideOutcome> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        nav.guideToWaypoint(target, new NavigationManager.Callback<NavigationManager.GuideOutcome>() {
            @Override
            public void onResult(NavigationManager.GuideOutcome value) {
                ref.set(value);
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return ref.get();
    }

    private void announceOutcome(QiContext context, WaypointEntity target,
                                NavigationManager.GuideOutcome outcome) {
        if (outcome == NavigationManager.GuideOutcome.ARRIVED) {
            SpeechManager.getInstance().systemSay(context,
                    "Wir sind da! Hier ist " + target.name + ". Kann ich sonst noch helfen?");
        } else if (outcome == null) {
            SpeechManager.getInstance().systemSay(context,
                    "Ich kann dich gerade nicht dorthin bringen.");
        }
    }

    private WaypointEntity match(String input, List<WaypointEntity> waypoints) {
        String norm = normalize(input);
        if (norm.isEmpty()) {
            return null;
        }
        Set<String> inputTokens = tokens(norm);
        WaypointEntity best = null;
        int bestScore = 0;
        for (WaypointEntity wp : waypoints) {
            String wpNorm = normalize(wp.name);
            if (wpNorm.isEmpty()) {
                continue;
            }
            int score;
            if (norm.contains(wpNorm)) {
                score = 1000 + wpNorm.length();
            } else {
                score = 0;
                for (String token : tokens(wpNorm)) {
                    if (token.length() >= 3 && inputTokens.contains(token)) {
                        score += token.length();
                    }
                }
            }
            if (score > bestScore) {
                bestScore = score;
                best = wp;
            }
        }
        return bestScore > 0 ? best : null;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String lower = value.toLowerCase(Locale.ROOT)
                .replace("ä", "a").replace("ö", "o").replace("ü", "u").replace("ß", "ss");
        lower = lower.replaceAll("[^a-z0-9 ]", " ");
        StringBuilder sb = new StringBuilder();
        for (String token : lower.split("\\s+")) {
            if (!token.isEmpty() && !FILLERS.contains(token)) {
                sb.append(token).append(' ');
            }
        }
        return sb.toString().trim();
    }

    private Set<String> tokens(String normalized) {
        Set<String> result = new HashSet<>();
        for (String token : normalized.split("\\s+")) {
            if (!token.isEmpty()) {
                result.add(token);
            }
        }
        return result;
    }

    private List<WaypointEntity> fetchWaypoints(NavigationManager nav) {
        AtomicReference<List<WaypointEntity>> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        nav.listWaypoints(new NavigationManager.Callback<List<WaypointEntity>>() {
            @Override
            public void onResult(List<WaypointEntity> value) {
                ref.set(value);
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                latch.countDown();
            }
        });
        try {
            latch.await(WAYPOINT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return ref.get();
    }

    private String availableList(List<WaypointEntity> waypoints) {
        StringBuilder sb = new StringBuilder("Ich kann dich zu diesen Orten bringen: ");
        for (int i = 0; i < waypoints.size(); i++) {
            if (i > 0) {
                sb.append(i == waypoints.size() - 1 ? " und " : ", ");
            }
            sb.append(waypoints.get(i).name);
        }
        sb.append(". Sag einfach: bring mich zu einem davon.");
        return sb.toString();
    }

    @Override
    public String getDescription() {
        return "Guides a visitor to a named location and physically drives there. Use when the "
                + "user asks to be taken or guided somewhere, e.g. 'bring mich zum Fotostand', "
                + "'wo finde ich den Ausgang', 'führe mich zu ...', 'take me to ...', "
                + "'where is the ...'. The destination is matched against saved navigation waypoints.";
    }
}
