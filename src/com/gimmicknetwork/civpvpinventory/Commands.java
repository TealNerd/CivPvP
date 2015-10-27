package com.gimmicknetwork.civpvpinventory;

import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
	private CivpvpInventory plugin;
	private DuelManager dm;

	public Commands(CivpvpInventory plugin) {
		this.plugin = plugin;
		dm = plugin.getDuelManager();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		Player player;
		if (!(sender instanceof Player)) {
			sender.sendMessage("This command can only be run by a player.");
			return true;
		} else {
			player = (Player) sender;
		}

		if (cmd.getName().equalsIgnoreCase("inv")) {
			if (args.length > 0) {
				switch (args[0]) {

				case "load":
					this.plugin.invLoad(player, args);
					return true;
				case "save":
					try {
						this.plugin.invSave(player, args);
					} catch (IOException io) {
						player.sendMessage(ChatColor.RED + "Save failed.");
					}
					return true;
				case "clear":
					this.plugin.invClear(player);
					return true;
				default:
					player.sendMessage(ChatColor.RED
							+ "Command not recognised, use /inv load <name>, /inv save <name> or /inv clear.");
					return true;
				}
			} else {
				player.sendMessage(ChatColor.RED
						+ "Command not recognised, use /inv load <name>, /inv save <name> or /inv clear.");
			}

		}

		if (cmd.getName().equalsIgnoreCase("duel")) {
			if (args.length == 0) {
				player.sendMessage("You have to specify a player you want to duel");
				return true;
			}
			Player enemy = plugin.getServer().getPlayer(args[0]);
			if (enemy == null) {
				player.sendMessage("This player is currently not online");
				return true;
			}
			if (enemy == player) {
				player.sendMessage("You can't duel yourself");
				return true;
			}
			if (!dm.isInDuel(player)) {
				dm.requestDuel(player,enemy);
				enemy.sendMessage(player.getName()+ " requested to duel you, run /accept to fight");
				player.sendMessage("Duel request sent to "+ enemy.getName());
				return true;
			}
			else {
				player.sendMessage("You are already in a duel");
				return true;
			}
			
		}
		
		if (cmd.getName().equalsIgnoreCase("accept")) {
			if (dm.isInDuel(player)) {
				player.sendMessage("You are already in a duel");
				return true;
			}
			if (dm.wasRequested(player)) {
				dm.acceptDuel(player);
				return true;
			}
			else {
				player.sendMessage("Nothing to accept");
				return true;
			}
		}
		return false;
	}
}