package dev.minecraftdorado.BlackMarket.Listeners;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;

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
import dev.minecraftdorado.BlackMarket.Utils.Market.sell.Sales;
import dev.minecraftdorado.BlackMarket.Utils.Market.BlackItem.Status;

public class MarketListener implements Listener {
	
	private ArrayList<UUID> list = new ArrayList<>();
	
	@EventHandler
	private void invClick(InventoryClickEvent e) {
		if(e.getInv().getTitle().equals(Market.getTitle())) {
			Player p = e.getPlayer();
			UUID uuid = p.getUniqueId();
			
			// ItemStack "previous"
			if(e.getItemStack().equals(Config.getItemStack("market.previous", "menus.market.items.previous", p))) {
				Market.setPlayerPage(uuid, Market.getPlayerPage(p)-1);
				InventoryManager.openInventory(p, Market.getInventory(p));
				return;
			}
			// ItemStack "next"
			if(e.getItemStack().equals(Config.getItemStack("market.next", "menus.market.items.next", p))) {
				Market.setPlayerPage(uuid, Market.getPlayerPage(p)+1);
				InventoryManager.openInventory(p, Market.getInventory(p));
				return;
			}
			// ItemStack "sell"
			if(e.getItemStack().equals(Config.getItemStack("market.sales", "menus.market.items.sales"))) {
				Sales.setItemStack(uuid, null);
				Sales.setPrice(uuid, 0);
				InventoryManager.openInventory(p, Sales.getInventory(p));
				return;
			}
			// ItemStack "storage"
			if(e.getItemStack().equals(Config.getItemStack("market.storage", "menus.market.items.storage"))) {
				InventoryManager.openInventory(p, Storage.getInventory(p));
				return;
			}
			// Select category
			Category cat = PlayerData.get(uuid).getCategory();
			CategoryUtils.getCategories().forEach(category -> {
				if(e.getItemStack().equals(category.getItemStack(category.equals(cat)))) {
					PlayerData.get(uuid).setCategory(category);
					Market.setPlayerPage(uuid, 0);
					InventoryManager.updateInventory(p, Market.getInventory(p));
					return;
				}
			});
			// Select order
			if(e.getItemStack().equals(Config.getItemStack("market.order", "menus.market.items.order", p))) {
				if(e.getAction().equals(InventoryAction.PICKUP_ALL)) {
					OrderType order = null;
					boolean a = false;
					
					if(e.getItemStack().getItemMeta().hasLore())
						if(!list.contains(uuid)) {
							list.add(uuid);
							
							for (int i = 0; i < e.getItemStack().getItemMeta().getLore().size(); i++) {
								String l = e.getItemStack().getItemMeta().getLore().get(i);
								
								if(l.contains(Config.getMessage("menus.market.items.order.active"))) {
									a = true;
									continue;
								}
								if(a || order == null) {
									for(OrderType type : OrderType.values())
										if(l.contains(Config.getMessage("menus.market.items.order.values." + type.getName()))) {
											order = type;
											break;
										}
									if(a && order != null) break;
								}
							}
							
							if(order != null) {
								PlayerData.get(uuid).setOrder(order);
								Market.setPlayerPage(uuid, 0);
								InventoryManager.openInventory(p, Market.getInventory(p));
							}
							
							Bukkit.getScheduler().runTaskLater(MainClass.main, () -> list.remove(uuid), 5L);
							return;
						}
				}
				if(e.getAction().equals(InventoryAction.PICKUP_HALF)) {
					if(!list.contains(uuid)) {
						list.add(uuid);
						
						PlayerData.get(uuid).setReverse(!PlayerData.get(uuid).isReverse());
						Market.setPlayerPage(uuid, 0);
						InventoryManager.openInventory(p, Market.getInventory(p));
						
						Bukkit.getScheduler().runTaskLater(MainClass.main, () -> list.remove(uuid), 5L);
					}
				}
			}
			// Item on sale
			if(!e.getInv().getBlackList().isEmpty() && e.getInv().getBlackList().keySet().contains(e.getSlot())) {
				BlackItem bItem = e.getInv().getBlackList().get(e.getSlot());
				
				
				if(bItem.getOwner().equals(uuid) || p.hasPermission("blackmarket.remove_item"))
					if(e.getAction().equals(InventoryAction.PICKUP_HALF) && bItem.getStatus().equals(Status.ON_SALE)) {
						bItem.setStatus(Status.TIME_OUT);
						
						Config.sendMessage("market.cancel_post", p);
						
						Market.setPlayerPage(uuid, 0);
						InventoryManager.openInventory(p, Market.getInventory(p));
						
						return;
					}
				
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
