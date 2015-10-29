package com.gimmicknetwork.civpvpinventory;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PvPListener implements Listener {
	CivpvpInventory plugin;
	DuelManager dm;
	TeamManager tm;

	public PvPListener(CivpvpInventory plugin) {
		this.plugin = plugin;
		this.dm = plugin.getDuelManager();
		this.tm = plugin.getTeamManager();
	}

	@EventHandler
	public void playerLogin(PlayerLoginEvent e) {
		if (dm.getElo(e.getPlayer()) == null) {
			dm.firstTimeJoin(e.getPlayer());
		}
	}

	@EventHandler
	public void blockPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		if (!p.hasPermission("civpvp.badmin")) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void blockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		if (!p.hasPermission("civpvp.badmin")) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void playerDeath(EntityDeathEvent e) {
		if (e.getEntity() instanceof Player) {
			e.getDrops().clear();
			Player p = (Player) e.getEntity();
			if (dm.isInDuel(p)) {
				dm.playerLostDuel(p);
			}
		}
	}

	@EventHandler
	public void combatLog(PlayerQuitEvent e) {
		if (dm.isInDuel(e.getPlayer())) {
			dm.playerLostDuel(e.getPlayer());
		}
		if(tm.playerHasTeam(e.getPlayer().getUniqueId())) {
			tm.leaveTeam(e.getPlayer());
		}
	}
	
	@EventHandler
	public void playerHitPlayer(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			if (!dm.areDueling((Player) e.getEntity(), (Player)e.getDamager())) {
				((Player)e.getDamager()).sendMessage("You are not fighting this player currently!");
				e.setCancelled(true);
			}
		}
	}

}
