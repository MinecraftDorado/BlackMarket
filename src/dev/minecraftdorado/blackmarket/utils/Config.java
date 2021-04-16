package dev.minecraftdorado.blackmarket.utils;

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
import org.bukkit.scheduler.BukkitTask;

import dev.minecraftdorado.blackmarket.mainclass.MainClass;
import dev.minecraftdorado.blackmarket.utils.database.mysql.dbMySQL;
import dev.minecraftdorado.blackmarket.utils.economy.EconomyManager.EconomyType;
import dev.minecraftdorado.blackmarket.utils.entities.npc.NPC;
import dev.minecraftdorado.blackmarket.utils.entities.npc.skins.SkinData;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.UMaterial;
import dev.minecraftdorado.blackmarket.utils.market.Market;
import dev.minecraftdorado.blackmarket.utils.market.PlayerData;

public class Config {
	
	private static HashMap<String, ItemStack> items = new HashMap<>();
	private static HashMap<String, Integer> slots = new HashMap<>();
	private static HashMap<String, String> msgs = new HashMap<>();
	private static List<String> desc, remove_desc, content_desc;
	private static YamlConfiguration conf, lang;
	private static File confFile, langFile;
	private static int expiredTime, defaultLimit, taxes;
	private static double minimum_price, maximum_price;
	private static ArrayList<NPC> npcs = new ArrayList<>();
	private static ItemStack market_background, market_border, storage_background, storage_border, sellmenu_background, sellmenu_border, content_border, confirm_background, confirm_border;
	private static StorageType storageType;
	private static boolean blacklist_enable, blacklistlore_enable, confirm_enable, multi_server;
	private static ArrayList<String> sellAlias = new ArrayList<>();
	private static BukkitTask multi_server_task;
	private static EconomyType econType;
	private static String econValue;
	
	public Config() {
		load();
	}
	
	public static void reload() {
		MainClass.main.reloadConfig();
		items.clear();
		msgs.clear();
		slots.clear();
		desc = null;
		if(multi_server_task != null)
			multi_server_task.cancel();
		
		StorageType stype = storageType;
		load();
		if(!stype.equals(storageType) || storageType.equals(StorageType.MySQL) && !multi_server)
			Market.setId(0);
		
		ArrayList<NPC> npcs = new ArrayList<>();
		npcs.addAll(MainClass.npcM.list.values());
		for(NPC npc : npcs)
			npc.respawn();
		
		Utils.dataCopy.clear();
		PlayerData.reload();
	}
	
	public static void load() {
		confFile = new File(MainClass.main.getDataFolder(), "config.yml");
		
		if(!confFile.exists())
			Utils.extract("config.yml", "config.yml");
		
		conf = YamlConfiguration.loadConfiguration(confFile);
		
		expiredTime = (int) getValue("expired_time");
		defaultLimit = (int) getValue("limit");
		taxes = (int) getValue("taxes");
		minimum_price = (double) getValue("minimum_price");
		maximum_price = (double) getValue("maximum_price");
		
		String[] eco = ((String) getValue("economy_type")).split("#");
		
		econType = EconomyType.valueOf(eco[0]);
		econValue = eco.length == 2 ? eco[1] : null;
		
		market_background = Utils.getMaterial((String) getValue("menus.market.background"));
		market_border = Utils.getMaterial((String) getValue("menus.market.border"));
		storage_background = Utils.getMaterial((String) getValue("menus.storage.background"));
		storage_border = Utils.getMaterial((String) getValue("menus.storage.border"));
		sellmenu_background = Utils.getMaterial((String) getValue("menus.sales.background"));
		sellmenu_border = Utils.getMaterial((String) getValue("menus.sales.border"));
		content_border = Utils.getMaterial((String) getValue("menus.content.border"));
		confirm_background = Utils.getMaterial((String) getValue("menus.confirm.background"));
		confirm_border = Utils.getMaterial((String) getValue("menus.confirm.border"));
		
		storageType = (boolean) getValue("mysql.enable") ? StorageType.MySQL : StorageType.File;
		
		blacklist_enable = (boolean) getValue("blacklist_enable");
		blacklistlore_enable = (boolean) getValue("blacklistlore_enable");
		confirm_enable = (boolean) getValue("confirm_menu_enable");
		multi_server = (boolean) getValue("multi_server.enable");
		
		if(conf.isSet("sell_alias"))
			conf.getStringList("sell_alias").forEach(cmd -> sellAlias.add(cmd));
		
		File langs = new File(MainClass.main.getDataFolder(), "languages");
		if(!langs.exists() || langs.listFiles().length == 0)
			for(String lang : getLangList())
				Utils.extract("resources/languages/" + lang + ".yml", "languages/" + lang + ".yml");
		
		langFile = new File(MainClass.main.getDataFolder(), "languages/" + (conf.isSet("lang") ? conf.getString("lang"): "en_US") + ".yml");
		
		if(!langFile.exists()) {
			MainClass.main.getLogger().severe(String.format("» Language not found: " + langFile.getName(), MainClass.main.getDescription().getName()));
			langFile = langs.listFiles()[0];
			MainClass.main.getLogger().info(String.format("Language loaded by default " + langFile.getName()));
		}
		
		lang = YamlConfiguration.loadConfiguration(langFile);
		
		if(isCustomLang()) {
			MainClass.main.getLogger().info(String.format("Custom language detected (" + langFile.getName().replace(".yml", "") + "). If you want to contribute to the plugin send the language file to Demon@4531."));
			Bukkit.getOnlinePlayers().forEach(p -> {
				if(p.hasPermission("blackmarket.admin"))
					p.sendMessage("§6[BlackMarket] §7» " + " §bCustom language detected §3(§a" + Config.getLangFile().getName().replace(".yml", "") + "§3)§b. If you want to contribute to the plugin send the language file to §3Demon@4531§b.");
			});
		}
		
		if(Utils.setDefaultData("resources/languages/en_US.yml", langFile, "menus.market.items.order.format"))
			reloadLang();
		Utils.orderFormat = lang.getString("menus.market.items.order.format");
		
		if(Utils.setDefaultData("resources/languages/en_US.yml", langFile, "menus.market.items.item_onsale"))
			reloadLang();
		desc = lang.getStringList("menus.market.items.item_onsale");
		
		if(Utils.setDefaultData("resources/languages/en_US.yml", langFile, "menus.market.items.item_onsale_remove"))
			reloadLang();
		remove_desc = lang.getStringList("menus.market.items.item_onsale_remove");
		
		if(Utils.setDefaultData("resources/languages/en_US.yml", langFile, "menus.market.items.item_onsale_content"))
			reloadLang();
		content_desc = lang.getStringList("menus.market.items.item_onsale_content");
		
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
		
		if(multiServerIsEnable()) {
			int x = (int) getValue("multi_server.check_new_items")*20;
			multi_server_task = Bukkit.getScheduler().runTaskTimer(MainClass.main, new Runnable() {
				@Override
				public void run() {
					dbMySQL.loadBlackItems();
				}
			}, x, x);
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
	
	public static boolean isCustomLang() {
		boolean customLang = true;
		for(String l : getLangList())
			if(l.equals(langFile.getName().replace(".yml", ""))) {
				customLang = false;
				break;
			}
		return customLang;
	}
	
	private static String[] getLangList(){
		return new String[]{"en_US","es_ES","tr_TR","pt_BR", "ru_RU", "zh_CN", "fr_FR"};
	}
	
	private static Object getValue(String key) {
		if(Utils.setDefaultData("config.yml", confFile, key))
			conf = YamlConfiguration.loadConfiguration(confFile);
		
		return conf.get(key);
	}
	
	public static int getSlot(String typeKey) {
		if(slots.containsKey(typeKey))
			return slots.get(typeKey);
		
		String key = "menus." + typeKey + ".slot";
		
		if(Utils.setDefaultData("config.yml", new File(MainClass.main.getDataFolder(), "config.yml"), key))
			conf = YamlConfiguration.loadConfiguration(confFile);
		
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
			conf = YamlConfiguration.loadConfiguration(confFile);
		
		if(conf.isSet(key)) {
			items.put(typeKey, Utils.applyMeta(Utils.getMaterial(conf.getString(key)), metaKey));
			return items.get(typeKey).clone();
		}else
			return UMaterial.BARRIER.getItemStack().clone();
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
	
	public static List<String> getRemoveDesc() {
		return remove_desc;
	}
	
	public static List<String> getContentDesc() {
		return content_desc;
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
	
	public static ItemStack getContentMenuBorder() {
		return content_border;
	}
	
	public static boolean confirmMenuIsEnable() {
		return confirm_enable;
	}
	
	public static ItemStack getConfirmMenuBackground() {
		return confirm_background;
	}
	
	public static ItemStack getConfirmMenuBorder() {
		return confirm_border;
	}
	
	public static double getMinimumPrice() {
		return minimum_price;
	}
	
	public static double getMaximumPrice() {
		return maximum_price;
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
	
	public static boolean blackListLoreIsEnable() {
		return blacklistlore_enable;
	}
	
	public static boolean multiServerIsEnable() {
		return getStorageType().equals(StorageType.MySQL) && multi_server;
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
	
	public static EconomyType getEconomyType() {
		return econType;
	}
	
	public static String getEconomyValue() {
		return econValue;
	}
}
