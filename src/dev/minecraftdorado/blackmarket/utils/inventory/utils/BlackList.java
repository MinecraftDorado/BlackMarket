package dev.minecraftdorado.blackmarket.utils.inventory.utils;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import dev.minecraftdorado.blackmarket.mainclass.MainClass;
import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.Utils;

public class BlackList {
	
	private static File file = new File(MainClass.main.getDataFolder(), "blacklist.yml");
	private static ArrayList<Material> list = new ArrayList<>();
	
	public BlackList() {
		if(!file.exists())
			Utils.extract("resources/blacklist.yml", "blacklist.yml");
		load();
	}
	
	public static boolean isAllow(Material umat) {
		return !list.contains(umat);
	}
	
	public static void load() {
		if(!Config.blackListIsEnable()) return;
		
		YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
		
		if(yml.isSet("list"))
			for(String s : yml.getStringList("list")) {
				Material umat = Material.matchMaterial(s);
				if(umat != null)
					list.add(umat);
			}
	}
	
	public static void reload() {
		list.clear();
		load();
	}
}
