package dev.minecraftdorado.blackmarket.v1_20_R1.utils.entities;

import java.util.Collections;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.ReflectionUtils;
import dev.minecraftdorado.blackmarket.utils.entities.hologram.HologramAbs;
import dev.minecraftdorado.blackmarket.utils.entities.npc.NPCAbs;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class NPC extends NPCAbs {
	
	private ServerPlayer npc;
	
	
	public void spawn(){
		MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
		ServerLevel level = ((CraftWorld) loc.getWorld()).getHandle();
		
		GameProfile gameProfile = new GameProfile(UUID.randomUUID(), Config.getMessage("npc.click"));
		
		// Skin
		if(skin != null && skin.isPremium()) {
			gameProfile.getProperties().put("textures", new Property("textures", skin.getTexture(), skin.getSignature()));
		}
		
		npc = new ServerPlayer(server, level, gameProfile);
		entityId = npc.getId();
		
		if(Config.healthBar()) {
			loc.add(0, .32, 0);
		}
		
		npc.setPos(loc.getX(), loc.getY(), loc.getZ());
		
		spawned = true;
		
		nameEntity = (HologramAbs) ReflectionUtils.getClass("utils.entities.Hologram");
		nameEntity.spawn(loc.clone().add(0, .08, 0), name);
	}
	
	public void display(Player... players) {
		for (Player player : players) {
			if (viewers.add(player)) {
				
				ServerGamePacketListenerImpl ps = ((CraftPlayer) player).getHandle().connection;
				ps.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, npc));
				ps.send(new ClientboundAddPlayerPacket(npc));
				
				getNameEntity().display(player);
			}
		}
	}
	
	public void hide(Player... players) {
		for (Player player : players)
			if (viewers.remove(player)) {
				ServerGamePacketListenerImpl ps = ((CraftPlayer) player).getHandle().connection;
				
				ps.send(new ClientboundRemoveEntitiesPacket(getEntityId()));
				ps.send(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(npc.getUUID())));
				
				getNameEntity().hide(player);
			}
	}
	
	protected void updateLocation() {
		viewers.forEach(player -> {
			
			ServerGamePacketListenerImpl ps = npc.connection;
			
			ps.send(new ClientboundSetEntityDataPacket(npc.getId(), npc.getEntityData().getNonDefaultValues()));
			
		});
	}
	
	public void updateHeadRotation(float yaw, float pitch) {
		viewers.forEach(player -> {
			
			ServerGamePacketListenerImpl ps = ((CraftPlayer) player).getHandle().connection;
			
			ps.send(new ClientboundRotateHeadPacket(npc, toAngle(yaw)));
			ps.send(new ClientboundMoveEntityPacket.Rot(getEntityId(), toAngle(yaw), toAngle(pitch), false));
			
		});
	}
}
