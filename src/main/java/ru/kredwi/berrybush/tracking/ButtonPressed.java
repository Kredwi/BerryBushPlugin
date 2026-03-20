package ru.kredwi.berrybush.tracking;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.kredwi.berrybush.BerryBushPlugin;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.apache.logging.log4j.util.Strings.EMPTY;

public class ButtonPressed {

    private static final String MSG_HOLDING_KEY = "messages.holding";
    private static final String BREAK_TIME_KEY = "bush.break-time";

    private final BerryBushPlugin plugin = JavaPlugin.getPlugin(BerryBushPlugin.class);
    private final Map<UUID, TrackingSession> lastTimeClick = new HashMap<>();

    public void startTracking(UUID targetBtn, Block targetBlock) {
        long time = System.currentTimeMillis();
        TrackingSession session = new TrackingSession(targetBtn, targetBlock, time, time);
        lastTimeClick.putIfAbsent(targetBtn, session);
    }

    public void removeTracker(UUID playerId) {
        lastTimeClick.remove(playerId);
    }

    public void stopTracking(Player player) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(EMPTY));

        lastTimeClick.remove(player.getUniqueId());
    }

    public Optional<TrackingSession> getSession(UUID targetBtn) {
        return Optional.ofNullable(lastTimeClick.get(targetBtn));
    }

    public void update(UUID targetBtn) {
        Optional<Player> player = Optional.ofNullable(Bukkit.getPlayer(targetBtn));
        lastTimeClick.computeIfPresent(targetBtn, (_n, ts) -> {
            ts.setLastClickTime(System.currentTimeMillis());
            return ts;
        });
        player.ifPresent(value -> value.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(MessageFormat
                        .format(plugin.getMessageOrKey(MSG_HOLDING_KEY), (getNeedsMs(value.getUniqueId()) / 1000) + 1))));
    }

    public long getNeedsMs(UUID uuid) {
        Optional<TrackingSession> session = Optional.ofNullable(lastTimeClick.get(uuid));
        long remaining = session.map(ts -> ts.getLastClickTime() - ts.getFirstClick()).orElse(0L);
        return (getBreakTime() * 1000) - remaining;
    }

    private long getBreakTime() {
        return plugin.getConfig().getLong(BREAK_TIME_KEY);
    }
}
