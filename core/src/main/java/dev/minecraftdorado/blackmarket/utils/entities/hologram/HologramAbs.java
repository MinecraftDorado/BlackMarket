package dev.minecraftdorado.blackmarket.utils.entities.hologram;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class HologramAbs {
	
	protected Location location;
	protected String text;
	protected Set<Player> viewers = new HashSet<>();
	
	public abstract void spawn(Location loc, String text);
	
	public abstract void display(Player... players);
	
	public void display() {
		Bukkit.getOnlinePlayers().forEach(player -> display(player));
	}
	
	public abstract void hide(Player... players);
	
	public void hide() {
		viewers.forEach(player -> hide(player));
		viewers.clear();
	}
	
	public abstract void setText(String text);
	
	public String getText() {
		return text;
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	public abstract int getEntityId();
}
