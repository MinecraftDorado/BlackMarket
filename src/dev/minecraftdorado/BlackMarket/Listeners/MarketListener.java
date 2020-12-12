package dev.minecraftdorado.BlackMarket.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import dev.minecraftdorado.BlackMarket.MainClass.MainClass;
import dev.minecraftdorado.BlackMarket.Utils.Config;
import dev.minecraftdorado.BlackMarket.Utils.Utils;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.InventoryManager;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Events.InventoryClickEvent;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils.CategoryUtils;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils.CategoryUtils.Category;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils.OrderUtils.OrderType;
import dev.minecraftdorado.BlackMarket.Utils.Market.BlackItem;
import dev.minecraftdorado.BlackMarket.Utils.Market.Market;
import dev.minecraftdorado.BlackMarket.Utils.Market.PlayerData;
import dev.minecraftdorado.BlackMarket.Utils.Market.Storage;
import dev.minecraftdorado.BlackMarket.Utils.Market.BlackItem.Status;

public class MarketListener implements Listener {
	
	@EventHandler
	private void invClick(InventoryClickEvent e) {
		if(e.getInv().getTitle().equals(Market.getMarketTitle())) {
			Player p = e.getPlayer();
			
			// ItemStack "previous"
			if(e.getItemStack().equals(Config.getItemStack("market.previous", "menus.market.items.previous", p))) {
				Market.setPlayerPage(p.getUniqueId(), Market.getPlayerPage(p)-1);
				InventoryManager.openInventory(p, Market.getMarketInventory(p));
				return;
			}
			// ItemStack "next"
			if(e.getItemStack().equals(Config.getItemStack("market.next", "menus.market.items.next", p))) {
				Market.setPlayerPage(p.getUniqueId(), Market.getPlayerPage(p)+1);
				InventoryManager.openInventory(p, Market.getMarketInventory(p));
				return;
			}
			// ItemStack "storage"
			if(e.getItemStack().equals(Config.getItemStack("market.storage", "menus.market.items.storage", p))) {
				InventoryManager.openInventory(p, Storage.getStorageInventory(p));
				return;
			}
			// Select category
			Category cat = PlayerData.get(p.getUniqueId()).getCategory();
			CategoryUtils.getCategories().forEach(category -> {
				if(e.getItemStack().equals(category.getItemStack(category.equals(cat)))) {
					PlayerData.get(p.getUniqueId()).setCategory(category);
					Market.setPlayerPage(p.getUniqueId(), 0);
					InventoryManager.updateInventory(p, Market.getMarketInventory(p));
					return;
				}
			});
			// Select order
			if(e.getItemStack().equals(Config.getItemStack("market.order", "menus.market.items.order", p))) {
				OrderType order = PlayerData.get(p.getUniqueId()).getOrder();
				boolean a = false;
				
				if(e.getItemStack().getItemMeta().hasLore()) {
					for (int i = 0; i < e.getItemStack().getItemMeta().getLore().size(); i++) {
						String l = e.getItemStack().getItemMeta().getLore().get(i);
						if(l.contains(Config.getMessage("menus.market.items.order.active"))) {
							a = true;
							continue;
						}
						if(a || i == 0) {
							for(OrderType type : OrderType.values())
								if(l.contains(Config.getMessage("menus.market.items.order.values." + type.getName()))) {
									order = type;
									break;
								}
							if(a)
								break;
						}
					}
					PlayerData.get(p.getUniqueId()).setOrder(order);
					Market.setPlayerPage(p.getUniqueId(), 0);
					InventoryManager.openInventory(p, Market.getMarketInventory(p));
					return;
				}
			}
			// Item on sale
			if(!e.getInv().getBlackList().isEmpty() && e.getInv().getBlackList().keySet().contains(e.getSlot())) {
				BlackItem bItem = e.getInv().getBlackList().get(e.getSlot());
				if(!bItem.getOwner().equals(p.getUniqueId()))
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
