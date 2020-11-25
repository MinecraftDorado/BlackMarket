package dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.configuration.file.YamlConfiguration;

import dev.minecraftdorado.BlackMarket.MainClass.MainClass;
import dev.minecraftdorado.BlackMarket.Utils.Utils;

public class BlackList {
	
	private static File file = new File(MainClass.main.getDataFolder(), "blacklist.yml");
	private static ArrayList<UMaterial> list = new ArrayList<>();
	
	public BlackList() {
		if(!file.exists())
			Utils.extract("resources/blacklist.yml", "blacklist.yml");
	}
	
	public static boolean isAllow(UMaterial umat) {
		return !list.contains(umat);
	}
	
	public static void load() {
		YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
		
		if(yml.isSet("list"))
			for(String s : yml.getStringList("list")) {
				UMaterial umat = UMaterial.match(s);
				if(umat != null)
					list.add(umat);
			}
	}
	
	public static void reload() {
		list.clear();
		load();
	}
}
