package dev.minecraftdorado.blackmarket.utils.packets;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import dev.minecraftdorado.blackmarket.mainclass.MainClass;
import dev.minecraftdorado.blackmarket.utils.entities.npc.events.NPCInteractEvent;

public class PacketReader {
	
	private static PacketAdapter interactAt;
	
	public PacketReader() {
		interactAt = interactAt();
		
		ProtocolLibrary.getProtocolManager().addPacketListener(interactAt);
	}
	
	private PacketAdapter interactAt() {
		return new PacketAdapter(MainClass.main, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
			@Override
			public void onPacketSending(PacketEvent e) {}
			
			@Override
			public void onPacketReceiving(PacketEvent e) {
				PacketContainer packet = e.getPacket().deepClone();
				
				int entityId = packet.getIntegers().read(0);
				
				// Entity isn't a NPC
				if(!MainClass.npcM.list.containsKey(entityId)) return;
				
				Player player = e.getPlayer();
				
				ArrayList<Integer> list = new ArrayList<>();
				if(MainClass.npcM.npcList.containsKey(player)) list = MainClass.npcM.npcList.get(player);
				
				// Entity is already clicked
				if(list.contains(entityId)) return;
				
				list.add(entityId);
				MainClass.npcM.npcList.put(player, list);
				
				Bukkit.getScheduler().runTask(MainClass.main, () -> {
					NPCInteractEvent event = new NPCInteractEvent(player, MainClass.npcM.list.get(entityId));
					Bukkit.getPluginManager().callEvent(event);
				});
			}
		};
	}
	
	public static void stop() {
		ProtocolLibrary.getProtocolManager().removePacketListener(interactAt);
	}
}