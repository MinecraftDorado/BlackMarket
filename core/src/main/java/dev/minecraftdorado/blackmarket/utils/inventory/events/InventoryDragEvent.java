package dev.minecraftdorado.blackmarket.utils.inventory.events;

import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import dev.minecraftdorado.blackmarket.utils.inventory.InventoryManager.Inv;

public class InventoryDragEvent extends inv_drag {

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public InventoryDragEvent(Player player, Inv inv, ItemStack cursor, ItemStack oldCursor, Map<Integer, ItemStack> newItems, Set<Integer> invSlot, Set<Integer> rawSlot, Inventory inv2, boolean usingCustomInv){
    	super(player, inv, cursor, oldCursor, newItems, invSlot, rawSlot, inv2, usingCustomInv);
    }
    
    private boolean isCancelled = true;
    
	public boolean isCancelled() {
		return isCancelled;
	}

	public void setCancelled(boolean toCancel) {
		isCancelled = toCancel;
	}
    
}
abstract class inv_drag extends Event implements Cancellable {
	private Player player;
	private Inv inv;
	private ItemStack cursor;
	private ItemStack oldCursor;
	private Map<Integer, ItemStack> newItems;
	private Set<Integer> invSlot;
	private Set<Integer> rawSlot;
	private Inventory inv2;
	private boolean usingCustomInv;
	
	public inv_drag(Player player, Inv inv, ItemStack cursor, ItemStack oldCursor, Map<Integer, ItemStack> newItems, Set<Integer> invSlot, Set<Integer> rawSlot, Inventory inv2, boolean usingCustomInv) {
		this.player = player;
		this.inv = inv;
		this.cursor = cursor;
		this.oldCursor = oldCursor;
		this.newItems = newItems;
		this.invSlot = invSlot;
		this.rawSlot = rawSlot;
		this.inv2 = inv2;
		this.usingCustomInv = usingCustomInv;
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public Inv getInv(){
		return inv;
	}
	
	public ItemStack getCursor() {
		return cursor;
	}
	
	public ItemStack getOldCursor() {
		return oldCursor;
	}
	
	public Map<Integer, ItemStack> getNewItems(){
		return newItems;
	}
	
	public Set<Integer> getInventorySlots(){
		return invSlot;
	}
	
	public Set<Integer> getRawSlots(){
		return rawSlot;
	}
	
	public Inventory getPlayerInventory() {
		return inv2;
	}
	
	public boolean usingCustomInv() {
		return usingCustomInv;
	}
}