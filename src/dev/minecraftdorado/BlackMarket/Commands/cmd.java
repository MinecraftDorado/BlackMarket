package dev.minecraftdorado.BlackMarket.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class cmd implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String args2, String[] args) {
		
		
		if(args.length>0)
			switch(args[0]) {
			default:
				sender.sendMessage("§cdebug");
				break;
			}
		
		sender.sendMessage("§chelp");
		return false;
	}
}
