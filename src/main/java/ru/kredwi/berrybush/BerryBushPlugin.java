package ru.kredwi.berrybush;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import ru.kredwi.berrybush.async.PlayerRunnable;
import ru.kredwi.berrybush.listener.PlayerEatInteract;
import ru.kredwi.berrybush.listener.PlayerQuit;
import ru.kredwi.berrybush.tracking.ButtonPressed;

import java.util.Optional;

@Getter
public class BerryBushPlugin extends JavaPlugin {

    private static final String BUSH_COOLDOWN_KEY = "bush.cooldown";

    private PlayerRunnable runnables;
    private Cooldown<Vector> cooldown;
    private ButtonPressed buttonPressed;

    @Getter(AccessLevel.PRIVATE)
    private Listener[] listeners;

    @Override
    public void onLoad() {
        if (!depedencies())
            return;

        saveDefaultConfig();

        int cooldownTime = getConfig().getInt(BUSH_COOLDOWN_KEY);

        this.runnables = new PlayerRunnable();
        this.cooldown = new Cooldown<>(cooldownTime);
        this.buttonPressed = new ButtonPressed(cooldown);

        try {
            this.listeners = new Listener[]{
                    new PlayerEatInteract(getLogger(), buttonPressed, runnables, cooldown),
                    new PlayerQuit(runnables)
            };
        } catch (NoClassDefFoundError e) {
            getLogger().severe(String.format("Error of create event: %s", e.getMessage()));
            Bukkit.getPluginManager().disablePlugin(this);
        }

    }

    @Override
    public void onEnable() {
        getLogger().info(String.format("Register %s listeners", listeners.length));

        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }

    @Override
    public void onDisable() {
        this.listeners = null;
    }

    private boolean depedencies() {
        PluginManager pm = Bukkit.getPluginManager();
        if (pm.getPlugin("WorldGuard") == null) {
            getLogger().severe("Required depedencies with name WorldGuard is not found");
            pm.disablePlugin(this);
            return false;
        }
        ;
        return true;
    }

    @NotNull
    public String getMessageOrKey(@NotNull String key) {
        return Optional.ofNullable(getConfig().getString(key))
                .orElse(key);
    }
}
