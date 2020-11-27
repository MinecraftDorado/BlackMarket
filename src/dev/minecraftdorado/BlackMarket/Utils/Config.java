package dev.minecraftdorado.BlackMarket.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.minecraftdorado.BlackMarket.MainClass.MainClass;
import dev.minecraftdorado.BlackMarket.Utils.Entities.NPC.NPC;
import dev.minecraftdorado.BlackMarket.Utils.Entities.NPC.Skins.SkinData;

public class Config {
	
	private static HashMap<String, ItemStack> items = new HashMap<>();
	private static HashMap<String, String> msgs = new HashMap<>();
	private static List<String> desc;
	private static YamlConfiguration conf, msgFile;
	private static int expiredTime, defaultLimit, taxes;
	private static double minimum_price;
	private static ArrayList<NPC> npcs = new ArrayList<>();
	private static ItemStack market_background, market_border, storage_background, storage_border;
	private static StorageType storageType;
	private static boolean blacklist_enable;
	
	public Config() {
		load();
	}
	
	public static void reload() {
		MainClass.main.reloadConfig();
		items.clear();
		msgs.clear();
		desc = null;
		load();
		
		for(NPC npc : MainClass.npcM.list.values())
			npc.respawn();
	}
	
	public static void load() {
		File file = new File(MainClass.main.getDataFolder(), "config.yml");
		
		if(!file.exists())
			Utils.extract("config.yml", "config.yml");
		
		conf = YamlConfiguration.loadConfiguration(file);
		
		conf.getConfigurationSection("items").getKeys(false).forEach(key -> {
			items.put(key, Utils.getItemStack(file, "items." + key));
		});
		
		Utils.orderFormat = conf.isSet("order.format") ? conf.getString("order.format") : "%active% %value%";
		
		desc = conf.getStringList("item_onsale");
		expiredTime = conf.isSet("expired_time") ? conf.getInt("expired_time") : 1440;
		defaultLimit = conf.isSet("limit") ? conf.getInt("limit") : 5;
		taxes = conf.isSet("taxes") ? conf.getInt("taxes") : 7;
		minimum_price = conf.isSet("minimum_price") ? conf.getDouble("minimum_price") : 1.0;
		
		market_background = conf.isSet("menus.market.background") ? Utils.getMaterial(conf.getString("menus.market.background")) : Utils.getMaterial("GRAY_STAINED_GLASS_PANE");
		market_border = conf.isSet("menus.market.border") ? Utils.getMaterial(conf.getString("menus.market.border")) : Utils.getMaterial("BLACK_STAINED_GLASS_PANE");
		storage_background = conf.isSet("menus.storage.background") ? Utils.getMaterial(conf.getString("menus.storage.background")) : Utils.getMaterial("GRAY_STAINED_GLASS_PANE");
		storage_border= conf.isSet("menus.storage.border") ? Utils.getMaterial(conf.getString("menus.storage.border")) : Utils.getMaterial("BLACK_STAINED_GLASS_PANE");
		
		storageType = conf.isSet("mysql.enable") ? conf.getBoolean("mysql.enable") ? StorageType.MySQL : StorageType.File : StorageType.File;
		
		blacklist_enable = conf.isSet("blacklist_enable") ? conf.getBoolean("blacklist_enable") : false;
		
		File msg = new File(MainClass.main.getDataFolder(), "messages.yml");
		
		if(!msg.exists())
			Utils.extract("resources/messages.yml", "messages.yml");
		
		msgFile = YamlConfiguration.loadConfiguration(msg);
		
		if(conf.isSet("npc_list"))
			for(String s : conf.getStringList("npc_list")) {
				String[] args = s.split(",");
				
				Location loc = new Location(Bukkit.getWorld(args[0]), Double.valueOf(args[1]), Double.valueOf(args[2]), Double.valueOf(args[3]));
				
				NPC npc = new NPC(loc);
				if(args.length == 5)
					npc.setSkin(SkinData.getSkin(args[4]));
				npcs.add(npc);
			}
	}
	
	public static YamlConfiguration getYml() {
		return msgFile;
	}
	
	public static ItemStack getItemStack(String key) {
		if(items.containsKey(key))
			return items.get(key).clone();
		return new ItemStack(Material.BARRIER);
	}
	
	public static ItemStack getItemStack(String key, Player player) {
		ItemStack item = getItemStack(key).clone();
		ItemMeta meta = item.getItemMeta();
		if(meta.hasLore()) {
			ArrayList<String> lore = new ArrayList<>();
			meta.getLore().forEach(l -> {
				lore.add(Utils.applyVariables(l, player));
			});
			meta.setLore(lore);
		}
		if(meta.hasDisplayName())
			meta.setDisplayName(Utils.applyVariables(meta.getDisplayName(), player));
		item.setItemMeta(meta);
		return item;
	}
	
	public static String getMessage(String key) {
		if(!msgs.containsKey(key))
			if(msgFile.isSet(key))
				msgs.put(key, ChatColor.translateAlternateColorCodes('&', msgFile.getString(key)));
			else {
				MainClass.main.getLogger().severe(String.format("» Message not found: " + key, MainClass.main.getDescription().getName()));
				return "";
			}
		return msgs.get(key);
	}
	
	public static void sendMessage(String key, Player player) {
		player.sendMessage(getMessage(key));
	}
	
	public static void sendMessage(String key, CommandSender sender) {
		sender.sendMessage(getMessage(key));
	}
	
	public static List<String> getDesc() {
		return desc;
	}

	public static int getExpiredTime() {
		return expiredTime;
	}

	public static int getDefaultLimit() {
		return defaultLimit;
	}
	
	public static ArrayList<NPC> getNPCs(){
		return npcs;
	}
	
	public static void saveNPCs(Collection<NPC> list) {
		npcs.clear();
		File file = new File(MainClass.main.getDataFolder(), "config.yml");
		YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
		
		List<String> l = new ArrayList<>();
		
		for(NPC npc : list) {
			Location loc = npc.getLocation();
			l.add(loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() +(npc.getSkin() != null ?  "," + npc.getSkin().getName() : ""));
			npcs.add(npc);
		}
		yml.set("npc_list", l);
		try {
			yml.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static int getTaxes() {
		return taxes;
	}
	
	public static ItemStack getMarketBackground() {
		return market_background;
	}

	public static ItemStack getMarketBorder() {
		return market_border;
	}
	
	public static ItemStack getStorageBackground() {
		return storage_background;
	}
	
	public static ItemStack getStorageBorder() {
		return storage_border;
	}
	
	public static double getMinimumPrice() {
		return minimum_price;
	}
	
	public static StorageType getStorageType() {
		return storageType;
	}
	
	public enum StorageType {
		File, MySQL
	}
	
	public static boolean blackListIsEnable() {
		return blacklist_enable;
	}
	
	public static String getString(String str) {
		if(conf.isSet(str))
			return ChatColor.translateAlternateColorCodes('&', conf.getString(str));
		MainClass.main.getLogger().severe(String.format("» String not found: " + str, MainClass.main.getDescription().getName()));
		return str;
	}
}
