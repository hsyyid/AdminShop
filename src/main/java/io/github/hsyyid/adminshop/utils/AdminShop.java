package io.github.hsyyid.adminshop.utils;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.world.Location;

public class AdminShop
{
	public int itemAmount;
	public double price;
	public String itemName;
	public Location signLocation;
	
	public AdminShop(int itemAmount, double price, String itemName, Location signLocation)
	{
		this.itemAmount = itemAmount;
		this.price = price;
		this.itemName = itemName;
		this.signLocation = signLocation;
	}
	
	public void setItemAmount(int itemAmount)
	{
		this.itemAmount = itemAmount;
	}
	
	public void setSignLocation(Location signLocation)
	{
		this.signLocation = signLocation;
	}
	
	public void setPrice(double price)
	{
		this.price = price;
	}
	
	public void setItemName(String itemName)
	{
		this.itemName = itemName;
	}
	
	public int getItemAmount()
	{
		return itemAmount;
	}
	
	public Location getSignLocation()
	{
		return signLocation;
	}
	
	public double getPrice()
	{
		return price;
	}
	
	public String getItemName()
	{
		return itemName;
	}
}
