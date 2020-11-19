package dev.minecraftdorado.BlackMarket.Utils.Market;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.minecraftdorado.BlackMarket.Utils.Config;
import dev.minecraftdorado.BlackMarket.Utils.Utils;
import dev.minecraftdorado.BlackMarket.Utils.DataBase.MySQL.dbMySQL;
import dev.minecraftdorado.BlackMarket.Utils.Config.StorageType;

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
		cal.add(Calendar.MINUTE, Config.getExpiredTime());
		this.date = cal.getTime();
		dbMySQL.addBlackItem(this);
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
		
		Duration d = Duration.between(new Date().toInstant(), date.toInstant());
		
		Config.getDesc().forEach(s -> {
			if(s.contains("%owner%")) s = s.replace("%owner%", Bukkit.getOfflinePlayer(owner).getName());
			if(s.contains("%value%")) s = s.replace("%value%", value + "");
			if(s.contains("%expired%")) s = s.replace("%expired%", (d.getSeconds() > 0 ? Utils.getTime(d.getSeconds()) : 0)  + "");
			
			lore.add(ChatColor.translateAlternateColorCodes('&', s));
		});
		
		meta.setLore(lore);
		black.setItemMeta(meta);
		return black;
	}
	
	public UUID getOwner() {
		return owner;
	}
	
	public Status getStatus() {
		if(status.equals(Status.ON_SALE)) {
			if(Duration.between(new Date().toInstant(), date.toInstant()).getSeconds() <= 0)
				setStatus(Status.TIME_OUT);
		}
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
		if(Config.getStorageType().equals(StorageType.MySQL))
			dbMySQL.updateStatus(this);
	}
	
	public enum Status {
		SOLD, ON_SALE, TIME_OUT, TAKED;
	}
	
	public Date getDate() {
		return date;
	}
}
