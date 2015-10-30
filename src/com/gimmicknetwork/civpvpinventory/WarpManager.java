package com.gimmicknetwork.civpvpinventory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class WarpManager {
	private Random rng = new Random();
	private HashMap<String, Warp> warps = new HashMap<String, Warp>();
	private HashMap<UUID, Warp> occupiedWarps = new HashMap<UUID, Warp>();

	public WarpManager() {
		loadWarps();
	}

	public void loadWarps() {
		File wrps = new File(CivpvpInventory.getInstance().getDataFolder()
				.getAbsolutePath()
				+ "warps.yml");
		if (wrps.exists()) {
		YamlConfiguration yml = YamlConfiguration.loadConfiguration(wrps);
		for (String key : yml.getKeys(false)) {
			ConfigurationSection current = yml.getConfigurationSection(key);
			Location first = parseLocation(current
					.getConfigurationSection("first"));
			Location second = parseLocation(current
					.getConfigurationSection("second"));
			warps.put(key, new Warp(key, first, second));
		}}
		else {
			try {
			wrps.createNewFile(); }
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public Location parseLocation(ConfigurationSection config) {
		String world = config.getString("world");
		int x = config.getInt("x");
		int y = config.getInt("y");
		int z = config.getInt("z");
		return new Location(CivpvpInventory.getInstance().getServer()
				.getWorld(world), x, y, z);
	}

	public void saveLocation(ConfigurationSection c, Location loc) {
		c.set("x", loc.getBlockX());
		c.set("y", loc.getBlockY());
		c.set("z", loc.getBlockZ());
		c.set("world", loc.getWorld().getName());
	}

	public void saveWarps() {
		File wrps = new File(CivpvpInventory.getInstance().getDataFolder()
				.getAbsolutePath()
				+ "warps.yml");
		YamlConfiguration yml = YamlConfiguration.loadConfiguration(wrps);
		for (Warp w : warps.values()) {
			ConfigurationSection currentWarp = yml.createSection(w.getName());
			ConfigurationSection firstSection = currentWarp
					.createSection("first");
			saveLocation(firstSection, w.getFirst());
			ConfigurationSection secondSection = currentWarp
					.createSection("second");
			saveLocation(secondSection, w.getSecond());
		}
		try {
			yml.save(wrps);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Warp getWarp(String name) {
		return warps.get(name);
	}

	public Warp getRandomWarp(UUID uuid) {
		Warp[] w = warps.values().toArray(new Warp [warps.values().size()]);
		if (w.length == occupiedWarps.size()) {
			return null;
		}
		if (w.length > 0) {
			Warp result = w[rng.nextInt(w.length)];
			if (occupiedWarps.values().contains(result)) {
				return getRandomWarp(uuid);
			} else {
				occupiedWarps.put(uuid, result);
				return result;
			}
		}
		return null;
	}

	public void warpFreeAgain(UUID uuid) {
		occupiedWarps.remove(uuid);
	}

	public void addWarp(Warp w) {
		warps.put(w.getName(), w);
	}

	public void removeWarp(String s) {
		warps.remove(s);
	}
}
