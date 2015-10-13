package com.gimmicknetwork.civpvpinventory;

import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor 
{
  private CivpvpInventory plugin;
  
  public Commands(CivpvpInventory plugin)
  {
    this.plugin = plugin;
  }
  
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
  {
    if (cmd.getName().equalsIgnoreCase("inv"))
    {
      if (!(sender instanceof Player))
      {
        sender.sendMessage("This command can only be run by a player.");
      }
      else
      {
        Player player = (Player)sender;
        if(this.plugin.isAllowed(player)) {        
	        if (args.length > 0) {
	        	switch (args[0]) {
	        	
	        	case "load":	this.plugin.invLoad(player, args);
	        					break;
	        	case "save":	try {this.plugin.invSave(player, args);} catch (IOException io) { player.sendMessage(ChatColor.RED + "Save failed."); }
	        					break;
	        	case "clear":	this.plugin.invClear(player);
	        					break;
	        	default:		player.sendMessage(ChatColor.RED + "Command not recognised, use /inv load <name>, /inv save <name> or /inv clear.");
	        					break;
	        	}       	
	        } else {
	        	player.sendMessage(ChatColor.RED + "Command not recognised, use /inv load <name>, /inv save <name> or /inv clear.");
	        }
        } else {
        	player.sendMessage(ChatColor.RED + "You can't do that here.");
        }
        
      }
      return true;
    }
    return false;
  }
}
