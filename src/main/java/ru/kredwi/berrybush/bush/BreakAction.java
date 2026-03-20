package ru.kredwi.berrybush.bush;

import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import ru.kredwi.berrybush.tracking.TrackingSession;

public class BreakAction implements BushAction {

    @Override
    public void accept(TrackingSession trackingSession, Player player) {
        BlockData block = trackingSession.getBlock().getBlockData();
        if (!(block instanceof Ageable)) return;
        Ageable ageable = (Ageable) block;
        ageable.setAge(ageable.getMaximumAge());
        trackingSession.getBlock().setBlockData(ageable);
        trackingSession.getBlock().breakNaturally(); // no set custom data
    }
}
