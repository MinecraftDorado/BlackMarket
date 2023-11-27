package dev.minecraftdorado.blackmarket.utils.entities.hologram;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.decoration.ArmorStand;

public class Hologram {

	private Location location;
	private String text;
	private ArmorStand armorstand;
	private final Set<Player> viewers = new HashSet<>();

	public Hologram(Location location, String text) {
		this.location = location;
		this.text = text;
		
		ServerLevel level = ((CraftWorld) location.getWorld()).getHandle();
		
		armorstand = new ArmorStand(level, location.getX(), location.getY() + 2, location.getZ());
		armorstand.setInvisible(true);
		armorstand.setNoGravity(true);
		armorstand.setMarker(true);
		
		armorstand.setCustomNameVisible(true);
		setText(text);
	}

	public void display(Player... players) {
		for (Player player : players) {
			if (viewers.add(player)) {
				ServerGamePacketListenerImpl ps = ((CraftPlayer) player).getHandle().connection;
				ps.send(new ClientboundAddEntityPacket(armorstand));
				ps.send(new ClientboundSetEntityDataPacket(getEntityId(), armorstand.getEntityData().getNonDefaultValues()));
			}
		}
	}
	
	public void display() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (viewers.add(player)) {
				ServerGamePacketListenerImpl ps = ((CraftPlayer) player).getHandle().connection;
				ps.send(new ClientboundAddEntityPacket(armorstand));
				ps.send(new ClientboundSetEntityDataPacket(getEntityId(), armorstand.getEntityData().getNonDefaultValues()));
			}
		}
	}

	public void hide(Player... players) {
		for (Player player : players) {
			if (viewers.remove(player)) {
				ServerGamePacketListenerImpl ps = ((CraftPlayer) player).getHandle().connection;
				ps.send(new ClientboundRemoveEntitiesPacket(getEntityId()));
			}
		}
	}
	
	public void hide() {
		viewers.forEach(player ->{
			ServerGamePacketListenerImpl ps = ((CraftPlayer) player).getHandle().connection;
			ps.send(new ClientboundRemoveEntitiesPacket(getEntityId()));
		});
		viewers.clear();
	}
	
	public void setText(String text) {
		this.text = text;
		armorstand.setCustomName(CraftChatMessage.fromString(text, true)[0]);
	}

	public Location getLocation() {
		return location;
	}

	public String getText() {
		return text;
	}
	
	public int getEntityId() {
		return armorstand.getId();
	}
	
}