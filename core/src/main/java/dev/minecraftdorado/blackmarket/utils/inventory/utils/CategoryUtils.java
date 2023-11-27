package dev.minecraftdorado.blackmarket.utils.inventory.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.minecraftdorado.blackmarket.MainClass;
import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.Utils;

public class CategoryUtils {
	
	private static HashMap<String, Category> list = new HashMap<>();
	private File f = new File(MainClass.main.getDataFolder() + "/categories");
	private static Category first_category;
	
	public CategoryUtils() {
		if(!f.exists() || f.listFiles().length == 0)
			for(String filter : new String[]{"all","ores","tools","wood","potions","redstone"})
				Utils.extract("resources/categories/" + filter + ".yml", "categories/" + filter + ".yml");
		for(File file : f.listFiles()) {
			YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
			
			String key = file.getName().replace(".yml", "");
			Category filter = new Category(key);
			
			if(yml.isSet("row"))
				filter.setRow(yml.getInt("row"));
			
			if(first_category == null || first_category.getRow() > filter.getRow())
				first_category = filter;
			
			if(yml.isSet("materials"))
				yml.getStringList("materials").forEach(mat -> filter.addMaterial(Material.matchMaterial(mat)));
			
			list.put(key, filter);
		}
	}
	
	public static Category addCategory(Category category) {
		list.put(category.getKey(), category);
		return category;
	}
	
	public static ArrayList<String> getKeys(){
		ArrayList<String> l = new ArrayList<>();
		list.keySet().forEach(k -> l.add(k));
		return l;
	}
	
	public static Collection<Category> getCategories(){
		return list.values();
	}
	
	public static Category getFirstCategory() {
		return first_category;
	}
	
	public class Category {
		
		private String key;
		private int row;
		
		private ArrayList<Material> mats = new ArrayList<>();
		private ItemStack item;
		
		public Category(String key) {
			this.key = key;
		}
		
		public String getKey() {
			return key;
		}
		
		public void setRow(int row) {
			this.row = row < 7 && row > 0 ? row : 1;
		}
		
		public int getRow() {
			return row;
		}
		
		public ItemStack getItemStack(Boolean status) {
			if(item != null)
				return item;
			ItemStack item = Config.getItemStack("market.categories." + key.toLowerCase(), "menus.market.items.categories." + key.toLowerCase());
			ItemMeta meta = item.getItemMeta();
			
			if(status)
				meta.addEnchant(Enchantment.DURABILITY, 1, false);
			
			item.setItemMeta(meta);
			return item;
		}
		
		public void addMaterial(Material uMat) {
			if(!this.mats.contains(uMat))
				this.mats.add(uMat);
		}
		
		public ArrayList<Material> getMaterials(){
			return mats;
		}
		
		public boolean contain(Material uMat) {
			return mats.contains(uMat);
		}
	}
}
