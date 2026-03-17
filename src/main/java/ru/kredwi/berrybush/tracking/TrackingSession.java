package ru.kredwi.berrybush.tracking;

import lombok.*;
import org.bukkit.block.Block;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackingSession {
    private static final int TIME_CLICK_EVENT = 250;

    private UUID playerId;
    private Block block;
    private long lastClickTime;
    private long firstClick;

    public boolean isNotExpired() {
        val timeDifference = System.currentTimeMillis() - lastClickTime;
        return timeDifference < TIME_CLICK_EVENT;
    }
}
