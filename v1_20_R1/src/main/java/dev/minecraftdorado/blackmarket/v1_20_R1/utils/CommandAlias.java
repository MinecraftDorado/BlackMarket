package dev.minecraftdorado.blackmarket.v1_20_R1.utils;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;

import dev.minecraftdorado.blackmarket.commands.sell;
import dev.minecraftdorado.blackmarket.utils.CommandAliasAbs;

public class CommandAlias extends CommandAliasAbs {
	
	public void addAlias(List<String> alias) {
		CraftServer craftserver = ((CraftServer) Bukkit.getServer());
		craftserver.getCommandMap().register(alias.get(0), new sell(alias.get(0)));
	}
	
}
