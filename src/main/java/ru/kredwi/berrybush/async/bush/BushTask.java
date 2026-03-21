package ru.kredwi.berrybush.async.bush;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import ru.kredwi.berrybush.BerryBushPlugin;
import ru.kredwi.berrybush.bush.BushFinishAction;
import ru.kredwi.berrybush.depend.Depend;
import ru.kredwi.berrybush.depend.Vault;
import ru.kredwi.berrybush.tracking.TrackingSession;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.function.Supplier;

@RequiredArgsConstructor
public abstract class BushTask extends BukkitRunnable {

    public static final String BUSH_SOUND_INSTRUMENT = "bush.sound.instrument";
    public static final String BUSH_SOUND_BUSH = "bush.sound.bush";
    public static final String BREAK_PARTICLE_ENALBE = "bush.particle.enable";
    public static final String BREAK_PARTICLE_COUNT_LAST = "bush.count-last";
    public static final String BREAK_PARTICLE = "bush.particle.particle";
    private static final String MSG_VAULT_REWARD = "messages.vault.reward";
    private static final String VAULT_REWARD_NUMBER = "depend.vault.harvest-reward";
    private static final String LAST_ACTION_KEY = "bush.final-action";
    @Getter
    private final BerryBushPlugin plugin;

    protected final Supplier<Sound[]> DEFAULT_SOUNDS = () -> new Sound[]{
            getSound(BUSH_SOUND_INSTRUMENT).orElse(null),
            getSound(BUSH_SOUND_BUSH).orElse(null)
    };

    protected void last(@NotNull TrackingSession ts, @NotNull Player player) {
        String lastAction = plugin.getConfig().getString(LAST_ACTION_KEY);
        getAction(lastAction)
                .ifPresent(method -> {
                    method.run(ts, player);
                    val loc = ts.getBlock().getLocation();
                    playSound(loc, DEFAULT_SOUNDS.get());

                    if (plugin.getConfig().getBoolean(BREAK_PARTICLE_ENALBE))
                        spawnParticles(ts.getBlock(), BREAK_PARTICLE, BREAK_PARTICLE_COUNT_LAST);
                });

        Optional<Depend> vault = plugin.getDependFactory().getDepend(Vault.class);
        if (vault.isPresent()) {
            double amount = plugin.getConfig().getDouble(VAULT_REWARD_NUMBER);
            ((Vault) vault.get()).depositPlayer(player, amount);
            String message = plugin.getMessageOrKey(MSG_VAULT_REWARD);
            player.sendMessage(MessageFormat.format(message, amount));
        }
    }

    protected void playSound(Location loc, Sound[] sounds) {
        for (Sound sound : sounds) {
            if (sound != null && loc.getWorld() != null)
                loc.getWorld()
                        .playSound(loc, sound, 1.0f, 1.0f);
        }

    }

    protected void spawnParticles(@NotNull Block block, @NotNull String particleName, @NotNull String countKey) {
        Optional<Particle> particle = getParticle(particleName);
        if (!particle.isPresent()) {
            plugin.getLog().debug("particle with name " + particleName + " is not found");
            return;
        }
        int count = plugin.getConfig().getInt(countKey);
        Location location = block.getLocation();
        block.getWorld().spawnParticle(particle.get(),
                location.getBlockX() + 0.5, location.getBlockY() + 0.5, location.getBlockZ() + 0.5,
                count, block.getBlockData());
    }

    private Optional<BushFinishAction> getAction(String actionName) {
        try {
            return Optional.of(BushFinishAction.valueOf(actionName));
        } catch (IllegalArgumentException e) {
            plugin.getLog().severe(e.getMessage());
            return Optional.empty();
        }
    }

    protected Optional<Sound> getSound(String key) {
        try {
            return Optional.of(Sound.valueOf(plugin.getConfig().getString(key)));
        } catch (Exception e) {
            plugin.getLog().debug("Error of get sound value " + e.getMessage());
            return Optional.empty();
        }
    }

    protected Optional<Particle> getParticle(String key) {
        try {
            return Optional.of(Particle.valueOf(plugin.getConfig().getString(key)));
        } catch (Exception e) {
            plugin.getLog().debug("Error of get particle value " + e.getMessage());
            return Optional.empty();
        }
    }
}
