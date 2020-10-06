package dev.minecraftdorado.BlackMarket.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import dev.minecraftdorado.BlackMarket.MainClass.MainClass;
import dev.minecraftdorado.BlackMarket.Utils.Config;
import dev.minecraftdorado.BlackMarket.Utils.Utils;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.InventoryManager;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Events.InventoryClickEvent;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils.CategoryUtils;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils.CategoryUtils.Category;
import dev.minecraftdorado.BlackMarket.Utils.Market.BlackItem;
import dev.minecraftdorado.BlackMarket.Utils.Market.Market;
import dev.minecraftdorado.BlackMarket.Utils.Market.PlayerData;
import dev.minecraftdorado.BlackMarket.Utils.Market.Storage;
import dev.minecraftdorado.BlackMarket.Utils.Market.BlackItem.Status;

public class MarketListener implements Listener {
	
	@EventHandler
	private void invClick(InventoryClickEvent e) {
		if(e.getInv().getTitle().equals(Market.getMarketTitle())) {
			// ItemStack "previous"
			if(e.getItemStack().equals(Config.getItemStack("previous", e.getPlayer()))) {
				Market.setPlayerPage(e.getPlayer().getUniqueId(), Market.getPlayerPage(e.getPlayer().getUniqueId())-1);
				InventoryManager.openInventory(e.getPlayer(), Market.getMarketInventory(e.getPlayer()));
				return;
			}
			// ItemStack "next"
			if(e.getItemStack().equals(Config.getItemStack("next", e.getPlayer()))) {
				Market.setPlayerPage(e.getPlayer().getUniqueId(), Market.getPlayerPage(e.getPlayer().getUniqueId())+1);
				InventoryManager.openInventory(e.getPlayer(), Market.getMarketInventory(e.getPlayer()));
				return;
			}
			// ItemStack "storage"
			if(e.getItemStack().equals(Config.getItemStack("storage", e.getPlayer()))) {
				InventoryManager.openInventory(e.getPlayer(), Storage.getStorageInventory(e.getPlayer()));
				return;
			}
			// Select category
			Category cat = PlayerData.get(e.getPlayer().getUniqueId()).getCategory();
			CategoryUtils.getCategories().forEach(category -> {
				if(e.getItemStack().equals(category.getItemStack(category.equals(cat)))) {
					PlayerData.get(e.getPlayer().getUniqueId()).setCategory(category);
					Market.setPlayerPage(e.getPlayer().getUniqueId(), 0);
					InventoryManager.updateInventory(e.getPlayer(), Market.getMarketInventory(e.getPlayer()));
					return;
				}
			});
			// Item on sale
			if(!e.getInv().getBlackList().isEmpty() && e.getInv().getBlackList().keySet().contains(e.getSlot())) {
				BlackItem bItem = e.getInv().getBlackList().get(e.getSlot());
				if(!bItem.getOwner().equals(e.getPlayer().getUniqueId()))
					if(MainClass.econ.has(e.getPlayer(), bItem.getValue()))
						if(bItem.getStatus().equals(Status.ON_SALE)) {
							if(Utils.canAddItem(e.getPlayer(), bItem.getOriginal())) {
								bItem.setStatus(Status.SOLD);
								e.getPlayer().getInventory().addItem(bItem.getOriginal());
								e.getPlayer().closeInventory();
								e.getPlayer().sendMessage("§eThanks for buy!");
								
								MainClass.econ.withdrawPlayer(e.getPlayer(), bItem.getValue());
								MainClass.econ.depositPlayer(Bukkit.getOfflinePlayer(bItem.getOwner()), bItem.getValue());
								return;
							}
							e.getPlayer().sendMessage("§cInventory full!");
						}else
							e.getPlayer().sendMessage("§cThis item is not for sale!");
					else
						e.getPlayer().sendMessage("§cYou haven't a money to buy this!");
				else
					e.getPlayer().sendMessage("§cYou can't buy your items!");
			}
		}
	}
}
