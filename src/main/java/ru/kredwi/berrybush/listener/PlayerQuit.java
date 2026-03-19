package ru.kredwi.berrybush.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.kredwi.berrybush.BerryBushPlugin;

@RequiredArgsConstructor
public class PlayerQuit implements Listener {

    private final BerryBushPlugin berryBushPlugin;

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (berryBushPlugin.getTaskRegistry().removeIfPresent(e.getPlayer().getUniqueId()) != null)
            berryBushPlugin.getLog().debug("Player with name " + e.getPlayer().getName() + " is quit");
    }

}
