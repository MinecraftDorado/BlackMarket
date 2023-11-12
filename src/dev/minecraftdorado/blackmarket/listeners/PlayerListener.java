package dev.minecraftdorado.blackmarket.listeners;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import dev.minecraftdorado.blackmarket.mainclass.MainClass;
import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.UpdateChecker;
import dev.minecraftdorado.blackmarket.utils.UpdateChecker.UpdateReason;
import dev.minecraftdorado.blackmarket.utils.database.mysql.dbMySQL;
import dev.minecraftdorado.blackmarket.utils.entities.npc.events.NPCInteractEvent;
import dev.minecraftdorado.blackmarket.utils.inventory.InventoryManager;
import dev.minecraftdorado.blackmarket.utils.market.BlackItem.Status;
import dev.minecraftdorado.blackmarket.utils.market.Market;
import dev.minecraftdorado.blackmarket.utils.market.PlayerData;
import dev.minecraftdorado.blackmarket.utils.market.sell.Sales;

public class PlayerListener implements Listener {
	
	public static ArrayList<UUID> npcRemove = new ArrayList<>();
	
	/*
	 * Inject PacketReader
	 */
	
	@EventHandler
	private void join(PlayerJoinEvent e) {
		
		if(e.getPlayer().hasPermission("blackmarket.admin")) {
			UpdateChecker.init(MainClass.main, 79819).requestUpdateCheck().whenComplete((result, ee) -> {
				if (result.requiresUpdate()) {
					e.getPlayer().sendMessage("§6[BlackMarket] §7» " + String.format("An update is available! BlackMarket %s may be downloaded on SpigotMC", result.getNewestVersion()));
					return;
				}
				
				UpdateReason reason = result.getReason();
				if (reason == UpdateReason.UNRELEASED_VERSION)
					e.getPlayer().sendMessage("§6[BlackMarket] §7» " + String.format("Your version of BlackMarket (%s) is more recent than the one publicly available. Are you on a development build?", result.getNewestVersion()));
				else if (reason != UpdateReason.UP_TO_DATE)
					e.getPlayer().sendMessage("§6[BlackMarket] §7» " + "Could not check for a new version of BlackMarket. Reason: " + reason);
			});
			
			if(Config.isCustomLang())
				e.getPlayer().sendMessage("§6[BlackMarket] §7» " + " §bCustom language detected §3(§a" + Config.getLangFile().getName().replace(".yml", "") + "§3)§b. If you want to contribute to the plugin send the language file to §3Demon@4531§b.");
		}
		
		Bukkit.getScheduler().runTaskLater(MainClass.main, () -> {
			// Sold notifications
			if(Config.multiServerIsEnable())
				dbMySQL.checkUnnotified(e.getPlayer().getUniqueId());
			else {
				PlayerData.get(e.getPlayer().getUniqueId()).getItems().forEach(bItem -> {
					if(bItem.getStatus().equals(Status.SOLD) && !bItem.isNotified())
						bItem.sendNotification();
				});
			}
		}, 20);
	}
	
	/*
	 * Uninject PacketReader
	 */
	
	@EventHandler
	private void leave(PlayerQuitEvent e) {
		npcRemove.remove(e.getPlayer().getUniqueId());
	}
	
	/*
	 * Interact
	 */
	
	@EventHandler
	private void interact(PlayerInteractEvent e) {
		if(npcRemove.contains(e.getPlayer().getUniqueId())) {
			npcRemove.remove(e.getPlayer().getUniqueId());
			Config.sendMessage("command.removenpc.invalid", e.getPlayer());
		}
	}
	
	@EventHandler
	private void interact(PlayerInteractAtEntityEvent e) {
		if(npcRemove.contains(e.getPlayer().getUniqueId())) {
			npcRemove.remove(e.getPlayer().getUniqueId());
			Config.sendMessage("command.removenpc.invalid", e.getPlayer());
		}
	}
	
	@EventHandler
	private void interact(PlayerInteractEntityEvent e) {
		if(npcRemove.contains(e.getPlayer().getUniqueId())) {
			npcRemove.remove(e.getPlayer().getUniqueId());
			Config.sendMessage("command.removenpc.invalid", e.getPlayer());
		}
	}
	
	/*
	 * NPC interact
	 */
	
	@EventHandler
	private void interact(NPCInteractEvent e) {
		if(npcRemove.contains(e.getPlayer().getUniqueId())) {
			npcRemove.remove(e.getPlayer().getUniqueId());
			MainClass.npcM.remove(e.getNPC());
			Config.sendMessage("command.removenpc.removed", e.getPlayer());
			return;
		}
		
		if(SalesListener.inList(e.getPlayer().getUniqueId())) {
			Sales.setItemStack(e.getPlayer().getUniqueId(), null);
			Sales.setPrice(e.getPlayer().getUniqueId(), 0);
		}
		
		// Set default values
		PlayerData.get(e.getPlayer().getUniqueId()).setCategory(null);
		Market.setPlayerPage(e.getPlayer().getUniqueId(), 0);
		InventoryManager.openInventory(e.getPlayer(), Market.getInventory(e.getPlayer()));
	}
}
