package dev.minecraftdorado.BlackMarket.Commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class bmTab implements TabCompleter {
	
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args){
		if(sender instanceof Player) {
			List<String> list = new ArrayList<>();
			
			if(args.length == 1) {
				list.add("reload");
				list.add("open");
				list.add("setnpc");
			}
			
			if(!list.isEmpty()) {
				int s = list.size();
				for (int i = 0; i < s; i++) {
					if(!list.get(i).toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
						list.remove(i);
						i--;
						s--;
					}
				}
				Collections.sort(list);
				return list;
			}
		}
		return null;
	}
}
