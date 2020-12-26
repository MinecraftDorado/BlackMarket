package dev.minecraftdorado.BlackMarket.Utils.Market.sell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.minecraftdorado.BlackMarket.Utils.Config;
import dev.minecraftdorado.BlackMarket.Utils.Utils;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.InventoryManager.Inv;
import dev.minecraftdorado.BlackMarket.Utils.Market.BlackItem;
import dev.minecraftdorado.BlackMarket.Utils.Market.PlayerData;

public class Sales {
	
	public static String getTitle() {
		return Config.getMessage("menus.sales.title");
	}
	
	public static Inv getInventory(Player player) {
		Inv inv = new Inv(getTitle(), 6);
		
		inv.setBackgroud(Config.getSellMenuBackground(), false);
		inv.setBackgroud(Config.getSellMenuBorder(), true);
		
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
		
		inv.setItem(Config.getSlot("sales.back"), Config.getItemStack("sales.back", "menus.sales.items.back"));
		inv.setItem(Config.getSlot("sales.item"), getItemStack(player.getUniqueId()) != null ? getItem(player.getUniqueId()) : Config.getItemStack("sales.item", "menus.sales.items.item", player));
		inv.setItem(Config.getSlot("sales.value"), Config.getItemStack("sales.value", "menus.sales.items.value", player));
		inv.setItem(Config.getSlot("sales.post"), Config.getItemStack("sales.post", "menus.sales.items.post", player));
		return inv;
	}
	
	private static HashMap<UUID, Double> priceList = new HashMap<>();
	
	public static void setPrice(UUID uuid, double price) {
		priceList.put(uuid, price);
	}
	
	public static double getPrice(UUID uuid) {
		return !priceList.containsKey(uuid) ? 0 : priceList.get(uuid);
	}
	
	private static HashMap<UUID, ItemStack> itemList = new HashMap<>();
	
	public static void setItemStack(UUID uuid, ItemStack item) {
		itemList.put(uuid, item);
	}
	
	public static ItemStack getItemStack(UUID uuid) {
		return itemList.get(uuid);
	}
	
	private static ItemStack getItem(UUID uuid) {
		ItemStack item = itemList.get(uuid).clone();
		ItemMeta meta = item.getItemMeta();
		
		List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
		
		if(Utils.setDefaultData("resources/languages/en_US.yml", Config.getLangFile(), "menus.sales.items.item_changed"))
			Config.reloadLang();
		
		Config.getLang().getStringList("menus.sales.items.item_changed").forEach(l -> lore.add(ChatColor.translateAlternateColorCodes('&', l)));;
		meta.setLore(lore);
		
		item.setItemMeta(meta);
		return item;
	}
}
