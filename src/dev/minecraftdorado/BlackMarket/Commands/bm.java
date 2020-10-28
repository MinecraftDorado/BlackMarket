package dev.minecraftdorado.BlackMarket.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.minecraftdorado.BlackMarket.Listeners.PlayerListener;
import dev.minecraftdorado.BlackMarket.MainClass.MainClass;
import dev.minecraftdorado.BlackMarket.Utils.Config;
import dev.minecraftdorado.BlackMarket.Utils.Utils;
import dev.minecraftdorado.BlackMarket.Utils.Entities.NPC.NPC;
import dev.minecraftdorado.BlackMarket.Utils.Entities.NPC.Skins.SkinData;
import dev.minecraftdorado.BlackMarket.Utils.Inventory.InventoryManager;
import dev.minecraftdorado.BlackMarket.Utils.Market.Market;
import dev.minecraftdorado.BlackMarket.Utils.Market.PlayerData;

public class bm implements CommandExecutor {

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
							
							Utils.items.clear();
							Config.reload();
							PlayerData.save();
							
							Config.sendMessage("command.reload.message", player);
						}else
							Config.sendMessage("no_permission", player);
						return false;
					}
					break;
				case "setnpc":
					if(args.length <= 2) {
						if(player.hasPermission("blackmarket.setnpc")) {
							
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
					Config.sendMessage("command.setnpc.usage", player);
					return false;
				case "removenpc":
					if(args.length == 1) {
						if(player.hasPermission("blackmarket.removenpc")) {
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
					if(args.length == 1) {
						if(player.hasPermission("blackmarket.open")) {
							PlayerData.get(player.getUniqueId()).setCategory(null);
							Market.setPlayerPage(player.getUniqueId(), 0);
							InventoryManager.openInventory(player, Market.getMarketInventory(player));
						}else
							Config.sendMessage("no_permission", player);
						return false;
					}
					break;
				}
		}else
			switch(args[0]) {
			case "reload":
				if(args.length == 1) {
					InventoryManager.closeInventory();
					
					Utils.items.clear();
					Config.reload();
					PlayerData.save();
					
					Config.sendMessage("command.reload.message", sender);
					return false;
				}
				break;
			case "open":
				Config.sendMessage("only_player", sender);
				return false;
			case "setnpc":
				Config.sendMessage("only_player", sender);
				return false;
			case "removenpc":
				Config.sendMessage("only_player", sender);
				return false;
			}
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.join("\n", Config.getYml().getStringList("help"))));
		
		return false;
	}
}
