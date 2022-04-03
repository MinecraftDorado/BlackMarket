package dev.minecraftdorado.blackmarket.utils.market;

import dev.minecraftdorado.blackmarket.mainclass.MainClass;
import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.database.mysql.dbMySQL;
import dev.minecraftdorado.blackmarket.utils.inventory.InventoryManager;
import dev.minecraftdorado.blackmarket.utils.inventory.InventoryManager.Inv;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.CategoryUtils;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.CategoryUtils.Category;
import dev.minecraftdorado.blackmarket.utils.market.PlayerData.Data;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Market {
	
	public Market() {
		Bukkit.getScheduler().runTaskTimer(MainClass.main, new Runnable() {
			// Market updater
			@Override
			public void run() {
				for(Player p : Bukkit.getOnlinePlayers()) {
					if(p.getOpenInventory() != null && InventoryManager.hasHistory(p)){
						Inv inv = InventoryManager.getLastInv(p);
						if(inv.getTitle().equals(getTitle())) {
							InventoryManager.updateInventory(p, getInventory(p));
						}
					}
				};
			}
		}, 100, 100);
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

		ArrayList<BlackItem> l = dbMySQL.loadBlackItems(data.getCategory().getKey(), data.getOrder().getName(), data.isInverted(), page*24);

		int slot = 10;
		int items = 0;
		
		for (int i = 0; i < 24; i++) {
			if(l.size() <= i) break;
			if(items%6 == 0 && items != 0)
				slot = slot + 4;
			else
				slot++;
			inv.setItem(slot, l.get(i).getItemStack(player, true));
			inv.addBlackItem(l.get(i), slot);
			items++;
		}
		inv.setItem(Config.getSlot("market.sales"), Config.getItemStack("market.sales", "menus.market.items.sales"));
		inv.setItem(Config.getSlot("market.close"), Config.getItemStack("market.close", "menus.market.items.close"));
		inv.setItem(Config.getSlot("market.info"), Config.getItemStack("market.info", "menus.market.items.info"));
		inv.setItem(Config.getSlot("market.order"), Config.getItemStack("market.order", "menus.market.items.order", player));
		inv.setItem(Config.getSlot("market.storage"), Config.getItemStack("market.storage", "menus.market.items.storage"));
		
		if(page != 0)
			inv.setItem(Config.getSlot("market.previous"), Config.getItemStack("market.previous", "menus.market.items.previous", player));
		if(l.size() > 24)
			inv.setItem(Config.getSlot("market.next"), Config.getItemStack("market.next", "menus.market.items.next", player));
		
		return inv;
	}

}
