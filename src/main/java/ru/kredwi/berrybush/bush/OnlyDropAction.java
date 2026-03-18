package ru.kredwi.berrybush.bush;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.kredwi.berrybush.tracking.TrackingSession;

import java.util.Objects;

public class OnlyDropAction implements BushAction {
    @Override
    public void accept(TrackingSession ts, Player player) {
        Location blockLoc = ts.getBlock().getLocation();
        ts.getBlock().getDrops().forEach(s -> Objects.requireNonNull(blockLoc.getWorld())
                .dropItemNaturally(blockLoc, s));
    }
}
