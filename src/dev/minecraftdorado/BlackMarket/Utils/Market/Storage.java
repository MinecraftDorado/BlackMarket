package dev.minecraftdorado.BlackMarket.Utils.Market;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import dev.minecraftdorado.BlackMarket.Utils.Config;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.InventoryManager.Inv;

public class Storage {
	
	public static String getStorageTitle() {
		return Config.getMessage("menus.storage");
	}
	
	public static Inv getStorageInventory(Player player) {
		Inv inv = new Inv(getStorageTitle(), 6);
		
		inv.setBackgroud(Config.getStorageBackground(), false);
		inv.setBackgroud(Config.getStorageBorder(), true);
		
		ArrayList<BlackItem> l = PlayerData.get(player.getUniqueId()).getStorage();
		
		int slot = 9;
		int items = 0;
		
		for (int i = 0; i < 28; i++) {
			if(l.size() <= i)
				break;
			if(items%7 == 0 && items != 0)
				slot = slot + 3;
			else
				slot++;
			inv.setItem(slot, l.get(i).getOriginal());
			inv.addBlackItem(l.get(i), slot);
			items++;
		}
		
		inv.setItem(49, Config.getItemStack("back"));
		inv.setItem(50, Config.getItemStack("take_items"));
		return inv;
	}
}