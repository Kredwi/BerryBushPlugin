package ru.kredwi.berrybush.async;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PlayerRunnable {

    private final Map<UUID, BukkitRunnable> brm = new HashMap<>();

    public void addRunnable(UUID id, BukkitRunnable runnable) {
        brm.putIfAbsent(id, runnable);
    }

    public Optional<BukkitRunnable> getRunnable(UUID id) {
        return Optional.ofNullable(brm.get(id));
    }

    public BukkitRunnable removeIfPresent(UUID id) {
        return brm.remove(id);
    }

}
