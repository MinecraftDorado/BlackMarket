package dev.minecraftdorado.BlackMarket.Utils.Entities.Hologram;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import dev.minecraftdorado.BlackMarket.MainClass.MainClass;

public class HologramManager {
	
	private static HashMap<Player, ArrayList<Hologram>> loaded = new HashMap<>();
	private static ArrayList<Hologram> list = new ArrayList<>();
	
	public HologramManager() {
		Bukkit.getScheduler().runTaskTimer(MainClass.main, ()->{
			
			list.forEach(holo -> {
				Bukkit.getOnlinePlayers().forEach(player ->{
					if(holo.getLocation().distance(player.getLocation()) < 50) {
						ArrayList<Hologram> l = loaded.containsKey(player) ? loaded.get(player) : new ArrayList<>();
						l.add(holo);
						loaded.put(player, l);
						
						holo.display(player);
					}else if(loaded.containsKey(player)){
						ArrayList<Hologram> l = loaded.get(player);
						l.remove(holo);
						loaded.put(player, l);
						holo.hide(player);
					}
				});
			});
		}, 20, 20);
	}
	
	public void add(Hologram holo) {
		list.add(holo);
	}
	
	public void remove(Hologram holo) {
		list.remove(holo);
	}
}
