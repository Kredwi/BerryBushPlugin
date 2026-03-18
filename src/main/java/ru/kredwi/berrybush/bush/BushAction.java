package ru.kredwi.berrybush.bush;

import org.bukkit.entity.Player;
import ru.kredwi.berrybush.tracking.TrackingSession;

import java.util.function.BiConsumer;

public interface BushAction extends BiConsumer<TrackingSession, Player> {
}
