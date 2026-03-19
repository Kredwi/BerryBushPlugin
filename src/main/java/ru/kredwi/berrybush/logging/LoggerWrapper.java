package ru.kredwi.berrybush.logging;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.kredwi.berrybush.BerryBushPlugin;

import java.util.logging.Logger;

@RequiredArgsConstructor
public class LoggerWrapper {

    private final BerryBushPlugin plugin;

    @Delegate
    private final Logger logger;

    public void debug(String message) {
        if (plugin.isDebug())
            logger.info("[DEBUG] " + message);
    }
}
