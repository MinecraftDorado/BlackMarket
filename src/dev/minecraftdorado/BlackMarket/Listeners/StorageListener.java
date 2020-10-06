package dev.minecraftdorado.BlackMarket.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import dev.minecraftdorado.BlackMarket.Utils.Config;
import dev.minecraftdorado.BlackMarket.Utils.Utils;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.InventoryManager;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Events.InventoryClickEvent;
import dev.minecraftdorado.BlackMarket.Utils.Market.BlackItem;
import dev.minecraftdorado.BlackMarket.Utils.Market.BlackItem.Status;
import dev.minecraftdorado.BlackMarket.Utils.Market.Market;
import dev.minecraftdorado.BlackMarket.Utils.Market.Storage;

public class StorageListener implements Listener {
	
	@EventHandler
	private void invClick(InventoryClickEvent e) {
		if(e.getInv().getTitle().equals(Storage.getStorageTitle())) {
			// ItemStack "back"
			if(e.getItemStack().equals(Config.getItemStack("back", e.getPlayer()))) {
				Market.setPlayerPage(e.getPlayer().getUniqueId(), 0);
				InventoryManager.openInventory(e.getPlayer(), Market.getMarketInventory(e.getPlayer()));
				return;
			}
			// Take item
			if(!e.getInv().getBlackList().isEmpty() && e.getInv().getBlackList().keySet().contains(e.getSlot())) {
				BlackItem bItem = e.getInv().getBlackList().get(e.getSlot());
				
				if(Utils.canAddItem(e.getPlayer(), bItem.getOriginal())){
					bItem.setStatus(Status.TAKED);
					e.getPlayer().getInventory().addItem(bItem.getOriginal());
					e.getPlayer().closeInventory();
				}else
					e.getPlayer().sendMessage("§cInventory full!");
			}
		}
	}
}
