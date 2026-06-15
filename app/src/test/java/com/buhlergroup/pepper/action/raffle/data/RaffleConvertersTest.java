package com.buhlergroup.pepper.action.raffle.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class RaffleConvertersTest {

    @Test
    public void roundTripsEveryStatus() {
        for (RaffleStatus status : RaffleStatus.values()) {
            assertEquals(status, RaffleConverters.toStatus(RaffleConverters.fromStatus(status)));
        }
    }

    @Test
    public void handlesNull() {
        assertNull(RaffleConverters.fromStatus(null));
        assertNull(RaffleConverters.toStatus(null));
    }

    @Test
    public void mapsKnownName() {
        assertEquals("ACTIVE", RaffleConverters.fromStatus(RaffleStatus.ACTIVE));
        assertEquals(RaffleStatus.ACTIVE, RaffleConverters.toStatus("ACTIVE"));
    }
}
