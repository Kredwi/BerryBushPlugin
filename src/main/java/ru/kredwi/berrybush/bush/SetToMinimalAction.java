package ru.kredwi.berrybush.bush;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.kredwi.berrybush.BerryBushPlugin;
import ru.kredwi.berrybush.tracking.TrackingSession;

public class SetToMinimalAction implements BushAction {
    private final Plugin plugin = JavaPlugin.getPlugin(BerryBushPlugin.class);

    @Override
    public void accept(TrackingSession trackingSession, Player player) {
        BlockData data = trackingSession.getBlock().getBlockData();
        Location loc = trackingSession.getBlock().getLocation();
        if (!(data instanceof Ageable)) return;
        if (loc.getWorld() != null) {
            getDrop(trackingSession.getBlock()).forEach(drop -> {
                addAttribute(drop, plugin);
                loc.getWorld().dropItemNaturally(loc, drop);
            });
        }
        trackingSession.getBlock().setBlockData(createNewBlockData(trackingSession.getBlock().getType()));
    }

    private BlockData createNewBlockData(Material mat) {
        BlockData blockData = mat.createBlockData();
        Ageable ageable = (Ageable) blockData;
        ageable.setAge(0);
        return blockData;
    }
}