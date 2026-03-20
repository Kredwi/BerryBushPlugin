package ru.kredwi.berrybush.async.bush;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.kredwi.berrybush.BerryBushPlugin;
import ru.kredwi.berrybush.tracking.TrackingSession;

import java.util.Optional;
import java.util.UUID;

public class ChangeBlockTask extends BushTask {

    private final BerryBushPlugin plugin;

    private final UUID uuid;

    public ChangeBlockTask(UUID uuid) {
        super(JavaPlugin.getPlugin(BerryBushPlugin.class));
        this.uuid = uuid;
        this.plugin = super.getPlugin();
    }

    @Override
    public void run() {
        Optional<TrackingSession> session = plugin.getButtonPressed().getSession(uuid);
        session.ifPresent(this::handle);
    }

    private void handle(TrackingSession ts) {
        if (ts.isExpired()) {
            // hey bro, TrackingSession already is expired, maybe you remove tracker and gc clear me
            plugin.getButtonPressed().removeTracker(ts.getPlayerId());
            return;
        }

        Optional<Player> player = Optional.ofNullable(Bukkit.getPlayer(ts.getPlayerId()));
        if (!player.isPresent())
            return;
        plugin.getButtonPressed().stopTracking(player.get());

        last(ts, player.get());

        plugin.getCooldown().newCooldown(ts.getBlock().getLocation().toVector());
    }
}
