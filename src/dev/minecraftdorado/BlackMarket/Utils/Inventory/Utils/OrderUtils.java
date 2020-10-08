package dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import dev.minecraftdorado.BlackMarket.Utils.Market.BlackItem;

public class OrderUtils {
	
	public static enum OrderType {
		ID, AMOUNT, VALUE, TYPE;
	}
	
	public static ArrayList<BlackItem> sortByAmount(ArrayList<BlackItem> unsortMap) {
		ArrayList<BlackItem> list = new ArrayList<>();
		for(BlackItem bItem : unsortMap) {
			ItemStack item = bItem.getOriginal();
			if(item != null && !item.getType().equals(Material.AIR))
				list.add(bItem);
		}
		
		Collections.sort(list, new Comparator<BlackItem>() {
			public int compare(BlackItem x, BlackItem y) {
				int startComparison = compare(x.getOriginal().getAmount(), y.getOriginal().getAmount());
			    return startComparison != 0 ? startComparison : compare(x.getOriginal().getAmount(), y.getOriginal().getAmount());
			}
			
			private int compare(int a, int b) {
				return a < b ? -1 : a > b ? 1 : 0;
			}
		});
	    return list;
	}
	
	public static ArrayList<BlackItem> sortByType(ArrayList<BlackItem> unsortMap) {
		ArrayList<BlackItem> list = new ArrayList<>();
		for(BlackItem bItem : unsortMap) {
			ItemStack item = bItem.getOriginal();
			if(item != null && !item.getType().equals(Material.AIR))
				list.add(bItem);
		}
		
		Collections.sort(list, new Comparator<BlackItem>() {
			@SuppressWarnings("deprecation")
			public int compare(BlackItem x, BlackItem y) {
				try {
					int startComparison = compare(x.getOriginal().getType().getId(), y.getOriginal().getType().getId());
					return startComparison != 0 ? startComparison : compare(x.getOriginal().getType().getId(), y.getOriginal().getType().getId());
				}catch(Exception ex) {
					return x.getOriginal().getType().name().compareTo(y.getOriginal().getType().name());
				}
			}
			
			private int compare(int a, int b) {
				return a < b ? -1 : a > b ? 1 : 0;
			}
		});
	    return list;
	}
	
	public static ArrayList<BlackItem> sortByValue(ArrayList<BlackItem> unsortMap) {
		ArrayList<BlackItem> list = new ArrayList<>();
		for(BlackItem bItem : unsortMap) {
			ItemStack item = bItem.getOriginal();
			if(item != null && !item.getType().equals(Material.AIR))
				list.add(bItem);
		}
		
		Collections.sort(list, new Comparator<BlackItem>() {
			public int compare(BlackItem x, BlackItem y) {
				int startComparison = compare(x.getValue(), y.getValue());
			    return startComparison != 0 ? startComparison : compare(x.getValue(), y.getValue());
			}
			
			private int compare(double a, double b) {
				return a < b ? -1 : a > b ? 1 : 0;
			}
		});
	    return list;
	}
}

