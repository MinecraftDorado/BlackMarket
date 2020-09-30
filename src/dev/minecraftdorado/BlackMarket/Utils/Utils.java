package dev.minecraftdorado.BlackMarket.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import org.bukkit.entity.Player;

import dev.minecraftdorado.BlackMarket.MainClass.MainClass;
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
}
