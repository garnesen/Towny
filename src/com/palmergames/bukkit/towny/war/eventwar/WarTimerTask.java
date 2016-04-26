package com.palmergames.bukkit.towny.war.eventwar;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.tasks.TownyTimerTask;

public class WarTimerTask extends TownyTimerTask {

	War warEvent;
	ConcurrentHashMap<TownBlock, WarZoneData> warzoneDataCache;

	public WarTimerTask(Towny plugin, War warEvent) {

		super(plugin);
		this.warEvent = warEvent;
		warzoneDataCache = new ConcurrentHashMap<TownBlock, WarZoneData>();
		initializeCache();
	}
	
	/**
	 * Every tick of war:
	 *	1. Check if war has ended
	 *	2. Updates all the active warzones
	 *
	 *	- In NationCommand.java: Removes a nation from war if a nation declares neutral and is in war.
	 *	- In TownyPlayerListener.java: Updates the HashMap when a player in war changes plots. 
	 */
	@Override
	public void run() {

		// Check if war has ended and end gracefully
		if (!warEvent.isWarTime()) {
			warEvent.end();
			universe.clearWarEvent();
			plugin.resetCache();
			TownyMessaging.sendDebugMsg("War ended.");
			return;
		}
			
		// Send warzone updates
		for (Entry<TownBlock, WarZoneData> entry : warzoneDataCache.entrySet()) {
			try {
				warEvent.updateWarzone(entry.getKey(), entry.getValue());
			} catch (NotRegisteredException e) {
				TownyMessaging.sendDebugMsg("[War]   WarZone Update Failed");
			}
		}

	}
	
	public void updateWarzoneDataCache(Player p, WorldCoord to, WorldCoord from) {
		
		// Already checked if this player is in war
		try {
			TownyMessaging.sendDebugMsg("[War]   " + p.getName() + ": ");
			
			Resident resident = TownyUniverse.getDataSource().getResident(p.getName());
			Nation nation = resident.getTown().getNation();
			
			if (!warEvent.isWarZone(to))
				return;
			TownyMessaging.sendDebugMsg("[War]   warZone");
			
			if (p.getLocation().getBlockY() < TownySettings.getMinWarHeight())
				return;
			TownyMessaging.sendDebugMsg("[War]   aboveMinHeight");
			
			TownBlock townBlock = to.getTownBlock();
			TownBlock fromBlock = (to == null) ? null : to.getTownBlock();
			boolean healablePlots = TownySettings.getPlotsHealableInWar();
			if (healablePlots && (nation == townBlock.getTown().getNation() || townBlock.getTown().getNation().hasAlly(nation))) {
				if (warzoneDataCache.containsKey(townBlock))
					warzoneDataCache.get(townBlock).addDefender(p);
				else {
					WarZoneData wzd = new WarZoneData();
					wzd.addDefender(p);
					warzoneDataCache.put(townBlock, wzd);
				}
				
				if (fromBlock != null && warzoneDataCache.contains(fromBlock)) {
					warzoneDataCache.get(fromBlock).removeDefender(p);
				}
				TownyMessaging.sendDebugMsg("[War]   healed");
				return;
			}
			TownyMessaging.sendDebugMsg("[War]   notAlly/notNation");
			
			boolean edgesOnly = TownySettings.getOnlyAttackEdgesInWar();
			if (edgesOnly && !isOnEdgeOfTown(townBlock, to, warEvent))
				return;
			if (edgesOnly)
				TownyMessaging.sendDebugMsg("[War]   onEdge");

			if (warzoneDataCache.containsKey(townBlock))
				warzoneDataCache.get(townBlock).addAttacker(p);
			else {
				WarZoneData wzd = new WarZoneData();
				wzd.addAttacker(p);
				warzoneDataCache.put(townBlock, wzd);
			}
			
			if (fromBlock != null && warzoneDataCache.contains(fromBlock)) {
				warzoneDataCache.get(fromBlock).removeAttacker(p);
			}
			TownyMessaging.sendDebugMsg("[War]   damaged");
			
			
		} catch (NotRegisteredException e) {}
	}
	
	private void initializeCache() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			try {
				Resident res = TownyUniverse.getDataSource().getResident(p.getName());
				if (warEvent.isResidentInWar(res)) {
					WorldCoord to = new WorldCoord(p.getWorld().getName(), Coord.parseCoord(p));
					updateWarzoneDataCache(p, to, null);
				}
			} catch (NotRegisteredException e) {
				// Not in war
			}
		}
	}
	
	public static boolean isOnEdgeOfTown(TownBlock townBlock, WorldCoord worldCoord, War warEvent) {

		int[][] offset = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
		for (int i = 0; i < 4; i++)
			try {
				TownBlock edgeTownBlock = worldCoord.getTownyWorld().getTownBlock(new Coord(worldCoord.getX() + offset[i][0], worldCoord.getZ() + offset[i][1]));
				boolean sameTown = edgeTownBlock.getTown() == townBlock.getTown();
				if (!sameTown || (sameTown && !warEvent.isWarZone(edgeTownBlock.getWorldCoord()))) {
					return true;
				}
			} catch (NotRegisteredException e) {
				return true;
			}
		return false;
	}
}