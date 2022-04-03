package dev.minecraftdorado.blackmarket.utils.market;

import dev.minecraftdorado.blackmarket.utils.Utils;
import dev.minecraftdorado.blackmarket.utils.database.mysql.dbMySQL;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.CategoryUtils;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.CategoryUtils.Category;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.OrderType;
import dev.minecraftdorado.blackmarket.utils.market.BlackItem.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PlayerData {
	
	public static HashMap<UUID, Data> list = new HashMap<>();
	
	public PlayerData() {
		load();
	}
	
	public static Data get(UUID uuid) {
		if(!list.containsKey(uuid))
			list.put(uuid, new Data(uuid));
		return list.get(uuid);
	}
	
	public static void save() {
	}
	
	public static void reload() {
		save();
		list.clear();
		load();
	}
	
	private static void load() {
		dbMySQL.load();
	}
	
	public static class Data {
		
		private UUID uuid;
		private OrderType order = OrderType.ID;
		private boolean inverted = false;
		
		private Category category = CategoryUtils.getFirstCategory();
		
		public Data(UUID uuid) {
			this.uuid = uuid;
		}
		
		public UUID getUUID() {
			return uuid;
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
		
		public void setOrder(OrderType order) {
			this.order = order;
		}
		
		public OrderType getOrder() {
			return order;
		}
		
		public void setInverted(boolean inverted) {
			this.inverted = inverted;
		}
		
		public boolean isInverted() {
			return this.inverted;
		}
	}
}
