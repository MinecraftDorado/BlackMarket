package dev.minecraftdorado.BlackMarket.Utils.Inventory.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import dev.minecraftdorado.BlackMarket.Utils.Inventory.InventoryManager.Inv;

public class InventoryClickEvent extends inv_click {

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public InventoryClickEvent(Player player, Inv inv, ItemStack item, int slot, InventoryAction ia, Inventory inv2, boolean usingCustomInv, ClickType ct){
        super(player, inv, item, slot, ia, inv2, usingCustomInv, ct);
    }
    
    private boolean isCancelled = true;
    
	public boolean isCancelled() {
		return isCancelled;
	}

	public void setCancelled(boolean toCancel) {
		isCancelled = toCancel;
	}
    
}
abstract class inv_click extends Event implements Cancellable {
	private Player player;
	private Inv inv;
	private ItemStack item;
	private int slot;
	private InventoryAction ia;
	private Inventory inv2;
	private boolean usingCustomInv;
	private ClickType ct;
	
	public inv_click(Player player, Inv inv, ItemStack item, int slot, InventoryAction ia, Inventory inv2, boolean usingCustomInv, ClickType ct) {
		this.player = player;
		this.inv = inv;
		this.item = item;
		this.slot = slot;
		this.ia = ia;
		this.inv2 = inv2;
		this.usingCustomInv = usingCustomInv;
		this.ct = ct;
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public Inv getInv(){
		return inv;
	}
	
	public ItemStack getItemStack() {
		return item;
	}
	
	public int getSlot() {
		return slot;
	}
	
	public InventoryAction getAction() {
		return ia;
	}
	
	public Inventory getPlayerInventory() {
		return inv2;
	}
	
	public boolean usingCustomInv() {
		return usingCustomInv;
	}
	
	public ClickType getClickType() {
		return ct;
	}
}