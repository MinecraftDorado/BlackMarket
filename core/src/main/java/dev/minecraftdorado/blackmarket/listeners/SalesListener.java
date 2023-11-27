package dev.minecraftdorado.blackmarket.listeners;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import dev.minecraftdorado.blackmarket.MainClass;
import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.inventory.InventoryManager;
import dev.minecraftdorado.blackmarket.utils.inventory.events.InventoryClickEvent;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.BlackList;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.BlackListLore;
import dev.minecraftdorado.blackmarket.utils.market.BlackItem;
import dev.minecraftdorado.blackmarket.utils.market.Market;
import dev.minecraftdorado.blackmarket.utils.market.PlayerData;
import dev.minecraftdorado.blackmarket.utils.market.sell.Sales;

public class SalesListener implements Listener {
	
	private static final HashMap<UUID, Integer> list = new HashMap<>();
	
	@EventHandler
	private void invClick(InventoryClickEvent event) {
		if(event.getInv().getTitle().equals(Sales.getTitle())) {
			Player player = event.getPlayer();
			UUID uuid = player.getUniqueId();
			
			if(event.usingCustomInv()) {
				// ItemStack "back"
				if(event.getItemStack().equals(Config.getItemStack("sales.back", "menus.sales.items.back"))) {
					PlayerData.get(uuid).setCategory(null);
					Market.setPlayerPage(uuid, 0);
					InventoryManager.openInventory(player, Market.getInventory(player));
					return;
				}
				// ItemStack "value"
				if(event.getItemStack().equals(Config.getItemStack("sales.value", "menus.sales.items.value", player))) {
					Config.sendMessage("sales.value", player);
					list.put(uuid, 0);
					player.closeInventory();
					return;
				}
				
				// ItemStack "Post"
				if(event.getItemStack().equals(Config.getItemStack("sales.post", "menus.sales.items.post", player))) {
					if(Sales.getPrice(uuid) >= Config.getMinimumPrice())
						if(Sales.getPrice(uuid) <= Config.getMaximumPrice()) {
							if(Sales.getItemStack(uuid) != null) {
								BlackItem bItem = new BlackItem(Sales.getItemStack(uuid), Sales.getPrice(uuid), uuid);
								
								if(PlayerData.get(uuid).addItem(bItem)) {
									Config.sendMessage("command.sell.message", player);
									player.getInventory().removeItem(Sales.getItemStack(uuid));
									player.closeInventory();
									
									Sales.setItemStack(uuid, null);
									Sales.setPrice(uuid, 0);
								}else
									Config.sendMessage("command.sell.error_limit", player);
							}else
								Config.sendMessage("sales.item_not_found", player);
						}else
							player.sendMessage(Config.getMessage("command.sell.error_maximum_price").replace("%price%", Config.getMaximumPrice() + ""));
					else
						player.sendMessage(Config.getMessage("command.sell.error_minimum_price").replace("%price%", Config.getMinimumPrice() + ""));
				}
			}else // ItemStack "Item"
				if(!Config.blackListIsEnable() || BlackList.isAllow(Material.matchMaterial(event.getItemStack().getType().name()))) {
					if(!Config.blackListLoreIsEnable() || !event.getItemStack().hasItemMeta() || !event.getItemStack().getItemMeta().hasLore() || BlackListLore.isAllow(event.getItemStack().getItemMeta().getLore())) {
						Sales.setItemStack(player.getUniqueId(), event.getItemStack());
						InventoryManager.updateInventory(player, Sales.getInventory(player));
					}else
						Config.sendMessage("command.sell.error_lore_not_allow", player);
				}else
					Config.sendMessage("command.sell.error_item_not_allow", player);
		}
	}
	
	@EventHandler
	private void chat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if(list.containsKey(player.getUniqueId())) {
			String message = event.getMessage();
			event.setCancelled(true);
			
			try {
				double value = Double.parseDouble(message);
				
				if(value >= Config.getMinimumPrice()) {
					list.remove(player.getUniqueId());
					Sales.setPrice(player.getUniqueId(), value);
					Bukkit.getScheduler().runTask(MainClass.main, () -> InventoryManager.openInventory(player, Sales.getInventory(player)));
				}else
					player.sendMessage(Config.getMessage("command.sell.error_minimum_price").replace("%price%", Config.getMinimumPrice() + ""));
			}catch (Exception ex) {
				list.put(player.getUniqueId(), list.get(player.getUniqueId()) + 1);
				if(list.get(player.getUniqueId()) == 3) {
					list.remove(player.getUniqueId());
					Sales.setItemStack(player.getUniqueId(), null);
					Sales.setPrice(player.getUniqueId(), 0);
					Config.sendMessage("command.sell.error_value_limit", player);
				}else {
					Config.sendMessage("command.sell.error_value", player);
				}

			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
		list.remove(event.getPlayer().getUniqueId());
		Sales.setItemStack(event.getPlayer().getUniqueId(), null);
		Sales.setPrice(event.getPlayer().getUniqueId(), 0);
	}
	
	public static boolean inList(UUID uuid) {
		return list.containsKey(uuid);
	}
}
