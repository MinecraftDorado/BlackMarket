package dev.minecraftdorado.BlackMarket.Commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.minecraftdorado.BlackMarket.Utils.Config;
import dev.minecraftdorado.BlackMarket.Utils.Market.BlackItem;
import dev.minecraftdorado.BlackMarket.Utils.Market.PlayerData;

public class sell implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String args2, String[] args) {
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(Config.getMessage("command.sell.only_player"));
			return false;
		}
		
		Player p = (Player) sender;
		
		if(args.length == 1)
			try {
				double value = Double.parseDouble(args[0]);
				
				if(value >= Config.getMinimumPrice())
					if(p.getInventory().getItemInHand() != null && !p.getInventory().getItemInHand().getType().equals(Material.AIR)) {
						BlackItem bItem = new BlackItem(p.getInventory().getItemInHand(), value, p.getUniqueId());
						
						if(PlayerData.get(p.getUniqueId()).addItem(bItem)) {
							Config.sendMessage("command.sell.message", p);
							p.getInventory().getItemInHand().setType(Material.AIR);
							p.getInventory().setItemInHand(null);
						}else
							Config.sendMessage("command.sell.error_limit", p);
					}else
						Config.sendMessage("command.sell.error_item_not_found", p);
				else
					p.sendMessage(Config.getMessage("command.sell.error_minimum_price").replace("%price%", Config.getMinimumPrice() + ""));
			}catch(Exception ex) {
				Config.sendMessage("command.sell.error_value", p);
			}
		else
			Config.sendMessage("command.sell.usage", p);
		return false;
	}
}
