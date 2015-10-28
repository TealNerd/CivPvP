package com.gimmicknetwork.civpvpinventory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class KitManager {

	private Database db;
	
	public KitManager(Database db) {
		this.db = db;
		db.execute("CREATE TABLE IF NOT EXISTS kits (name VARCHAR(16) not null UNIQUE, owner VARCHAR(40) not null)");
	}
	
	public boolean kitExists(String name) {
		try {
			PreparedStatement getKit = db.prepareStatement("SELECT * FROM kits WHERE name = ?");
			getKit.setString(1, name);
			ResultSet set = getKit.executeQuery();
			return set.first();
		} catch (Exception ex) {
			return false;
		}
	}
	
	public Kit getKitByName(String name) {
		try {
			PreparedStatement getKit = db.prepareStatement("SELECT * FROM " + name + "_items");
			ResultSet itemSet = getKit.executeQuery();
			ArrayList<ItemStack> itemList = new ArrayList<ItemStack>();
			ArrayList<ItemStack> armorList = new ArrayList<ItemStack>();
			while(itemSet.next()) {
				String material = itemSet.getString("material");
				int amount = itemSet.getInt("amount");
				String itemName = itemSet.getString("name");
				boolean isArmor = itemSet.getBoolean("armor");
				ItemStack item = new ItemStack(Material.getMaterial(material), amount);
				if(itemSet.getString("enchants") != null) {
					String[] enchantments = itemSet.getString("enchants").split(",");
					for(String enchant : enchantments) {
						String enchantName = enchant.split("\\|")[0];
						int enchantLevel = Integer.parseInt(enchant.split("\\|")[1]);
						item.addEnchantment(Enchantment.getByName(enchantName), enchantLevel);
					}
				}
				short durability = itemSet.getShort("durability");
				item.setDurability(durability);
				if(itemName != null && itemName.length() > 0) {
					item.getItemMeta().setDisplayName(name);
				}
				if(isArmor) {
					armorList.add(item);
				} else {
					itemList.add(item);
				}
			}
			PreparedStatement getOwners = db.prepareStatement("SELECT * FROM " + name + "_owners");
			ResultSet ownerSet = getOwners.executeQuery();
			ArrayList<UUID> ownerList = new ArrayList<UUID>();
			while(ownerSet.next()) {
				ownerList.add(UUID.fromString(ownerSet.getString("uuid")));
			}
			PreparedStatement getOwner = db.prepareStatement("SELECT owner FROM kits WHERE name = '" + name + "'");
			ResultSet set = getOwner.executeQuery();
			set.next();
			UUID owner = UUID.fromString(set.getString("owner"));
			ItemStack[] items = new ItemStack[itemList.size()];
			for(int i = 0; i < items.length; i++) {
				items[i] = itemList.get(i);
			}
			ItemStack[] armor = new ItemStack[armorList.size()];
			for(int i = 0; i < armor.length; i++) {
				armor[i] = armorList.get(i);
			}
			if(ownerList.size() == 0) {
				return new Kit(name, owner, items, armor);
			} else {
				return new Kit(name, owner, ownerList, items, armor);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public void addOwnerToKit(String kitName, UUID owner) {
		db.execute("INSERT INTO " + kitName + "_owners (uuid) VALUES ('" + owner.toString() + "')");
	}
	
	public void removeOwner(String kitName, UUID owner) {
		try {
			PreparedStatement removeOwner = db.prepareStatement("DELETE FROM " + kitName + "_owners WHERE uuid = ?");
			removeOwner.setString(1, owner.toString());
			removeOwner.execute();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void transferOwner(String kitName, UUID newOwner) {
		try {
			PreparedStatement transferOwner = db.prepareStatement("UPDATE kits SET name=?,owner=? WHERE name=?");
			transferOwner.setString(1, kitName);
			transferOwner.setString(2, newOwner.toString());
			transferOwner.setString(3, kitName);
			transferOwner.execute();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void updateKitInventory(Kit kit, ItemStack[] items, ItemStack[] armor) {
		System.out.println("Updating kit " + kit.getName() + " in the database");
		try {
			db.execute("truncate table " + kit.getName() + "_items");
			PreparedStatement insertItem = db.prepareStatement("INSERT INTO " + kit.getName() + "_items (material, amount, enchants, name, armor, durability) VALUES (?,?,?,?,?,?)");
			for(ItemStack is : items) {
				if(is != null) {
					insertItem.setString(1, is.getType().toString());
					insertItem.setInt(2, is.getAmount());
					if(is.getEnchantments() != null && is.getEnchantments().size() > 0) {
						StringBuilder enchantments = new StringBuilder();
						for(Enchantment e : is.getEnchantments().keySet()) {
							enchantments.append(e.getName()).append("|").append(is.getEnchantmentLevel(e)).append(",");
						}
						insertItem.setString(3, enchantments.toString().substring(0, enchantments.toString().length() - 2));
					} else {
						insertItem.setString(3, null);
					}
					insertItem.setString(4, is.getItemMeta() != null ? is.getItemMeta().getDisplayName() : "");
					insertItem.setBoolean(5, false);
					insertItem.setShort(6, is.getDurability());
					insertItem.execute();
				}
			}
			for(ItemStack is : armor) {
				if(is != null) {
					insertItem.setString(1, is.getType().toString());
					insertItem.setInt(2, is.getAmount());
					if(is.getEnchantments() != null && is.getEnchantments().size() > 0) {
						StringBuilder enchantments = new StringBuilder();
						for(Enchantment e : is.getEnchantments().keySet()) {
							enchantments.append(e.getName()).append("|").append(is.getEnchantmentLevel(e)).append(",");
						}
						insertItem.setString(3, enchantments.toString().substring(0, enchantments.toString().length() - 2));
					} else {
						insertItem.setString(3, null);
					}
					insertItem.setString(4, is.getItemMeta() != null ? is.getItemMeta().getDisplayName() : "");
					insertItem.setBoolean(5, true);
					insertItem.setShort(6, is.getDurability());
					insertItem.execute();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void addKit(Kit kit) {
		System.out.println("Adding kit " + kit.getName() + " to the database");
		try {
			PreparedStatement initItemTable = db.prepareStatement("CREATE TABLE IF NOT EXISTS " + kit.getName() + "_items ("
																	+ "material VARCHAR(40) not null,"
																	+ "amount INT(64) not null,"
																	+ "enchants VARCHAR(250),"
																	+ "name VARCHAR(30),"
																	+ "armor BOOLEAN not null,"
																	+ "durability SMALLINT not null);");
			initItemTable.execute();
			PreparedStatement initOwnerTable = db.prepareStatement("CREATE TABLE IF NOT EXISTS " + kit.getName() + "_owners ("
																	+ "uuid VARCHAR(40) not null UNIQUE);");
			initOwnerTable.execute();
			db.execute("INSERT INTO kits (name, owner) VALUES ('" + kit.getName() + "','" + kit.getOwner().toString() + "')");
			PreparedStatement insertItem = db.prepareStatement("INSERT INTO " + kit.getName() + "_items (material, amount, enchants, name, armor, durability) VALUES (?,?,?,?,?,?)");
			for(ItemStack is : kit.getItems()) {
				if(is != null) {
					insertItem.setString(1, is.getType().toString());
					insertItem.setInt(2, is.getAmount());
					if(is.getEnchantments() != null && is.getEnchantments().size() > 0) {
						StringBuilder enchantments = new StringBuilder();
						for(Enchantment e : is.getEnchantments().keySet()) {
							enchantments.append(e.getName()).append("|").append(is.getEnchantmentLevel(e)).append(",");
						}
						insertItem.setString(3, enchantments.toString().substring(0, enchantments.toString().length() - 2));
					} else {
						insertItem.setString(3, null);
					}
					insertItem.setString(4, is.getItemMeta() != null ? is.getItemMeta().getDisplayName() : "");
					insertItem.setBoolean(5, false);
					insertItem.setShort(6, is.getDurability());
					insertItem.execute();
				}
			}
			for(ItemStack is : kit.getArmor()) {
				if(is != null) {
					insertItem.setString(1, is.getType().toString());
					insertItem.setInt(2, is.getAmount());
					if(is.getEnchantments() != null && is.getEnchantments().size() > 0) {
						StringBuilder enchantments = new StringBuilder();
						for(Enchantment e : is.getEnchantments().keySet()) {
							enchantments.append(e.getName()).append("|").append(is.getEnchantmentLevel(e)).append(",");
						}
						insertItem.setString(3, enchantments.toString().substring(0, enchantments.toString().length() - 1));
					} else {
						insertItem.setString(3, null);
					}
					insertItem.setString(4, is.getItemMeta() != null ? is.getItemMeta().getDisplayName() : "");
					insertItem.setBoolean(5, true);
					insertItem.setShort(6, is.getDurability());
					insertItem.execute();
				}
			}
			PreparedStatement addOwner = db.prepareStatement("INSERT INTO " + kit.getName() + "_owners (uuid) VALUES (?)");
			for(UUID id : kit.getOwners()) {
				addOwner.setString(1, id.toString());
				addOwner.execute();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
