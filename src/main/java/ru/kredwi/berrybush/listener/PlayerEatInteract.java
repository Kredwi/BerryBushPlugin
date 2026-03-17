package ru.kredwi.berrybush.listener;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import ru.kredwi.berrybush.Cooldown;
import ru.kredwi.berrybush.async.PlayerRunnable;
import ru.kredwi.berrybush.tracking.ButtonPressed;
import ru.kredwi.berrybush.tracking.ChangeBlockData;
import ru.kredwi.berrybush.tracking.TrackingSession;

import java.util.*;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class PlayerEatInteract implements Listener {

    private final Logger logger;
    private final Plugin plugin;
    private final ButtonPressed buttonPressed;
    private final PlayerRunnable playerRunnable;
    private final Cooldown<Vector> blockCooldowns = new Cooldown<>();

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        val player = e.getPlayer();

        if (!isSupportBush(e.getClickedBlock())) return;
        e.setCancelled(true); // disable next execution for this block
        if (!e.hasItem() || e.getItem().getType() != Material.SHEARS) return;

        logger.fine("Все проверки прошли");

        Optional<TrackingSession> session = buttonPressed.getSession(player.getUniqueId());

        // update or reset click session
        if (session.isPresent()) {
            if (!session.get().isNotExpired()) {
                stopTracking(player);
                return;
            }

            if (!locationEquals(session.get().getBlock().getLocation(), e.getClickedBlock().getLocation())) {
                stopTracking(player);
                return;
            }

            buttonPressed.update(player.getUniqueId());
            return;
        }
        if (!(e.getClickedBlock().getBlockData() instanceof Ageable)) {
            logger.info("Блок не имеет возможности роста");
            return;
        } else {
            Ageable ageable = (Ageable) e.getClickedBlock().getBlockData();
            if (ageable.getAge() != ageable.getMaximumAge()) {
                org.bukkit.block.data.BlockData bd = e.getClickedBlock().getType().createBlockData();

                ((Ageable) bd).setAge(((Ageable) bd).getMaximumAge());
                e.getClickedBlock().setBlockData(bd);
                logger.info("Maximum AGE");
                return;
            }
        }
        startTracking(player.getUniqueId(), e.getClickedBlock());
    }

    private void stopTracking(Player player) {
        logger.info("Зажатие прервано");
        buttonPressed.stopTracking(player);
        Optional.ofNullable(playerRunnable.remove(player.getUniqueId()))
                .ifPresent(BukkitRunnable::cancel);
    }

    private void startTracking(UUID pid, Block block) {
        if (blockCooldowns.isOnCooldown(block.getLocation().toVector(), 20 * 1000)) {
            Optional.ofNullable(Bukkit.getPlayer(pid))
                    .ifPresent(p -> p.sendMessage("Подождите N-время, чтобы снова начать собирать урожай"));
            return;
        }
        logger.info("Запуск отслеживания: " + pid.toString());
        buttonPressed.startTracking(pid, block);

        ChangeBlockData br = new ChangeBlockData(logger, buttonPressed, pid, blockCooldowns);
        br.runTaskLater(plugin, 5 * 20);
        this.playerRunnable.put(pid, br);
    }

    private boolean locationEquals(@Nullable Location a, @Nullable Location b) {
        boolean isNull = a == null || b == null;
        return isNull ||
                a.getX() == b.getX() &&
                a.getY() == b.getY() &&
                a.getZ() == b.getZ();
    }

    private boolean isSupportBush(@Nullable Block material) {
        if (material == null)
            return false;
        return material.getType() == Material.SWEET_BERRY_BUSH;
    }
}
