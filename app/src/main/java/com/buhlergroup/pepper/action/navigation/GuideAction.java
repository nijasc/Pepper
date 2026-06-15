package com.buhlergroup.pepper.action.navigation;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.action.navigation.data.WaypointEntity;
import com.buhlergroup.pepper.lang.SpeechManager;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class GuideAction extends Action {

    private static final long WAYPOINT_TIMEOUT_MS = 8000;

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
        SpeechManager.getInstance().systemSay(context, availableList(waypoints));
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
