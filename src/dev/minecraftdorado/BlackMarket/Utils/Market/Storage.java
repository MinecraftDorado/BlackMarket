package dev.minecraftdorado.BlackMarket.Utils.Market;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import dev.minecraftdorado.BlackMarket.Utils.Config;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.InventoryManager.Inv;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils.UMaterial;

public class Storage {
	
	public static String getStorageTitle() {
		return Config.getMessage("menus.storage");
	}
	
	public static Inv getStorageInventory(Player player) {
		Inv inv = new Inv(getStorageTitle(), 6);
		
		inv.setBackgroud(UMaterial.GRAY_STAINED_GLASS_PANE.getItemStack(), false);
		inv.setBackgroud(UMaterial.BLACK_STAINED_GLASS_PANE.getItemStack(), true);
		
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
			inv.setItem(slot, l.get(i).getItemStack());
			inv.addBlackItem(l.get(i), slot);
			items++;
		}
		
		inv.setItem(49, Config.getItemStack("back"));
		return inv;
	}
}