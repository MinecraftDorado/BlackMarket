package dev.minecraftdorado.blackmarket.listeners;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;

import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.inventory.InventoryManager;
import dev.minecraftdorado.blackmarket.utils.inventory.events.InventoryClickEvent;
import dev.minecraftdorado.blackmarket.utils.market.BlackItem;
import dev.minecraftdorado.blackmarket.utils.market.Market;
import dev.minecraftdorado.blackmarket.utils.market.PlayerData;
import dev.minecraftdorado.blackmarket.utils.market.BlackItem.Status;
import dev.minecraftdorado.blackmarket.utils.market.confirm.Confirm;
import dev.minecraftdorado.blackmarket.utils.market.content.Content;

public class ContentListener implements Listener {
	
	@EventHandler
	private void invClick(InventoryClickEvent e) {
		if(e.getInv().getTitle().equals(Content.getTitle()) && e.usingCustomInv()) {
			Player p = e.getPlayer();
			UUID uuid = p.getUniqueId();
			
			// ItemStack "back"
			if(e.getItemStack().equals(Config.getItemStack("content.back", "menus.content.items.back"))) {
				PlayerData.get(uuid).setCategory(null);
				Market.setPlayerPage(uuid, 0);
				InventoryManager.openInventory(p, Market.getInventory(p));
				return;
			}
			
			if(!e.getInv().getBlackList().isEmpty() && e.getInv().getBlackList().keySet().contains(e.getSlot())) {
				BlackItem bItem = e.getInv().getBlackList().get(e.getSlot());
				
				// remove
				if(bItem.getOwner().equals(uuid) || p.hasPermission("blackmarket.remove_item"))
					if(e.getAction().equals(InventoryAction.PICKUP_HALF) && bItem.getStatus().equals(Status.ON_SALE)) {
						bItem.setStatus(Status.TIME_OUT);
						
						Config.sendMessage("market.cancel_post", p);
						
						Market.setPlayerPage(uuid, 0);
						InventoryManager.openInventory(p, Market.getInventory(p));
						
						return;
					}
				
				// buy
				if(!bItem.getOwner().equals(uuid))
					if(Config.confirmMenuIsEnable()) {
						InventoryManager.openInventory(p, Confirm.getInventory(p, bItem));
						return;
					}else
						bItem.buy(p);
				else
					Config.sendMessage("market.item_owner", p);
			}
		}
	}
}
