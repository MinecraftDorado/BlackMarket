package dev.minecraftdorado.BlackMarket.Utils.DataBase.Folder;

import java.io.File;
import java.util.Date;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;

import dev.minecraftdorado.BlackMarket.MainClass.MainClass;
import dev.minecraftdorado.BlackMarket.Utils.Market.BlackItem;
import dev.minecraftdorado.BlackMarket.Utils.Market.BlackItem.Status;
import dev.minecraftdorado.BlackMarket.Utils.Market.Market;
import dev.minecraftdorado.BlackMarket.Utils.Market.PlayerData;

public class dbFolder {
	
	private static File file = new File(MainClass.main.getDataFolder(), "items");
	
	public static void load() {
		if(file.exists() && file.listFiles().length != 0) {
			for(File f : file.listFiles()) {
				YamlConfiguration yml = YamlConfiguration.loadConfiguration(f);
				Status status = Status.valueOf(yml.getString("status"));
				int id = Integer.parseInt(f.getName().replace(".yml", ""));
				if(status.equals(Status.ON_SALE) || status.equals(Status.TIME_OUT) || (status.equals(Status.SOLD) && !yml.getBoolean("notified"))) {
					UUID owner = UUID.fromString(yml.getString("owner"));
					
					BlackItem bItem = new BlackItem(yml.getItemStack("item"), yml.getDouble("value"), owner, Status.valueOf(yml.getString("status")), new Date(yml.getLong("date")), id, yml.getBoolean("notified"));
					PlayerData.get(owner).addItem(bItem);
				}else
					if(Market.getId()<id)
						Market.setId(id);
			}
		}
	}
	
	public static void save() {
		PlayerData.list.values().forEach(data -> {
			data.getItems().forEach(bItem -> {
				File f = new File(file, bItem.getId() + ".yml");
				
				YamlConfiguration yml = YamlConfiguration.loadConfiguration(f);
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
