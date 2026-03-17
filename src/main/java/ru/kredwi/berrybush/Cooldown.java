package ru.kredwi.berrybush;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Cooldown<K> {

    private final Map<K, Long> cooldowns = new HashMap<>();

    public void newCooldown(K k) {
        cooldowns.put(k, System.currentTimeMillis());
    }

    public boolean isOnCooldown(K k, long ms) {
        long currentTime = System.currentTimeMillis();
        Long startTime = cooldowns.get(k);
        if (startTime == null)
            return false;

        boolean expire = (currentTime - startTime) > ms;
        if (expire)
            cooldowns.remove(k);
        return !expire;
    }

}
