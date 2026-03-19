package ru.kredwi.berrybush.depend;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import ru.kredwi.berrybush.BerryBushPlugin;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

public class Vault implements Depend {

    private final BerryBushPlugin plugin;

    private Object provider;
    private Method addBalance;
    private Method getBalance;
    private Method withdrawPlayer;

    public Vault(BerryBushPlugin plugin) {
        this.plugin = plugin;
        try {

            Class<?> clazz = Class.forName("net.milkbowl.vault.economy.Economy");

            RegisteredServiceProvider<?> rsp = Bukkit.getServicesManager()
                    .getRegistration(clazz);

            if (rsp == null)
                throw new IllegalStateException("RegisteredServiceProvider return is null");

            this.provider = rsp.getProvider();
            this.addBalance = provider.getClass().getMethod("depositPlayer", OfflinePlayer.class, double.class);
            this.getBalance = provider.getClass().getMethod("getBalance", OfflinePlayer.class);
            this.withdrawPlayer = provider.getClass().getMethod("withdrawPlayer", OfflinePlayer.class, double.class);
            plugin.getLog().info("[DEPEND] Vault found");
        } catch (Exception e) {
            plugin.getLog().debug("[DEPEND] Error of loading vault addon " + e.getMessage());
            this.provider = null;
            plugin.getLog().info("[DEPEND] Vault not found");
        }
    }

    public void depositPlayer(OfflinePlayer offlinePlayer, double amount) {
        if (provider == null | addBalance == null)
            return;
        try {
            addBalance.invoke(provider, offlinePlayer, amount);
        } catch (Exception e) {
            plugin.getLog().debug("[DEPEND] Error of invoke vault method (Vault#depositPlayer) " + e.getMessage());
        }
    }

    public void withdrawPlayer(OfflinePlayer offlinePlayer, double amount) {
        if (provider == null || withdrawPlayer == null)
            return;
        try {
            withdrawPlayer.invoke(provider, offlinePlayer, amount);
        } catch (Exception e) {
            plugin.getLog().debug("[DEPEND] Error of invoke vault method (Vault#withdrawPlayer) " + e.getMessage());
        }
    }

    public void getBalance(UUID uuid, int number) {
        if (provider == null || getBalance == null)
            return;
        try {
            getBalance.invoke(provider, uuid, number);
        } catch (Exception e) {
            plugin.getLog().debug("[DEPEND] Error of invoke vault method (Vault#getBalance) " + e.getMessage());
        }
    }

    @Override
    public Optional<Depend> get() {
        if (provider == null)
            return Optional.empty();
        return Optional.of(this);
    }
}
