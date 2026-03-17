package ru.kredwi.berrybush.tracking;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ru.kredwi.berrybush.Cooldown;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class ChangeBlockData extends BukkitRunnable {

    private final Logger logger;
    private final ButtonPressed buttonPressed;
    private final UUID uuid;
    private final Cooldown<Vector> blockCooldowns;

    @Override
    public void run() {
        logger.info("N-время прошло, запуск runnable");
        Optional<TrackingSession> session = buttonPressed.getSession(uuid);
        session.ifPresent(this::handle);
    }

    private void handle(TrackingSession ts) {
        if (!ts.isNotExpired()) {
            logger.info("Нажатие вышло из строя");
            return;
        }

        Optional<Player> player = Optional.ofNullable(Bukkit.getPlayer(ts.getPlayerId()));
        if (!player.isPresent())
            return;
        buttonPressed.stopTracking(player.get());

        Location blockLoc = ts.getBlock().getLocation();
        ts.getBlock().getDrops().forEach(s -> {
            Objects.requireNonNull(blockLoc.getWorld())
                    .dropItemNaturally(blockLoc, s);
        });

        blockCooldowns.newCooldown(ts.getBlock().getLocation().toVector());
    }
}
