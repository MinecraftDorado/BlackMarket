package dev.minecraftdorado.BlackMarket.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import dev.minecraftdorado.BlackMarket.Utils.Entities.NPC.Events.NPCInteractEvent;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.InventoryManager;
import dev.minecraftdorado.BlackMarket.Utils.Market.Market;
import dev.minecraftdorado.BlackMarket.Utils.Market.PlayerData;
import dev.minecraftdorado.BlackMarket.Utils.Packets.PacketReader;

public class PlayerListener implements Listener {
	
	/*
	 * Inject PacketReader
	 */
	
	@EventHandler
	private void join(PlayerJoinEvent e) {PacketReader.get(e.getPlayer()).inject();}
	
	/*
	 * Uninject PacketReader
	 */
	
	@EventHandler
	private void leave(PlayerBedLeaveEvent e) {PacketReader.get(e.getPlayer()).uninject();}
	
	/*
	 * NPC interact
	 */
	
	@EventHandler
	private void interact(NPCInteractEvent e) {
		// Set default values
		PlayerData.get(e.getPlayer().getUniqueId()).setCategory(null);
		Market.setPlayerPage(e.getPlayer().getUniqueId(), 0);
		InventoryManager.openInventory(e.getPlayer(), Market.getMarketInventory(e.getPlayer()));
	}
}
