package io.github.hsyyid.adminshop.utils;

import org.spongepowered.api.entity.player.Player;

public class ShopItem
{
	public Class type = this.getClass();
	public Player player;
	public String itemID;
	
	public ShopItem(Player player, String itemID)
	{
		this.player = player;
		this.itemID = itemID;
	}
	
	public Player getPlayer()
	{
		return player;
	}
	
	public String getItemID()
	{
		return itemID;
	}
}
