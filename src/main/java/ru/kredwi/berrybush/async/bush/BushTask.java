package ru.kredwi.berrybush.async.bush;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.kredwi.berrybush.BerryBushPlugin;
import ru.kredwi.berrybush.bush.BushFinishAction;
import ru.kredwi.berrybush.depend.Depend;
import ru.kredwi.berrybush.depend.Vault;
import ru.kredwi.berrybush.tracking.TrackingSession;

import java.text.MessageFormat;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class BushTask extends BukkitRunnable {

    public static final String BREAK_SOUND = "bush.farm-sound";
    private static final String MSG_VAULT_REWARD = "messages.vault.reward";
    private static final String VAULT_REWARD_NUMBER = "depend.vault.harvest-reward";
    private static final String LAST_ACTION_KEY = "bush.final-action";
    @Getter
    private final BerryBushPlugin plugin;

    protected void last(@NotNull TrackingSession ts, @NotNull Player player) {
        String lastAction = plugin.getConfig().getString(LAST_ACTION_KEY);
        getAction(lastAction)
                .ifPresent(method -> {
                    method.run(ts, player);
                    val loc = ts.getBlock().getLocation();
                    Sound sound = getSound(BREAK_SOUND);
                    if (sound != null && loc.getWorld() != null)
                        loc.getWorld()
                                .playSound(loc, sound, 1.0f, 1.0f);
                });

        Optional<Depend> vault = plugin.getDependFactory().getDepend(Vault.class);
        if (vault.isPresent()) {
            double amount = plugin.getConfig().getDouble(VAULT_REWARD_NUMBER);
            ((Vault) vault.get()).depositPlayer(player, amount);
            String message = plugin.getMessageOrKey(MSG_VAULT_REWARD);
            player.sendMessage(MessageFormat.format(message, amount));
        }
    }

    private Optional<BushFinishAction> getAction(String actionName) {
        try {
            return Optional.of(BushFinishAction.valueOf(actionName));
        } catch (IllegalArgumentException e) {
            plugin.getLog().severe(e.getMessage());
            return Optional.empty();
        }
    }

    @Nullable
    protected Sound getSound(@NotNull String key) {
        try {
            return Sound.valueOf(plugin.getConfig().getString(key));
        } catch (Exception e) {
            return null;
        }
    }

}
