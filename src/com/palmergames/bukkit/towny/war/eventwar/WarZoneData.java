package com.palmergames.bukkit.towny.war.eventwar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class WarZoneData {

	private HashMap<Town, HashSet<Player>> attackerTowns;
	private LinkedHashSet<Player> attackerQueue;
	private HashSet<Player> defenders;
	private HashSet<Player> allPlayers;
	
	public WarZoneData() {
		attackerTowns = new HashMap<Town, HashSet<Player>>();
		attackerQueue = new LinkedHashSet<Player>();
		defenders = new HashSet<Player>();
		allPlayers = new HashSet<Player>();
	}

	public int getHealthChange() {
		return defenders.size() - attackerQueue.size();
	}
	
	public void addAttacker(Player p) throws NotRegisteredException {
		Town town = getResident(p).getTown();
		if (attackerTowns.containsKey(town)) {
			attackerTowns.get(town).add(p);
		} else {
			HashSet<Player> playerAttackers = new HashSet<Player>();
			playerAttackers.add(p);
			attackerTowns.put(town, playerAttackers);
		}
		allPlayers.add(p);
		attackerQueue.add(p);
	}
	
	public void addDefender(Player p) {
		allPlayers.add(p);
		defenders.add(p);
	}
	
	public void removeDefender(Player p) {
		allPlayers.remove(p);
		defenders.remove(p);
	}
	
	public void removeAttacker(Player p) throws NotRegisteredException {
		allPlayers.remove(p);
		Town attackerTown = getResident(p).getTown();
		HashSet<Player> attackerPlayers = attackerTowns.get(attackerTown);
		if (attackerPlayers != null) {
			attackerPlayers.remove(p);
			if (attackerPlayers.size() == 0) {
				attackerTowns.remove(attackerTown);
			}
		}
		attackerQueue.remove(p);
	}
	
	public boolean hasAttackers() {
		return attackerQueue.size() != defenders.size();
	}
	
	public boolean hasDefenders() {
		return !defenders.isEmpty();
	}
	
	public HashSet<Player> getDefenders() {
		return defenders;
	}
	
	public Set<Town> getAttackerTowns() {
		return attackerTowns.keySet();
	}
	
	public Player getLongestStandingAttacker() {
		if (!attackerQueue.isEmpty()) {
			return attackerQueue.iterator().next();
		}
		return null;
	}
	
	public Player getRandomDefender() {
		// Not truly random, gets the first player in the set
		if (!defenders.isEmpty()) {
			return defenders.iterator().next();
		}
		return null;
	}
	
	public HashSet<Player> getAllPlayers() {
		return allPlayers;
	}
	
	private Resident getResident(Player p) throws NotRegisteredException {
		return TownyUniverse.getDataSource().getResident(p.getName());
	}
}