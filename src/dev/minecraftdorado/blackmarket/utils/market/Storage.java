package dev.minecraftdorado.blackmarket.utils.market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;

import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.database.mysql.dbMySQL;
import dev.minecraftdorado.blackmarket.utils.inventory.InventoryManager.Inv;

public class Storage {
	
	public static String getTitle() {
		return Config.getMessage("menus.storage.title");
	}
	
	public static Inv getInventory(Player player) {
		Inv inv = new Inv(getTitle(), 6);
		
		inv.setBackgroud(Config.getStorageBackground(), false);
		inv.setBackgroud(Config.getStorageBorder(), true);

		int page = getPlayerPage(player);

		ArrayList<BlackItem> l = dbMySQL.loadStorage(player.getUniqueId(), page);

		int slot = 9;
		int items = 0;

		for (int i = 0; i < 28; i++) {

			if(l.size() <= i) break;

			if(items%7 == 0 && items != 0)
				slot = slot + 3;
			else
				slot++;

			inv.setItem(slot, l.get(i).getOriginal());
			inv.addBlackItem(l.get(i), slot);
			items++;
		}
		
		inv.setItem(Config.getSlot("storage.back"), Config.getItemStack("storage.back", "menus.storage.items.back"));
		inv.setItem(Config.getSlot("storage.take_items"), Config.getItemStack("storage.take_items", "menus.storage.items.take_items"));
		
		if(page != 0)
			inv.setItem(Config.getSlot("storage.previous"), Config.getItemStack("storage.previous", "menus.storage.items.previous", player));
		if(l.size() > 28)
			inv.setItem(Config.getSlot("storage.next"), Config.getItemStack("storage.next", "menus.storage.items.next", player));
		
		return inv;
	}

	private static HashMap<UUID, Integer> playerPage = new HashMap<>();
	
	public static int getPlayerPage(Player player) {
		UUID uuid = player.getUniqueId();
		if(!playerPage.containsKey(uuid))
			playerPage.put(uuid, 0);
		return playerPage.get(uuid);
	}
	
	public static void setPlayerPage(UUID uuid, int value) {
		playerPage.put(uuid, value);
	}
}