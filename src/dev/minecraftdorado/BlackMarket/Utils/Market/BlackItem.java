package dev.minecraftdorado.BlackMarket.Utils.Market;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BlackItem {
	
	private int id;
	private ItemStack item;
	private double value;
	
	private UUID owner;
	/*
	public BlackItem(ItemStack item, double value, UUID owner, int id) {
		this.id = id;
		this.item = item;
		this.value = value;
		this.owner = owner;
	}*/
	
	public BlackItem(ItemStack item, double value, UUID owner) {
		Market.addId();
		this.id = Market.getId();
		this.item = item;
		this.value = value;
		this.owner = owner;
	}
	
	public int getId() {
		return id;
	}
	
	public double getValue() {
		return value;
	}
	
	public ItemStack getOriginal() {
		return item;
	}
	
	public ItemStack getItemStack() {
		ItemStack black = item.clone();
		ItemMeta meta = black.getItemMeta();
		
		List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
		
		lore.add("§8Value: §6$" + value);
		lore.add("");
		lore.add("§eclick to buy!");
		
		return black;
	}
	
	public UUID getOwner() {
		return owner;
	}
}
