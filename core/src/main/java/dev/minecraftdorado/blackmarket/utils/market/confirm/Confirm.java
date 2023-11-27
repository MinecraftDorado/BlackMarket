package dev.minecraftdorado.blackmarket.utils.market.confirm;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.inventory.InventoryManager.Inv;
import dev.minecraftdorado.blackmarket.utils.market.BlackItem;

public class Confirm {
	
	public static String getTitle() {
		return Config.getMessage("menus.confirm.title");
	}
	
	public static Inv getInventory(Player player, BlackItem bItem) {
		Inv inv = new Inv(getTitle(), 6);
		
		inv.setBackgroud(Config.getConfirmMenuBackground(), false);
		inv.setBackgroud(Config.getConfirmMenuBorder(), true);
		
		ItemStack buy = Config.getItemStack("confirm.buy", "menus.confirm.items.buy");
		ItemMeta meta = buy.getItemMeta();
		List<String> lore = meta.hasLore() ? meta.getLore() : null;
		if(lore != null)
			for (int i = 0; i < lore.size(); i++) {
				String l = lore.get(i);
				if(l.contains("%owner%")) l = l.replace("%owner%", Bukkit.getOfflinePlayer(bItem.getOwner()) != null ? Bukkit.getOfflinePlayer(bItem.getOwner()).getName() : "§cUnknown");
				if(l.contains("%value%")) l = l.replace("%value%", bItem.getValue() + "");
				lore.set(i, l);
			}
		meta.setLore(lore);
		buy.setItemMeta(meta);
		
		inv.setItem(Config.getSlot("confirm.buy"), buy);
		inv.setItem(Config.getSlot("confirm.cancel"), Config.getItemStack("confirm.cancel", "menus.confirm.items.cancel"));
		
		inv.addBlackItem(bItem, Config.getSlot("confirm.buy"));
		inv.setItem(Config.getSlot("confirm.item"), bItem.getOriginal());
		inv.setItem(Config.getSlot("confirm.back"), Config.getItemStack("confirm.back", "menus.confirm.items.back"));
		
		return inv;
	}
}
