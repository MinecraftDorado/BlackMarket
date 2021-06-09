package dev.minecraftdorado.blackmarket.utils.entities.npc;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import dev.minecraftdorado.blackmarket.mainclass.MainClass;
import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.Utils;
import dev.minecraftdorado.blackmarket.utils.entities.hologram.Hologram;
import dev.minecraftdorado.blackmarket.utils.entities.npc.skins.SkinData.Skin;
import dev.minecraftdorado.blackmarket.utils.packets.Reflections;
import dev.minecraftdorado.blackmarket.utils.packets.ServerVersion;

public class NPC {
	
	private static final Class<?> EntityPlayer = Reflections.getNMSClass("EntityPlayer"),
			World = Reflections.getNMSClass("World"),
			CraftWorld = Reflections.getOBCClass("CraftWorld"),
			CraftServer = Reflections.getOBCClass("CraftServer"),
	        MinecraftServer = Reflections.getNMSClass("MinecraftServer"),
	        WorldServer = Reflections.getNMSClass("WorldServer"),
	        PlayerInteractManager = Reflections.getNMSClass("PlayerInteractManager"),
	        Entity = Reflections.getNMSClass("Entity"),
	        PacketPlayOutEntityDestroy = Reflections.getNMSClass("PacketPlayOutEntityDestroy"),
	        PacketPlayOutEntityMetadata = Reflections.getNMSClass("PacketPlayOutEntityMetadata"),
	        PacketPlayOutEntityTeleport = Reflections.getNMSClass("PacketPlayOutEntityTeleport"),
	        PacketPlayOutEntityLook = Reflections.getNMSClass("PacketPlayOutEntity$PacketPlayOutEntityLook"),
	        PacketPlayOutEntityHeadRotation = Reflections.getNMSClass("PacketPlayOutEntityHeadRotation"),
	        PacketPlayOutPlayerInfo = Reflections.getNMSClass("PacketPlayOutPlayerInfo"),
	        PacketPlayOutNamedEntitySpawn = Reflections.getNMSClass("PacketPlayOutNamedEntitySpawn"),
	        EntityHuman = Reflections.getNMSClass("EntityHuman"),
	        EnumPlayerInfoAction = Reflections.getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction"),
	        DataWatcher = Reflections.getNMSClass("DataWatcher"),
	        DataWatcherObject = Reflections.existNMSClass("DataWatcherObject") ? Reflections.getNMSClass("DataWatcherObject") : null,
	        DataWatcherSerializer = Reflections.existNMSClass("DataWatcherSerializer") ? Reflections.getNMSClass("DataWatcherSerializer") : null
	        ;
	
	private static Constructor<?> EntityPlayerConstructor = null,
	        PacketPlayOutEntityDestroyConstructor = null,
	        PacketPlayOutEntityMetadataConstructor = null,
	        PacketPlayOutEntityTeleportConstructor = null,
	        PacketPlayOutEntityHeadRotationConstructor = null,
	        PacketPlayOutPlayerInfoConstructor = null,
	        PacketPlayOutEntityLookConstructor = null,
	        PacketPlayOutNamedEntitySpawnConstructor = null,
	        DataWatcherObjectConstructor = null,
	        PlayerInteractManagerConstructor = null
	        ;
	
	private static Method setLocation = null,
			setPositionRotation = null,
			getId = null,
			getServer = null,
			getDataWatcher = null,
			set = null,
			getScoreboard = null,
			getObjective = null
			;
	
	private static Object a_field = null;
	private static int index = 16;
	
	static {
		try {
	        EntityPlayerConstructor = EntityPlayer.getConstructor(MinecraftServer, WorldServer, GameProfile.class, PlayerInteractManager);
	        setLocation = Entity.getMethod("setLocation", double.class, double.class, double.class, float.class, float.class);
	        setPositionRotation = Entity.getMethod("setPositionRotation", double.class, double.class, double.class, float.class, float.class);
	        getId = Entity.getMethod("getId");
	        
	        PacketPlayOutEntityDestroyConstructor = PacketPlayOutEntityDestroy.getConstructor(int[].class);
	        PacketPlayOutEntityMetadataConstructor = PacketPlayOutEntityMetadata.getConstructor(int.class, DataWatcher, boolean.class);
	        PacketPlayOutEntityTeleportConstructor = PacketPlayOutEntityTeleport.getConstructor(Entity);
	        PacketPlayOutEntityHeadRotationConstructor = PacketPlayOutEntityHeadRotation.getConstructor(Entity, byte.class);
	        
	        Class<?> ArrayEntityPlayer = Class.forName("[Lnet.minecraft.server." + ServerVersion.getVersion() + ".EntityPlayer;");
	        PacketPlayOutPlayerInfoConstructor = PacketPlayOutPlayerInfo.getConstructor(EnumPlayerInfoAction, ArrayEntityPlayer);
	        
	        getServer = CraftServer.getMethod("getServer");
	        getDataWatcher = Entity.getMethod("getDataWatcher");
	        
	        PacketPlayOutEntityLookConstructor = PacketPlayOutEntityLook.getConstructor(int.class, byte.class, byte.class, boolean.class);
	        
	        PacketPlayOutNamedEntitySpawnConstructor = PacketPlayOutNamedEntitySpawn.getConstructor(EntityHuman);
	        
	        Class<?> Scoreboard = Reflections.getNMSClass("Scoreboard");
        	getScoreboard = EntityPlayer.getMethod("getScoreboard");
        	getObjective = Scoreboard.getMethod("getObjective", String.class);
	        
	        
	        if(ServerVersion.getVersion().contains("1_14") || ServerVersion.getVersion().contains("1_15") || ServerVersion.getVersion().contains("1_16")) {
	        	PlayerInteractManagerConstructor = PlayerInteractManager.getConstructor(WorldServer);
	        }else
	        	PlayerInteractManagerConstructor = PlayerInteractManager.getConstructor(World);
	        
	        if(!ServerVersion.getVersion().contains("1_8")) {
	        	set = DataWatcher.getMethod("set", DataWatcherObject, Object.class);
	        	DataWatcherObjectConstructor = DataWatcherObject.getConstructor(int.class, DataWatcherSerializer);
	        }
        	
        	switch(ServerVersion.getVersion()) {
        	case "v1_8_R3":
        		index = 10;
        		break;
        	case "v1_9_R1":
        		index = 12;
        		a_field = net.minecraft.server.v1_9_R1.DataWatcherRegistry.a;
        		break;
        	case "v1_9_R2":
        		index = 12;
        		a_field = net.minecraft.server.v1_9_R2.DataWatcherRegistry.a;
        		break;
        	case "v1_10_R1":
        		index = 13;
        		a_field = net.minecraft.server.v1_10_R1.DataWatcherRegistry.a;
        		break;
        	case "v1_11_R1":
        		index = 13;
        		a_field = net.minecraft.server.v1_11_R1.DataWatcherRegistry.a;
        		break;
        	case "v1_12_R1":
        		index = 13;
        		a_field = net.minecraft.server.v1_12_R1.DataWatcherRegistry.a;
        		break;
        	case "v1_13_R1":
        		index = 14;
        		a_field = net.minecraft.server.v1_13_R1.DataWatcherRegistry.a;
        		break;
        	case "v1_13_R2":
        		index = 14;
        		a_field = net.minecraft.server.v1_13_R2.DataWatcherRegistry.a;
        		break;
        	case "v1_14_R1":
        		a_field = net.minecraft.server.v1_14_R1.DataWatcherRegistry.a;
        		break;
        	case "v1_15_R1":
        		a_field = net.minecraft.server.v1_15_R1.DataWatcherRegistry.a;
        		break;
        	case "v1_16_R1":
        		a_field = net.minecraft.server.v1_16_R1.DataWatcherRegistry.a;
        		break;
        	case "v1_16_R2":
        		a_field = net.minecraft.server.v1_16_R2.DataWatcherRegistry.a;
        		break;
        	case "v1_16_R3":
        		a_field = net.minecraft.server.v1_16_R3.DataWatcherRegistry.a;
        		break;
        	default:
        		Bukkit.getConsoleSender().sendMessage("§cServer version: " + ServerVersion.getVersion());
        		break;
        	}
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private Object entity;
	private int id;
	
	private String name = Config.getMessage("npc.name");
	private Skin skin;
	private Location loc;
	
	private Hologram nameEntity;
	
	private Object packetPlayOutPlayerInfo_add;
	private Object packetPlayOutPlayerInfo_remove;
	private Object packetPlayOutEntityDestroy;
	
	private boolean spawned = false;
	
	public NPC(Location loc) {
		this.loc = loc;
	}
	
	public void spawn(){
		GameProfile profile = new GameProfile(UUID.randomUUID(), Config.getMessage("npc.click"));
		if(skin != null && skin.isPremium())
			profile.getProperties().put("textures", new Property("textures",skin.getSkin()[0],skin.getSkin()[1]));
		
		try {
			Object server = getServer.invoke(CraftServer.cast(Bukkit.getServer()));
			Object world = Reflections.getHandle(CraftWorld.cast(loc.getWorld()));
			
			Object npc = EntityPlayerConstructor.newInstance(
					server,
					world,
					profile,
					PlayerInteractManagerConstructor
							.newInstance(world));
			setLocation.invoke(npc, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
			setPositionRotation.invoke(npc, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
			this.entity = npc;
			this.id = (int) getId.invoke(npc);
			
			Object array = Array.newInstance(EntityPlayer, 1);
	        Array.set(array, 0, npc);
			
			this.packetPlayOutPlayerInfo_add = PacketPlayOutPlayerInfoConstructor.newInstance(EnumPlayerInfoAction.getField("ADD_PLAYER").get(null),array);
			this.packetPlayOutPlayerInfo_remove = PacketPlayOutPlayerInfoConstructor.newInstance(EnumPlayerInfoAction.getField("REMOVE_PLAYER").get(null), array);
	        this.packetPlayOutEntityDestroy = PacketPlayOutEntityDestroyConstructor.newInstance((Object) new int[]{id});
	        spawned = true;
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
		nameEntity = new Hologram(loc.clone().add(0, .08, 0), name);
	}
	
	public void respawn() {
		Set<Player> v = viewers;
		hide();
		name = Config.getMessage("npc.name");
		nameEntity.hide();
		MainClass.npcM.list.remove(getId());
		spawn();
		MainClass.npcM.list.put(getId(), this);
		v.forEach(viewer -> display(viewer));
	}
	
	public boolean isSpawned() {
		return spawned;
	}
	
	public String getName() {
		return name;
	}
	
	public void setLocation(Location loc) {
		try {
			this.loc = loc;
			if(entity != null) {
				setLocation.invoke(entity, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
				setPositionRotation.invoke(entity, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
				updateLocation();
			}
		} catch (InvocationTargetException | IllegalAccessException | InstantiationException ignored) {}
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
	
	public Object getEntity() {
		return entity;
	}
	
	public int getId() {
		return id;
	}
	
	public Hologram getNameEntity() {
		return nameEntity;
	}
	
	private final Set<Player> viewers = new HashSet<>();
	
	public void display(Player... players) {
		try {
	         for (Player player : players) {
	            if (viewers.add(player)) {
	            	Utils.sendPacket(player, packetPlayOutPlayerInfo_add);
	            	Utils.sendPacket(player, PacketPlayOutNamedEntitySpawnConstructor.newInstance(getEntity()));
	            	
	            	if(a_field != null) {
	            		Object data = getDataWatcher.invoke(entity);
	            		set.invoke(data,
	            				DataWatcherObjectConstructor
	            					.newInstance(
	            							index,
	            							a_field
	            						),
	            				(byte) 127
	            				);
	            	}else if(ServerVersion.getVersion().equals("v1_8_R3"))
	            		((net.minecraft.server.v1_8_R3.DataWatcher) getDataWatcher.invoke(entity)).watch(index, (byte) 127);
	            	
	            	if(getObjective.invoke(getScoreboard.invoke(entity), "health") != null) {
	            		nameEntity.setLocation(getLocation().clone().add(0, .32, 0));
	            	}
	            	
	                updateMetadata(player);
	        		
	        		Bukkit.getScheduler().scheduleSyncDelayedTask(MainClass.main, new Runnable() {
	        			public void run() {
	        				Utils.sendPacket(player, packetPlayOutPlayerInfo_remove);
	        			}
	        		}, 60);
	            }
	         }
	      } catch (Exception ex) {
	    	  ex.printStackTrace();
	      }
	}
	
	private static byte toAngle(float v) {
		return (byte) ((int) (v * 256.0F / 360.0F));
	}
	
	public void hide(Player... players) {
		for (Player player : players)
			if (viewers.remove(player))
				Utils.sendPacket(player, packetPlayOutEntityDestroy);
	}
	
	public void hide() {
		viewers.forEach(p -> Utils.sendPacket(p, packetPlayOutEntityDestroy));
		viewers.clear();
	}
	
	private void updateMetadata(Player player) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Utils.sendPacket(player, PacketPlayOutEntityMetadataConstructor.newInstance(id, getDataWatcher.invoke(entity), true));
	}
	
	private void updateLocation() throws IllegalAccessException, InvocationTargetException, InstantiationException {
		for (Player player : viewers)
			Utils.sendPacket(player, PacketPlayOutEntityTeleportConstructor.newInstance(entity));
	}
	
	public void updateHeadRotation(float yaw, float pitch) {
		try {
			for (Player player : viewers) {
				Utils.sendPacket(player, PacketPlayOutEntityHeadRotationConstructor.newInstance(entity, toAngle(yaw)));
				Utils.sendPacket(player, PacketPlayOutEntityLookConstructor.newInstance(getId(), toAngle(yaw), toAngle(pitch), false));
			}
		}catch(Exception ex) {}
	}
	
	public Set<Player> getViewers(){
		return viewers;
	}
}
