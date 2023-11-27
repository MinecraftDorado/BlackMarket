package dev.minecraftdorado.blackmarket.utils.entities.npc.skins;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.configuration.file.YamlConfiguration;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.minecraftdorado.blackmarket.mainclass.MainClass;
import dev.minecraftdorado.blackmarket.utils.Utils;

public class SkinData {
	
	private static HashMap<String, Skin> skinList = new HashMap<>();
	private static File file = new File(MainClass.main.getDataFolder() + "/skindata.yml");
	
	public SkinData() {
		
		YamlConfiguration conf;
		Utils.extract("resources/customskins.yml", "skindata.yml");
		
		conf = YamlConfiguration.loadConfiguration(file);
		for(String name : conf.getKeys(false))
			addSkin(new Skin(name, conf.getString(name + ".texture"), conf.getString(name + ".signature")));
		
		for(String key : conf.getKeys(false)) {
			addSkin(new Skin("skin_" + key, conf.getString(key + ".texture"), conf.getString(key + ".signature")));
		}
	}
	
	public static void addSkin(Skin skin) {
		if(!skinList.containsKey(skin.getName().toLowerCase())) {
			skinList.put(skin.getName().toLowerCase(), skin);
		}
	}
	
	public static boolean existSkin(String name) {
		return skinList.containsKey(name.toLowerCase());
	}
	
	public static Skin getSkin(String name) {
		if(!skinList.containsKey(name.toLowerCase())) skinList.put(name.toLowerCase(), new Skin(name.toLowerCase()));
		return skinList.get(name.toLowerCase());
	}
	
	public static ArrayList<Skin> getSkins(){
		ArrayList<Skin> list = new ArrayList<>();
		for(String name : skinList.keySet()) {
			list.add(skinList.get(name));
		}
		return list;
	}
	
	public static class Skin {
		
		private String name;
		private boolean premium = false;
		private String[] skin;
		
		public Skin(String name) {
			this.name = name;
			
			String[] skin = getFromName(name);
			
			if(skin[0] != null && skin[1] != null) {
				
				this.skin = skin;
				premium = true;
				
				if(file.exists()) {
					YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
					conf.set(name + ".texture", skin[0]);
					conf.set(name + ".signature", skin[1]);
					try {
						conf.save(file);
					} catch (IOException e) {
						MainClass.main.getLogger().severe(String.format("» Skin not loaded: " + name, MainClass.main.getDescription().getName()));
					}
				}
			}else {
				String texture = "eyJ0aW1lc3RhbXAiOjE1MDAzMjA5MDg1NDMsInByb2ZpbGVJZCI6IjMyMDZiYTRjOTY4ZDQyNGM4NzdkMTU0N2ZhOTc0ZDYwIiwicHJvZmlsZU5hbWUiOiJudWxsIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9hMmNlN2QxYWYxZDgyZTliOTUzZDdjNjkyODQxODJiOWZlMTM0ZTM1N2VjZDUzMjBkM2Q5ODM4Yzg4Y2Y5OCJ9fX0=";
    	    	String signature = "PBZ1RSmX8xIv1j+vqbt5dXW35/jsufI0L6aZtDixoALoPjWZZ7GT0GZ73TdN+sA1KmMaNu5EfJlCyoVKazynqQHmU9lokbGhGk68uuOGTQHetoSRKSQYhm6oe+AFWDSjGccRsOypBUArq7gyyosXzDPkPbb4CEJorffloOpvgYnXso7DvkvmsD1MdSGE5uuoFy3Qhfi68PnO+xnimEuTE6Nbx5GmgMGhJKrJu9FLdSNMVfA+7pcc5+0ibDTzU9BMrBcC4kNZiKmJMJqHoSuWyd5CKasoye7AXPmpUjCrAiFWf3Gy+Rwqc4146jSHeGDycgYQHuDVCFXpvpB3iBtmAFyWcy8trkOV6xs4hQ4MEuGYfPw2aS4r5x93N3hmp/DihI/uQsF3ybdCxhZYfFVX1lL/4XuSbN4IQRbCQLsvKZ1QEWcOuapLnKEVx4mRuy7cwfk5eYePcFuFj3GoKwPZBJRDiAr0iiA26f1Yq3RGkiSWuc45ycK2P4NxmZFSwFEodTdmUCG38BtDZa609hDVEY/mKg39oOtvBlaO0bK/3nE4vnXaiIUGapuAr0t8VUM5bO6IAEImiGWuG2W2JJCkCOMArdaJlRg2C+DjWVmZavkR9Arpfz5FDfzeLO3RYNQJxrxqKc14js2Xl0puOuhWmd24jz02Z6AMK3Kko/xM93g=";
    			skin = new String[] {texture, signature};
			}
		}
		
		public Skin(String name, String texture, String signature) {
			premium = true;
			this.name = name;
			skin = new String[] {texture, signature};
		}
		
		public boolean isPremium() {
			return premium;
		}
		
		public String[] getSkin() {
			return skin;
		}
		
		public String getTexture() {
			return skin[0];
		}
		
		public String getSignature() {
			return skin[1];
		}
		
		public String getName() {
			return name;
		}
	}
    
    private static String[] getFromName(String name) {
    	String texture = null, signature = null;
    	boolean ready = false;
    	
    	int tries = 2;
    	
    	while (ready == false) {
    		if(tries != 0) {
    			tries --;
    			try {
    				String uuid;
    				try {
	    				URL url_0 = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
	    				InputStreamReader reader_0 = new InputStreamReader(url_0.openStream());
	    				uuid = new JsonParser().parse(reader_0).getAsJsonObject().get("id").getAsString();
	    			}catch(Exception ex) {
	    				MainClass.main.getLogger().severe(String.format("» Player not premium: " + name, MainClass.main.getDescription().getName()));
	    				break;
	    			}

					URL url_1 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
					InputStreamReader reader_1 = new InputStreamReader(url_1.openStream());
					JsonObject textureProperty = new JsonParser().parse(reader_1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
					
					texture = textureProperty.get("value").getAsString();
					signature = textureProperty.get("signature").getAsString();
    				ready = true;
    			} catch (IOException e) {
    				e.printStackTrace();
    				ready = false;
    			}
    		}else
    			ready = true;
		}
    	return new String[] {texture, signature};
    }
}