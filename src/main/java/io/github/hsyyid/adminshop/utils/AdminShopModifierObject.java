package io.github.hsyyid.adminshop.utils;

import org.spongepowered.api.entity.living.player.Player;

public class AdminShopModifierObject
{
	public Player player;
	public String itemID;
	public Integer meta = null;

	public AdminShopModifierObject(Player player, String itemID)
	{
		this.player = player;
		this.itemID = itemID;
	}

	public AdminShopModifierObject(Player player, String itemID, int meta)
	{
		this.player = player;
		this.itemID = itemID;
		this.meta = meta;
	}

	public Player getPlayer()
	{
		return player;
	}

	public String getItemID()
	{
		return itemID;
	}

	public Integer getMeta()
	{
			return meta;
	}
}
