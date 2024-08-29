package io.github.greenmc.dragonevent.economy;

import io.github.greenmc.dragonevent.DragonEvent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultManager {

    private final DragonEvent plugin;
    private Economy economy;

    public VaultManager(DragonEvent plugin) {
        this.plugin = plugin;

        setupEconomy();
    }

    public void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        economy = rsp.getProvider();
    }

    public Economy getEconomy() {
        return economy;
    }

}