package dev.minecraftdorado.BlackMarket.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.minecraftdorado.BlackMarket.MainClass.MainClass;

public class Config {
	
	private static HashMap<String, ItemStack> items = new HashMap<>();
	private static HashMap<String, String> msgs = new HashMap<>();
	private static List<String> desc;
	private static YamlConfiguration msgFile;
	private static int expiredTime, limit;
	
	public Config() {
		load();
	}
	
	public static void reload() {
		MainClass.main.reloadConfig();
		items.clear();
		msgs.clear();
		desc = null;
		load();
	}
	
	public static void load() {
		if(!new File(MainClass.main.getDataFolder(), "config.yml").exists())
			MainClass.main.saveDefaultConfig();
		
		File file = new File(MainClass.main.getDataFolder(), "config.yml");
		YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
		
		yml.getConfigurationSection("items").getKeys(false).forEach(key -> {
			items.put(key, Utils.getItemStack(file, "items." + key));
		});
		
		Utils.orderFormat = yml.isSet("order.format") ? yml.getString("order.format") : "%active% %value%";
		
		desc = yml.getStringList("item_onsale");
		expiredTime = yml.isSet("expired_time") ? yml.getInt("expired_time") : 1440;
		limit = yml.isSet("limit") ? yml.getInt("limit") : 5;
		
		if(!new File(MainClass.main.getDataFolder(), "messages.yml").exists())
			Utils.extract("resources/messages.yml", "messages.yml");
		
		msgFile = YamlConfiguration.loadConfiguration(new File(MainClass.main.getDataFolder(), "messages.yml"));
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
				Bukkit.getConsoleSender().sendMessage("§6[BlackMarket] §c» Message not found: " + key);
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

	public static int getLimit() {
		return limit;
	}
}
