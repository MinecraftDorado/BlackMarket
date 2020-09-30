package dev.minecraftdorado.BlackMarket.Utils.Packets;

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
}