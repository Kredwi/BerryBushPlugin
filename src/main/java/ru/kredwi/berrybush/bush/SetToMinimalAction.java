package ru.kredwi.berrybush.bush;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.kredwi.berrybush.tracking.TrackingSession;

import java.util.Collection;

public class SetToMinimalAction implements BushAction {
    @Override
    public void accept(TrackingSession trackingSession, Player player) {
        BlockData data = trackingSession.getBlock().getBlockData();
        Location loc = trackingSession.getBlock().getLocation();
        if (!(data instanceof Ageable)) return;
        if (loc.getWorld() != null) {
            Collection<ItemStack> drops = trackingSession.getBlock().getDrops();
            drops.forEach(drop -> loc.getWorld().dropItemNaturally(loc, drop));
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