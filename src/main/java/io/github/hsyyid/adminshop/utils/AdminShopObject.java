package io.github.hsyyid.adminshop.utils;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class AdminShopObject
{
	public int itemAmount;
	public Integer meta = null;
	public double price;
	public String itemName;
	public Location<World> signLocation;

	public AdminShopObject(int itemAmount, double price, String itemName, Location<World> signLocation)
	{
		this.itemAmount = itemAmount;
		this.price = price;
		this.itemName = itemName;
		this.signLocation = signLocation;
	}

	public AdminShopObject(int itemAmount, double price, String itemName, Location<World> signLocation, int meta)
	{
		this.itemAmount = itemAmount;
		this.price = price;
		this.itemName = itemName;
		this.signLocation = signLocation;
		this.meta = meta;
	}

	public void setItemAmount(int itemAmount)
	{
		this.itemAmount = itemAmount;
	}

	public void setSignLocation(Location<World> signLocation)
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

	public void setMeta(int meta)
	{
		this.meta = meta;
	}

	public int getItemAmount()
	{
		return itemAmount;
	}

	public Location<World> getSignLocation()
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

	public Integer getMeta()
	{
			return meta;
	}
}
