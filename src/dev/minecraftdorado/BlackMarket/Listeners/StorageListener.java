package dev.minecraftdorado.BlackMarket.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import dev.minecraftdorado.BlackMarket.Utils.Config;
import dev.minecraftdorado.BlackMarket.Utils.Utils;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.InventoryManager;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Events.InventoryClickEvent;
import dev.minecraftdorado.BlackMarket.Utils.Market.BlackItem;
import dev.minecraftdorado.BlackMarket.Utils.Market.BlackItem.Status;
import dev.minecraftdorado.BlackMarket.Utils.Market.Market;
import dev.minecraftdorado.BlackMarket.Utils.Market.PlayerData;
import dev.minecraftdorado.BlackMarket.Utils.Market.Storage;

public class StorageListener implements Listener {
	
	@EventHandler
	private void invClick(InventoryClickEvent e) {
		if(e.getInv().getTitle().equals(Storage.getStorageTitle())) {
			Player p = e.getPlayer();
			
			// ItemStack "back"
			if(e.getItemStack().equals(Config.getItemStack("back", p))) {
				PlayerData.get(e.getPlayer().getUniqueId()).setCategory(null);
				Market.setPlayerPage(p.getUniqueId(), 0);
				InventoryManager.openInventory(p, Market.getMarketInventory(p));
				return;
			}
			// Take all items
			if(e.getItemStack().equals(Config.getItemStack("take_items"))) {
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
