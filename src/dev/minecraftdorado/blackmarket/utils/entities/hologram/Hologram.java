package dev.minecraftdorado.blackmarket.utils.entities.hologram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

public class Hologram {

	private Location location;
	private String text;
	private final int entityId;
	private final PacketContainer packetPlayOutSpawnEntityLiving;
	private final PacketContainer packetPlayOutEntityDestroy;
	private PacketContainer packetPlayEntityMetadata;
	private final Set<Player> viewers = new HashSet<>();
	
	private ProtocolManager protocolManager;

	public Hologram(Location location, String text) {
		this.protocolManager = ProtocolLibrary.getProtocolManager();
		
		this.location = location;
		this.text = text;
		this.entityId = UUID.randomUUID().variant();
		
		packetPlayOutSpawnEntityLiving = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
		
		// Crucial data, do not remove
		packetPlayOutSpawnEntityLiving.getIntegers().write(0, entityId);
		packetPlayOutSpawnEntityLiving.getUUIDs().write(0, UUID.randomUUID());
		packetPlayOutSpawnEntityLiving.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);
		
		packetPlayOutSpawnEntityLiving.getDoubles()
		.write(0, location.getX())
		.write(1, location.getY())
		.write(2, location.getZ());
		
		packetPlayOutEntityDestroy = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
		packetPlayOutEntityDestroy.getIntLists().write(0, Arrays.asList(entityId));
		
		// Generete armorstand metadata
		genereteMetadata();
	}
	
	private void genereteMetadata() {
		packetPlayEntityMetadata = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
		packetPlayEntityMetadata.getIntegers().write(0, this.entityId);
        
        WrappedDataWatcher watcher = new WrappedDataWatcher();
        
        // Invisibility
        watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20);
        
        // Custom name
        Optional<?> name = Optional.of(WrappedChatComponent.fromChatMessage(this.text)[0].getHandle());
        watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true)), name);
        
        // Display custom name
        watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class)), true);
        
        // Set attributes
        List<WrappedDataValue> wrappedDataValueList = new ArrayList<>();
        watcher.getWatchableObjects().stream().filter(Objects::nonNull).forEach(entry -> {
            WrappedDataWatcher.WrappedDataWatcherObject dataWatcherObject = entry.getWatcherObject();
            wrappedDataValueList.add(new WrappedDataValue(dataWatcherObject.getIndex(), dataWatcherObject.getSerializer(), entry.getRawValue()));
        });
        packetPlayEntityMetadata.getDataValueCollectionModifier().write(0, wrappedDataValueList);
    }

	public void display(Player... players) {
		for (Player player : players) {
			if (viewers.add(player)) {
				protocolManager.sendServerPacket(player, packetPlayOutSpawnEntityLiving);
				protocolManager.sendServerPacket(player, packetPlayEntityMetadata);
			}
		}
	}
	
	public void display() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (viewers.add(player)) {
				protocolManager.sendServerPacket(player, packetPlayOutSpawnEntityLiving);
				protocolManager.sendServerPacket(player, packetPlayEntityMetadata);
			}
		}
	}

	public void hide(Player... players) {
		for (Player player : players) {
			if (viewers.remove(player)) {
				protocolManager.sendServerPacket(player, packetPlayOutEntityDestroy);
			}
		}
	}
	
	public void hide() {
		viewers.forEach(player ->{
			protocolManager.sendServerPacket(player, packetPlayOutEntityDestroy);
		});
		viewers.clear();
	}
	
	public void setText(String text) {
		this.text = text;
		genereteMetadata();
	}

	public Location getLocation() {
		return location;
	}

	public String getText() {
		return text;
	}
	
}