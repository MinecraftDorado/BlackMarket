package dev.minecraftdorado.BlackMarket.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.minecraftdorado.BlackMarket.Utils.Config;
import dev.minecraftdorado.BlackMarket.Utils.Utils;
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
				Config.sendMessage("command.open.only_player", sender);
				return false;
			}
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.join("\n", Config.getYml().getStringList("help"))));
		
		return false;
	}
}
