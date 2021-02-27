package dev.minecraftdorado.blackmarket.utils.packets;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class Reflections {
	
	public static Class<?> getNMSClass(String className) {
		try {
			return Class.forName("net.minecraft.server." + ServerVersion.getVersion() + "." + className);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException("An error occurred while finding NMS class.", ex);
	    }
	}
	
	public static boolean existNMSClass(String className) {
		try {
			Class.forName("net.minecraft.server." + ServerVersion.getVersion() + "." + className);
		}catch(Exception ex) {
			return false;
		}
		return true;
	}
	
	public static boolean existMethod(String className, String method, Class<?>... parameterTypes) {
		try {
			Class.forName(className).getMethod(method, parameterTypes);
		}catch(Exception ex) {
			return false;
		}
		return true;
	}
	
	public static Class<?> getOBCClass(String className) {
		try {
			return Class.forName("org.bukkit.craftbukkit." + ServerVersion.getVersion() + "." + className);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException("An error occurred while finding OBC class.", ex);
		}
	}
	
	public static Object getHandle(Object object) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		return object.getClass().getMethod("getHandle").invoke(object);
	}
	
	public static Object getField(Object object, String fieldName) {
        Field field;
        Object o = null;
        try {
        	field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            o = field.get(object);
        } catch(NoSuchFieldException|IllegalAccessException e) {e.printStackTrace();}
        return o;
    }
	
	public static void setValue(Object obj,String name,Object value){
    	try{
            Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(obj, value);
    	}catch(Exception e){e.printStackTrace();}
    }
}

