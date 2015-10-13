package com.gimmicknetwork.civpvpinventory;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class CivpvpInventory
  extends JavaPlugin
{
  public void onEnable()
  {
	  this.saveDefaultConfig();
	  getLogger().info("[CivpvpInventory] plugin enabled!");
	  getCommand("inv").setExecutor(new Commands(this));
    
  }
  
  public void onDisable()
  {
    getLogger().info("[CivpvpInventory] plugin disabled!");
  }
  
  public void invSave(Player p, String[] args)
    throws IOException
  {
    if (args.length != 2)
    {
      p.sendMessage(ChatColor.RED + "Invalid arguments, do /inv save <name>.");
    }
    else
    {
      String inv = args[1].substring(0, Math.min(args[1].length(), 16));
      String path = getDataFolder() + "/inventories/";
      if (invExists(inv).booleanValue())
      {
        YamlConfiguration c = YamlConfiguration.loadConfiguration(new File(path, inv + ".yml"));
        String owner = c.getString("inventory.owner");
        if (owner.equals(p.getName()))
        {
          c.set("inventory.owner", p.getName());
          c.set("inventory.armor", p.getInventory().getArmorContents());
          c.set("inventory.content", p.getInventory().getContents());
          c.save(new File(path, inv + ".yml"));
          p.sendMessage(ChatColor.GREEN + "Inventory " + inv + " has been saved.");
        }
        else
        {
          p.sendMessage(ChatColor.RED + "Inventory " + inv + " does not belong to you.");
        }
      }
      else
      {
        YamlConfiguration c = new YamlConfiguration();
        c.set("inventory.owner", p.getName());
        c.set("inventory.armor", p.getInventory().getArmorContents());
        c.set("inventory.content", p.getInventory().getContents());
        c.save(new File(path, inv + ".yml"));
        p.sendMessage(ChatColor.GREEN + "Inventory " + inv + " has been saved.");
      }
    }
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
public void invLoad(Player p, String[] args)
  {
    if (args.length != 2)
    {
      p.sendMessage(ChatColor.RED + "Invalid arguments, do /inv load <name>.");
    }
    else
    {
      if(invExists(args[1]))
      {
	      String inv = args[1].substring(0, Math.min(args[1].length(), 16));
	      String path = getDataFolder() + "/inventories/";
	      YamlConfiguration c = YamlConfiguration.loadConfiguration(new File(path, inv + ".yml"));
	      ItemStack[] content = (ItemStack[])((List)c.get("inventory.armor")).toArray(new ItemStack[0]);
	      p.getInventory().setArmorContents(content);
	      content = (ItemStack[])((List)c.get("inventory.content")).toArray(new ItemStack[0]);
	      p.getInventory().setContents(content);
	      p.sendMessage(ChatColor.GREEN + "Inventory " + inv + " has been loaded.");
      } else {
    	  p.sendMessage(ChatColor.RED + "Inventory " + args[1] + " doesn't exist.");
      }
      
    }
  }
  
  public void invClear(Player p)
  {
    p.getInventory().clear();
    p.getInventory().setArmorContents(null);
  }
  
  public Boolean invExists(String inv)
  {
    File f = new File(getDataFolder() + "/inventories/" + inv + ".yml");
    return Boolean.valueOf(f.exists());
  }
  
  public Boolean isAllowed(Player p)
  {
	  boolean allowed = false;
	  if(this.getConfig().getBoolean("useworldguard", false))
	  {
		  World world = p.getWorld();
		  Location location = p.getLocation();
		  ApplicableRegionSet set = WGBukkit.getRegionManager(world).getApplicableRegions(location);
		  
		  List<String> list = this.getConfig().getStringList("allowedregions");
		  String[] allowedregions = list.toArray(new String[0]);
		  
		  for (ProtectedRegion region : set) {
			  for(String r : allowedregions) {
				  if(region.getId().equals(r))
				  {
					  allowed = true;
				  }
			  }
		  }
	  } else {
		  allowed = true;
	  }
	  return allowed;
	  
  }
  
}
