package dev.minecraftdorado.blackmarket.utils.packets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import dev.minecraftdorado.blackmarket.mainclass.MainClass;
import dev.minecraftdorado.blackmarket.utils.entities.npc.events.NPCInteractEvent;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class PacketReader {
	
	private static Class<?> CraftPlayer = Reflections.getOBCClass("entity.CraftPlayer");
	private static HashMap<Player, reader> list = new HashMap<>();
	
	public static reader get(Player player) {
		if(!list.containsKey(player))
			list.put(player, new reader(player));
		return list.get(player);
	}
    
    public static class reader {
    	
    	Player player;
        Channel channel;
    	
        
        public reader(Player player) {
        	this.player = player;
        	try {
        		Object cp = Reflections.getHandle(CraftPlayer.cast(this.player));
        		cp = Reflections.getField(cp, "playerConnection");
        		cp = Reflections.getField(cp, "networkManager");
        		this.channel = (Channel) Reflections.getField(cp, "channel");
        	}catch(Exception ex) {
        		ex.printStackTrace();
        	}
        }
        
        public void inject() {
        	if(channel.pipeline().get("PacketInjector") == null)
        		channel.pipeline().addAfter("decoder", "PacketInjector",new MessageToMessageDecoder<Object>() {
        			@Override
        			protected void decode(ChannelHandlerContext arg0,Object p,List<Object> arg2) throws Exception {
        				arg2.add(p);readPacket(p);
        			}
        		});
        }
        
        public void uninject(){
        	if(channel.pipeline().get("PacketInjector") != null)
        		channel.pipeline().remove("PacketInjector");
        }
        
        public void readPacket(Object packet){
        	if(packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayInUseEntity")){
        		int id = (Integer)Reflections.getField(packet, "a");
                
                if(!Reflections.getField(packet, "action").toString().equals("INTERACT_AT")) return;
                
                ArrayList<Integer> list = new ArrayList<>();
                if(MainClass.npcM.npcList.containsKey(player)) list = MainClass.npcM.npcList.get(player);
                
                if(list.contains(id)) return;
                list.add(id);
                MainClass.npcM.npcList.put(player, list);
                
                Bukkit.getScheduler().runTask(MainClass.main, () -> {
                	if(MainClass.npcM.list.containsKey(id)) {
                		NPCInteractEvent event = new NPCInteractEvent(player, MainClass.npcM.list.get(id));
                		Bukkit.getPluginManager().callEvent(event);
                	}
                });
        	}
        }
    }
}