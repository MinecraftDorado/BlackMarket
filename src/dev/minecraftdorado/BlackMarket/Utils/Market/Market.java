package dev.minecraftdorado.BlackMarket.Utils.Market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.minecraftdorado.BlackMarket.MainClass.MainClass;
import dev.minecraftdorado.BlackMarket.Utils.Config;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.InventoryManager;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.InventoryManager.Inv;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils.CategoryUtils;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils.CategoryUtils.Category;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils.OrderUtils;
import dev.minecraftdorado.BlackMarket.Utils.Market.BlackItem.Status;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils.UMaterial;

public class Market {
	
	private static HashMap<Integer, BlackItem> list = new HashMap<>();
	private static int id = 0;
	
	public Market() {
		Bukkit.getScheduler().runTaskTimer(MainClass.main, new Runnable() {
			// Market updater
			@Override
			public void run() {
				HashMap<Integer, ItemStack> l = new HashMap<>();
				
				for(Player p : Bukkit.getOnlinePlayers()) {
					if(p.getOpenInventory() != null && InventoryManager.hasHistory(p)){
						Inv inv = InventoryManager.getLastInv(p);
						if(inv.getTitle().equals(getMarketTitle())) {
							boolean update = true;
							
							for(int slot : inv.getBlackList().keySet()) {
								BlackItem bItem = inv.getBlackList().get(slot);
								
								if(!l.containsKey(bItem.getId()))
									if(!bItem.getStatus().equals(Status.ON_SALE)) {
										Bukkit.getScheduler().runTask(MainClass.main, () -> InventoryManager.updateInventory(p, getMarketInventory(p)));
										update = false;
										break;
									}else
										l.put(bItem.getId(), bItem.getItemStack());
								
								inv.setItem(slot, l.get(bItem.getId()));
							}
							if(update)
								InventoryManager.updateInventory(p, inv);
						}
					}
				};
			}
		}, 20, 20);
	}
	
	public static void addId() {
		id++;
	}
	
	public static int getId() {
		return id;
	}
	
	public static void setId(int ID) {
		id = ID;
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
		return Config.getMessage("menus.market.title");
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
	
	public static Inv getMarketInventory(Player player) {
		int page = getPlayerPage(player);
		Inv inv = new Inv(getMarketTitle(), 6);
		inv.setBackgroud(Config.getMarketBackground(), false);
		inv.setBackgroud(Config.getMarketBorder(), true);
		
		ItemStack item = Config.getMarketBorder();
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(" ");
		item.setItemMeta(meta);
		
		inv.setItem(10, item);
		inv.setItem(19, item);
		inv.setItem(28, item);
		inv.setItem(37, item);
		
		Category cat = PlayerData.get(player.getUniqueId()).getCategory();
		
		CategoryUtils.getCategories().forEach(category -> {
			inv.setItem((category.getRow()-1) * 9, category.getItemStack(category.equals(cat)));
		});
		
		ArrayList<BlackItem> l = new ArrayList<>();
		
		Category category = PlayerData.get(player.getUniqueId()).getCategory();
		
		ArrayList<Integer> toRemove = new ArrayList<>();
		
		for(BlackItem bItem : list.values()) {
			if(bItem.getStatus().equals(Status.ON_SALE)) {
				if(category == null || category.getMaterials().isEmpty() || category.contain(UMaterial.match(bItem.getItemStack())))
					l.add(bItem);
			}else
				toRemove.add(bItem.getId());
		}
		
		
		switch(PlayerData.get(player.getUniqueId()).getOrder()) {
		case AMOUNT:
			l = OrderUtils.sortByAmount(l);
			break;
		case TYPE:
			l = OrderUtils.sortByType(l);
			break;
		case VALUE:
			l = OrderUtils.sortByValue(l);
			break;
		default:
			break;
		}
		
		if(!toRemove.isEmpty())
			toRemove.forEach(id -> list.remove(id));
		
		
		int slot = 10;
		int items = 0;
		
		for (int i = 0; i < 24; i++) {
			if(l.size() <= ((page*24) + i))
				break;
			if(items%6 == 0 && items != 0)
				slot = slot + 4;
			else
				slot++;
			int id = (page*24) + i;
			inv.setItem(slot, l.get(id).getItemStack());
			inv.addBlackItem(l.get(id), slot);
			items++;
		}
		inv.setItem(49, Config.getItemStack("market.close", "menus.market.items.close"));
		inv.setItem(50, Config.getItemStack("market.info", "menus.market.items.info"));
		inv.setItem(51, Config.getItemStack("market.order", "menus.market.items.order", player));
		inv.setItem(52, Config.getItemStack("market.storage", "menus.market.items.storage"));
		
		if(page != 0)
			inv.setItem(46, Config.getItemStack("market.previous", "menus.market.items.previous", player));
		if(l.size() > ((page+1)*24))
			inv.setItem(53, Config.getItemStack("market.next", "menus.market.items.next", player));
		
		return inv;
	}
	
	public static int getPages() {
		return list.values().size()/24 + 1;
	}
}
