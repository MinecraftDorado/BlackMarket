package dev.minecraftdorado.blackmarket.listeners;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.inventory.InventoryManager;
import dev.minecraftdorado.blackmarket.utils.inventory.events.InventoryClickEvent;
import dev.minecraftdorado.blackmarket.utils.market.BlackItem;
import dev.minecraftdorado.blackmarket.utils.market.Market;
import dev.minecraftdorado.blackmarket.utils.market.PlayerData;
import dev.minecraftdorado.blackmarket.utils.market.confirm.Confirm;

public class ConfirmListener implements Listener {
	
	@EventHandler
	private void invClick(InventoryClickEvent e) {
		if(e.getInv().getTitle().equals(Confirm.getTitle()) && e.usingCustomInv()) {
			Player p = e.getPlayer();
			UUID uuid = p.getUniqueId();
			
			// ItemStack "back"
			if(e.getItemStack().equals(Config.getItemStack("confirm.back", "menus.confirm.items.back"))) {
				PlayerData.get(uuid).setCategory(null);
				Market.setPlayerPage(uuid, 0);
				InventoryManager.openInventory(p, InventoryManager.getHistory(p).get(InventoryManager.getHistory(p).size()-2));
				return;
			}
			
			// ItemStack "cancel"
			if(e.getItemStack().equals(Config.getItemStack("confirm.cancel", "menus.confirm.items.cancel"))) {
				PlayerData.get(uuid).setCategory(null);
				Market.setPlayerPage(uuid, 0);
				InventoryManager.openInventory(p, Market.getInventory(p));
				return;
			}
			
			// ItemStack "buy"
			if(!e.getInv().getBlackList().isEmpty() && e.getInv().getBlackList().keySet().contains(e.getSlot())) {
				BlackItem bItem = e.getInv().getBlackList().get(e.getSlot());
				bItem.buy(p);
			}
		}
	}
}
