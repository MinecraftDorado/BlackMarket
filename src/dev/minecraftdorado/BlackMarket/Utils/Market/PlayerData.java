package dev.minecraftdorado.BlackMarket.Utils.Market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PlayerData {
	
	private static HashMap<UUID, Data> list = new HashMap<>();
	
	public PlayerData() {
		
	}
	
	public static Data get(UUID uuid) {
		if(!list.containsKey(uuid))
			list.put(uuid, new Data(uuid));
		return list.get(uuid);
	}
	
	
	public static class Data {
		
		private UUID uuid;
		private ArrayList<BlackItem> items = new ArrayList<>();
		private int limit = 5;
		
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
	}
}
