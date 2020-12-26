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
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.minecraftdorado.BlackMarket.MainClass.MainClass;
import dev.minecraftdorado.BlackMarket.Utils.Entities.NPC.NPC;
import dev.minecraftdorado.BlackMarket.Utils.Entities.NPC.Skins.SkinData;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils.UMaterial;

public class Config {
	
	private static HashMap<String, ItemStack> items = new HashMap<>();
	private static HashMap<String, Integer> slots = new HashMap<>();
	private static HashMap<String, String> msgs = new HashMap<>();
	private static List<String> desc;
	private static YamlConfiguration conf, lang;
	private static File langFile;
	private static int expiredTime, defaultLimit, taxes;
	private static double minimum_price;
	private static ArrayList<NPC> npcs = new ArrayList<>();
	private static ItemStack market_background, market_border, storage_background, storage_border, sellmenu_background, sellmenu_border;
	private static StorageType storageType;
	private static boolean blacklist_enable;
	private static ArrayList<String> sellAlias = new ArrayList<>();
	
	public Config() {
		load();
	}
	
	public static void reload() {
		MainClass.main.reloadConfig();
		items.clear();
		msgs.clear();
		slots.clear();
		desc = null;
		load();
		
		for(NPC npc : MainClass.npcM.list.values())
			npc.respawn();
		
		Utils.dataCopy.clear();
	}
	
	public static void load() {
		File file = new File(MainClass.main.getDataFolder(), "config.yml");
		
		if(!file.exists())
			Utils.extract("config.yml", "config.yml");
		
		conf = YamlConfiguration.loadConfiguration(file);
		
		expiredTime = (int) getValue(conf, "expired_time", 1440);
		defaultLimit = (int) getValue(conf, "limit", 5);
		taxes = (int) getValue(conf, "taxes", 7);
		minimum_price = (double) getValue(conf, "minimum_price", 1.0);
		
		market_background = Utils.getMaterial((String) getValue(conf, "menus.market.background", "GRAY_STAINED_GLASS_PANE"));
		market_border = Utils.getMaterial((String) getValue(conf, "menus.market.border", "BLACK_STAINED_GLASS_PANE"));
		storage_background = Utils.getMaterial((String) getValue(conf, "menus.storage.background", "GRAY_STAINED_GLASS_PANE"));
		storage_border= Utils.getMaterial((String) getValue(conf, "menus.storage.border", "BLACK_STAINED_GLASS_PANE"));
		sellmenu_background = Utils.getMaterial((String) getValue(conf, "menus.storage.background", "GRAY_STAINED_GLASS_PANE"));
		sellmenu_border= Utils.getMaterial((String) getValue(conf, "menus.storage.border", "BLACK_STAINED_GLASS_PANE"));
		
		storageType = (boolean) getValue(conf, "mysql.enable", false) ? StorageType.MySQL : StorageType.File;
		
		blacklist_enable = (boolean) getValue(conf, "blacklist_enable", false);
		
		if(conf.isSet("sell_alias"))
			conf.getStringList("sell_alias").forEach(cmd -> sellAlias.add(cmd));
		
		File langs = new File(MainClass.main.getDataFolder(), "languages");
		if(!langs.exists() || langs.listFiles().length == 0)
			for(String lang : new String[]{"en_US","es_ES","tr_TR","pt_BR"})
				Utils.extract("resources/languages/" + lang + ".yml", "languages/" + lang + ".yml");
		
		langFile = new File(MainClass.main.getDataFolder(), "languages/" + (conf.isSet("lang") ? conf.getString("lang"): "en_US") + ".yml");
		
		if(!langFile.exists()) {
			MainClass.main.getLogger().severe(String.format("» Language not found: " + langFile.getName(), MainClass.main.getDescription().getName()));
			langFile = langs.listFiles()[0];
			MainClass.main.getLogger().info(String.format("Language loaded by default " + langFile.getName()));
		}
		
		lang = YamlConfiguration.loadConfiguration(langFile);
		
		Utils.orderFormat = (String) getValue(lang, "menus.market.items.order.format", "%active% %value%");
		desc = lang.getStringList("menus.market.items.item_onsale");
		
		File npcsFile = new File(MainClass.main.getDataFolder(), "npcs.yml");
		if(npcsFile.exists()) {
			YamlConfiguration npcsYml = YamlConfiguration.loadConfiguration(npcsFile);
			if(npcsYml.isSet("npc_list"))
				for(String s : npcsYml.getStringList("npc_list")) {
					String[] args = s.split(",");
					
					Location loc = new Location(Bukkit.getWorld(args[0]), Double.valueOf(args[1]), Double.valueOf(args[2]), Double.valueOf(args[3]));
					
					NPC npc = new NPC(loc);
					if(args.length == 5)
						npc.setSkin(SkinData.getSkin(args[4]));
					npcs.add(npc);
				}
		}
	}
	
	public static YamlConfiguration getLang() {
		return lang;
	}
	
	public static File getLangFile() {
		return langFile;
	}
	
	public static void reloadLang() {
		lang = YamlConfiguration.loadConfiguration(langFile);
	}
	
	private static Object getValue(YamlConfiguration yml, String key, Object valueDefault) {
		return yml.isSet(key) ? yml.get(key) : valueDefault;
	}
	
	public static int getSlot(String typeKey) {
		if(slots.containsKey(typeKey))
			return slots.get(typeKey);
		
		String key = "menus." + typeKey + ".slot";
		
		if(Utils.setDefaultData("config.yml", new File(MainClass.main.getDataFolder(), "config.yml"), key))
			conf = YamlConfiguration.loadConfiguration(new File(MainClass.main.getDataFolder(), "config.yml"));
		
		if(conf.isSet(key))
			return conf.getInt(key) < 54 ? conf.getInt(key) : 53;
		else
			return 0;
	}
	
	public static ItemStack getItemStack(String typeKey, String metaKey) {
		if(items.containsKey(typeKey))
			return items.get(typeKey).clone();
		
		String key = "menus." + typeKey + ".type";
		if(Utils.setDefaultData("config.yml", new File(MainClass.main.getDataFolder(), "config.yml"), key))
			conf = YamlConfiguration.loadConfiguration(new File(MainClass.main.getDataFolder(), "config.yml"));
		
		if(conf.isSet(key)) {
			items.put(typeKey, Utils.applyMeta(Utils.getMaterial(conf.getString(key)), metaKey));
			return items.get(typeKey);
		}else
			return UMaterial.BARRIER.getItemStack();
	}
	
	public static ItemStack getItemStack(String typeKey, String metaKey, Player player) {
		ItemStack item = getItemStack(typeKey, metaKey).clone();
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
		if(msgs.containsKey(key))
			return msgs.get(key);
		
		if(Utils.setDefaultData("resources/languages/en_US.yml", langFile, key))
			reloadLang();
		
		if(lang.isSet(key)) {
			msgs.put(key, ChatColor.translateAlternateColorCodes('&', lang.getString(key)));
			return msgs.get(key);
		}else
			return "";
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
		File file = new File(MainClass.main.getDataFolder(), "npcs.yml");
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
	
	public static ItemStack getSellMenuBackground() {
		return sellmenu_background;
	}
	
	public static ItemStack getSellMenuBorder() {
		return sellmenu_border;
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
	
	public static ArrayList<String> getSellAlias(){
		return sellAlias;
	}
}
