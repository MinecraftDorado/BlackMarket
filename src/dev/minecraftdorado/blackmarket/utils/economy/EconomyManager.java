package dev.minecraftdorado.blackmarket.utils.economy;

import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.hook.GemsEconomyHook;
import dev.minecraftdorado.blackmarket.utils.hook.PlayerPointsHook;
import dev.minecraftdorado.blackmarket.utils.hook.VaultHook;
import me.xanium.gemseconomy.api.GemsEconomyAPI;
import net.milkbowl.vault.economy.Economy;

public class EconomyManager {
	
	public static enum EconomyType {
		VAULT, PLAYERPOINTS, GEMSECONOMY;
	}
	
	private static EconomyType type;
	public static Object econ;
	private boolean status = false;
	private static String econValue = null;
	
	public EconomyManager() {
		type = Config.getEconomyType(); 
		
		switch (type) {
		case PLAYERPOINTS:
			if(PlayerPointsHook.setupEconomy())
				status = true;
			break;
		case VAULT:
			if(VaultHook.setupEconomy())
				status = true;
			break;
		case GEMSECONOMY:
			if(GemsEconomyHook.setupEconomy()) {
				status = true;
				if(Config.getEconomyValue() != null && ((GemsEconomyAPI) econ).getCurrency(Config.getEconomyValue()) != null)
					econValue = Config.getEconomyValue();
			}
			break;
		}
	}
	
	public boolean hasEconomy() {
		return status;
	}
	
	public static void deposit(OfflinePlayer player, double value) {
		switch(type) {
		case PLAYERPOINTS:
			((PlayerPointsAPI) econ).give(player.getUniqueId(), (int) value);
			break;
		case VAULT:
			((Economy) econ).depositPlayer(player, value);
			break;
		case GEMSECONOMY:
			if(econValue != null)
				((GemsEconomyAPI) econ).deposit(player.getUniqueId(), value, ((GemsEconomyAPI) econ).getCurrency(econValue));
			else
				((GemsEconomyAPI) econ).deposit(player.getUniqueId(), value);
		}
	}
	
	public static double get(Player player) {
		double bal = 0;
		
		switch (type) {
		case PLAYERPOINTS:
			bal = ((PlayerPointsAPI) econ).look(player.getUniqueId());
			break;
		case VAULT:
			bal = ((Economy) econ).getBalance(player);
			break;
		case GEMSECONOMY:
			if(econValue != null)
				((GemsEconomyAPI) econ).getBalance(player.getUniqueId(), ((GemsEconomyAPI) econ).getCurrency(econValue));
			else
				((GemsEconomyAPI) econ).getBalance(player.getUniqueId());
			break;
		}
		
		return bal;
	}
	
	public static boolean has(Player player, double value) {
		return get(player) >= value;
	}
	
	public static void withdraw(Player player, double value) {
		switch (type) {
		case PLAYERPOINTS:
			((PlayerPointsAPI) econ).take(player.getUniqueId(), (int) value);
			break;
		case VAULT:
			((Economy) econ).withdrawPlayer(player, value);
			break;
		case GEMSECONOMY:
			if(econValue != null)
				((GemsEconomyAPI) econ).withdraw(player.getUniqueId(), value, ((GemsEconomyAPI) econ).getCurrency(econValue));
			else
				((GemsEconomyAPI) econ).withdraw(player.getUniqueId(), value);
			break;
		}
	}
}
