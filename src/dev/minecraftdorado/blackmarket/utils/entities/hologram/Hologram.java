package dev.minecraftdorado.blackmarket.utils.entities.hologram;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import dev.minecraftdorado.blackmarket.utils.Utils;
import dev.minecraftdorado.blackmarket.utils.packets.Reflections;
import dev.minecraftdorado.blackmarket.utils.packets.ServerVersion;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Hologram {

   private static class IChatBaseComponent {

      private static final Logger logger = Logger.getLogger(IChatBaseComponent.class.getName());
      private static final Class<?> IChatBaseComponent = Reflections.getNMSClass("IChatBaseComponent");
      private static Method newIChatBaseComponent = null;

      static {
         try {
            newIChatBaseComponent = IChatBaseComponent.getDeclaredClasses()[0].getMethod("a", String.class);
         } catch (NoSuchMethodException e) {
            logger.log(Level.SEVERE, "An error occurred while initializing IChatBaseComponent.");
         }
      }

      private static Object of(String string) throws InvocationTargetException, IllegalAccessException {
         return newIChatBaseComponent.invoke(null, "{\"text\": \"" + string + "\"}");
      }
   }
   
   private static final Class<?> getEnumItemSlot(){
	   try{
		   return Reflections.getNMSClass("EnumItemSlot");
	   }catch(Exception ex) {
		   return null;
	   }
   }

   private static final Class<?> CraftWorld = Reflections.getOBCClass("CraftWorld"),
         World = Reflections.getNMSClass("World"),
         EntityArmorStand = Reflections.getNMSClass("EntityArmorStand"),
         PacketPlayOutSpawnEntityLiving = Reflections.getNMSClass("PacketPlayOutSpawnEntityLiving"),
         PacketPlayOutEntityDestroy = Reflections.getNMSClass("PacketPlayOutEntityDestroy"),
         PacketPlayOutEntityMetadata = Reflections.getNMSClass("PacketPlayOutEntityMetadata"),
         PacketPlayOutEntityTeleport = Reflections.getNMSClass("PacketPlayOutEntityTeleport"),
         PacketPlayOutEntityEquipment = Reflections.getNMSClass("PacketPlayOutEntityEquipment"),
         Entity = Reflections.getNMSClass("Entity"),
         DataWatcher = Reflections.getNMSClass("DataWatcher"),
         EntityLiving = Reflections.getNMSClass("EntityLiving"),
         ItemStack = Reflections.getNMSClass("ItemStack"),
         CraftItemStack = Reflections.getOBCClass("inventory.CraftItemStack");
   private static Constructor<?> EntityArmorStandConstructor = null,
         PacketPlayOutSpawnEntityLivingConstructor = null,
         PacketPlayOutEntityDestroyConstructor = null,
         PacketPlayOutEntityMetadataConstructor = null,
         PacketPlayOutEntityTeleportConstructor = null,
         PacketPlayOutEntityEquipmentConstructor = null;
   private static Method setInvisible = null, setCustomNameVisible = null,
         setCustomName = null, getId = null, getDataWatcher = null,
         setLocation = null, setSlot = null, setNoGravity = null, setGravity = null;

   static {
      try {
         EntityArmorStandConstructor = EntityArmorStand.getConstructor(World, double.class, double.class, double.class);
         PacketPlayOutSpawnEntityLivingConstructor = PacketPlayOutSpawnEntityLiving.getConstructor(EntityLiving);
         PacketPlayOutEntityDestroyConstructor = PacketPlayOutEntityDestroy.getConstructor(int[].class);
         PacketPlayOutEntityMetadataConstructor = PacketPlayOutEntityMetadata.getConstructor(int.class, DataWatcher, boolean.class);
         PacketPlayOutEntityTeleportConstructor = PacketPlayOutEntityTeleport.getConstructor(Entity);
         if(getEnumItemSlot() != null)
        	 try {
        		 PacketPlayOutEntityEquipmentConstructor = PacketPlayOutEntityEquipment.getConstructor(int.class, getEnumItemSlot(), ItemStack);
        	 } catch (NoSuchMethodException x) {
        		 PacketPlayOutEntityEquipmentConstructor = PacketPlayOutEntityEquipment.getConstructor(int.class, List.class);
        	 }
         else
        	 PacketPlayOutEntityEquipmentConstructor = PacketPlayOutEntityEquipment.getConstructor(int.class, int.class, ItemStack);
         setInvisible = EntityArmorStand.getMethod("setInvisible", boolean.class);
         try {
        	 setCustomNameVisible = EntityArmorStand.getMethod("setCustomNameVisible", boolean.class);
         } catch (NoSuchMethodException x) {
        	 setCustomNameVisible = Entity.getMethod("setCustomNameVisible", boolean.class);
         }
         setLocation = Entity.getMethod("setLocation", double.class, double.class, double.class, float.class, float.class);
         if(getEnumItemSlot() != null)
        	 setSlot = EntityArmorStand.getMethod("setSlot", getEnumItemSlot(), ItemStack);
         else
        	 setSlot = EntityArmorStand.getMethod("setEquipment", int.class, ItemStack);
         try {
        	 setNoGravity = EntityArmorStand.getMethod("setNoGravity", boolean.class);
         } catch (NoSuchMethodException x) {
        	 setGravity = EntityArmorStand.getMethod("setGravity", boolean.class);
         }
         try {
            setCustomName = EntityArmorStand.getMethod("setCustomName", String.class);
         } catch (NoSuchMethodException x) {
            try{
            	setCustomName = EntityArmorStand.getMethod("setCustomName", IChatBaseComponent.IChatBaseComponent);
            } catch (NoSuchMethodException x2) {
            	setCustomName = Entity.getMethod("setCustomName", IChatBaseComponent.IChatBaseComponent);
            }
         }
         getId = EntityArmorStand.getMethod("getId");
         getDataWatcher = Entity.getMethod("getDataWatcher");
      } catch (NoSuchMethodException ignored) {
    	  ignored.printStackTrace();
      }
   }

   private Location location;
   private String text;
   private final Object armorStand;
   private final int id;
   private final Object packetPlayOutSpawnEntityLiving;
   private final Object packetPlayOutEntityDestroy;
   private final Set<Player> viewers = new HashSet<>();
   
   private Object head;

   public Hologram(Location location, String text) {
      this.location = location;
      this.text = text;
      try {
         this.armorStand = EntityArmorStandConstructor.newInstance(Reflections.getHandle(CraftWorld.cast(location.getWorld())), location.getX(), location.getY(), location.getZ());
         setInvisible.invoke(armorStand, true);
         setCustomNameVisible.invoke(armorStand, true);
         if (setCustomName.getParameterTypes()[0].equals(String.class)) {
            setCustomName.invoke(armorStand, text);
         } else {
            setCustomName.invoke(armorStand, IChatBaseComponent.of(text));
         }
         this.id = (int) getId.invoke(armorStand);
         this.packetPlayOutSpawnEntityLiving = PacketPlayOutSpawnEntityLivingConstructor.newInstance(armorStand);
         this.packetPlayOutEntityDestroy = PacketPlayOutEntityDestroyConstructor.newInstance((Object) new int[]{id});
      } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
         throw new RuntimeException("An error occurred while creating the hologram.", e);
      }
   }

   public void display(Player... players) {
      try {
         for (Player player : players) {
            if (viewers.add(player)) {
               Utils.sendPacket(player, packetPlayOutSpawnEntityLiving);
               updateMetadata(player);
               updateHead();
            }
         }
      } catch (InvocationTargetException | IllegalAccessException | InstantiationException | IllegalArgumentException | NoSuchFieldException | SecurityException | NoSuchMethodException ignored) {
      }
   }
   
   public void display() {
	      try {
	         for (Player player : Bukkit.getOnlinePlayers()) {
	            if (viewers.add(player)) {
	            	Utils.sendPacket(player, packetPlayOutSpawnEntityLiving);
	               updateMetadata(player);
	               updateHead();
	            }
	         }
	      } catch (InvocationTargetException | IllegalAccessException | InstantiationException | IllegalArgumentException | NoSuchFieldException | SecurityException | NoSuchMethodException ignored) {
	      }
	   }

   public void hide(Player... players) {
      for (Player player : players) {
         if (viewers.remove(player)) {
        	 Utils.sendPacket(player, packetPlayOutEntityDestroy);
         }
      }
   }
   
   public void hide() {
	   viewers.forEach(player ->{
		   Utils.sendPacket(player, packetPlayOutEntityDestroy);
	   });
   }

   public void setLocation(Location location) {
      try {
         setLocation.invoke(armorStand, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
         this.location = location;
         updateLocation();
      } catch (InvocationTargetException | IllegalAccessException | InstantiationException ignored) {
      }
   }
   
   public void setHead(ItemStack item) {
	   try {
		   this.head = item;
		   if(getEnumItemSlot() != null)
			   setSlot.invoke(armorStand, getEnumItemSlot().getEnumConstants()[5], CraftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, head));
		   else
			   setSlot.invoke(armorStand, 4, CraftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, head));
		   updateHead();
	   } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException | NoSuchMethodException | InstantiationException | NoSuchFieldException e) {
		   e.printStackTrace();
	   }
   }

   public void setText(String text) {
      try {
         if (setCustomName.getParameterTypes()[0].equals(String.class)) {
            setCustomName.invoke(armorStand, text);
         } else {
            setCustomName.invoke(armorStand, IChatBaseComponent.of(text));
         }
         this.text = text;
         updateMetadata();
      } catch (InvocationTargetException | IllegalAccessException | InstantiationException ignored) {
      }
   }

   public Location getLocation() {
      return location;
   }

   public String getText() {
      return text;
   }
   
   public void setNoGravity(Boolean value) {
	   try {
		   if(setNoGravity != null)
			   setNoGravity.invoke(armorStand, value);
		   else
			   setGravity.invoke(armorStand, !value);
	   } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		   e.printStackTrace();
	   }
   }
   
   public Object getArmorStand() {
	   return armorStand;
   }

   private void updateMetadata() throws IllegalAccessException, InvocationTargetException, InstantiationException {
      Object packet = PacketPlayOutEntityMetadataConstructor.newInstance(id, getDataWatcher.invoke(armorStand), true);
      for (Player player : viewers) {
    	  Utils.sendPacket(player, packet);
      }
   }

   private void updateMetadata(Player player) throws IllegalAccessException, InvocationTargetException, InstantiationException {
	   Utils.sendPacket(player, PacketPlayOutEntityMetadataConstructor.newInstance(id, getDataWatcher.invoke(armorStand), true));
   }

   private void updateLocation() throws IllegalAccessException, InvocationTargetException, InstantiationException {
      for (Player player : viewers) {
    	  Utils.sendPacket(player, PacketPlayOutEntityTeleportConstructor.newInstance(armorStand));
      }
   }
   
   private void updateHead() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, SecurityException, NoSuchMethodException {
	   if(head != null) {
		   Object packet;
		   if(getEnumItemSlot() != null)
			   try {
				   packet = PacketPlayOutEntityEquipmentConstructor.newInstance(id, getEnumItemSlot().getEnumConstants()[5], CraftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, head));
			   }catch(Exception ex) {
				   if(ServerVersion.getVersion().contains("1_16_R1")) {
					   List<Pair<net.minecraft.server.v1_16_R1.EnumItemSlot, net.minecraft.server.v1_16_R1.ItemStack>> list = Lists.newArrayList();
					   list.add(
							   Pair.of(
									   net.minecraft.server.v1_16_R1.EnumItemSlot.HEAD,
									   (net.minecraft.server.v1_16_R1.ItemStack) CraftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, head)
									   ));
					   packet = PacketPlayOutEntityEquipmentConstructor.newInstance(id, list);
				   }else {
					   List<Pair<net.minecraft.server.v1_16_R2.EnumItemSlot, net.minecraft.server.v1_16_R2.ItemStack>> list = Lists.newArrayList();
					   list.add(
							   Pair.of(
									   net.minecraft.server.v1_16_R2.EnumItemSlot.HEAD,
									   (net.minecraft.server.v1_16_R2.ItemStack) CraftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, head)
									   ));
					   packet = PacketPlayOutEntityEquipmentConstructor.newInstance(id, list);

				   }
			   }
		   else
			   packet = PacketPlayOutEntityEquipmentConstructor.newInstance(id, 4, CraftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, head));
		   for (Player player : viewers) {
			   Utils.sendPacket(player, packet);
		   }
	   }
   }
}