package ru.kredwi.berrybush.bush;

import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.kredwi.berrybush.BerryBushPlugin;
import ru.kredwi.berrybush.tracking.TrackingSession;

import java.util.Objects;

public class OnlyDropAction implements BushAction {
    private final BerryBushPlugin plugin = JavaPlugin.getPlugin(BerryBushPlugin.class);

    @Override
    public void accept(TrackingSession ts, Player player) {
        val blockLoc = ts.getBlock().getLocation();

        getDrop(ts.getBlock()).forEach(s -> {
            addAttribute(s, plugin);

            Objects.requireNonNull(blockLoc.getWorld())
                    .dropItemNaturally(blockLoc, s);
        });
    }
}
