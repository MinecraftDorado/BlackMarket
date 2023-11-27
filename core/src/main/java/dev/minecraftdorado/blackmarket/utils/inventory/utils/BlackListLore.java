package dev.minecraftdorado.blackmarket.utils.inventory.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import dev.minecraftdorado.blackmarket.MainClass;
import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.Utils;

public class BlackListLore {
	
	private static File file = new File(MainClass.main.getDataFolder(), "blacklist_lore.yml");
	private static ArrayList<String> list = new ArrayList<>();
	
	public BlackListLore() {
		if(!file.exists())
			Utils.extract("resources/blacklist_lore.yml", "blacklist_lore.yml");
		load();
	}
	
	public static boolean isAllow(List<String> lore) {
		for(String s : lore)
			if(list.contains(s))
				return false;
		return true;
	}
	
	public static void load() {
		if(!Config.blackListLoreIsEnable()) return;
		
		YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
		
		if(yml.isSet("list"))
			yml.getStringList("list").forEach(s -> list.add(ChatColor.translateAlternateColorCodes('&', s)));
	}
	
	public static void reload() {
		list.clear();
		load();
	}
}
