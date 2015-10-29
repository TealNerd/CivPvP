package com.gimmicknetwork.civpvpinventory;

import java.util.ArrayList;
import java.util.UUID;

public class Team {
	
	private UUID owner;
	private ArrayList<UUID> members;
	
	public Team(UUID owner) {
		this.owner = owner;
		this.members = new ArrayList<UUID>();
	}

	public void addMember(UUID member) {
		if(!members.contains(member)) {
			members.add(member);
		}
	}
	
	public void removeMember(UUID member) {
		if(members.contains(member)) {
			members.remove(member);
		}
	}
	
	public UUID getOwner() {
		return owner;
	}
	
	public ArrayList<UUID> getMembers() {
		return members;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Team)) {
			return false;
		}
		return ((Team)o).getOwner().equals(owner);
	}
}
