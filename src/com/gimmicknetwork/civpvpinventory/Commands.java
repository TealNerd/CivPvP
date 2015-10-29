package com.gimmicknetwork.civpvpinventory;

import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.NameAPI;

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
			if(cmd.getName().equalsIgnoreCase("inv") && args[0].equalsIgnoreCase("del")) {
				String inv = args[1];
				if(plugin.getKitManager().kitExists(inv)) {
					plugin.getKitManager().deleteKit(inv);
				}
			}
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
				case "addowner":
					this.plugin.addOwner(player, args);
					return true;
				case "removeowner":
					this.plugin.removeOwner(player, args);
					return true;
				case "transfer":
					this.plugin.transferInv(player, args);
					return true;
				case "del":
					this.plugin.deleteInv(player, args);
					return true;
				default:
					player.sendMessage(ChatColor.RED
							+ "Command not recognised, use /inv load <name>, /inv save <name>, /inv addowner <inv> <name, /inv removeowner <inv> <name>, /inv transfer <inv> <name>, or /inv clear.");
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
			Player enemy = plugin.getServer().getPlayer(NameAPI.getUUID(args[0]));
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
		
		if(cmd.getName().equalsIgnoreCase("team")) {
			if(args.length == 0) {
				player.sendMessage(ChatColor.RED + "Invalid arguments, please do /team create, /team invite <player>, /team accept <team>, or /team leave");
				return true;
			}
			if(args.length > 0) {
				switch(args[0]) {
				case "create":
					plugin.getTeamManager().addTeam(player);
					return true;
				case "invite":
					plugin.getTeamManager().invitePlayer(player, args);
					return true;
				case "leave":
					plugin.getTeamManager().leaveTeam(player);
					return true;
				case "accept":
					plugin.getTeamManager().acceptInvite(player, args);
					return true;
				default:
					player.sendMessage(ChatColor.RED + "Invalid arguments, please do /team create, /team invite <player>, /team accept <team>, or /team leave");
					return true;
				}
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
		if(cmd.getName().equalsIgnoreCase("elo")) {
			plugin.getDuelManager().checkElo(player);
			return true;
		}
		return false;
	}
}
