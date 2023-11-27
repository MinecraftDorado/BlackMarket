package dev.minecraftdorado.blackmarket.utils.packets;

import java.lang.reflect.Field;
import org.bukkit.entity.Player;

public abstract class PacketReaderAbs {
	
	public abstract void inject(Player player);
	
	public abstract void readPackets(Object packet, Player player);
	
	public Object getValue(Object instance, String name) {

		Object result = null;

		try {
			// gets "action" field
			Field field = instance.getClass().getDeclaredField(name);

			// reads value
			field.setAccessible(true);
			result = field.get(instance);
			field.setAccessible(false);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	public Field connectionField;
	
	public abstract Object getConnection(final Object playerConnection);

}