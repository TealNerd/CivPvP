package com.gimmicknetwork.civpvpinventory;

import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.namelayer.NameAPI;

public final class CivpvpInventory extends JavaPlugin {
	private DuelManager dm;
	private KitManager km;
	private Database db;
	private TeamManager tm;
	private static CivpvpInventory instance;

	public void onEnable() {
		instance = this;
		this.saveDefaultConfig();
		initializeDB();
		km = new KitManager(db);
		dm = new DuelManager(this, db);
		dm.loadElos();
		tm = new TeamManager();
		getServer().getPluginManager().registerEvents(new PvPListener(this),
				this);
		getLogger().info("[CivpvpInventory] plugin enabled!");
		Commands c = new Commands(this);
		getCommand("inv").setExecutor(c);
		getCommand("duel").setExecutor(c);
		getCommand("accept").setExecutor(c);
		getCommand("team").setExecutor(c);
		getCommand("elo").setExecutor(c);
	}

	public void onDisable() {
		getLogger().info("[CivpvpInventory] plugin disabled!");
		dm.saveElos();
	}

	public void invSave(Player p, String[] args) throws IOException {
		if (args.length != 2) {
			p.sendMessage(ChatColor.RED
					+ "Invalid arguments, do /inv save <name>.");
		} else {
			String inv = args[1].substring(0, Math.min(args[1].length(), 16));
			if (invExists(inv)) {
				Kit kit = km.getKitByName(inv);
				if(kit.isOwner(p.getUniqueId())) {
					km.updateKitInventory(kit, p.getInventory().getContents(), p.getInventory().getArmorContents());
					p.sendMessage(ChatColor.GREEN + "Inventory " + inv
							+ " has been saved.");
				} else {
					p.sendMessage(ChatColor.RED + "You do not have permissions to overwrite inventory " + inv);
				}
			} else {
				Kit kit = new Kit(inv, p.getUniqueId(), p.getInventory().getContents(), 
								p.getInventory().getArmorContents());
				km.addKit(kit);
				p.sendMessage(ChatColor.GREEN + "Inventory " + inv
						+ " has been saved.");
			}
		}
	}
	
	public void addOwner(Player p, String[] args) {
		if(args.length != 3) {
			p.sendMessage(ChatColor.RED + "Invalid arguments, do /inv addowner <inventory> <player>");
		} else {
			String inv = args[1].substring(0, Math.min(args[1].length(), 16));
			String newOwner = args[2];
			if(NameAPI.getUUID(newOwner) == null) {
				p.sendMessage(ChatColor.RED + "Unknown player: " + newOwner + " has either never player or was spelled wrong");
			} else if(!invExists(inv)) {
				p.sendMessage(ChatColor.RED + "Inventory " + inv + " does not exist");
			} else {
				km.addOwnerToKit(inv, NameAPI.getUUID(newOwner));
				p.sendMessage(ChatColor.GREEN + "Added " + newOwner + " as owner on " + inv);
			}
		}
	}
	
	public void removeOwner(Player p, String[] args) {
		if(args.length != 3) {
			p.sendMessage(ChatColor.RED + "Invalid arguments, do /inv removeowner <inventory> <player>");
		} else {
			String inv = args[1].substring(0, Math.min(args[1].length(), 16));
			String owner = args[2];
			if(NameAPI.getUUID(owner) == null) {
				p.sendMessage(ChatColor.RED + "Unknown player: " + owner + " has either never player or was spelled wrong");
			} else if(!invExists(inv)) {
				p.sendMessage(ChatColor.RED + "Inventory " + inv + " does not exist");
			} else if(!km.getKitByName(inv).isOwner(NameAPI.getUUID(owner))){
				p.sendMessage(ChatColor.RED + owner + " is not an owner on " + inv);
			} else {
				km.removeOwner(inv, NameAPI.getUUID(owner));
			}
		}
	}
	
	public void transferInv(Player p, String[] args) {
		if(args.length != 3) {
			p.sendMessage(ChatColor.RED + "Invalid arguments, do /inv transfer <inventory> <player>");
		} else {
			String inv = args[1].substring(0, Math.min(args[1].length(), 16));
			String owner = args[2];
			if(NameAPI.getUUID(owner) == null) {
				p.sendMessage(ChatColor.RED + "Unknown player: " + owner + " has either never player or was spelled wrong");
			} else if(!invExists(inv)) {
				p.sendMessage(ChatColor.RED + "Inventory " + inv + " does not exist");
			} else {
				if(km.getKitByName(inv).isOwner(NameAPI.getUUID(owner))) {
					km.removeOwner(inv, NameAPI.getUUID(owner));
				}
				km.transferOwner(inv, NameAPI.getUUID(owner));
			}
		}
	}	
	
	public void invLoad(Player p, String[] args) {
		if (dm.isInDuel(p)) {
			p.sendMessage("Nice try");
			getServer()
					.broadcastMessage(
							p.getName()
									+ "thought he is clever and tried to reload his kit during a fight");
			return;
		}

		if (args.length != 2) {
			p.sendMessage(ChatColor.RED
					+ "Invalid arguments, do /inv load <name>.");
		} else {
			String inv = args[1].substring(0, Math.min(args[1].length(), 16));
			if(invExists(inv)) {
				Kit kit = km.getKitByName(inv);
				kit.loadKit(p);
				p.sendMessage(ChatColor.GREEN + "Inventory " + kit.getName()
						+ " has been loaded.");
			} else {
				p.sendMessage(ChatColor.RED + "Inventory " + args[1]
						+ " doesn't exist.");
			}
		}
	}

	public void invClear(Player p) {
		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
	}

	public boolean invExists(String inv) {
		return km.kitExists(inv);
	}

	public DuelManager getDuelManager() {
		return dm;
	}
	
	public KitManager getKitManager() {
		return km;
	}
	
	public Database getDB() {
		return db;
	}
	
	public TeamManager getTeamManager() {
		return tm;
	}
	
	public static CivpvpInventory getInstance() {
		return instance;
	}

	public void initializeDB() {
		FileConfiguration config = getConfig();
		String host = config.getString("sql.hostname");
		int port = config.getInt("sql.port");
		String dbname = config.getString("sql.dbname");
		String username = config.getString("sql.username");
		String password = config.getString("sql.password");
		db = new Database(host, port, dbname, username, password, getLogger());
		db.connect();
	}
}
