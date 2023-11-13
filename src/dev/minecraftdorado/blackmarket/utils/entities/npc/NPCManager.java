package dev.minecraftdorado.blackmarket.utils.entities.npc;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.entity.Player;

import dev.minecraftdorado.blackmarket.utils.Config;

public class NPCManager {
	
	public HashMap<Integer, NPC> list = new HashMap<>();
	public HashMap<Player, ArrayList<Integer>> npcList = new HashMap<>();
	
	public void add(NPC npc) {
		list.put(npc.getEntityId(), npc);
	}
	
	public void remove(NPC npc) {
		npc.hide();
		npc.getNameEntity().hide();
		list.remove(npc.getEntityId());
	}
	
	public void saveAll() {
		Config.saveNPCs(list.values());
	}
	
	public NPCManager() {
		
		for(NPC npc : Config.getNPCs()) {
			npc.spawn();
			list.put(npc.getEntityId(), npc);
		}
	}
}
