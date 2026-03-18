package ru.kredwi.berrybush.bush;

import org.bukkit.entity.Player;
import ru.kredwi.berrybush.tracking.TrackingSession;

public class BreakAction implements BushAction {

    @Override
    public void accept(TrackingSession trackingSession, Player player) {
        trackingSession.getBlock().breakNaturally();
    }
}
