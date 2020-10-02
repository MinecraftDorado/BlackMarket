package dev.minecraftdorado.BlackMarket.Commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.minecraftdorado.BlackMarket.Utils.Market.BlackItem;
import dev.minecraftdorado.BlackMarket.Utils.Market.PlayerData;

public class sell implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String args2, String[] args) {
		
		if(!(sender instanceof Player)) {
			sender.sendMessage("§cOnly for players!");
			return false;
		}
		
		if(args.length == 1)
			try {
				int value = Integer.parseInt(args[0]);
				
				Player p = (Player) sender;
				
				if(p.getInventory().getItemInHand() != null && !p.getInventory().getItemInHand().getType().equals(Material.AIR)) {
					BlackItem bItem = new BlackItem(p.getInventory().getItemInHand(), value, p.getUniqueId());
					
					if(PlayerData.get(p.getUniqueId()).addItem(bItem)) {
						sender.sendMessage("§aItem added!");
						p.getInventory().getItemInHand().setType(Material.AIR);
					}else
						sender.sendMessage("§cLimit !");
				}else
					sender.sendMessage("§cItem not found!");
			}catch(Exception ex) {
				sender.sendMessage("§cValue invalid!");
			}
		else
			sender.sendMessage("§cUsage /sell <value>");
		return false;
	}
}
