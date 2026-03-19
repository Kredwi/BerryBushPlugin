package ru.kredwi.berrybush.depend;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import ru.kredwi.berrybush.BerryBushPlugin;
import ru.kredwi.berrybush.logging.LoggerWrapper;

import java.lang.reflect.Method;
import java.util.Optional;

public class WorldGuard implements Depend {

    private final LoggerWrapper logger;
    private Object protectionQuery;
    private Method testBlockInteract;

    public WorldGuard(BerryBushPlugin plugin) {
        this.logger = plugin.getLog();
        try {
            Class<?> clazz = Class.forName("com.sk89q.worldguard.bukkit.ProtectionQuery");
            this.protectionQuery = clazz.getDeclaredConstructor().newInstance();
            this.testBlockInteract = protectionQuery.getClass()
                    .getMethod("testBlockInteract", Object.class, Block.class);
            logger.info("[DEPEND] WorldGuard found");
        } catch (Exception e) {
            logger.debug(e.getMessage());
            logger.info("[DEPEND] WorldGuard not found");
            this.protectionQuery = null;
        }
    }

    public boolean testBlockInteract(Player player, Block block) {
        try {
            if (protectionQuery != null)
                return (boolean) testBlockInteract
                        .invoke(protectionQuery, player, block);
        } catch (Exception e) {
            logger.warning("[DEPEND] Error of invoke worldguard method: " + e.getMessage());
        }
        return true;

    }

    @Override
    public Optional<Depend> get() {
        return protectionQuery != null
                ? Optional.of(this)
                : Optional.empty();
    }
}
