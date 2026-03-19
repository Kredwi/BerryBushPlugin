package ru.kredwi.berrybush.tracking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.kredwi.berrybush.BerryBushPlugin;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackingSession {
    private final static Plugin plugin = JavaPlugin.getPlugin(BerryBushPlugin.class);

    private UUID playerId;
    private Block block;
    private long lastClickTime;
    private long firstClick;

    public boolean isNotExpired() {
        val timeDifference = System.currentTimeMillis() - lastClickTime;
        return timeDifference >= getTimeClickEvent();
    }

    private int getTimeClickEvent() {
        return plugin.getConfig()
                .getInt("bush.max-click-interval", 250);
    }
}
