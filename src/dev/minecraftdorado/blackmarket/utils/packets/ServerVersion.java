package dev.minecraftdorado.blackmarket.utils.packets;

import org.bukkit.Bukkit;

public class ServerVersion {
	
	private static String version;
	
	static {
		String[] versionArray = Bukkit.getServer().getClass().getName().replace('.', ',').split(",");
		if (versionArray.length >= 4) {
			version = versionArray[3];
		} else {
			version = "";
		}
	}
	
	public static String getVersion() {
		return version;
	}
	
	public interface Versionable {
	    String VERSION = Bukkit.getVersion();
	    boolean EIGHT = VERSION.contains("1.8");
	    boolean NINE = VERSION.contains("1.9");
	    boolean TEN = VERSION.contains("1.10");
	    boolean ELEVEN = VERSION.contains("1.11");
	    boolean TWELVE = VERSION.contains("1.12");
	}
}