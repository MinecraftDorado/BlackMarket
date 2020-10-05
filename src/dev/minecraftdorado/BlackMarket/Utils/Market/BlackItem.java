package dev.minecraftdorado.BlackMarket.Utils.Market;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BlackItem {
	
	private int id;
	private ItemStack item;
	private double value;
	
	private UUID owner;
	
	private Status status = Status.ON_SALE;
	private Date date;
	
	public BlackItem(ItemStack item, double value, UUID owner, Status status, Date date, int id) {
		this.id = id;
		if(Market.getId() < id)
			Market.setId(id);
		this.item = item;
		this.value = value;
		this.owner = owner;
		this.date = date;
		this.status = status;
	}
	
	public BlackItem(ItemStack item, double value, UUID owner) {
		Market.addId();
		this.id = Market.getId();
		this.item = item;
		this.value = value;
		this.owner = owner;
		
		// Expiration time
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MINUTE, 5);
		this.date = cal.getTime();
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
		
		lore.add("§8Owner: §b" + Bukkit.getOfflinePlayer(owner).getName());
		lore.add("§8Value: §6$" + value);
		
		Duration d = Duration.between(new Date().toInstant(), date.toInstant());
		String df = "" + d.getSeconds();
		
		lore.add("§8To expire: §b" + df + "s");
		lore.add("");
		lore.add("§eClick to buy!");
		lore.add("§8§o#" + id);
		
		meta.setLore(lore);
		black.setItemMeta(meta);
		return black;
	}
	
	public UUID getOwner() {
		return owner;
	}
	
	public Status getStatus() {
		if(status.equals(Status.ON_SALE))
			if(date.before(new Date()))
				status = Status.TIME_OUT;
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public enum Status {
		SOLD, ON_SALE, TIME_OUT;
	}
	
	public Date getDate() {
		return date;
	}
}
