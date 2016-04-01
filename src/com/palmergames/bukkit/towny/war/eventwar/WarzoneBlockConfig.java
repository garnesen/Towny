package com.palmergames.bukkit.towny.war.eventwar;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.config.ConfigNodes;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.object.TownyPermission.ActionType;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.palmergames.bukkit.util.BukkitTools;

public class WarzoneBlockConfig {

	private static Set<Material> editableMaterialsInWarZone = null;
	
	public static boolean canDoInWarzone(Player player, Integer blockId, byte data, TownyPermission.ActionType action) {
		
		if (action == ActionType.BUILD && !isEditableMaterialInWarZone(BukkitTools.getMaterial(blockId))) {
			return false;
		} else if (action == ActionType.DESTROY && !isEditableMaterialInWarZone(BukkitTools.getMaterial(blockId))) {
			PlayerCacheUtil.cacheBlockErrMsg(player, String.format(TownySettings.getLangString("msg_err_warzone_cannot_edit_material"), "destroy", BukkitTools.getMaterial(blockId).toString().toLowerCase()));
			return false;
		} else if (action == ActionType.ITEM_USE) {
			return false;
		} else if (action == ActionType.SWITCH) {
			return false;
		} 
		
		return true;
	}
	
	public static void setEditableMaterialsInWarZone(Set<Material> editableMaterialsInWarZone) {

		WarzoneBlockConfig.editableMaterialsInWarZone = editableMaterialsInWarZone;
	}

	public static boolean isEditableMaterialInWarZone(Material material) {

		return WarzoneBlockConfig.editableMaterialsInWarZone.contains(material);
	}
	
	public static boolean isAllowingSwitchesInWarZone() {

		return TownySettings.getBoolean(ConfigNodes.WAR_WARZONE_SWITCH);
	}

	public static boolean isAllowingFireInWarZone() {

		return TownySettings.getBoolean(ConfigNodes.WAR_WARZONE_FIRE);
	}

	public static boolean isAllowingItemUseInWarZone() {

		return TownySettings.getBoolean(ConfigNodes.WAR_WARZONE_ITEM_USE);
	}

	public static boolean isAllowingExplosionsInWarZone() {

		return TownySettings.getBoolean(ConfigNodes.WAR_WARZONE_EXPLOSIONS);
	}

	public static boolean explosionsBreakBlocksInWarZone() {

		return TownySettings.getBoolean(ConfigNodes.WAR_WARZONE_EXPLOSIONS_BREAK_BLOCKS);
	}

	public static boolean regenBlocksAfterExplosionInWarZone() {

		return TownySettings.getBoolean(ConfigNodes.WAR_WARZONE_EXPLOSIONS_REGEN_BLOCKS);
	}
	
}
