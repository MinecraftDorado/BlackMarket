package dev.minecraftdorado.blackmarket.utils.economy;

import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.hook.PlayerPointsHook;
import dev.minecraftdorado.blackmarket.utils.hook.VaultHook;
import net.milkbowl.vault.economy.Economy;

public class EconomyManager {
	
	public static enum EconomyType {
		VAULT, PLAYERPOINTS;
	}
	
	private static EconomyType type;
	public static Object econ;
	private boolean status = false;
	
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
		}
	}
}
