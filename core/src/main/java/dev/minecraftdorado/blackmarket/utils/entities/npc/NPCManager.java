package dev.minecraftdorado.blackmarket.utils.entities.npc;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.entity.Player;

import dev.minecraftdorado.blackmarket.utils.Config;

public class NPCManager {
	
	public HashMap<Integer, NPCAbs> list = new HashMap<>();
	public HashMap<Player, ArrayList<Integer>> npcList = new HashMap<>();
	
	public void add(NPCAbs npc) {
		list.put(npc.getEntityId(), npc);
	}
	
	public void remove(NPCAbs npc) {
		npc.hide();
		npc.getNameEntity().hide();
		list.remove(npc.getEntityId());
	}
	
	public void saveAll() {
		Config.saveNPCs(list.values());
	}
	
	public NPCManager() {
		
		for(NPCAbs npc : Config.getNPCs()) {
			npc.spawn();
			list.put(npc.getEntityId(), npc);
		}
	}
}
