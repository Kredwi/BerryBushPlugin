package ru.kredwi.berrybush.async;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PlayerTaskRegistry {

    private final Map<UUID, BukkitRunnable> brm = new HashMap<>();

    public void addRunnable(UUID id, BukkitRunnable runnable) {
        brm.putIfAbsent(id, runnable);
    }

    public Optional<BukkitRunnable> getRunnable(UUID id) {
        return Optional.ofNullable(brm.get(id));
    }

    public BukkitRunnable removeIfPresent(UUID id) {
        BukkitRunnable runnable = brm.remove(id);
        if (runnable != null)
            runnable.cancel();
        return runnable;
    }

    public void cleanUp() {
        new HashSet<>(brm.keySet())
                .forEach(this::removeIfPresent);
    }

}
