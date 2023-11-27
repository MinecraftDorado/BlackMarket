package dev.minecraftdorado.blackmarket.v1_20_R1.utils.entities;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;

import dev.minecraftdorado.blackmarket.utils.entities.hologram.HologramAbs;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.decoration.ArmorStand;

public class Hologram extends HologramAbs {
	
	private ArmorStand armorstand;
	
	public void spawn(Location location, String text) {
		this.location = location;
		this.text = text;
		
		ServerLevel level = ((CraftWorld) location.getWorld()).getHandle();
		
		armorstand = new ArmorStand(level, location.getX(), location.getY() + 2, location.getZ());
		armorstand.setInvisible(true);
		armorstand.setNoGravity(true);
		armorstand.setMarker(true);
		
		armorstand.setCustomNameVisible(true);
		setText(text);
		
		display();
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

	public void hide(Player... players) {
		for (Player player : players) {
			if (viewers.remove(player)) {
				ServerGamePacketListenerImpl ps = ((CraftPlayer) player).getHandle().connection;
				ps.send(new ClientboundRemoveEntitiesPacket(getEntityId()));
			}
		}
	}
	
	public void setText(String text) {
		this.text = text;
		armorstand.setCustomName(CraftChatMessage.fromString(text, true)[0]);
	}
	
	public int getEntityId() {
		return armorstand.getId();
	}
	
}
