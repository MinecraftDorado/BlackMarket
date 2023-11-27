package dev.minecraftdorado.blackmarket.utils;

import org.bukkit.Bukkit;

import dev.minecraftdorado.blackmarket.MainClass;

public class ReflectionUtils {
	
	@SuppressWarnings("deprecation")
	public static Object getClass(String packet){
		String packageName = MainClass.class.getPackage().getName();
		String internalsName = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		
		
		try {
			return Class.forName(packageName + "." + internalsName + "." + packet).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			Bukkit.getConsoleSender().sendMessage("§6[BlackMarket] §7» §5PACKET not found: " + packageName + "." + internalsName + "." + packet);
			return null;
		}
	}
	
}
