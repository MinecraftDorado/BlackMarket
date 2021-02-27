package dev.minecraftdorado.blackmarket.utils.entities.npc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import dev.minecraftdorado.blackmarket.mainclass.MainClass;
import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.packets.Reflections;

public class NPCManager {
	
	public HashMap<Integer, NPC> list = new HashMap<>();
	public HashMap<Player, ArrayList<Integer>> npcList = new HashMap<>();
	
	public void add(NPC npc) {
		list.put(npc.getId(), npc);
	}
	
	public void remove(NPC npc) {
		npc.hide();
		npc.getNameEntity().hide();
		list.remove(npc.getId());
	}
	
	private static HashMap<Player, ArrayList<NPC>> loaded = new HashMap<>();
	
	public void saveAll() {
		Config.saveNPCs(list.values());
	}
	
	Location a = null;
	@SuppressWarnings("unchecked")
	public NPCManager() {
		
		for(NPC npc : Config.getNPCs()) {
			npc.spawn();
			list.put(npc.getId(), npc);
		}
		
		/*
		 * 	Clicks clear - Display/hide npc
		 */
		
		Bukkit.getScheduler().runTaskTimer(MainClass.main, ()->{
			npcList.clear();
			list.values().forEach(npc -> {
				Bukkit.getOnlinePlayers().forEach(player ->{
					if(npc.getLocation().getWorld().equals(player.getWorld()) && npc.getLocation().distance(player.getLocation()) < 50) {
						ArrayList<NPC> l = loaded.containsKey(player) ? loaded.get(player) : new ArrayList<>();
						l.add(npc);
						loaded.put(player, l);
						npc.display(player);
					}else if(loaded.containsKey(player)){
						ArrayList<NPC> l = loaded.get(player);
						l.remove(npc);
						loaded.put(player, l);
						npc.hide(player);
					}
				});
			});
		}, 20, 20);
		
		/*
		 *	Head rotation
		 */
		
		Bukkit.getScheduler().runTaskTimer(MainClass.main, ()->{
			list.values().forEach(npc -> {
				if(npc.isSpawned()) {
					a = null;
					
					try {
						Class<?> EntityPlayer = Reflections.getNMSClass("EntityPlayer");
						Method getBukkitEntity = EntityPlayer.getMethod("getBukkitEntity");
						
						Class<?> CraftEntity = Reflections.getOBCClass("entity.CraftEntity");
						Method getNearbyEntities = CraftEntity.getMethod("getNearbyEntities", double.class, double.class, double.class);
						
						for(Entity e : (List<Entity>) getNearbyEntities.invoke(getBukkitEntity.invoke(EntityPlayer.cast(npc.getEntity())), 10, 5, 10)) {
							if(e.getType().equals(EntityType.PLAYER)) {
								if(a == null || npc.getLocation().distance(a) > npc.getLocation().distance(e.getLocation()))
									a = e.getLocation();
							}
						}
						
						if(a != null) {
							a = npc.getLocation().setDirection(a.subtract(npc.getLocation()).toVector());
							npc.updateHeadRotation(a.getYaw(), a.getPitch());
						}
					}catch(Exception ex) {
						ex.printStackTrace();
					}
				}
			});
		}, 5, 5);
	}
}
