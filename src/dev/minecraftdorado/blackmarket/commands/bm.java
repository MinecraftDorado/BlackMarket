package dev.minecraftdorado.blackmarket.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import dev.minecraftdorado.blackmarket.listeners.PlayerListener;
import dev.minecraftdorado.blackmarket.mainclass.MainClass;
import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.entities.npc.NPC;
import dev.minecraftdorado.blackmarket.utils.entities.npc.skins.SkinData;
import dev.minecraftdorado.blackmarket.utils.inventory.InventoryManager;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.BlackList;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.BlackListLore;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.UMaterial;
import dev.minecraftdorado.blackmarket.utils.market.BlackItem;
import dev.minecraftdorado.blackmarket.utils.market.Market;
import dev.minecraftdorado.blackmarket.utils.market.PlayerData;

public class bm implements CommandExecutor {

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] args) {
		
		if(sender instanceof Player) {
			Player player = (Player) sender;
			
			if(args.length >= 1)
				switch(args[0]) {
				case "reload":
					if(args.length == 1) {
						if(player.hasPermission("blackmarket.reload")) {
							InventoryManager.closeInventory();
							
							Config.reload();
							PlayerData.save();
							BlackList.reload();
							BlackListLore.reload();
							
							Config.sendMessage("command.reload.message", player);
						}else
							Config.sendMessage("no_permission", player);
						return false;
					}
					break;
				case "setnpc":
					if(args.length <= 2) {
						if(player.hasPermission("blackmarket.npc.set")) {
							
							NPC npc = new NPC(player.getLocation());
							
							if(args.length == 2)
								npc.setSkin(SkinData.getSkin(args[1]));
							npc.spawn();
							
							MainClass.npcM.add(npc);
							
							Config.sendMessage("command.setnpc.message", player);
						}else
							Config.sendMessage("no_permission", player);
						return false;
					}
					return false;
				case "removenpc":
					if(args.length == 1) {
						if(player.hasPermission("blackmarket.npc.remove")) {
							if(!PlayerListener.npcRemove.contains(player.getUniqueId()))
								PlayerListener.npcRemove.add(player.getUniqueId());
							Config.sendMessage("command.removenpc.message", player);
						}else
							Config.sendMessage("no_permission", player);
						return false;
					}
					Config.sendMessage("command.setnpc.usage", player);
					return false;
				case "open":
					if(args.length <= 2) {
						if(player.hasPermission("blackmarket.open")) {
							
							if(args.length == 2)
								if(Bukkit.getOfflinePlayer(args[1]) != null && Bukkit.getOfflinePlayer(args[1]).isOnline())
									player = Bukkit.getPlayer(args[1]);
								else {
									player.sendMessage(Config.getMessage("offline_player").replace("%player%", args[1]));
									return false;
								}
							
							PlayerData.get(player.getUniqueId()).setCategory(null);
							Market.setPlayerPage(player.getUniqueId(), 0);
							InventoryManager.openInventory(player, Market.getInventory(player));
						}else
							Config.sendMessage("no_permission", player);
						return false;
					}
					break;
				case "sell":
					if(args.length == 2) {
						try {
							double value = Double.parseDouble(args[1]);
							
							if(value >= Config.getMinimumPrice()) {
								ItemStack item = player.getInventory().getItemInHand();
								
								if(item != null && !item.getType().equals(Material.AIR))
									if(!Config.blackListIsEnable() || BlackList.isAllow(UMaterial.match(item))) {
										if(!Config.blackListLoreIsEnable() || !item.hasItemMeta() || !item.getItemMeta().hasLore() || BlackListLore.isAllow(item.getItemMeta().getLore())) {
											BlackItem bItem = new BlackItem(item, value, player.getUniqueId());
											
											if(PlayerData.get(player.getUniqueId()).addItem(bItem)) {
												Config.sendMessage("command.sell.message", player);
												player.getInventory().getItemInHand().setType(Material.AIR);
												player.getInventory().setItemInHand(null);
											}else
												Config.sendMessage("command.sell.error_limit", player);
										}else
											Config.sendMessage("command.sell.error_lore_not_allow", player);
									}else
										Config.sendMessage("command.sell.error_item_not_allow", player);
								else
									Config.sendMessage("command.sell.error_item_not_found", player);
							}else
								player.sendMessage(Config.getMessage("command.sell.error_minimum_price").replace("%price%", Config.getMinimumPrice() + ""));
						}catch(Exception ex) {
							Config.sendMessage("command.sell.error_value", player);
						}
						return false;
					}
					break;
				}
		}else
			if(args.length >= 1)
				switch(args[0]) {
				case "reload":
					if(args.length == 1) {
						InventoryManager.closeInventory();
						
						Config.reload();
						PlayerData.save();
						BlackList.reload();
						BlackListLore.reload();
						
						Config.sendMessage("command.reload.message", sender);
						return false;
					}
					break;
				case "open":
					if(args.length == 2)
						if(Bukkit.getOfflinePlayer(args[1]) != null && Bukkit.getOfflinePlayer(args[1]).isOnline()) {
							Player player = Bukkit.getPlayer(args[1]);
							PlayerData.get(player.getUniqueId()).setCategory(null);
							Market.setPlayerPage(player.getUniqueId(), 0);
							InventoryManager.openInventory(player, Market.getInventory(player));
						}else {
							sender.sendMessage(Config.getMessage("offline_player").replace("%player%", args[1]));
							return false;
						}
					Config.sendMessage("only_player", sender);
					return false;
				case "setnpc":
					Config.sendMessage("only_player", sender);
					return false;
				case "removenpc":
					Config.sendMessage("only_player", sender);
					return false;
				case "sell":
					Config.sendMessage("only_player", sender);
					return false;
				}
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.join("\n", Config.getLang().getStringList("help"))));
		
		return false;
	}
}
