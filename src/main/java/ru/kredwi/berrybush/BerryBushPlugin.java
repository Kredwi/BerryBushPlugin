package ru.kredwi.berrybush;

import lombok.Getter;
import lombok.val;
import lombok.var;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import ru.kredwi.berrybush.async.PlayerTaskRegistry;
import ru.kredwi.berrybush.depend.DependFactory;
import ru.kredwi.berrybush.depend.Vault;
import ru.kredwi.berrybush.depend.WorldGuard;
import ru.kredwi.berrybush.listener.PlayerEatInteract;
import ru.kredwi.berrybush.listener.PlayerQuit;
import ru.kredwi.berrybush.logging.LoggerWrapper;
import ru.kredwi.berrybush.tracking.ButtonPressed;

@Getter
public class BerryBushPlugin extends JavaPlugin {

    private static final int REQUIRED_CONFIG_VERSION = 1;
    private static final String BUSH_COOLDOWN_KEY = "bush.cooldown";
    private final LoggerWrapper log = new LoggerWrapper(this, super.getLogger());
    private final DependFactory dependFactory = new DependFactory(getLog());

    private PlayerTaskRegistry taskRegistry;
    private Cooldown<Vector> cooldown;
    private ButtonPressed buttonPressed;

    @Override
    public void onEnable() {
        if (!isEnabled())
            return;
        var startTime = System.currentTimeMillis();
        getProvidingPlugin(net.milkbowl.vault.economy.Economy.class);
        getLog().debug("Debug logging is enabled");

        saveDefaultConfig();

        if (getConfig().getInt("version") != REQUIRED_CONFIG_VERSION) {
            getLog().severe("Invalid config version");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        int cooldownTime = getConfig().getInt(BUSH_COOLDOWN_KEY);

        this.taskRegistry = new PlayerTaskRegistry();
        this.cooldown = new Cooldown<>(cooldownTime);
        this.buttonPressed = new ButtonPressed();

        try {

            val listeners = new Listener[]{
                    new PlayerEatInteract(),
                    new PlayerQuit(this)
            };

            getLog().info(String.format("Register %s listeners", listeners.length));

            for (Listener listener : listeners) {
                Bukkit.getPluginManager().registerEvents(listener, this);
            }
        } catch (Exception e) {
            getLog().severe(String.format("Error of create event: %s", e.getMessage()));
            Bukkit.getPluginManager().disablePlugin(this);
        }

        log.debug("Loading softdepend addons");
        dependFactory.addDepend(new WorldGuard(this));
        dependFactory.addDepend(new Vault(this));

        var loadingEndTime = System.currentTimeMillis();

        log.debug("Complete plugin load with time " + (loadingEndTime - startTime) + "ms");
    }

    @Override
    public void onDisable() {
        log.debug("Clear player task registry");
        taskRegistry.cleanUp();
        log.debug("Clear player cooldowns");
        cooldown.cleanUp();
    }

    public String getMessageOrKey(@NotNull String key) {
        return getConfig().getString(key, key);
    }

    public boolean isDebug() {
        return getConfig().getBoolean("debug");
    }
}
