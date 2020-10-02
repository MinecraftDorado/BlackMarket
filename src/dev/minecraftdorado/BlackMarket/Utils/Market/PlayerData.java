package dev.minecraftdorado.BlackMarket.Utils.Market;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;

import dev.minecraftdorado.BlackMarket.MainClass.MainClass;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils.CategoryUtils.Category;

public class PlayerData {
	
	private static HashMap<UUID, Data> list = new HashMap<>();
	private static File file = new File(MainClass.main.getDataFolder(), "items");
	
	public PlayerData() {
		if(file.exists() && file.listFiles().length != 0) {
			
			for(File f : file.listFiles()) {
				YamlConfiguration yml = YamlConfiguration.loadConfiguration(f);
				
				UUID owner = UUID.fromString(yml.getString("owner"));
				
				BlackItem bItem = new BlackItem(yml.getItemStack("item"), yml.getDouble("value"), owner, Integer.parseInt(f.getName().replace(".yml", "")));
				get(owner).addItem(bItem);
			}
			
		}
	}
	
	public static Data get(UUID uuid) {
		if(!list.containsKey(uuid))
			list.put(uuid, new Data(uuid));
		return list.get(uuid);
	}
	
	public static void save() {
		list.values().forEach(data -> {
			
			data.getItems().forEach(bItem -> {
				File f = new File(file, bItem.getId() + ".yml");
				
				YamlConfiguration yml = YamlConfiguration.loadConfiguration(f);
				yml.set("owner", data.getUUID().toString());
				yml.set("value", bItem.getValue());
				yml.set("item", bItem.getOriginal());
				
				try {
					yml.save(f);
				}catch(Exception ex) {
					ex.printStackTrace();
				}
			});
			
		});
	}
	
	
	public static class Data {
		
		private UUID uuid;
		private ArrayList<BlackItem> items = new ArrayList<>();
		private int limit = 50;
		
		private Category category = null;
		
		public Data(UUID uuid) {
			this.uuid = uuid;
		}
		
		public UUID getUUID() {
			return uuid;
		}
		
		public ArrayList<BlackItem> getItems(){
			return items;
		}
		
		public boolean addItem(BlackItem bItem) {
			if(items.size() < limit) {
				items.add(bItem);
				Market.addItem(bItem);
				return true;
			}
			return false;
		}
		
		public int getLimit() {
			return limit;
		}
		
		public void setCategory(Category category) {
			this.category = category;
		}
		
		public Category getCategory() {
			return category;
		}
	}
}
