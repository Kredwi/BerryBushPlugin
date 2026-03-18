package ru.kredwi.berrybush.listener;

import com.sk89q.worldguard.bukkit.ProtectionQuery;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.kredwi.berrybush.BerryBushPlugin;
import ru.kredwi.berrybush.Cooldown;
import ru.kredwi.berrybush.async.PlayerRunnable;
import ru.kredwi.berrybush.tracking.ButtonPressed;
import ru.kredwi.berrybush.tracking.ChangeBlockData;
import ru.kredwi.berrybush.tracking.TrackingSession;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class PlayerEatInteract implements Listener {

    private static final String PERM_QUICK_MATURITY = "bbush.fast";
    private static final String PERM_ACCESS = "bbush.access";

    private final BerryBushPlugin plugin = JavaPlugin.getPlugin(BerryBushPlugin.class);

    private final ProtectionQuery protectionQuery = new ProtectionQuery();

    private final Logger logger;
    private final ButtonPressed buttonPressed;
    private final PlayerRunnable playerRunnable;
    private final Cooldown<Vector> blockCooldowns;

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        val player = e.getPlayer();

        if (!isSupportBush(e.getClickedBlock())) return;
        e.setCancelled(true); // disable next execution for this block
        if (!player.hasPermission(PERM_ACCESS)) {
            player.sendMessage(plugin.getMessageOrKey("messages.no-permission"));
            return;
        }
        if (!e.hasItem() || e.getItem().getType() != Material.SHEARS) {
            player.sendMessage(plugin.getMessageOrKey("messages.wrong-item"));
            return;
        }

        if (!protectionQuery.testBlockInteract(player, e.getClickedBlock())) {
            player.sendMessage(plugin.getMessageOrKey("messages.worldguard-blocked"));
            return;
        }

        logger.fine("All checkings is completed");

        Optional<TrackingSession> session = buttonPressed.getSession(player.getUniqueId());

        // update or reset click session
        if (session.isPresent()) {
            update(session.get(), player, e.getClickedBlock());
            return;
        }

        if (!(e.getClickedBlock().getBlockData() instanceof Ageable)) {
            logger.info("Блок не имеет возможности роста");
            return;
        } else {
            if (player.hasPermission(PERM_QUICK_MATURITY)) {
                quickMaturity(e.getClickedBlock());
            }
        }
        startTracking(player.getUniqueId(), e.getClickedBlock());
    }

    private void update(TrackingSession session, Player player, Block clickedBlock) {
        if (session.isNotExpired()) {
            stopTracking(player);
            return;
        }

        if (!locationEquals(session.getBlock().getLocation(), clickedBlock.getLocation())) {
            stopTracking(player);
            return;
        }

        buttonPressed.update(player.getUniqueId());
    }

    private void quickMaturity(@NotNull Block block) {
        if (!(block instanceof Ageable)) return;
        Ageable ageable = (Ageable) block.getBlockData();
        if (ageable.getAge() != ageable.getMaximumAge()) {
            org.bukkit.block.data.BlockData bd = block.getType().createBlockData();

            ((Ageable) bd).setAge(((Ageable) bd).getMaximumAge());
            block.setBlockData(bd);
        }
    }

    private void stopTracking(Player player) {
        buttonPressed.stopTracking(player);
        Optional.ofNullable(playerRunnable.removeIfPresent(player.getUniqueId()))
                .ifPresent(BukkitRunnable::cancel);
        player.sendMessage(plugin.getMessageOrKey("messages.cancelled"));
    }

    private void startTracking(UUID pid, Block block) {
        Vector blockVector = block.getLocation().toVector();
        if (blockCooldowns.isOnCooldown(blockVector)) {
            String mes = MessageFormat.format(plugin.getMessageOrKey("messages.cooldown"),
                    blockCooldowns.getWriteTime(blockVector));
            Optional.ofNullable(Bukkit.getPlayer(pid))
                    .ifPresent(p -> p.sendMessage(mes));
            return;
        }
        logger.fine("Start tracking for player: " + pid.toString());
        buttonPressed.startTracking(pid, block);

        ChangeBlockData br = new ChangeBlockData(logger, buttonPressed, pid, blockCooldowns);
        br.runTaskLater(plugin, 5 * 20);
        this.playerRunnable.addRunnable(pid, br);
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
