package dev.minecraftdorado.blackmarket.listeners;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.inventory.InventoryManager;
import dev.minecraftdorado.blackmarket.utils.inventory.events.InventoryClickEvent;
import dev.minecraftdorado.blackmarket.utils.market.Market;
import dev.minecraftdorado.blackmarket.utils.market.PlayerData;
import dev.minecraftdorado.blackmarket.utils.market.content.Content;

public class ContentListener implements Listener {
	
	@EventHandler
	private void invClick(InventoryClickEvent e) {
		if(e.getInv().getTitle().equals(Content.getTitle())) {
			Player p = e.getPlayer();
			UUID uuid = p.getUniqueId();
			
			// ItemStack "back"
			if(e.getItemStack().equals(Config.getItemStack("content.back", "menus.content.items.back"))) {
				PlayerData.get(uuid).setCategory(null);
				Market.setPlayerPage(uuid, 0);
				InventoryManager.openInventory(p, Market.getInventory(p));
				return;
			}
		}
	}
}
