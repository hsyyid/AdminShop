package io.github.hsyyid.adminshop.utils;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@ConfigSerializable
public class Shop
{
	@Setting
	private Location<World> signLocation;

	@Setting
	private ItemStackSnapshot item;

	@Setting
	private double price;

	@Setting
	private boolean buyShop;

	public Shop()
	{
		;
	}

	public Shop(Location<World> signLocation, ItemStackSnapshot item, double price, boolean buyShop)
	{
		this.signLocation = signLocation;
		this.item = item;
		this.price = price;
		this.buyShop = buyShop;
	}

	public ItemStackSnapshot getItem()
	{
		return item;
	}

	public void setItem(ItemStackSnapshot item)
	{
		this.item = item;
	}

	public double getPrice()
	{
		return price;
	}

	public void setPrice(double price)
	{
		this.price = price;
	}

	public Location<World> getSignLocation()
	{
		return signLocation;
	}

	public void setSignLocation(Location<World> signLocation)
	{
		this.signLocation = signLocation;
	}

	public boolean isBuyShop()
	{
		return buyShop;
	}

	public void setBuyShop(boolean buyShop)
	{
		this.buyShop = buyShop;
	}
}
