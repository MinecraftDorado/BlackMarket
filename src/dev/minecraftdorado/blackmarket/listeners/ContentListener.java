package dev.minecraftdorado.blackmarket.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;

import dev.minecraftdorado.blackmarket.mainclass.MainClass;
import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.Utils;
import dev.minecraftdorado.blackmarket.utils.inventory.InventoryManager;
import dev.minecraftdorado.blackmarket.utils.inventory.events.InventoryClickEvent;
import dev.minecraftdorado.blackmarket.utils.market.BlackItem;
import dev.minecraftdorado.blackmarket.utils.market.Market;
import dev.minecraftdorado.blackmarket.utils.market.PlayerData;
import dev.minecraftdorado.blackmarket.utils.market.BlackItem.Status;
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
					if(MainClass.econ.has(p, bItem.getValue()))
						if(bItem.getStatus().equals(Status.ON_SALE)) {
							if(Utils.canAddItem(p, bItem.getOriginal())) {
								bItem.setStatus(Status.SOLD);
								p.getInventory().addItem(bItem.getOriginal());
								p.closeInventory();
								Config.sendMessage("market.buy", p);
								
								MainClass.econ.withdrawPlayer(p, bItem.getValue());
								MainClass.econ.depositPlayer(Bukkit.getOfflinePlayer(bItem.getOwner()), bItem.getFinalValue());
								return;
							}
							Config.sendMessage("market.inventory_full", p);
						}else
							Config.sendMessage("market.item_invalid", p);
					else
						Config.sendMessage("market.missing_money", p);
				else
					Config.sendMessage("market.item_owner", p);
			}
		}
	}
}
