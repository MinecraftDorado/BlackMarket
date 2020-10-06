package dev.minecraftdorado.BlackMarket.Utils;

import java.io.File;
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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import dev.minecraftdorado.BlackMarket.MainClass.MainClass;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils.UMaterial;
import dev.minecraftdorado.BlackMarket.Utils.Market.Market;
import dev.minecraftdorado.BlackMarket.Utils.Packets.Reflections;

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
	public static HashMap<String, ItemStack> items = new HashMap<>();
	public static String orderFormat;
	
	public static ItemStack getItemStack(File file, String key) {
		String a = file.getName().split(".yml")[0] + "_" + key;
		if(items.containsKey(a))
			return items.get(a).clone();
		YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
		ConfigurationSection k = yml.getConfigurationSection(key);
		ItemStack item = new ItemStack(getMaterial(k.getString("type"), false));
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', k.getString("name")));
		for(ItemFlag flag : ItemFlag.values())
			meta.addItemFlags(flag);
		ArrayList<String> lore = new ArrayList<>();
		k.getStringList("lore").forEach(l -> {
			lore.add(ChatColor.translateAlternateColorCodes('&', l));
		});
		meta.setLore(lore);
		item.setItemMeta(meta);
		if(k.contains("owner")) {
			if(meta instanceof SkullMeta) {
				SkullMeta sm = (SkullMeta) item.getItemMeta();
				sm.setOwner(k.getString("owner"));
				item.setItemMeta(sm);
			}
		}
		items.put(a, item);
		return item.clone();
	}
	
	public static String applyVariables(String s, Player player) {
		if(s.contains("%actual_page%")) s = s.replace("%actual_page%", "" + (Market.getPlayerPage(player.getUniqueId())+1));
		if(s.contains("%pages%")) s = s.replace("%pages%", "" + Market.getPages());
		return s;
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack getMaterial(String key, Boolean value) {
		if(mats.containsKey(key) && value)
			return mats.get(key);
		
		int data = 0;
		
		String[] s = key.split("/");
		
		UMaterial um = null;
		
		try {
			um = UMaterial.valueOf(s[0]);
		}catch(Exception ex) {
			try {
				um = UMaterial.valueOf(Material.getMaterial(Integer.valueOf(s[0])).name());
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
		
		if(value)
			mats.put(key, item);
		return item;
	}
	
	public static ItemStack getMaterial(String key) {
		return getMaterial(key, true);
	}
	
	public static boolean canAddItem(Player player, ItemStack item) {
		Inventory inv = Bukkit.createInventory(null, 45);
		int slot = 0;
		for(ItemStack i : player.getInventory().getContents())
			if(i != null && !i.getType().equals(Material.AIR)) {
				inv.setItem(slot, i);
				slot++;
			}
		inv.addItem(item);
		
		int a = 0;
		for(ItemStack i : inv.getContents())
			if(i != null && !i.getType().equals(Material.AIR))
				a++;
		if(a > 36)
			return false;
		return true;
	}
}
