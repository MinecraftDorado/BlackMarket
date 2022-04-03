package dev.minecraftdorado.blackmarket.utils.market;

import java.sql.Timestamp;
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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.minecraftdorado.blackmarket.mainclass.MainClass;
import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.Utils;
import dev.minecraftdorado.blackmarket.utils.database.mysql.dbMySQL;
import dev.minecraftdorado.blackmarket.utils.economy.EconomyManager;
import net.md_5.bungee.api.chat.TextComponent;

public class BlackItem {
	
	private int id;
	private ItemStack item;
	private double value;
	
	private UUID owner;
	
	public Status status = Status.ON_SALE;
	private Timestamp expiration_date;
	
	private boolean notified = false;
	
	public BlackItem(ItemStack item, double value, UUID owner, Status status, Timestamp expiration_date, int id, boolean notified) {
		this.id = id;
		this.item = item;
		this.value = value;
		this.owner = owner;
		this.expiration_date = expiration_date;
		this.status = status;
		this.notified = notified;
	}
	
	public BlackItem(ItemStack item, double value, UUID owner) {
		this.item = item;
		this.value = value;
		this.owner = owner;
		
		// Expiration time
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MINUTE, Config.getExpiredTime());
		this.expiration_date = new Timestamp(cal.getTime().getTime());
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
	
	public ItemStack getItemStack(Player p, boolean inspect) {
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
		
		Duration d = Duration.between(new Date().toInstant(), expiration_date.toInstant());
		
		for(String s : Config.getDesc()) {
			if(s.contains("%owner%")) s = s.replace("%owner%", Bukkit.getOfflinePlayer(owner) != null ? Bukkit.getOfflinePlayer(owner).getName() : "§cUnknown");
			if(s.contains("%value%")) s = s.replace("%value%", value + "");
			if(s.contains("%expired%")) s = s.replace("%expired%", (d.getSeconds() > 0 ? Utils.getTime(d.getSeconds()) : 0)  + "");
			
			lore.add(ChatColor.translateAlternateColorCodes('&', s));
		}
		
		if(inspect && item.getType().name().contains("SHULKER_BOX"))
			for(String s : Config.getContentDesc())
				lore.add(ChatColor.translateAlternateColorCodes('&', s));
		
		if(p.hasPermission("blackmarket.remove_item") || getOwner().equals(p.getUniqueId()))
			for(String s : Config.getRemoveDesc())
				lore.add(ChatColor.translateAlternateColorCodes('&', s));
		
		meta.setLore(lore);
		black.setItemMeta(meta);
		return black;
	}
	
	public UUID getOwner() {
		return owner;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
		dbMySQL.updateStatus(this);
	}
	
	public enum Status {
		SOLD(2), ON_SALE(1), TIME_OUT(4), TAKED(3);

		private int id;

		Status(int id){
			this.id = id;
		}

		public int getId(){
			return id;
		}
	}
	
	public Timestamp getExpirationDate() {
		return expiration_date;
	}
	
	public boolean isNotified() {
		return notified;
	}
	
	public void sendNotification() {
		String[] split = Config.getMessage("market.notification").replace("%value%", "" + getFinalValue()).split("%name%");
		
		TextComponent tc = new TextComponent(split[0]);
		if(split.length > 1) {
			if(item.hasItemMeta() && item.getItemMeta().hasDisplayName())
				tc.addExtra(item.getItemMeta().getDisplayName());
			else
				tc.addExtra(Utils.getTranlatableName(item.getType()));
			tc.addExtra(split[1]);
		}
		Bukkit.getPlayer(owner).spigot().sendMessage(tc);
		notified = true;

		dbMySQL.updateNotified(this);
	}
	
	public double getFinalValue() {
		return Double.parseDouble(new DecimalFormat("0.00").format(value - (value * Config.getTaxes() / 100)).replace(",", "."));
	}
	
	public boolean buy(Player player) {
		if(EconomyManager.has(player, getValue()))
			if(getStatus().equals(Status.ON_SALE)) {
				if(Utils.canAddItem(player, getOriginal())) {
					setStatus(Status.SOLD);
					player.getInventory().addItem(getOriginal());
					player.closeInventory();
					Config.sendMessage("market.buy", player);
					
					EconomyManager.withdraw(player, getValue());
					EconomyManager.deposit(Bukkit.getOfflinePlayer(getOwner()), getFinalValue());
					return true;
				}
				Config.sendMessage("market.inventory_full", player);
			}else
				Config.sendMessage("market.item_invalid", player);
		else
			Config.sendMessage("market.missing_money", player);
		return false;
	}
}
