package dev.minecraftdorado.BlackMarket.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.minecraftdorado.BlackMarket.MainClass.MainClass;

public class Config {
	
	private static HashMap<String, ItemStack> items = new HashMap<>();
	
	public Config() {
		if(!new File(MainClass.main.getDataFolder() + "/config.yml").exists())
			MainClass.main.saveDefaultConfig();
		
		File file = new File(MainClass.main.getDataFolder(), "config.yml");
		YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
		
		yml.getConfigurationSection("items").getKeys(false).forEach(key -> {
			items.put(key, Utils.getItemStack(file, "items." + key));
		});
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
}
