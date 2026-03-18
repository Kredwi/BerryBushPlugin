package ru.kredwi.berrybush.bush;

import org.bukkit.entity.Player;
import ru.kredwi.berrybush.tracking.TrackingSession;

public enum BushFinishAction {
    SET_TO_MINIMAL(new SetToMinimalAction()),
    ONLY_DROP(new OnlyDropAction()),
    BREAK(new BreakAction());

    private final BushAction action;

    BushFinishAction(BushAction action) {
        this.action = action;
    }

    public void run(TrackingSession block, Player player) {
        action.accept(block, player);
    }
}
