package ru.kredwi.berrybush.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.kredwi.berrybush.async.PlayerRunnable;

@RequiredArgsConstructor
public class PlayerQuit implements Listener {

    private final PlayerRunnable playerRunnable;

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        playerRunnable.removeIfPresent(e.getPlayer().getUniqueId());
    }

}
