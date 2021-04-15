package dev.minecraftdorado.blackmarket.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.Utils;
import dev.minecraftdorado.blackmarket.utils.inventory.InventoryManager;
import dev.minecraftdorado.blackmarket.utils.inventory.events.InventoryClickEvent;
import dev.minecraftdorado.blackmarket.utils.market.BlackItem;
import dev.minecraftdorado.blackmarket.utils.market.Market;
import dev.minecraftdorado.blackmarket.utils.market.PlayerData;
import dev.minecraftdorado.blackmarket.utils.market.Storage;
import dev.minecraftdorado.blackmarket.utils.market.BlackItem.Status;

public class StorageListener implements Listener {
	
	@EventHandler
	private void invClick(InventoryClickEvent e) {
		if(e.getInv().getTitle().equals(Storage.getTitle()) && e.usingCustomInv()) {
			Player p = e.getPlayer();
			
			// ItemStack "back"
			if(e.getItemStack().equals(Config.getItemStack("storage.back", "menus.storage.items.back"))) {
				PlayerData.get(e.getPlayer().getUniqueId()).setCategory(null);
				Market.setPlayerPage(p.getUniqueId(), 0);
				InventoryManager.openInventory(p, Market.getInventory(p));
				return;
			}
			

			// ItemStack "previous"
			if(e.getItemStack().equals(Config.getItemStack("storage.previous", "menus.storage.items.previous", p))) {
				Storage.setPlayerPage(p.getUniqueId(), Storage.getPlayerPage(p)-1);
				InventoryManager.openInventory(p, Storage.getInventory(p));
				return;
			}
			
			// ItemStack "next"
			if(e.getItemStack().equals(Config.getItemStack("storage.next", "menus.storage.items.next", p))) {
				Storage.setPlayerPage(p.getUniqueId(), Storage.getPlayerPage(p)+1);
				InventoryManager.openInventory(p, Storage.getInventory(p));
				return;
			}
			
			// Take all items
			if(e.getItemStack().equals(Config.getItemStack("storage.take_items", "menus.storage.items.take_items"))) {
				boolean taked = false;
				for(BlackItem bItem : e.getInv().getBlackList().values())
					if(bItem.getStatus().equals(Status.TIME_OUT) && Utils.canAddItem(p, bItem.getOriginal())) {
						bItem.setStatus(Status.TAKED);
						p.getInventory().addItem(bItem.getOriginal());
						taked = true;
					}
				if(taked) {
					Config.sendMessage("market.take_items", p);
					p.closeInventory();
				}
				
				return;
			}
			// Take item
			if(!e.getInv().getBlackList().isEmpty() && e.getInv().getBlackList().keySet().contains(e.getSlot())) {
				BlackItem bItem = e.getInv().getBlackList().get(e.getSlot());
				
				if(Utils.canAddItem(p, bItem.getOriginal())){
					bItem.setStatus(Status.TAKED);
					p.getInventory().addItem(bItem.getOriginal());
					p.closeInventory();
				}else
					Config.sendMessage("market.inventory_full", p);
			}
		}
	}
}
