package dev.minecraftdorado.blackmarket.utils.entities.npc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import dev.minecraftdorado.blackmarket.mainclass.MainClass;
import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.entities.hologram.Hologram;
import dev.minecraftdorado.blackmarket.utils.entities.npc.skins.SkinData.Skin;
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

public class NPC {
	
	private ServerPlayer npc;
	
	private String name = Config.getMessage("npc.name");
	private Skin skin;
	private Location loc;
	
	private Hologram nameEntity;
	
	private boolean spawned = false;
	
	public NPC(Location loc) {
		this.loc = loc;
	}
	
	public void spawn(){
		MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
		ServerLevel level = ((CraftWorld) loc.getWorld()).getHandle();
		
		GameProfile gameProfile = new GameProfile(UUID.randomUUID(), Config.getMessage("npc.click"));
		
		// Skin
		if(skin != null && skin.isPremium()) {
			gameProfile.getProperties().put("textures", new Property("textures", skin.getTexture(), skin.getSignature()));
		}
		
		npc = new ServerPlayer(server, level, gameProfile);
		
		if(Config.healthBar()) {
			loc.add(0, .32, 0);
		}
		
		npc.setPos(loc.getX(), loc.getY(), loc.getZ());
		
		spawned = true;
		nameEntity = new Hologram(loc.clone().add(0, .08, 0), name);
	}
	
	public void respawn() {
		Set<Player> v = viewers;
		hide();
		name = Config.getMessage("npc.name");
		nameEntity.hide();
		MainClass.npcM.list.remove(getEntityId());
		spawn();
		MainClass.npcM.list.put(getEntityId(), this);
		v.forEach(viewer -> display(viewer));
	}
	
	public boolean isSpawned() {
		return spawned;
	}
	
	public String getName() {
		return name;
	}
	
	public void setLocation(Location loc) {
		this.loc = loc;
		updateLocation();
	}
	
	public Location getLocation() {
		return loc;
	}
	
	public void setSkin(Skin skin) {
		this.skin = skin;
	}
	
	public Skin getSkin() {
		return skin;
	}
	
	public ServerPlayer getNPC() {
		return npc;
	}
	
	public int getEntityId() {
		return npc.getId();
	}
	
	public Hologram getNameEntity() {
		return nameEntity;
	}
	
	private final Set<Player> viewers = new HashSet<>();
	
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
	
	private static byte toAngle(float v) {
		return (byte) ((int) (v * 256.0F / 360.0F));
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
	
	public void hide() {
		viewers.forEach(player -> {
			
			ServerGamePacketListenerImpl ps = ((CraftPlayer) player).getHandle().connection;
			
			ps.send(new ClientboundRemoveEntitiesPacket(getEntityId()));
			ps.send(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(npc.getUUID())));
			
			getNameEntity().hide(player);
		});
		viewers.clear();
	}
	
	private void updateLocation() {
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
	
	public Set<Player> getViewers(){
		return viewers;
	}
}
