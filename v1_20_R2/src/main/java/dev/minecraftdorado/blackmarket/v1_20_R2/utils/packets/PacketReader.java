package dev.minecraftdorado.blackmarket.v1_20_R2.utils.packets;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import dev.minecraftdorado.blackmarket.MainClass;
import dev.minecraftdorado.blackmarket.utils.ReflectionUtils;
import dev.minecraftdorado.blackmarket.utils.entities.npc.events.NPCInteractEvent;
import dev.minecraftdorado.blackmarket.utils.packets.PacketReaderAbs;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;

public class PacketReader extends PacketReaderAbs {
	
	public void inject(Player player) {
		
		Connection connection = getConnection(((CraftPlayer) player).getHandle().connection);
		ChannelPipeline pipeline = connection.channel.pipeline();
		
		ChannelHandler packet = pipeline.get("BMPacketInjector");
		
		if(packet != null) {
			pipeline.remove(packet);
		}
		
		pipeline.addAfter("decoder", "BMPacketInjector",
				new MessageToMessageDecoder<ServerboundInteractPacket>(){
					@Override
					protected void decode(ChannelHandlerContext ctx, ServerboundInteractPacket packet, List<Object> out) throws Exception {
						out.add(packet);
						readPackets(packet, player);
					}
				});
	}
	
	public void readPackets(Object packet, Player player) {
		
		// getting the entity id
		int entityId = (int) ReflectionUtils.getValue((ServerboundInteractPacket) packet, "a");
		
		// Entity isn't a NPC
		if(!MainClass.npcM.list.containsKey(entityId)) return;
		
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
	
	public Connection getConnection(final Object playerConnection) {
		try {
			if (connectionField == null) {
				connectionField = ServerCommonPacketListenerImpl.class.getDeclaredField("c");
				connectionField.setAccessible(true);
			}
			return (Connection) connectionField.get((ServerCommonPacketListenerImpl) playerConnection);
		} catch (final NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}