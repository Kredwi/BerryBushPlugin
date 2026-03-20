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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.kredwi.berrybush.BerryBushPlugin;
import ru.kredwi.berrybush.Cooldown;
import ru.kredwi.berrybush.async.bush.ChangeBlockTask;
import ru.kredwi.berrybush.async.bush.SlowBreakTask;
import ru.kredwi.berrybush.depend.Depend;
import ru.kredwi.berrybush.depend.WorldGuard;
import ru.kredwi.berrybush.tracking.TrackingSession;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class PlayerBushInteract implements Listener {

    private static final String PERM_QUICK_MATURITY = "bbush.fast";
    private static final String PERM_ACCESS = "bbush.access";

    private static final String BREAK_TIME = "bush.break-time";
    private static final String ANIMATE_VISIBLE = "bush.visible";

    private static final String MSG_NO_PERMISSION = "messages.no-permission";
    private static final String MSG_WRONG_ITEM = "messages.wrong-item";
    private static final String MSG_CANCELLED = "messages.cancelled";
    private static final String MSG_COOLDOWN = "messages.cooldown";
    private final static String MSG_NON_GROWING = "messages.non-grown";

    private final BerryBushPlugin plugin = JavaPlugin.getPlugin(BerryBushPlugin.class);

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        val player = e.getPlayer();

        if (!isSupportBush(e.getClickedBlock())) return;
        e.setCancelled(true); // disable next execution for this block
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (!player.hasPermission(PERM_ACCESS)) {
            player.sendMessage(plugin.getMessageOrKey(MSG_NO_PERMISSION));
            return;
        }
        if (!e.hasItem() || e.getItem().getType() != Material.SHEARS) {
            player.sendMessage(plugin.getMessageOrKey(MSG_WRONG_ITEM));
            return;
        }

        Optional<Depend> wg = plugin.getDependFactory().getDepend(WorldGuard.class);

        if (wg.isPresent()) {
            if (!((WorldGuard) wg.get()).testBlockInteract(player, e.getClickedBlock()))
                return; // auto message from worldguard
        }

        Optional<TrackingSession> session = plugin.getButtonPressed().getSession(player.getUniqueId());
        // update or reset click session
        if (session.isPresent()) {
            update(session.get(), player, e.getClickedBlock());
            return;
        }

        if (!(e.getClickedBlock().getBlockData() instanceof Ageable)) {
            plugin.getLog().debug(String.format("Block with name %s is not ageable", e.getClickedBlock().getType().name()));
            return;
        } else {
            Ageable ageable = (Ageable) e.getClickedBlock().getBlockData();
            if (ageable.getAge() != ageable.getMaximumAge()) {
                if (player.hasPermission(PERM_QUICK_MATURITY)) {
                    quickMaturity(e.getClickedBlock());
                } else {
                    player.sendMessage(plugin.getMessageOrKey(MSG_NON_GROWING));
                    return;
                }
            }
        }

        startTracking(player.getUniqueId(), e.getClickedBlock());
    }

    private void update(TrackingSession session, Player player, Block clickedBlock) {
        if (session.isExpired()) {
            stopTracking(player);
            return;
        }

        if (!locationEquals(session.getBlock().getLocation(), clickedBlock.getLocation())) {
            stopTracking(player);
            return;
        }

        plugin.getButtonPressed().update(player.getUniqueId());
    }

    private void quickMaturity(@NotNull Block block) {
        if (!(block.getBlockData() instanceof Ageable)) return;
        Ageable ageable = (Ageable) block.getBlockData();
        if (ageable.getAge() != ageable.getMaximumAge()) {
            org.bukkit.block.data.BlockData bd = block.getType().createBlockData();

            ((Ageable) bd).setAge(((Ageable) bd).getMaximumAge());
            block.setBlockData(bd);
        }
    }

    private void stopTracking(Player player) {
        plugin.getButtonPressed().stopTracking(player);
        Optional.ofNullable(plugin.getTaskRegistry().removeIfPresent(player.getUniqueId()))
                .ifPresent(BukkitRunnable::cancel);
        player.sendMessage(plugin.getMessageOrKey(MSG_CANCELLED));
    }

    private void startTracking(UUID pid, Block block) {
        Vector blockVector = block.getLocation().toVector();
        Cooldown<Vector> blockCooldowns = plugin.getCooldown();
        if (blockCooldowns.isOnCooldown(blockVector)) {
            int remainingTime = Math.toIntExact((System.currentTimeMillis() - blockCooldowns.getWriteTime(blockVector)) / 1000);
            String mes = MessageFormat.format(plugin.getMessageOrKey(MSG_COOLDOWN),
                    blockCooldowns.getCooldownTime() - remainingTime);
            Optional.ofNullable(Bukkit.getPlayer(pid))
                    .ifPresent(p -> p.sendMessage(mes));
            return;
        }
        plugin.getLog().debug("Start tracking for player: " + pid.toString());

        plugin.getButtonPressed().startTracking(pid, block);

        BukkitRunnable br;

        if (plugin.getConfig().getBoolean(ANIMATE_VISIBLE)) {
            br = new SlowBreakTask(pid);
            int timer = Math.max((getBreakTime() / ((Ageable) block.getBlockData()).getMaximumAge()), 0);
            br.runTaskTimer(plugin, timer, timer);
        } else {
            br = new ChangeBlockTask(pid);
            br.runTaskLater(plugin, getBreakTime());
        }
        plugin.getTaskRegistry().addRunnable(pid, br);
    }

    private boolean locationEquals(@Nullable Location a, @Nullable Location b) {
        boolean isNull = a == null || b == null;
        return isNull ||
                a.getX() == b.getX() &&
                        a.getY() == b.getY() &&
                        a.getZ() == b.getZ();
    }

    private int getBreakTime() {
        // 20 tick
        return plugin.getConfig().getInt(BREAK_TIME) * 20;
    }

    private boolean isSupportBush(@Nullable Block material) {
        if (material == null)
            return false;
        return material.getType() == Material.SWEET_BERRY_BUSH;
    }
}
