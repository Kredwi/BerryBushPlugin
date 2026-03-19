package ru.kredwi.berrybush.tracking;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.kredwi.berrybush.BerryBushPlugin;
import ru.kredwi.berrybush.bush.BushFinishAction;
import ru.kredwi.berrybush.depend.Depend;
import ru.kredwi.berrybush.depend.Vault;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class ChangeBlockData extends BukkitRunnable {


    private static final String LAST_ACTION_KEY = "bush.final-action";
    private final BerryBushPlugin plugin = JavaPlugin.getPlugin(BerryBushPlugin.class);

    private final UUID uuid;

    @Override
    public void run() {
        Optional<TrackingSession> session = plugin.getButtonPressed().getSession(uuid);
        session.ifPresent(this::handle);
    }

    private void handle(TrackingSession ts) {
        if (ts.isNotExpired())
            return;

        Optional<Player> player = Optional.ofNullable(Bukkit.getPlayer(ts.getPlayerId()));
        if (!player.isPresent())
            return;
        plugin.getButtonPressed().stopTracking(player.get());

        String lastAction = plugin.getConfig().getString(LAST_ACTION_KEY);

        getAction(lastAction)
                .ifPresent(method -> method.run(ts, player.get()));

        Optional<Depend> vault = plugin.getDependFactory().getDepend(Vault.class);
        if (vault.isPresent()) {
            ((Vault) vault.get()).depositPlayer(player.get(), 255);
            player.get().sendMessage("YOU GIVED 200 MONEY");
        }

        plugin.getCooldown().newCooldown(ts.getBlock().getLocation().toVector());
    }

    private Optional<BushFinishAction> getAction(String actionName) {
        try {
            return Optional.of(BushFinishAction.valueOf(actionName));
        } catch (IllegalArgumentException e) {
            plugin.getLog().severe(e.getMessage());
            return Optional.empty();
        }
    }
}
