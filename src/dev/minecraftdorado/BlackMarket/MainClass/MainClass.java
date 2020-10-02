package dev.minecraftdorado.BlackMarket.MainClass;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import dev.minecraftdorado.BlackMarket.Commands.sell;
import dev.minecraftdorado.BlackMarket.Utils.Packets.PacketReader;
import dev.minecraftdorado.BlackMarket.Utils.Config;
import dev.minecraftdorado.BlackMarket.Utils.Entities.Hologram.HologramManager;
import dev.minecraftdorado.BlackMarket.Utils.Entities.NPC.NPC;
import dev.minecraftdorado.BlackMarket.Utils.Entities.NPC.NPCManager;
import dev.minecraftdorado.BlackMarket.Utils.Entities.NPC.Events.NPCInteractEvent;
import dev.minecraftdorado.BlackMarket.Utils.Entities.NPC.Skins.SkinData;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.InventoryManager;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Events.InventoryClickEvent;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils.CategoryUtils;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils.CategoryUtils.Category;
import dev.minecraftdorado.BlackMarket.Utils.Market.BlackItem;
import dev.minecraftdorado.BlackMarket.Utils.Market.Market;
import dev.minecraftdorado.BlackMarket.Utils.Market.PlayerData;

public class MainClass extends JavaPlugin {
	
	public static MainClass main;
	public static HologramManager hm;
	public static NPCManager npcM;
	
	public void onEnable() {
		main = this;
		hm = new HologramManager();
		
		getServer().getPluginCommand("sell").setExecutor(new sell());
		
		npcM = new NPCManager();
		
		new SkinData();
		new Config();
		new PlayerData();
		new CategoryUtils();
		
		for (int i = 0; i < 50; i++) {
			Market.addItem(new BlackItem(new ItemStack(Material.SADDLE), 10, null));
			Market.addItem(new BlackItem(new ItemStack(Material.EMERALD), 10, null));
		}
		
		NPC npc = new NPC("Hello World §e!");
		npc.setSkin(SkinData.getSkin("skin_fisher"));
		npc.spawn(new Location(Bukkit.getWorld("world"), 5, 100, 0));
		
		npcM.add(npc);
		
		Bukkit.getPluginManager().registerEvents(new Listener() {
			@EventHandler
			private void join(PlayerJoinEvent e) {PacketReader.get(e.getPlayer()).inject();}
			@EventHandler
			private void leave(PlayerBedLeaveEvent e) {PacketReader.get(e.getPlayer()).uninject();}
			@EventHandler
			private void a(NPCInteractEvent e) {
				e.getPlayer().sendMessage("§c" + e.getNPC().getName());
				PlayerData.get(e.getPlayer().getUniqueId()).setCategory(null);
				Market.setPlayerPage(e.getPlayer().getUniqueId(), 0);
				InventoryManager.openInventory(e.getPlayer(), Market.getMarketInventory(e.getPlayer()));
			}
			@EventHandler
			private void a(InventoryClickEvent e) {
				if(e.getInv().getTitle().equals(Market.getMarketTitle())) {
					if(e.getItemStack().equals(Config.getItemStack("previous", e.getPlayer()))) {
						Market.setPlayerPage(e.getPlayer().getUniqueId(), Market.getPlayerPage(e.getPlayer().getUniqueId())-1);
						InventoryManager.openInventory(e.getPlayer(), Market.getMarketInventory(e.getPlayer()));
					}
					if(e.getItemStack().equals(Config.getItemStack("next", e.getPlayer()))) {
						Market.setPlayerPage(e.getPlayer().getUniqueId(), Market.getPlayerPage(e.getPlayer().getUniqueId())+1);
						InventoryManager.openInventory(e.getPlayer(), Market.getMarketInventory(e.getPlayer()));
					}
					Category cat = PlayerData.get(e.getPlayer().getUniqueId()).getCategory();
					CategoryUtils.getCategories().forEach(category -> {
						if(e.getItemStack().equals(category.getItemStack(category.equals(cat)))) { // si establezco por defecto en false, no serviria para recargar la misma categoria
							PlayerData.get(e.getPlayer().getUniqueId()).setCategory(category);
							Market.setPlayerPage(e.getPlayer().getUniqueId(), 0);
							InventoryManager.updateInventory(e.getPlayer(), Market.getMarketInventory(e.getPlayer()));
							return;
						}
					});
				}
			}
		}, this);
		Bukkit.getPluginManager().registerEvents(new InventoryManager(), this);
		Bukkit.getOnlinePlayers().forEach(player -> PacketReader.get(player).inject());
	}
	
	public void onDisable() {
		Bukkit.getOnlinePlayers().forEach(player -> {
			if(player.getOpenInventory() != null && player.getOpenInventory().getTopInventory().getTitle().equals(Market.getMarketTitle()))
				player.closeInventory();
			npcM.list.values().forEach(npc ->{
				npc.hide(player);
			});
			PacketReader.get(player).uninject();
		});
	}
}
