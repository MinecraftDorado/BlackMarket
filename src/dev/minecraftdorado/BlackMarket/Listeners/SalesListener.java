package dev.minecraftdorado.BlackMarket.Listeners;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import dev.minecraftdorado.BlackMarket.MainClass.MainClass;
import dev.minecraftdorado.BlackMarket.Utils.Config;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.InventoryManager;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Events.InventoryClickEvent;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils.BlackList;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils.UMaterial;
import dev.minecraftdorado.BlackMarket.Utils.Market.BlackItem;
import dev.minecraftdorado.BlackMarket.Utils.Market.Market;
import dev.minecraftdorado.BlackMarket.Utils.Market.PlayerData;
import dev.minecraftdorado.BlackMarket.Utils.Market.sell.Sales;

public class SalesListener implements Listener {
	
	private ArrayList<UUID> list = new ArrayList<>();
	
	@EventHandler
	private void invClick(InventoryClickEvent e) {
		if(e.getInv().getTitle().equals(Sales.getTitle())) {
			Player p = e.getPlayer();
			UUID uuid = p.getUniqueId();
			
			// ItemStack "back"
			if(e.getItemStack().equals(Config.getItemStack("sales.back", "menus.sales.items.back"))) {
				PlayerData.get(uuid).setCategory(null);
				Market.setPlayerPage(uuid, 0);
				InventoryManager.openInventory(p, Market.getInventory(p));
				return;
			}
			// ItemStack "value"
			if(e.getItemStack().equals(Config.getItemStack("sales.value", "menus.sales.items.value", p))) {
				Config.sendMessage("sales.value", p);
				list.add(uuid);
				p.closeInventory();
				return;
			}
			
			// ItemStack "Post"
			if(e.getItemStack().equals(Config.getItemStack("sales.post", "menus.sales.items.post", p))) {
				if(Sales.getPrice(uuid) >= Config.getMinimumPrice()) {
					if(Sales.getItemStack(uuid) != null) {
						BlackItem bItem = new BlackItem(Sales.getItemStack(uuid), Sales.getPrice(uuid), uuid);
						
						if(PlayerData.get(uuid).addItem(bItem)) {
							Config.sendMessage("command.sell.message", p);
							p.getInventory().removeItem(Sales.getItemStack(uuid));
							p.closeInventory();
						}else
							Config.sendMessage("command.sell.error_limit", p);
					}else
						Config.sendMessage("sales.item_not_found", p);
				}else
					p.sendMessage(Config.getMessage("command.sell.error_minimum_price").replace("%price%", Config.getMinimumPrice() + ""));
				return;
			}
			
			// ItemStack "Item"
			if(!e.usingCustomInv())
				if(!Config.blackListIsEnable() || BlackList.isAllow(UMaterial.match(e.getItemStack()))) {
					Sales.setItemStack(p.getUniqueId(), e.getItemStack());
					InventoryManager.updateInventory(p, Sales.getInventory(p));
				}else
					Config.sendMessage("command.sell.error_item_not_allow", p);
		}
	}
	
	@EventHandler
	private void chat(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		if(list.contains(p.getUniqueId())) {
			String s = e.getMessage();
			e.setCancelled(true);
			try {
				double value = Double.parseDouble(s);
				
				if(value >= Config.getMinimumPrice()) {
					Sales.setPrice(p.getUniqueId(), value);
					Bukkit.getScheduler().runTask(MainClass.main, () -> InventoryManager.openInventory(p, Sales.getInventory(p)));
					list.remove(p.getUniqueId());
				}else
					p.sendMessage(Config.getMessage("command.sell.error_minimum_price").replace("%price%", Config.getMinimumPrice() + ""));				
			}catch (Exception ex) {
				Config.sendMessage("command.sell.error_value", p);
			}
		}
	}
}
