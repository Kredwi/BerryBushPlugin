package ru.kredwi.berrybush.async.bush;

import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.kredwi.berrybush.BerryBushPlugin;
import ru.kredwi.berrybush.tracking.TrackingSession;

import java.util.Optional;
import java.util.UUID;

public class SlowBreakTask extends BushTask {
    private final BerryBushPlugin plugin;

    @NotNull
    private final UUID pid;

    public SlowBreakTask(@NotNull UUID uuid) {
        super(JavaPlugin.getPlugin(BerryBushPlugin.class));
        this.pid =uuid;
        this.plugin = super.getPlugin();
    }

    @Override
    public void run() {
        Optional<TrackingSession> session = plugin.getButtonPressed().getSession(pid);

        if (!session.isPresent()) {
            stop(null);
            plugin.getLog().debug("Session is not present");
            return;
        }

        TrackingSession ts = session.get();

        if (ts.isExpired()) {
            plugin.getLog().debug("TS is expired");
            plugin.getButtonPressed().removeTracker(ts.getPlayerId());
            cancel();
            return;
        }

        if (!(ts.getBlock().getBlockData() instanceof Ageable)) {
            plugin.getLog().debug("Block dont instanceof Ageable");
            stop(ts);

            return;
        }

        Ageable ageable = (Ageable) ts.getBlock().getBlockData();
        if (ageable.getAge() == 0) {
            stop(ts);
            Optional<Player> player = Optional.ofNullable(Bukkit.getPlayer(pid));
            player.ifPresent(p -> last(ts, p));

            return;
        }
        BlockData blockData = ts.getBlock().getType().createBlockData();
        Ageable newAgeable = (Ageable) blockData;
        newAgeable.setAge(ageable.getAge() - 1);

        ts.getBlock().setBlockData(newAgeable);
        playSound(ts.getBlock().getLocation());
    }

    private void playSound(Location loc) {
        Sound sound = getSound(BREAK_SOUND);
        if (sound != null && loc.getWorld() != null)
            loc.getWorld()
                    .playSound(loc, sound,1.0f, 1.0f);
    }

    private void stop(TrackingSession ts) {
        cancel();
        if (ts != null)
            plugin.getCooldown().newCooldown(ts.getBlock().getLocation().toVector());
        plugin.getTaskRegistry().removeIfPresent(pid);
        Optional<Player> player = Optional.ofNullable(Bukkit.getPlayer(pid));
        if (player.isPresent()) {
            plugin.getButtonPressed().stopTracking(player.get());
            return;
        }
        plugin.getButtonPressed().removeTracker(pid);

    }
}
