package dev.minecraftdorado.BlackMarket.Utils.Market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;

import dev.minecraftdorado.BlackMarket.Utils.Config;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.InventoryManager.Inv;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils.UMaterial;

public class Market {
	
	private static HashMap<Integer, BlackItem> list = new HashMap<>();
	private static int id = 0;
	
	public static void addId() {
		id++;
	}
	
	public static int getId() {
		return id;
	}
	
	public static void addItem(BlackItem bItem) {
		list.put(bItem.getId(), bItem);
	}
	
	public static BlackItem getBlackItemById(int id) {
		if(list.containsKey(id))
			return list.get(id);
		return null;
	}
	
	public static String getMarketTitle() {
		return "BlackMarket";
	}
	
	private static HashMap<UUID, Integer> playerPage = new HashMap<>();
	
	public static int getPlayerPage(UUID uuid) {
		if(!playerPage.containsKey(uuid))
			playerPage.put(uuid, 0);
		return playerPage.get(uuid);
	}
	
	public static void setPlayerPage(UUID uuid, int value) {
		playerPage.put(uuid, value);
	}
	
	public static Inv getMarketInventory(Player player) {
		int page = getPlayerPage(player.getUniqueId());
		Inv inv = new Inv(getMarketTitle(), 6);
		inv.setBackgroud(UMaterial.GRAY_STAINED_GLASS_PANE.getItemStack(), false);
		inv.setBackgroud(UMaterial.BLACK_STAINED_GLASS_PANE.getItemStack(), true);
		
		inv.setItem(0, Config.getItemStack("filter"));
		inv.setItem(9, Config.getItemStack("filter"));
		inv.setItem(18, Config.getItemStack("filter"));
		inv.setItem(27, Config.getItemStack("filter"));
		inv.setItem(36, Config.getItemStack("filter"));
		inv.setItem(45, Config.getItemStack("filter"));
		
		ArrayList<BlackItem> l = new ArrayList<>();
		
		for(BlackItem bItem : list.values())
			/*
			 * Future Order
			 */
			l.add(bItem);
		
		
		int slot = 10;
		int items = 0;
		
		for (int i = 0; i < 24; i++) {
			if(l.size() <= ((page*24) + i))
				break;
			if(items%6 == 0 && items != 0)
				slot = slot + 4;
			else
				slot++;
			
			inv.setItem(slot, l.get((page*24) + i).getItemStack());
			items++;
		}
		
		inv.setItem(49, Config.getItemStack("close"));
		
		if(page != 0)
			inv.setItem(46, Config.getItemStack("previous", player));
		if(l.size() > ((page+1)*24))
			inv.setItem(53, Config.getItemStack("next", player));
		
		return inv;
	}
	
	public static int getPages() {
		return list.values().size()/24 + 1;
	}
}
