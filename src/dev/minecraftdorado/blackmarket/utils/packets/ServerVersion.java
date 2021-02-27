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
	    boolean EIGHT = VERSION.contains("1.8"), NINE = VERSION.contains("1.9"), TEN = VERSION.contains("1.10"), ELEVEN = VERSION.contains("1.11"), TWELVE = VERSION.contains("1.12"), THIRTEEN = VERSION.contains("1.13"), FOURTEEN = VERSION.contains("1.14"), FIFTEEN = VERSION.contains("1.15"), SIXTEEN = VERSION.contains("1.16");
	    boolean LEGACY = EIGHT || NINE || TEN || ELEVEN || TWELVE;
	}
}