package ru.kredwi.berrybush.depend;

import java.util.Optional;

public interface Depend {
    Optional<Depend> get();
}
