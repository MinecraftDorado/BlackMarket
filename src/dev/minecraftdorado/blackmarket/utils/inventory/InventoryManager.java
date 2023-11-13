package dev.minecraftdorado.blackmarket.utils.inventory;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.minecraftdorado.blackmarket.mainclass.MainClass;
import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.market.BlackItem;

public class InventoryManager implements Listener {
	
	private static HashMap<Player, ArrayList<Inv>> history = new HashMap<>();
	
	public static boolean hasHistory(Player player) {
		return history.containsKey(player);
	}
	
	public static ArrayList<Inv> getHistory(Player player){
		if(!hasHistory(player))
				history.put(player, new ArrayList<Inv>());
		return history.get(player);
	}
	
	public static Inv getLastInv(Player player) {
		ArrayList<Inv> history = getHistory(player);
		return history.size() > 0 ? history.get(history.size()-1) : null;
	}
	
	public static void openInventory(Player player, Inv inv) {
		ArrayList<Inv> a = getHistory(player);
		if(a.size() > 0)
			if(!inv.getTitle().equals(getLastInv(player).getTitle())) {
				dev.minecraftdorado.blackmarket.utils.inventory.events.InventoryCloseEvent event = new dev.minecraftdorado.blackmarket.utils.inventory.events.InventoryCloseEvent(
						player
						, getLastInv(player)
						);
				
				Bukkit.getPluginManager().callEvent(event);
			}else {
				updateInventory(player, inv);
				return;
			}
		a.add(inv);
		if(a.size() > 5) a.remove(0);
		history.put(player, a);
				
		player.closeInventory();
		player.openInventory(inv.inv);
	}
	
	public static void updateInventory(Player player, Inv inv) {
		Inventory iv = player.getOpenInventory().getTopInventory();
		
		for(int slot = 0; slot < iv.getSize(); slot++)
			if(inv.getItem(slot) != null)
				iv.setItem(slot, inv.getItem(slot));
			else
				iv.setItem(slot, new ItemStack(Material.AIR));
		
		player.updateInventory();
		
		ArrayList<Inv> a = getHistory(player);
		
		for (int i = a.size() - 1; i > -1; i--) {
			Inv aa = a.get(i);
			a.remove(i);
			if(aa.getTitle().equals(inv.getTitle()))
				break;
		}
		
		a.add(inv);
		history.put(player, a);
	}
	
	public static void closeInventory() {
		Bukkit.getOnlinePlayers().forEach(p -> {
			if(p.getOpenInventory() != null &&
					p.getOpenInventory().getTopInventory() != null &&
					getLastInv(p) != null &&
					p.getOpenInventory().getTitle().equals(getLastInv(p).getTitle()))
				p.closeInventory();
		});
		history.clear();
	}
	
	@EventHandler
	protected static void interactEvent(InventoryClickEvent e) {
		if(e.getClickedInventory() != null && e.getView().getTopInventory() != null) {
			Player player = (Player) e.getWhoClicked();
			
			if(player.getOpenInventory() == null || !history.containsKey(player) || getLastInv(player) == null || !e.getView().getTitle().equals(getLastInv(player).getTitle())) return;
			
			ItemStack item = e.getCurrentItem();
			
			if(item == null || item.getType().equals(Material.AIR))
				if(e.getCursor() != null && !e.getCursor().getType().equals(Material.AIR))
					item = e.getCursor();
				else
					return;
			
			if(item.equals(Config.getItemStack("market.close", "menus.market.items.close"))) {
				e.setCancelled(true);
				Bukkit.getScheduler().runTask(MainClass.main, () -> player.closeInventory());
				return;
			}
			
			dev.minecraftdorado.blackmarket.utils.inventory.events.InventoryClickEvent event = new dev.minecraftdorado.blackmarket.utils.inventory.events.InventoryClickEvent(
					player
					, getLastInv(player)
					, item
					, e.getSlot()
					, e.getAction()
					, e.getView().getBottomInventory()
					, e.getClickedInventory().equals(e.getView().getTopInventory())
					, e.getClick());
			
			Bukkit.getPluginManager().callEvent(event);
			
			if(e.getClick().isKeyboardClick()) e.setCancelled(true);
			else e.setCancelled(event.isCancelled());
		}
	}
	
	@EventHandler
	protected static void dragEvent(InventoryDragEvent e) {
		if(e.getInventory() != null) {
			Player player = (Player) e.getWhoClicked();
			
			if(!history.containsKey(player) || !e.getView().getTitle().equals(getLastInv(player).getTitle())) return;
			
			dev.minecraftdorado.blackmarket.utils.inventory.events.InventoryDragEvent event = new dev.minecraftdorado.blackmarket.utils.inventory.events.InventoryDragEvent(
					player
					, getLastInv(player)
					, e.getCursor()
					, e.getOldCursor()
					, e.getNewItems()
					, e.getInventorySlots()
					, e.getRawSlots()
					, e.getView().getBottomInventory()
					, e.getInventory().equals(e.getView().getTopInventory())
					);
			
			Bukkit.getPluginManager().callEvent(event);
			e.setCancelled(event.isCancelled());
		}
	}
	
	@EventHandler
	protected static void closeInv(InventoryCloseEvent e) {
		Player player = (Player) e.getPlayer();
		
		if(!history.containsKey(player)) return;
		
		if(getLastInv(player) == null || !e.getView().getTitle().equals(getLastInv(player).getTitle()))
			return;
		
		
		dev.minecraftdorado.blackmarket.utils.inventory.events.InventoryCloseEvent event = new dev.minecraftdorado.blackmarket.utils.inventory.events.InventoryCloseEvent(
				player
				, getLastInv(player)
				);
		
		Bukkit.getPluginManager().callEvent(event);
		
		history.remove(player);
		
		MainClass.npcM.npcList.get(player).clear();
	}
	
	public static class Inv {
		
		String title;
		int row;
		Inventory inv;
		
		HashMap<Integer, BlackItem> bList = new HashMap<>();
		
		public Inv(String title, int row) {
			this.title = title;
			this.row = row;
			inv = Bukkit.createInventory(null, row*9, title);
		}
		
		public void setItem(int slot, ItemStack item) {
			if(item != null && slot < row*9 && slot >= 0)
				inv.setItem(slot, item);
		}
		
		public void addItem(ItemStack item) {
			if(item != null)
				inv.addItem(item);
		}
		
		public ItemStack getItem(int slot) {
			if(slot < row*9)
				return inv.getItem(slot);
			return null;
		}
		
		public ItemStack[] getItems() {
			return inv.getContents();
		}
		
		public String getTitle() {
			return title;
		}
		
		public int getRow() {
			return row;
		}
		
		public void setBackgroud(ItemStack item, boolean onlyBorder) {
			if(!item.getType().equals(Material.AIR)) {
				ItemMeta meta = item.getItemMeta();
				if(!meta.hasDisplayName())
					meta.setDisplayName(" ");
				item.setItemMeta(meta);
			}
			if(!onlyBorder)
				for (int i = 0; i < inv.getSize(); i++)
					setItem(i, item);
			else {
				for (int i = 0; i < 9; i++) {
					setItem(i, item);
					setItem(i + (row*9 - 9), item);
				}
				for (int i = 0; i < row; i++) {
					setItem(i*9, item);
					setItem(8 + (i*9), item);
				}
			}
		}
		
		public HashMap<Integer, BlackItem> getBlackList(){
			return bList;
		}
		
		public void addBlackItem(BlackItem bItem, int slot) {
			bList.put(slot, bItem);
		}
	}
}
