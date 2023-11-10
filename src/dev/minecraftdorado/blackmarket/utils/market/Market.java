package dev.minecraftdorado.blackmarket.utils.market;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.minecraftdorado.blackmarket.mainclass.MainClass;
import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.inventory.InventoryManager;
import dev.minecraftdorado.blackmarket.utils.inventory.InventoryManager.Inv;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.CategoryUtils;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.CategoryUtils.Category;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.OrderUtils;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.UMaterial;
import dev.minecraftdorado.blackmarket.utils.market.BlackItem.Status;
import dev.minecraftdorado.blackmarket.utils.market.PlayerData.Data;

public class Market {
	
	private static HashMap<Category, ArrayList<BlackItem>> catList = new HashMap<>();
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
						if(inv.getTitle().equals(getTitle())) {
							boolean update = true;
							
							Category cat = PlayerData.get(p.getUniqueId()).getCategory();
							
							if(catList.containsKey(cat) && inv.getBlackList().size() < 28 && catList.get(cat).size() > inv.getBlackList().size()) {
								Bukkit.getScheduler().runTask(MainClass.main, () -> InventoryManager.openInventory(p, getInventory(p)));
								continue;
							}
							
							if(!inv.getBlackList().keySet().isEmpty()) {
								for(int slot : inv.getBlackList().keySet()) {
									BlackItem bItem = inv.getBlackList().get(slot);
									
									if(!l.containsKey(bItem.getId()))
										if(!bItem.getStatus().equals(Status.ON_SALE)) {
											Bukkit.getScheduler().runTask(MainClass.main, () -> InventoryManager.openInventory(p, getInventory(p)));
											update = false;
											break;
										}else
											l.put(bItem.getId(), bItem.getItemStack(p, true));
									
									inv.setItem(slot, l.get(bItem.getId()));
								}
								if(update)
									InventoryManager.updateInventory(p, inv);
							}
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
		id = id < bItem.getId() ? bItem.getId() : id;
		
		for(Category c : CategoryUtils.getCategories()) {
			if(c.getMaterials().isEmpty() || c.contain(UMaterial.match(bItem.getOriginal()))) {
				if(!catList.containsKey(c))
					catList.put(c, new ArrayList<>());
				if(!catList.get(c).contains(bItem))
					catList.get(c).add(bItem);
			}
		};
	}
	
	public static String getTitle() {
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
	
	public static Inv getInventory(Player player) {
		Data data = PlayerData.get(player.getUniqueId());
		
		int page = getPlayerPage(player);
		Inv inv = new Inv(getTitle(), 6);
		inv.setBackgroud(Config.getMarketBackground(), false);
		inv.setBackgroud(Config.getMarketBorder(), true);
		
		ItemStack item = Config.getMarketBorder();
		if(!item.getType().equals(Material.AIR)) {
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(" ");
			item.setItemMeta(meta);
		}
		
		inv.setItem(10, item);
		inv.setItem(19, item);
		inv.setItem(28, item);
		inv.setItem(37, item);
		
		Category category = data.getCategory();
		
		CategoryUtils.getCategories().forEach(c -> {
			inv.setItem((c.getRow()-1) * 9, c.getItemStack(c.equals(category)));
		});
		
		ArrayList<BlackItem> l = new ArrayList<>();
		
		ArrayList<BlackItem> toRemove = new ArrayList<>();
		
		if(catList.containsKey(category))
			for(BlackItem bItem : catList.get(category)) {
				if(bItem.getStatus().equals(Status.ON_SALE)) {
					l.add(bItem);
				}else
					toRemove.add(bItem);
			}
		
		if(!toRemove.isEmpty())
			toRemove.forEach(bItem -> catList.get(category).remove(bItem));
		
		switch(data.getOrder()) {
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
		
		if(data.isReverse())
			Collections.reverse(l);
		
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
			inv.setItem(slot, l.get(id).getItemStack(player, true));
			inv.addBlackItem(l.get(id), slot);
			items++;
		}
		inv.setItem(Config.getSlot("market.sales"), Config.getItemStack("market.sales", "menus.market.items.sales"));
		inv.setItem(Config.getSlot("market.close"), Config.getItemStack("market.close", "menus.market.items.close"));
		inv.setItem(Config.getSlot("market.info"), Config.getItemStack("market.info", "menus.market.items.info"));
		inv.setItem(Config.getSlot("market.order"), Config.getItemStack("market.order", "menus.market.items.order", player));
		inv.setItem(Config.getSlot("market.storage"), Config.getItemStack("market.storage", "menus.market.items.storage"));
		
		if(page != 0)
			inv.setItem(Config.getSlot("market.previous"), Config.getItemStack("market.previous", "menus.market.items.previous", player));
		if(l.size() > ((page+1)*24))
			inv.setItem(Config.getSlot("market.next"), Config.getItemStack("market.next", "menus.market.items.next", player));
		
		return inv;
	}
	
	public static int getPages(Category cat) {
		return catList.containsKey(cat) ? catList.get(cat).size()/24 + 1 : 0;
	}
}
