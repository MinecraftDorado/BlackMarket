package dev.minecraftdorado.blackmarket.utils.hook;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import dev.minecraftdorado.blackmarket.mainclass.MainClass;
import net.milkbowl.vault.economy.Economy;

public class VaultHook {
	
	public static boolean setupEconomy() {
		if (Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
			RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
			if (economyProvider != null)
				MainClass.econ = economyProvider.getProvider();
		}
        return MainClass.econ != null;
    }
}
