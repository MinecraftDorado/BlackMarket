package dev.minecraftdorado.blackmarket.utils.packets;

import java.lang.reflect.Field;
import org.bukkit.entity.Player;

public abstract class PacketReaderAbs {
	
	public abstract void inject(Player player);
	
	public abstract void readPackets(Object packet, Player player);
	
	public Field connectionField;
	
	public abstract Object getConnection(final Object playerConnection);

}