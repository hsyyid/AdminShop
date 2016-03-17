package io.github.hsyyid.adminshop.utils;

import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.UUID;

public class ShopModifier
{
	private UUID uuid;
	private ItemStackSnapshot item;
	private double price;
	private boolean buyShop;

	public ShopModifier(UUID uuid, ItemStackSnapshot item, double price, boolean buyShop)
	{
		this.uuid = uuid;
		this.item = item;
		this.price = price;
		this.buyShop = buyShop;
	}

	public UUID getUuid()
	{
		return uuid;
	}

	public void setUuid(UUID uuid)
	{
		this.uuid = uuid;
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

	public boolean isBuyShop()
	{
		return buyShop;
	}

	public void setBuyShop(boolean buyShop)
	{
		this.buyShop = buyShop;
	}
}
