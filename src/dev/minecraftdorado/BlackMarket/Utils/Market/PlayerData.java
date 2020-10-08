package dev.minecraftdorado.BlackMarket.Utils.Market;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;

import dev.minecraftdorado.BlackMarket.MainClass.MainClass;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils.CategoryUtils.Category;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils.OrderUtils.OrderType;
import dev.minecraftdorado.BlackMarket.Utils.Market.BlackItem.Status;

public class PlayerData {
	
	private static HashMap<UUID, Data> list = new HashMap<>();
	private static File file = new File(MainClass.main.getDataFolder(), "items");
	
	public PlayerData() {
		if(file.exists() && file.listFiles().length != 0) {
			for(File f : file.listFiles()) {
				YamlConfiguration yml = YamlConfiguration.loadConfiguration(f);
				
				if(!Status.valueOf(yml.getString("status")).equals(Status.SOLD)) {
					UUID owner = UUID.fromString(yml.getString("owner"));
					
					BlackItem bItem = new BlackItem(yml.getItemStack("item"), yml.getDouble("value"), owner, Status.valueOf(yml.getString("status")), new Date(yml.getLong("date")), Integer.parseInt(f.getName().replace(".yml", "")));
					get(owner).addItem(bItem);
				}
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
				yml.set("date", bItem.getDate().getTime());
				yml.set("status", bItem.getStatus().name());
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
		private int limit = 5;
		private OrderType order = OrderType.ID;
		
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
			int i = 0;
			for(BlackItem item : items)
				if(item.getStatus().equals(Status.ON_SALE))
					i++;
			if(i < limit) {
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

		public ArrayList<BlackItem> getStorage() {
			ArrayList<BlackItem> list = new ArrayList<>();
			
			items.forEach(item -> {
				if(item.getStatus().equals(Status.TIME_OUT))
					list.add(item);
			});
			
			return list;
		}
		
		public void setOrder(OrderType order) {
			this.order = order;
		}
		
		public OrderType getOrder() {
			return order;
		}
	}
}
