package com.gimmicknetwork.civpvpinventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import vg.civcraft.mc.namelayer.NameAPI;

public class TeamManager {

	private ArrayList<Team> teams;
	private HashMap<UUID, UUID> invites;
	
	public TeamManager() {
		teams = new ArrayList<Team>();
		invites = new HashMap<UUID, UUID>();
	}
	
	public void addTeam(Player p) {
		if(playerHasTeam(p.getUniqueId())) {
			p.sendMessage(ChatColor.RED + "You are already on a team!");
		} else {
			teams.add(new Team(p.getUniqueId()));
			p.sendMessage(ChatColor.GREEN + "Successfully created a team!");
		}
	}

	public void acceptInvite(Player p, String[] args) {
		if(args.length < 2) {
			p.sendMessage(ChatColor.RED + "You didn't enter a team to join silly!");
			return;
		}
		String teamName = args[1];
		if(NameAPI.getUUID(teamName) == null) {
			p.sendMessage(ChatColor.RED + "The player who's team you tried to join does not exist or has not joined this server");
		} else if(!inviteExists(NameAPI.getUUID(teamName), p.getUniqueId())) {
			p.sendMessage(ChatColor.RED + "You were not invited to join " + teamName + "'s team");
		} else {
			getByUser(NameAPI.getUUID(teamName)).addMember(p.getUniqueId());
			p.sendMessage(ChatColor.GREEN + "You have joined " + teamName + "'s team!");
		}
	}
	
	public void invitePlayer(Player p, String[] args) {
		if(args.length < 2) {
			p.sendMessage(ChatColor.RED + "You have to invite someone, do /team invite <player>");
			return;
		}
		String invitee = args[1];
		if(NameAPI.getUUID(invitee) == null) {
			p.sendMessage(ChatColor.RED + "The player you tried to invite does not exist or has not joined this server");
		} else if(!ownsTeam(p.getUniqueId())) {
			p.sendMessage(ChatColor.RED + "You do not own a team, do /team create before inviting players");
		} else if(inviteExists(p.getUniqueId(), NameAPI.getUUID(invitee))) {
			p.sendMessage(ChatColor.RED + "You have already invited " + invitee + " to join your team");
		} else {
			Player invited = CivpvpInventory.getInstance().getServer().getPlayer(NameAPI.getUUID(invitee));
			if(invited == null) {
				p.sendMessage(ChatColor.RED + "Player not found, try again");
				return;
			}
			invites.put(p.getUniqueId(), NameAPI.getUUID(invitee));
			p.sendMessage(ChatColor.GREEN + "You have invited " + invitee + " to your team");
			String name = NameAPI.getCurrentName(p.getUniqueId());
			invited.sendMessage(ChatColor.GREEN + "You have been invited to " + name + "'s team, do /team accept " + name + " to join");
		}
	}
	
	public void leaveTeam(Player p) {
		if(!playerHasTeam(p.getUniqueId())) {
			p.sendMessage(ChatColor.RED + "You aren't on a team");
		} else if(ownsTeam(p.getUniqueId())) {
			for(UUID id : getByUser(p.getUniqueId()).getMembers()) {
				CivpvpInventory.getInstance().getServer().getPlayer(id).sendMessage(ChatColor.RED + "Your team leader has disbanded the team");
			}
			teams.remove(getByUser(p.getUniqueId()));
			p.sendMessage(ChatColor.GREEN + "You have left and disbanded your team");
		} else {
			getByUser(p.getUniqueId()).removeMember(p.getUniqueId());
			p.sendMessage(ChatColor.GREEN + "You have left your team");
		}
	}
	
	public boolean playerHasTeam(UUID player) {
		for(Team team : teams) {
			if(team.getOwner().equals(player) || team.getMembers().contains(player)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean ownsTeam(UUID player) {
		for(Team team : teams) {
			if(team.getOwner().equals(player)) {
				return true;
			}
		}
		return false;
	}
	
	public Team getByUser(UUID user) {
		for(Team team : teams) {
			if(team.getOwner().equals(user) || team.getMembers().contains(user)) {
				return team;
			}
		}
		return null;
	}
	
	private boolean inviteExists(UUID owner, UUID invitee) {
		for(Entry<UUID, UUID> entry : invites.entrySet()) {
			if(entry.getKey().equals(owner) && entry.getValue().equals(invitee)) {
				return true;
			}
		}
		return false;
	}
}
