package dev.minecraftdorado.BlackMarket.MainClass;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import dev.minecraftdorado.BlackMarket.Commands.cmd;
import dev.minecraftdorado.BlackMarket.Utils.Packets.PacketReader;
import dev.minecraftdorado.BlackMarket.Utils.Entities.Hologram.Hologram;
import dev.minecraftdorado.BlackMarket.Utils.Entities.Hologram.HologramManager;
import dev.minecraftdorado.BlackMarket.Utils.Entities.NPC.NPC;
import dev.minecraftdorado.BlackMarket.Utils.Entities.NPC.NPCManager;
import dev.minecraftdorado.BlackMarket.Utils.Entities.NPC.Events.NPCInteractEvent;
import dev.minecraftdorado.BlackMarket.Utils.Entities.NPC.Skins.SkinData;

public class MainClass extends JavaPlugin {
	
	public static MainClass main;
	public static HologramManager hm;
	public static NPCManager npcM;
	
	public void onEnable() {
		main = this;
		hm = new HologramManager();
		
		getServer().getPluginCommand("bm").setExecutor(new cmd());
		
		Hologram holo = new Hologram(new Location(Bukkit.getWorld("world"), 0,100,0), "DEBUG");
		hm.add(holo);
		
		npcM = new NPCManager();
		
		new SkinData();
		
		NPC npc = new NPC("Hello World §e!");
		npc.setSkin(SkinData.getSkin("skin_fisher"));
		npc.spawn(new Location(Bukkit.getWorld("world"), 5, 100, 0));
		
		npcM.add(npc);
		
		Bukkit.getPluginManager().registerEvents(new Listener() {
			@EventHandler
			private void a(PlayerJoinEvent e) {
				new PacketReader(e.getPlayer()).inject();
			}
			@EventHandler
			private void a(NPCInteractEvent e) {
				e.getPlayer().sendMessage("§c" + e.getNPC().getName());
			}
		}, this);
		
	}
}
