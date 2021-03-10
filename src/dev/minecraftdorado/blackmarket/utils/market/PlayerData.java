package dev.minecraftdorado.blackmarket.utils.market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.Utils;
import dev.minecraftdorado.blackmarket.utils.database.folder.dbFolder;
import dev.minecraftdorado.blackmarket.utils.database.mysql.dbMySQL;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.CategoryUtils;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.CategoryUtils.Category;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.OrderUtils.OrderType;
import dev.minecraftdorado.blackmarket.utils.market.BlackItem.Status;

public class PlayerData {
	
	public static HashMap<UUID, Data> list = new HashMap<>();
	
	public PlayerData() {
		switch(Config.getStorageType()) {
		case MySQL:
			dbMySQL.load();
			break;
		default:
			dbFolder.load();
			break;
		}
	}
	
	public static Data get(UUID uuid) {
		if(!list.containsKey(uuid))
			list.put(uuid, new Data(uuid));
		return list.get(uuid);
	}
	
	public static void save() {
		switch(Config.getStorageType()) {
		case MySQL:
			dbMySQL.save();
			break;
		default:
			dbFolder.save();
			break;
		}
	}
	
	public static class Data {
		
		private UUID uuid;
		private ArrayList<BlackItem> items = new ArrayList<>();
		private OrderType order = OrderType.ID;
		private boolean reverse = false;
		
		private Category category = CategoryUtils.getFirstCategory();
		
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
			int i = 0, ti = 0;
			for(BlackItem item : items)
				if(item.getStatus().equals(Status.ON_SALE))
					i++;
				else if(item.getStatus().equals(Status.TIME_OUT))
					ti++;
			int limit = getLimit();
			if(i < limit || limit == -1) // on_sale items
				if(ti+i < 28) { // storage limit
					items.add(bItem);
					Market.addItem(bItem);
					return true;
				}
			return false;
		}
		
		public void setItem(BlackItem bItem) {
			items.add(bItem);
			Market.addItem(bItem);
		}
		
		public int getLimit() {
			return Utils.getLimit(uuid);
		}
		
		public void setCategory(Category category) {
			this.category = category == null ? CategoryUtils.getFirstCategory() : category;
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
		
		public void setReverse(boolean value) {
			this.reverse = value;
		}
		
		public boolean isReverse() {
			return this.reverse;
		}
	}
}
