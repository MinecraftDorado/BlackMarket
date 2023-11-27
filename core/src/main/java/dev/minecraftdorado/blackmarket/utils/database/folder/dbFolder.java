package dev.minecraftdorado.blackmarket.utils.database.folder;

import java.io.File;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import dev.minecraftdorado.blackmarket.MainClass;
import dev.minecraftdorado.blackmarket.utils.market.BlackItem;
import dev.minecraftdorado.blackmarket.utils.market.BlackItem.Status;
import dev.minecraftdorado.blackmarket.utils.market.Market;
import dev.minecraftdorado.blackmarket.utils.market.PlayerData;

public class dbFolder {
	
	private static File file = new File(MainClass.main.getDataFolder(), "items");
	
	public static void load() {
		if(file.exists() && file.listFiles().length != 0) {
			for(File f : file.listFiles()) {
				int id = Integer.parseInt(f.getName().replace(".yml", ""));
				
				try {
					YamlConfiguration yml = YamlConfiguration.loadConfiguration(f);
					Status status = Status.valueOf(yml.getString("status"));
					if(status.equals(Status.ON_SALE) || status.equals(Status.TIME_OUT) || (status.equals(Status.SOLD) && !yml.getBoolean("notified"))) {
						UUID owner = UUID.fromString(yml.getString("owner"));
						
						if(yml.getItemStack("item") != null) {
							BlackItem bItem = new BlackItem(yml.getItemStack("item"), yml.getDouble("value"), owner, Status.valueOf(yml.getString("status")), new Date(yml.getLong("date")), id, yml.getBoolean("notified"));
							PlayerData.get(owner).addItem(bItem);
							continue;
						}else {
							Bukkit.getConsoleSender().sendMessage("§c[BlackMarket] §7» Corrupt item: ID#" + id);
						}
					}
				}catch(Exception e) {
					Bukkit.getConsoleSender().sendMessage("§c[BlackMarket] §7» Corrupt item: ID#" + id);
				}
				
				if(Market.getId()<id)
					Market.setId(id);
			}
		}
	}
	
	public static void save() {
		PlayerData.list.values().forEach(data -> {
			data.getItems().forEach(bItem -> {
				File f = new File(file, bItem.getId() + ".yml");
				
				YamlConfiguration yml = new YamlConfiguration();
				
				yml.set("owner", data.getUUID().toString());
				yml.set("value", bItem.getValue());
				yml.set("date", bItem.getDate().getTime());
				yml.set("status", bItem.getStatus().name());
				yml.set("item", bItem.getOriginal());
				yml.set("notified", bItem.isNotified());
				
				try {
					yml.save(f);
				}catch(Exception ex) {
					ex.printStackTrace();
				}
			});
		});
	}
}
