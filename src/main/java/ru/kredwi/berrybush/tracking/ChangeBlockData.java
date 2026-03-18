package ru.kredwi.berrybush.tracking;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ru.kredwi.berrybush.BerryBushPlugin;
import ru.kredwi.berrybush.Cooldown;
import ru.kredwi.berrybush.bush.BushFinishAction;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class ChangeBlockData extends BukkitRunnable {


    private static final String LAST_ACTION_KEY = "bush.final-action";
    private final BerryBushPlugin plugin = JavaPlugin.getPlugin(BerryBushPlugin.class);

    private final Logger logger;
    private final ButtonPressed buttonPressed;
    private final UUID uuid;
    private final Cooldown<Vector> blockCooldowns;

    @Override
    public void run() {
        Optional<TrackingSession> session = buttonPressed.getSession(uuid);
        session.ifPresent(this::handle);
    }

    private void handle(TrackingSession ts) {
        if (ts.isNotExpired())
            return;

        Optional<Player> player = Optional.ofNullable(Bukkit.getPlayer(ts.getPlayerId()));
        if (!player.isPresent())
            return;
        buttonPressed.stopTracking(player.get());

        String lastAction = plugin.getConfig().getString(LAST_ACTION_KEY);

        getAction(lastAction)
                .ifPresent(method -> method.run(ts, player.get()));

        blockCooldowns.newCooldown(ts.getBlock().getLocation().toVector());
    }

    private Optional<BushFinishAction> getAction(String actionName) {
        try {
            return Optional.of(BushFinishAction.valueOf(actionName));
        } catch (IllegalArgumentException e) {
            logger.severe(e.getMessage());
            return Optional.empty();
        }
    }
}
