package dev.minecraftdorado.BlackMarket.MainClass;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import dev.minecraftdorado.BlackMarket.Commands.bm;
import dev.minecraftdorado.BlackMarket.Commands.bmTab;
import dev.minecraftdorado.BlackMarket.Commands.sell;
import dev.minecraftdorado.BlackMarket.Listeners.MarketListener;
import dev.minecraftdorado.BlackMarket.Listeners.PlayerListener;
import dev.minecraftdorado.BlackMarket.Listeners.StorageListener;
import dev.minecraftdorado.BlackMarket.Utils.Packets.PacketReader;
import net.milkbowl.vault.economy.Economy;
import dev.minecraftdorado.BlackMarket.Utils.Config;
import dev.minecraftdorado.BlackMarket.Utils.Entities.Hologram.HologramManager;
import dev.minecraftdorado.BlackMarket.Utils.Entities.NPC.NPC;
import dev.minecraftdorado.BlackMarket.Utils.Entities.NPC.NPCManager;
import dev.minecraftdorado.BlackMarket.Utils.Entities.NPC.Skins.SkinData;
import dev.minecraftdorado.BlackMarket.Utils.Hook.VaultHook;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.InventoryManager;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils.CategoryUtils;
import dev.minecraftdorado.BlackMarket.Utils.Market.Market;
import dev.minecraftdorado.BlackMarket.Utils.Market.PlayerData;

public class MainClass extends JavaPlugin {
	
	public static MainClass main;
	public static HologramManager hm;
	public static NPCManager npcM;
	
	public static Economy econ;
	
	public void onEnable() {
		main = this;
		
		if(!VaultHook.setupEconomy()) {
			Bukkit.getConsoleSender().sendMessage("§cVault is needed !");
			this.getPluginLoader().disablePlugin(this);
			return;
		}
		
		getServer().getPluginCommand("sell").setExecutor(new sell());
		getServer().getPluginCommand("bm").setExecutor(new bm());
		getServer().getPluginCommand("bm").setTabCompleter(new bmTab());
		
		hm = new HologramManager();
		npcM = new NPCManager();
		
		new SkinData();
		new Config();
		new PlayerData();
		new CategoryUtils();
		new Market();
		
		NPC npc = new NPC("Hello World §e!");
		npc.setSkin(SkinData.getSkin("skin_fisher"));
		npc.spawn(new Location(Bukkit.getWorld("world"), 5, 100, 0));
		
		npcM.add(npc);
		
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getPluginManager().registerEvents(new MarketListener(), this);
		Bukkit.getPluginManager().registerEvents(new StorageListener(), this);
		Bukkit.getPluginManager().registerEvents(new InventoryManager(), this);
		Bukkit.getOnlinePlayers().forEach(player -> PacketReader.get(player).inject());
	}
	
	public void onDisable() {
		InventoryManager.closeInventory();
		Bukkit.getOnlinePlayers().forEach(player -> {
			npcM.list.values().forEach(npc ->{
				npc.hide(player);
			});
			PacketReader.get(player).uninject();
		});
		PlayerData.save();
	}
}
