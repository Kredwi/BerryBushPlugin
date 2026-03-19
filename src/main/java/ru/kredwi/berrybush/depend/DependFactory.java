package ru.kredwi.berrybush.depend;

import lombok.RequiredArgsConstructor;
import ru.kredwi.berrybush.logging.LoggerWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class DependFactory {

    private final Map<Class<? extends Depend>, Depend> depend = new HashMap<>();
    private final LoggerWrapper logger;

    public Optional<Depend> getDepend(Class<? extends Depend> dep) {
        return Optional.ofNullable(depend.get(dep));
    }

    public void addDepend(Depend depend) {
        if (depend.get().isPresent()) {
            logger.info("Register dependencies with " + depend.getClass());
            this.depend.put(depend.getClass(), depend);
        }
    }
}
