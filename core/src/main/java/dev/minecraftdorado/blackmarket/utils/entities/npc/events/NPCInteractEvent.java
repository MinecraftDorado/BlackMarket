package dev.minecraftdorado.blackmarket.utils.entities.npc.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import dev.minecraftdorado.blackmarket.utils.entities.npc.NPC;

public class NPCInteractEvent extends NPC_Event{

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public NPCInteractEvent(Player player, NPC npc){
        super(player, npc);
    }
    
}
abstract class NPC_Event extends Event {
	private Player player;
	private NPC npc;
	
	public NPC_Event(Player player, NPC npc) {
		this.player = player;
		this.npc = npc;
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public NPC getNPC(){
		return npc;
	}
}
