package dev.minecraftdorado.blackmarket.utils.inventory.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import dev.minecraftdorado.blackmarket.utils.inventory.InventoryManager.Inv;

public class InventoryCloseEvent extends inv_close {

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public InventoryCloseEvent(Player player, Inv inv){
        super(player, inv);
    }
    
}
abstract class inv_close extends Event {
	private Player player;
	private Inv inv;
	
	public inv_close(Player player, Inv inv) {
		this.player = player;
		this.inv = inv;
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public Inv getInv(){
		return inv;
	}
}