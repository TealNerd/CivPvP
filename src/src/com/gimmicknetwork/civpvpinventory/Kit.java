package com.gimmicknetwork.civpvpinventory;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Kit {

	//name of the kit
	private String name;
	//items in main inventory
	private ItemStack[] items;
	//items in armor inventory
	private ItemStack[] armor;
	//main owner, only person who can add more owners
	private UUID owner;
	//owners who have write permissions the the inventory
	private ArrayList<UUID> owners = new ArrayList<UUID>();
	
	public Kit(String name, UUID owner, ItemStack[] items, ItemStack[] armor) {
		this.name = name;
		this.owner = owner;
		this.items = items;
		this.armor = armor;
	}
	
	public Kit(String name, UUID owner, ArrayList<UUID> owners, ItemStack[] items, ItemStack[] armor) {
		this.name = name;
		this.owner = owner;
		this.owners = (ArrayList<UUID>) owners.clone();
		this.items = items;
		this.armor = armor;
	}
	
	public void addOwner(UUID id) {
		if(owners.contains(id)) {
			return;
		}
		owners.add(id);
	}
	
	public void loadKit(Player p) {
		//p.getInventory().clear();
		//p.getInventory().setArmorContents(null);
		p.getInventory().setArmorContents(armor);
		p.getInventory().setContents(items);
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isOwner(UUID id) {
		return owners.contains(id) || owner.equals(id);
	}
	
	public UUID getOwner() {
		return owner;
	}
	
	public ArrayList<UUID> getOwners() {
		return owners;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Kit)) {
			return false;
		}
		return ((Kit) o).getName().equals(this.getName());
	}
	
	public ItemStack[] getItems() {
		return items;
	}
	
	public ItemStack[] getArmor() {
		return armor;
	}
}
