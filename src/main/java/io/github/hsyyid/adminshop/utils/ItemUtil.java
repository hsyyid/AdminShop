package io.github.hsyyid.adminshop.utils;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class ItemUtil
{
	public static void givePlayerItem(Player player, String itemId, int quantity, int meta)
	{
		Optional<ItemType> itemType = Sponge.getRegistry().getType(ItemType.class, itemId);

		if (itemType.isPresent())
		{
			ItemStack stack = ItemStack.builder().itemType(itemType.get()).quantity(quantity).build();
			DataContainer container = stack.toContainer().set(DataQuery.of("UnsafeDamage"), meta);
			stack = ItemStack.builder().fromContainer(container).build();
			player.getInventory().offer(stack);
		}
		else
		{
			player.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "Item not found!"));
		}
	}

	public static void givePlayerItem(Player player, String itemId, int quantity)
	{
		Optional<ItemType> itemType = Sponge.getRegistry().getType(ItemType.class, itemId);

		if (itemType.isPresent())
		{
			ItemStack stack = ItemStack.builder().itemType(itemType.get()).quantity(quantity).build();
			player.getInventory().offer(stack);
		}
		else
		{
			player.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "Item not found!"));
		}
	}
}
