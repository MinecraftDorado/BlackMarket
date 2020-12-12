package dev.minecraftdorado.BlackMarket.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;

import dev.minecraftdorado.BlackMarket.MainClass.MainClass;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils.OrderUtils.OrderType;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils.UMaterial;
import dev.minecraftdorado.BlackMarket.Utils.Market.Market;
import dev.minecraftdorado.BlackMarket.Utils.Market.PlayerData;
import dev.minecraftdorado.BlackMarket.Utils.Packets.Reflections;
import dev.minecraftdorado.BlackMarket.Utils.Packets.ServerVersion;
import net.md_5.bungee.api.chat.TranslatableComponent;

public class Utils {
	
	public static File extract(final String from, final String to) {
        final File datafolder = MainClass.main.getDataFolder();
        final File destination = new File(datafolder, to);
        if (destination.exists()) {
            return destination;
        }
        final int lastIndex = to.lastIndexOf(47);
        final File dir = new File(datafolder, to.substring(0, (lastIndex >= 0) ? lastIndex : 0));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        final InputStream is = MainClass.class.getResourceAsStream("/" + from);
        Objects.requireNonNull(is, "Inbuilt resource not found: " + from);
        try {
            Files.copy(is, Paths.get(destination.toURI()), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException ex) {
            throw new RuntimeException("Error copying: " + from + " to: " + to, ex);
        }
        return destination;
    }
	
	public static File getFileFromResource(InputStream in) {
		try {
			File tempFile = File.createTempFile(MainClass.main.getDataFolder().getAbsolutePath(), "temp");
			tempFile.deleteOnExit();
			FileOutputStream out = new FileOutputStream(tempFile);
			IOUtils.copy(in, out);
			return tempFile;
		}catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
    }
	
	private static Method sendPacket = null;
	
	public static void sendPacket(Player player, Object packet) {
		try {
			Object handle = Reflections.getHandle(player);
	        Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
	        if (sendPacket == null)
	        	sendPacket = playerConnection.getClass().getMethod("sendPacket", Reflections.getNMSClass("Packet"));
	        sendPacket.invoke(playerConnection, packet);
		} catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException ignored) {}
	}
	
	private static HashMap<String, ItemStack> mats = new HashMap<>();
	public static String orderFormat;
	
	public static String applyVariables(String s, Player player) {
		if(s.contains("%")) {
			if(s.contains("%actual_page%")) s = s.replace("%actual_page%", "" + (Market.getPlayerPage(player)+1));
			if(s.contains("%pages%")) s = s.replace("%pages%", "" + Market.getPages());
			if(s.contains("%order_"))
				for(OrderType type : OrderType.values()) {
					String name = type.getName();
					if(s.contains("%order_" + name + "%")) {
						s = s.replace("%order_" + name + "%"
								, orderFormat.replace("%active%"
								, PlayerData.get(player.getUniqueId()).getOrder().equals(type) ? Config.getMessage("menus.market.items.order.active") : "")
								.replace("%value%",Config.getMessage("menus.market.items.order.values." + name)));
					break;
				}
			}
		}
		return ChatColor.translateAlternateColorCodes('&', s);
	}
	
	public static ItemStack applyMeta(ItemStack item, String metaKey) {
		ItemStack i = item.clone();
		ItemMeta meta = i.getItemMeta();
		
		if(Config.getLang().isSet(metaKey)) {
			ConfigurationSection conf = Config.getLang().getConfigurationSection(metaKey);
			
			if(conf.isSet("name"))
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', conf.getString("name")));
			
			if(conf.isSet("lore")) {
				ArrayList<String> lore = new ArrayList<>();
				conf.getStringList("lore").forEach(l -> lore.add(ChatColor.translateAlternateColorCodes('&', l)));
				meta.setLore(lore);
			}
			
			i.setItemMeta(meta);
		}else
			MainClass.main.getLogger().severe(String.format("» Item meta not found: " + metaKey, MainClass.main.getDescription().getName()));
		return i;
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack getMaterial(String key) {
		if(key == null)
			return UMaterial.BARRIER.getItemStack();
		if(mats.containsKey(key))
			return mats.get(key);
		
		int data = 0;
		
		String[] s = key.split("/");
		UMaterial um = null;
		
		try {
			um = UMaterial.valueOf(s[0]);
		}catch(Exception ex) {
			try {
				if(Reflections.existMethod(Material.class.toString(), "getMaterial", Integer.class)) {
					Method getMaterial = Material.class.getMethod("getMaterial", Integer.class);
					um= UMaterial.valueOf(((Material) getMaterial.invoke(null, Integer.valueOf(s[0]))).name());
				}
			}catch(Exception ex2) {
				ex2.printStackTrace();
			}
		}
		
		if(um == null || um.getMaterial() == null)
			um = UMaterial.BARRIER;
		
		if(s.length == 2)
			data = Integer.valueOf(s[1]);
		
		ItemStack item = um.getItemStack();
		if(data != 0)
			item.setDurability((short) data);
		
		ItemMeta meta = item.getItemMeta();
		for (ItemFlag flag : ItemFlag.values())
			meta.addItemFlags(flag);
		item.setItemMeta(meta);
		mats.put(key, item);
		return item;
	}
	
	public static boolean canAddItem(Player player, ItemStack item) {
		Inventory inv = Bukkit.createInventory(null, 45);
		int slot = 0;
		for(ItemStack i : player.getInventory().getContents())
			if(i != null && !i.getType().equals(Material.AIR)) {
				inv.setItem(slot, i.clone());
				slot++;
			}
		inv.addItem(item.clone());
		
		int a = 0;
		for(ItemStack i : inv.getContents())
			if(i != null && !i.getType().equals(Material.AIR))
				a++;
		if(a > 36)
			return false;
		return true;
	}
	
	public static String getTime(long pTime) {
		if(pTime >= 60*60*24) { // day : hour
			String x = String.format("%02d:%02d", pTime / 60 /60 / 24, (pTime / 60 /60) % 24);
			String[] split = x.split(":");
			x = split[0] + Config.getMessage("time_data.day") + " " + split[1] + Config.getMessage("time_data.hour");
			return x;
		}
		if(pTime >= 60*60) { // hour : minute
			String x = String.format("%02d:%02d", pTime / 60 /60, (pTime / 60) % 60);
			String[] split = x.split(":");
			x = split[0] + Config.getMessage("time_data.hour") + " " + split[1] + Config.getMessage("time_data.minute");
			return x;
		}
		// minute : second
	    String x = String.format("%02d:%02d", pTime / 60, pTime % 60);
	    String[] split = x.split(":");
		x = split[0] + Config.getMessage("time_data.minute") + " " + split[1] + Config.getMessage("time_data.second");
		return x;
	}
	
	public static int getLimit(UUID uuid) {
		final AtomicInteger max = new AtomicInteger();
		
		if (Bukkit.getOfflinePlayer(uuid).isOp())
			return -1;
		else
			max.set(Config.getDefaultLimit());
		
        if(Bukkit.getOfflinePlayer(uuid).isOnline()) {
        	Bukkit.getPlayer(uuid).getEffectivePermissions().stream().map(PermissionAttachmentInfo::getPermission).map(String::toLowerCase).filter(value -> value.startsWith("blackmarket.limit")).map(value -> value.replace("blackmarket.limit", "")).forEach(value -> {
        		if (value.equalsIgnoreCase("*")) {
        			max.set(-1);
        			return;
        		}
        		
        		if (max.get() == -1)
        			return;
        		
        		try {
        			int amount = Integer.parseInt(value);
        			
        			if (amount > max.get())
        				max.set(amount);
        		} catch (NumberFormatException ignored) {
        		}
        	});
        }
        
        return max.get();
    }
	
	public static TranslatableComponent getTranlatableName(Material material) {
		String key = null;
		
		try {
			Class<?> craftMagicNumbers = Reflections.getOBCClass("util.CraftMagicNumbers");
			Object newItem = null;
			
	    	Method m = craftMagicNumbers.getDeclaredMethod("getItem", material.getClass());
	    	m.setAccessible(true);
	    	newItem = m.invoke(craftMagicNumbers, material);
	    	if (newItem == null)
	    		throw new IllegalArgumentException(material.name() + " material could not be queried!");
			
	    	key = (String) Reflections.getNMSClass("Item").getMethod("getName").invoke(newItem);
		}catch (Exception ex) {
			ex.printStackTrace();
		}
		
		if(ServerVersion.getVersion().contains("1_8") || ServerVersion.getVersion().contains("1_9") || ServerVersion.getVersion().contains("1_10") || ServerVersion.getVersion().contains("1_11") || ServerVersion.getVersion().contains("1_12"))
			key+= ".name";
		return new TranslatableComponent(key);
	}
}
