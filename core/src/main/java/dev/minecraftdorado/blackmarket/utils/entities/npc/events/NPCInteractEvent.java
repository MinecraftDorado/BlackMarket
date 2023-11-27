package dev.minecraftdorado.blackmarket.utils.entities.npc.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import dev.minecraftdorado.blackmarket.utils.entities.npc.NPCAbs;

public class NPCInteractEvent extends NPC_Event{

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public NPCInteractEvent(Player player, NPCAbs npc){
        super(player, npc);
    }
    
}
abstract class NPC_Event extends Event {
	private Player player;
	private NPCAbs npc;
	
	public NPC_Event(Player player, NPCAbs npc) {
		this.player = player;
		this.npc = npc;
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public NPCAbs getNPC(){
		return npc;
	}
}
