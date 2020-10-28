package dev.minecraftdorado.BlackMarket.Listeners;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import dev.minecraftdorado.BlackMarket.MainClass.MainClass;
import dev.minecraftdorado.BlackMarket.Utils.Config;
import dev.minecraftdorado.BlackMarket.Utils.Entities.NPC.Events.NPCInteractEvent;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.InventoryManager;
import dev.minecraftdorado.BlackMarket.Utils.Market.Market;
import dev.minecraftdorado.BlackMarket.Utils.Market.PlayerData;
import dev.minecraftdorado.BlackMarket.Utils.Packets.PacketReader;

public class PlayerListener implements Listener {
	
	public static ArrayList<UUID> npcRemove = new ArrayList<>();
	
	/*
	 * Inject PacketReader
	 */
	
	@EventHandler
	private void join(PlayerJoinEvent e) {PacketReader.get(e.getPlayer()).inject();}
	
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
