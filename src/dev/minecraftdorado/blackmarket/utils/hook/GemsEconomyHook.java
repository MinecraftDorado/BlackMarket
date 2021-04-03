package dev.minecraftdorado.blackmarket.utils.hook;

import org.bukkit.Bukkit;
import dev.minecraftdorado.blackmarket.utils.economy.EconomyManager;
import me.xanium.gemseconomy.api.GemsEconomyAPI;

public class GemsEconomyHook {
	
	public static boolean setupEconomy() {
		if(Bukkit.getServer().getPluginManager().getPlugin("GemsEconomy") != null)
			EconomyManager.econ = new GemsEconomyAPI();
        return EconomyManager.econ != null;
    }
}
