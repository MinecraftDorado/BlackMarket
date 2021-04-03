package dev.minecraftdorado.blackmarket.utils.hook;

import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import dev.minecraftdorado.blackmarket.utils.economy.EconomyManager;

public class PlayerPointsHook {
	
	public static boolean setupEconomy() {
		if(Bukkit.getServer().getPluginManager().getPlugin("PlayerPoints") != null)
			EconomyManager.econ = PlayerPoints.class.cast(Bukkit.getServer().getPluginManager().getPlugin("PlayerPoints")).getAPI();
        return EconomyManager.econ != null;
    }
}
