package ru.kredwi.berrybush.tracking;

import lombok.val;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ButtonPressed {

    private final Map<UUID, TrackingSession> lastTimeClick = new HashMap<>();

    public void startTracking(UUID targetBtn, Block targetBlock) {
        long time = System.currentTimeMillis();
        TrackingSession session = new TrackingSession(targetBtn, targetBlock, time, time);
        lastTimeClick.putIfAbsent(targetBtn, session);
    }

    public void stopTracking(Player player) {

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(""));

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
                TextComponent.fromLegacyText("Держите еще: " + (getNeedsClick(value.getUniqueId()) / 1000) + "с")));
    }

    public long getNeedsClick(UUID uuid) {
        Optional<TrackingSession> session = Optional.ofNullable(lastTimeClick.get(uuid));
        return session.map(ts -> ts.getLastClickTime() - ts.getFirstClick()).orElse(0L);
    }
}
