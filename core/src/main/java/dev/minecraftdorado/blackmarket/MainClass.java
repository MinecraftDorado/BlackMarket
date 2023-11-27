package dev.minecraftdorado.blackmarket;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import dev.minecraftdorado.blackmarket.commands.bm;
import dev.minecraftdorado.blackmarket.commands.bmTab;
import dev.minecraftdorado.blackmarket.listeners.ConfirmListener;
import dev.minecraftdorado.blackmarket.listeners.ContentListener;
import dev.minecraftdorado.blackmarket.listeners.MarketListener;
import dev.minecraftdorado.blackmarket.listeners.PlayerListener;
import dev.minecraftdorado.blackmarket.listeners.SalesListener;
import dev.minecraftdorado.blackmarket.listeners.StorageListener;
import dev.minecraftdorado.blackmarket.utils.CommandAliasAbs;
import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.ReflectionUtils;
import dev.minecraftdorado.blackmarket.utils.UpdateChecker;
import dev.minecraftdorado.blackmarket.utils.Utils;
import dev.minecraftdorado.blackmarket.utils.UpdateChecker.UpdateReason;
import dev.minecraftdorado.blackmarket.utils.economy.EconomyManager;
import dev.minecraftdorado.blackmarket.utils.entities.hologram.HologramManager;
import dev.minecraftdorado.blackmarket.utils.entities.npc.NPCManager;
import dev.minecraftdorado.blackmarket.utils.entities.npc.skins.SkinData;
import dev.minecraftdorado.blackmarket.utils.inventory.InventoryManager;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.BlackList;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.BlackListLore;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.CategoryUtils;
import dev.minecraftdorado.blackmarket.utils.market.Market;
import dev.minecraftdorado.blackmarket.utils.market.PlayerData;
import dev.minecraftdorado.blackmarket.utils.metrics.Metrics;
import dev.minecraftdorado.blackmarket.utils.metrics.custom.CustomMetrics;

public class MainClass extends JavaPlugin {
	
	public static MainClass main;
	public static HologramManager hm;
	public static NPCManager npcM;
	
	public void onEnable() {
		main = this;
		
		new SkinData();
		new Config();
		
		EconomyManager em = new EconomyManager();
		
		if(!em.hasEconomy()) {
			getLogger().severe(String.format("» Disabled due to no Economy plugin dependency found!", getDescription().getName()));
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
		
		new CategoryUtils();
		new PlayerData();
		new Market();
		new BlackList();
		new BlackListLore();
		
		Metrics metrics = new Metrics(this, 10119);
		new CustomMetrics(metrics);
		
		if(!Config.getSellAlias().isEmpty()) {
			CommandAliasAbs commandAlias = (CommandAliasAbs) ReflectionUtils.getClass("utils.CommandAlias");
			commandAlias.addAlias(Config.getSellAlias());
		}
		
		hm = new HologramManager();
		npcM = new NPCManager();
		
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getPluginManager().registerEvents(new MarketListener(), this);
		Bukkit.getPluginManager().registerEvents(new StorageListener(), this);
		Bukkit.getPluginManager().registerEvents(new SalesListener(), this);
		Bukkit.getPluginManager().registerEvents(new ContentListener(), this);
		Bukkit.getPluginManager().registerEvents(new ConfirmListener(), this);
		Bukkit.getPluginManager().registerEvents(new InventoryManager(), this);
		
		Bukkit.getOnlinePlayers().forEach(player -> {
			Utils.injectPacketReader(player);
			
			npcM.list.values().forEach(npc -> {
				npc.display(player);
			});
		});
	}
	
	public void onDisable() {
		InventoryManager.closeInventory();
		
		npcM.list.values().forEach(npc -> {
			npc.hide();
		});
		npcM.saveAll();
		npcM.list.clear();
		npcM.npcList.clear();
		
		PlayerData.save();
	}
}
