package ru.kredwi.berrybush;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ru.kredwi.berrybush.listener.PlayerEatInteract;
import ru.kredwi.berrybush.tracking.ButtonPressed;

public class BerryBushPlugin extends JavaPlugin {

    private ButtonPressed buttonPressed;
    private Listener playerEatInteract;

    public BerryBushPlugin() {
        this.buttonPressed = new ButtonPressed();
        this.playerEatInteract = new PlayerEatInteract(getLogger(), this, buttonPressed);
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(playerEatInteract, this);
    }
}
