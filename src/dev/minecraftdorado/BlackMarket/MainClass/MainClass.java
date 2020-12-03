package dev.minecraftdorado.BlackMarket.MainClass;

import java.io.File;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import dev.minecraftdorado.BlackMarket.Commands.bm;
import dev.minecraftdorado.BlackMarket.Commands.bmTab;
import dev.minecraftdorado.BlackMarket.Commands.sell;
import dev.minecraftdorado.BlackMarket.Listeners.MarketListener;
import dev.minecraftdorado.BlackMarket.Listeners.PlayerListener;
import dev.minecraftdorado.BlackMarket.Listeners.StorageListener;
import dev.minecraftdorado.BlackMarket.Utils.Packets.PacketReader;
import dev.minecraftdorado.BlackMarket.Utils.Packets.Reflections;
import net.milkbowl.vault.economy.Economy;
import dev.minecraftdorado.BlackMarket.Utils.Config;
import dev.minecraftdorado.BlackMarket.Utils.UpdateChecker;
import dev.minecraftdorado.BlackMarket.Utils.UpdateChecker.UpdateReason;
import dev.minecraftdorado.BlackMarket.Utils.Utils;
import dev.minecraftdorado.BlackMarket.Utils.Entities.Hologram.HologramManager;
import dev.minecraftdorado.BlackMarket.Utils.Entities.NPC.NPCManager;
import dev.minecraftdorado.BlackMarket.Utils.Entities.NPC.Skins.SkinData;
import dev.minecraftdorado.BlackMarket.Utils.Hook.VaultHook;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.InventoryManager;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils.BlackList;
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
		
		if (!VaultHook.setupEconomy() ) {
            getLogger().severe(String.format("» Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
		
		UpdateChecker.init(this, 85558).requestUpdateCheck().whenComplete((result, e) -> {
			if (result.requiresUpdate()) {
		        this.getLogger().info(String.format("An update is available! BlackMarket %s may be downloaded on SpigotMC", result.getNewestVersion()));
		        return;
		    }
			
			UpdateReason reason = result.getReason();
			if (reason == UpdateReason.UNRELEASED_VERSION)
				this.getLogger().info(String.format("Your version of BlackMarket (%s) is more recent than the one publicly available. Are you on a development build?", result.getNewestVersion()));
			else if (reason != UpdateReason.UP_TO_DATE)
				this.getLogger().warning("Could not check for a new version of BlackMarket. Reason: " + reason);
			}
		);
		
		getServer().getPluginCommand("bm").setExecutor(new bm());
		getServer().getPluginCommand("bm").setTabCompleter(new bmTab());
		
		new SkinData();
		new Config();
		new PlayerData();
		new CategoryUtils();
		new Market();
		new BlackList();
		
		if(Config.blackListIsEnable())
			BlackList.load();
		
		if(!Config.getSellAlias().isEmpty())
			try {
				Class<?> CraftServer = Reflections.getOBCClass("CraftServer");
				Method getCommandMap = CraftServer.getMethod("getCommandMap");
				((SimpleCommandMap) getCommandMap.invoke(CraftServer.cast(getServer()))).register(Config.getSellAlias().get(0), new sell(Config.getSellAlias().get(0)));
			}catch(Exception ex) {
				ex.printStackTrace();
			}
		
		hm = new HologramManager();
		npcM = new NPCManager();
		
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getPluginManager().registerEvents(new MarketListener(), this);
		Bukkit.getPluginManager().registerEvents(new StorageListener(), this);
		Bukkit.getPluginManager().registerEvents(new InventoryManager(), this);
		Bukkit.getOnlinePlayers().forEach(player -> PacketReader.get(player).inject());

		if(!new File(MainClass.main.getDataFolder(), "translations.rar").exists())
			Utils.extract("resources/translations.rar", "translations.rar");
	}
	
	public void onDisable() {
		InventoryManager.closeInventory();
		Bukkit.getOnlinePlayers().forEach(player -> {
			if(npcM != null)
				npcM.list.values().forEach(npc ->{
				npc.hide(player);
				npc.getNameEntity().hide(player);
			});
			PacketReader.get(player).uninject();
		});
		PlayerData.save();
		if(npcM != null) npcM.saveAll();
	}
}
