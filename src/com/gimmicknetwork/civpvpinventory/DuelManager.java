package com.gimmicknetwork.civpvpinventory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class DuelManager {
	private final int AVERAGE_ELO_GAIN = 20;
	private final int STARTING_ELO = 1000;
	private CivpvpInventory plugin;
	private HashMap<UUID, Integer> elo = new HashMap<UUID, Integer>();
	private HashMap<UUID, UUID> runningDuels = new HashMap<UUID, UUID>();
	private HashMap<UUID, UUID> requestedDuels = new HashMap<UUID, UUID>();

	public DuelManager(CivpvpInventory plugin) {
		this.plugin = plugin;
	}

	public void parseElosFromFile() {
		YamlConfiguration c = YamlConfiguration.loadConfiguration(new File(
				plugin.getDataFolder() + "/elos.yml"));
		for(String key:c.getKeys(false)) {
			UUID uuid = UUID.fromString(key);
			int playerElo = c.getInt(key);
			elo.put(uuid, playerElo);
		}
	}

	public void saveElosToFile() {
		YamlConfiguration c = YamlConfiguration.loadConfiguration(new File(
				plugin.getDataFolder() + "/elos.yml"));
		for (Map.Entry<UUID, Integer> current : elo.entrySet()) {
			c.set(current.getKey().toString(), current.getValue().intValue());
		}
		try {
			c.save(new File(plugin.getDataFolder() + "/elos.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public Integer getElo(Player p) {
		return elo.get(p.getUniqueId());
	}

	public void startDuel(Player first, Player second) {
		if (first != null && second != null) {
			runningDuels.put(first.getUniqueId(), second.getUniqueId());
			runningDuels.put(second.getUniqueId(), first.getUniqueId());
			first.sendMessage("You are now fighting " + second.getName());
			second.sendMessage("You are now fighting " + first.getName());
		}
	}

	public boolean isInDuel(Player p) {
		return runningDuels.containsKey(p.getUniqueId());
	}

	public void playerWonDuel(Player winner) {
		if (!isInDuel(winner)) {
			plugin.getLogger().log(
					Level.INFO,
					"Player " + winner.getName()
							+ " tried to win but wasnt in a duel");
			return;
		}
		UUID winnerUUID = winner.getUniqueId();
		Player loser = plugin.getServer().getPlayer(
				runningDuels.get(winnerUUID));
		int eloDif = calculateEloDifference(winner);
		winner.sendMessage("You won against " + loser.getName()
				+ ". You gained " + eloDif + " elo, total now: "
				+ (elo.get(winnerUUID) + eloDif));
		elo.put(winnerUUID, elo.get(winnerUUID) + eloDif);
		UUID loserUUID = loser.getUniqueId();
		loser.sendMessage("You lost against " + winner.getName()
				+ ". You lost " + eloDif + " elo, total now: "
				+ (elo.get(loserUUID) - eloDif));
		elo.put(loserUUID, elo.get(loserUUID) - eloDif);
		plugin.getLogger().log(
				Level.INFO,
				winner.getName() + "won against " + loser.getName()
						+ " elo diff:" + eloDif);
		runningDuels.remove(loserUUID);
		runningDuels.remove(winnerUUID);
		teleportToLobby(winner);
		teleportToLobby(loser);
	}

	public void playerLostDuel(Player loser) {
		playerWonDuel(plugin.getServer().getPlayer(
				runningDuels.get(loser.getUniqueId())));
	}

	public void requestDuel(Player isAsking, Player gotAsked) {
		requestedDuels.put(gotAsked.getUniqueId(), isAsking.getUniqueId());
	}

	public void acceptDuel(Player p) {
		startDuel(
				p,
				plugin.getServer().getPlayer(
						requestedDuels.get(p.getUniqueId())));
		requestedDuels.remove(p.getUniqueId());
	}

	public int calculateEloDifference(Player p) {
		int loser = elo.get(runningDuels.get(p.getUniqueId()));
		int winner = elo.get(p.getUniqueId());
		double diff = winner - loser;
		double factor = diff / (double) 400;
		double relativeFactor = 1D / (1D + Math.pow(10D, factor));
		return (int) (relativeFactor * AVERAGE_ELO_GAIN);
	}

	public void teleportToLobby(Player p) {
		// p.teleport(new Location(p.getWorld(), 0, 0, 0)); // TODO adjust
	}

	public void firstTimeJoin(Player p) {
		elo.put(p.getUniqueId(), STARTING_ELO);
	}

	public boolean wasRequested(Player p) {
		return requestedDuels.containsKey(p.getUniqueId());
	}
	
	public boolean areDueling(Player a, Player b) {
		return runningDuels.get(a.getUniqueId()) == b.getUniqueId();
	}

}
