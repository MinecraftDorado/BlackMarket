package dev.minecraftdorado.BlackMarket.Listeners;

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

import dev.minecraftdorado.BlackMarket.MainClass.MainClass;
import dev.minecraftdorado.BlackMarket.Utils.Config;
import dev.minecraftdorado.BlackMarket.Utils.UpdateChecker;
import dev.minecraftdorado.BlackMarket.Utils.UpdateChecker.UpdateReason;
import dev.minecraftdorado.BlackMarket.Utils.Entities.NPC.Events.NPCInteractEvent;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.InventoryManager;
import dev.minecraftdorado.BlackMarket.Utils.Market.BlackItem.Status;
import dev.minecraftdorado.BlackMarket.Utils.Market.Market;
import dev.minecraftdorado.BlackMarket.Utils.Market.PlayerData;
import dev.minecraftdorado.BlackMarket.Utils.Packets.PacketReader;

public class PlayerListener implements Listener {
	
	public static ArrayList<UUID> npcRemove = new ArrayList<>();
	
	/*
	 * Inject PacketReader
	 */
	
	@EventHandler
	private void join(PlayerJoinEvent e) {
		PacketReader.get(e.getPlayer()).inject();
		
		if(e.getPlayer().hasPermission("blackmarket.admin"))
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
				}
			);
		
		Bukkit.getScheduler().runTask(MainClass.main, () -> {
			// Sold notifications
			PlayerData.get(e.getPlayer().getUniqueId()).getItems().forEach(bItem -> {
				if(bItem.getStatus().equals(Status.SOLD) && !bItem.isNotified())
					bItem.sendNotification();
			});
		});
	}
	
	/*
	 * Uninject PacketReader
	 */
	
	@EventHandler
	private void leave(PlayerQuitEvent e) {
		PacketReader.get(e.getPlayer()).uninject();
		npcRemove.remove(e.getPlayer().getUniqueId());
	}
	
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
		
		// Set default values
		PlayerData.get(e.getPlayer().getUniqueId()).setCategory(null);
		Market.setPlayerPage(e.getPlayer().getUniqueId(), 0);
		InventoryManager.openInventory(e.getPlayer(), Market.getMarketInventory(e.getPlayer()));
	}
}
