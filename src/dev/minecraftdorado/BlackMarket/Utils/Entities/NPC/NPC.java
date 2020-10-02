package dev.minecraftdorado.BlackMarket.Utils.Entities.NPC;

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

import dev.minecraftdorado.BlackMarket.MainClass.MainClass;
import dev.minecraftdorado.BlackMarket.Utils.Utils;
import dev.minecraftdorado.BlackMarket.Utils.Entities.NPC.Skins.SkinData.Skin;
import dev.minecraftdorado.BlackMarket.Utils.Packets.Reflections;
import dev.minecraftdorado.BlackMarket.Utils.Packets.ServerVersion;

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
			set = null
			;
	
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
	        
	        
	        if(!ServerVersion.getVersion().contains("1_8")) {
	        	PlayerInteractManagerConstructor = PlayerInteractManager.getConstructor(WorldServer);
	        	set = DataWatcher.getMethod("set", DataWatcherObject, Object.class);
	        	DataWatcherObjectConstructor = DataWatcherObject.getConstructor(int.class, DataWatcherSerializer);
	        }else {
	        	PlayerInteractManagerConstructor = PlayerInteractManager.getConstructor(World);
	        }
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private Object entity;
	private int id;
	
	private String name;
	private Skin skin;
	private Location loc;
	
	private Object packetPlayOutPlayerInfo_add;
	private Object packetPlayOutPlayerInfo_remove;
	private Object packetPlayOutEntityDestroy;
	
	private boolean spawned = false;
	
	public NPC(String name) {
		this.name = name;
	}
	
	public void spawn(Location loc){
		this.loc = loc;
		GameProfile profile = new GameProfile(UUID.randomUUID(), "§eClick here!");
		try {
			profile.getProperties().put("textures", new Property("textures",skin.getSkin()[0],skin.getSkin()[1]));
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
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
	
	private final Set<Player> viewers = new HashSet<>();
	
	public void display(Player... players) {
		try {
	         for (Player player : players) {
	            if (viewers.add(player)) {
	            	Utils.sendPacket(player, packetPlayOutPlayerInfo_add);
	            	Utils.sendPacket(player, PacketPlayOutNamedEntitySpawnConstructor.newInstance(getEntity()));
	            	
	            	Object a_field = null;
	            	int index = 16;
	            	
	            	switch(ServerVersion.getVersion()) {
	            	case "v1_8_R3":
	            		index = 10;
	            		((net.minecraft.server.v1_8_R3.DataWatcher) getDataWatcher.invoke(entity)).watch(index, (byte) 127);
	            		break;
	            	case "v1_9_R1":
	            		a_field = net.minecraft.server.v1_9_R1.DataWatcherRegistry.a;
	            		break;
	            	case "v1_9_R2":
	            		a_field = net.minecraft.server.v1_9_R2.DataWatcherRegistry.a;
	            		break;
	            	case "v1_10_R1":
	            		a_field = net.minecraft.server.v1_10_R1.DataWatcherRegistry.a;
	            		break;
	            	case "v1_11_R1":
	            		a_field = net.minecraft.server.v1_11_R1.DataWatcherRegistry.a;
	            		break;
	            	case "v1_12_R1":
	            		a_field = net.minecraft.server.v1_12_R1.DataWatcherRegistry.a;
	            		break;
	            	case "v1_13_R1":
	            		a_field = net.minecraft.server.v1_13_R1.DataWatcherRegistry.a;
	            		break;
	            	case "v1_13_R2":
	            		a_field = net.minecraft.server.v1_13_R2.DataWatcherRegistry.a;
	            		break;
	            	case "v1_14_R1":
	            		a_field = net.minecraft.server.v1_14_R1.DataWatcherRegistry.a;
	            		break;
	            	case "v1_15_R1":
	            		a_field = net.minecraft.server.v1_15_R1.DataWatcherRegistry.a;
	            		index = 16;
	            		break;
	            	case "v1_16_R1":
	            		a_field = net.minecraft.server.v1_16_R1.DataWatcherRegistry.a;
	            		break;
	            	case "v1_16_R2":
	            		a_field = net.minecraft.server.v1_16_R2.DataWatcherRegistry.a;
	            		break;
	            	default:
	            		Bukkit.getConsoleSender().sendMessage("§cServer version: " + ServerVersion.getVersion());
	            		break;
	            	}
	            	
	            	if(a_field != null) {
	            		Object data = getDataWatcher.invoke(entity);
	            		set.invoke(data,
	            				DataWatcherObjectConstructor.newInstance(index,
	            						a_field
	            						),
	            				(byte) 127
	            				);
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
