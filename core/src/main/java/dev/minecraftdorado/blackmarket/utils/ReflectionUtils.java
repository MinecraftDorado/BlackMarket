package dev.minecraftdorado.blackmarket.utils;

import java.lang.reflect.Field;

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
	
	public static void setValue(Object packet, String fieldName, Object value) {
	    try {
	        Field field = packet.getClass().getDeclaredField(fieldName);
	        field.setAccessible(true);
	        field.set(packet, value);
	    } catch (Exception exception) {
	        exception.printStackTrace();
	    }
	}
	
	public static Object getValue(Object instance, String name) {
		Object result = null;
		
		try {
			Field field = instance.getClass().getDeclaredField(name);
			field.setAccessible(true);
			result = field.get(instance);
			field.setAccessible(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
}
