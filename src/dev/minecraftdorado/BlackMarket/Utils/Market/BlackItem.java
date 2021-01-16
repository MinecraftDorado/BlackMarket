package dev.minecraftdorado.BlackMarket.Utils.Market;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.minecraftdorado.BlackMarket.MainClass.MainClass;
import dev.minecraftdorado.BlackMarket.Utils.Config;
import dev.minecraftdorado.BlackMarket.Utils.Utils;
import dev.minecraftdorado.BlackMarket.Utils.DataBase.MySQL.dbMySQL;
import net.md_5.bungee.api.chat.TextComponent;
import dev.minecraftdorado.BlackMarket.Utils.Config.StorageType;

public class BlackItem {
	
	private int id;
	private ItemStack item;
	private double value;
	
	private UUID owner;
	
	private Status status = Status.ON_SALE;
	private Date date;
	
	private boolean notified = false;
	
	public BlackItem(ItemStack item, double value, UUID owner, Status status, Date date, int id, boolean notified) {
		this.id = id;
		if(Market.getId() < id)
			Market.setId(id);
		this.item = item;
		this.value = value;
		this.owner = owner;
		this.date = date;
		this.status = status;
		this.notified = notified;
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
		if(Config.getStorageType().equals(StorageType.MySQL))
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
		
		List<String> lore = new ArrayList<>();
		try {
			if(meta.hasLore())
				lore = meta.getLore();
		}catch(Exception ex) {
			MainClass.main.getLogger().info(String.format("» Item fixed: " + id + " - " + item.getType().name(), MainClass.main.getDescription().getName()));
			this.status = Status.TAKED;
			return new ItemStack(Material.STONE);
		}
		
		Duration d = Duration.between(new Date().toInstant(), date.toInstant());
		
		for(String s : Config.getDesc()) {
			if(s.contains("%owner%")) s = s.replace("%owner%", Bukkit.getOfflinePlayer(owner).getName());
			if(s.contains("%value%")) s = s.replace("%value%", value + "");
			if(s.contains("%expired%")) s = s.replace("%expired%", (d.getSeconds() > 0 ? Utils.getTime(d.getSeconds()) : 0)  + "");
			
			lore.add(ChatColor.translateAlternateColorCodes('&', s));
		}
		
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
		if(status.equals(Status.SOLD) && !isNotified())
			if(Bukkit.getOfflinePlayer(owner).isOnline())
				sendNotification();
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
	
	public boolean isNotified() {
		return notified;
	}
	
	public void sendNotification() {
		String[] split = Config.getMessage("market.notification").replace("%value%", "" + getFinalValue()).split("%name%");
		
		TextComponent tc = new TextComponent(split[0]);
		if(item.hasItemMeta() && item.getItemMeta().hasDisplayName())
			tc.addExtra(item.getItemMeta().getDisplayName());
		else
			tc.addExtra(Utils.getTranlatableName(item.getType()));
		if(split.length > 1)
			tc.addExtra(split[1]);
		Bukkit.getPlayer(owner).spigot().sendMessage(tc);
		
		notified = true;
		
		if(Config.getStorageType().equals(StorageType.MySQL))
			dbMySQL.updateNotified(this);
	}
	
	public double getFinalValue() {
		return Double.parseDouble(new DecimalFormat("0.00").format(value - (value * Config.getTaxes() / 100)).replace(",", "."));
	}
}
