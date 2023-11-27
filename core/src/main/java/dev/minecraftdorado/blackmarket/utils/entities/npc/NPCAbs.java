package dev.minecraftdorado.blackmarket.utils.entities.npc;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import dev.minecraftdorado.blackmarket.MainClass;
import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.entities.hologram.HologramAbs;
import dev.minecraftdorado.blackmarket.utils.entities.npc.skins.SkinData.Skin;

public abstract class NPCAbs {
	
	protected String name = Config.getMessage("npc.name");
	protected Skin skin;
	protected Location loc;
	protected int entityId;
	
	protected HologramAbs nameEntity;
	
	protected boolean spawned = false;
	
	protected final Set<Player> viewers = new HashSet<>();
	
	public abstract void spawn();
	
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
	
	public int getEntityId() {
		return entityId;
	}
	
	public HologramAbs getNameEntity() {
		return nameEntity;
	}
	
	public abstract void display(Player... players);
	
	protected byte toAngle(float v) {
		return (byte) ((int) (v * 256.0F / 360.0F));
	}
	
	public abstract void hide(Player... players);
	
	public void hide() {
		viewers.forEach(player -> hide(player));
		viewers.clear();
	}
	
	protected abstract void updateLocation();
	
	public abstract void updateHeadRotation(float yaw, float pitch);
	
	public Set<Player> getViewers(){
		return viewers;
	}
}
