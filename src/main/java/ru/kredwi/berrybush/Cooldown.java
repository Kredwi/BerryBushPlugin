package ru.kredwi.berrybush;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

public class Cooldown<K> {

    @Getter
    private final long cooldownTime;
    private Cache<K, Long> cooldowns;

    public Cooldown(long number) {
        this.cooldownTime = number;
        this.cooldowns = CacheBuilder.newBuilder()
                .expireAfterWrite(number, TimeUnit.SECONDS)
                .build();
    }

    public void newCooldown(K k) {
        cooldowns.put(k, System.currentTimeMillis());
    }

    public boolean isOnCooldown(K k) {
        return cooldowns.getIfPresent(k) != null;
    }

    public Long getWriteTime(K k) {
        return cooldowns.getIfPresent(k);
    }

    public void cleanUp() {
        cooldowns.cleanUp();
    }
}
