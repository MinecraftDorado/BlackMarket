package dev.minecraftdorado.blackmarket.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import dev.minecraftdorado.blackmarket.utils.Config;

public class sell extends BukkitCommand {
	
	public sell(String name) {
        super(name);
        this.setAliases(Config.getSellAlias());
    }

	@Override
	public boolean execute(CommandSender sender, String arg1, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(Config.getMessage("only_player"));
			return false;
		}
		
		Player p = (Player) sender;
		
		if(args.length == 1)
			p.chat("/bm sell " + args[0]);
		else
			p.sendMessage(Config.getMessage("command.sell.usage").replace("%cmd%", arg1));
		return false;
	}
}
