package io.github.hsyyid.adminshop.listeners;

import io.github.hsyyid.adminshop.AdminShop;
import io.github.hsyyid.adminshop.utils.ConfigManager;
import io.github.hsyyid.adminshop.utils.Shop;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class BreakBlockListener
{
	@Listener
	public void onBreakBlock(ChangeBlockEvent.Break event)
	{
		if (event.getCause().first(Player.class).isPresent())
		{
			Player player = event.getCause().first(Player.class).get();

			for (Transaction<BlockSnapshot> transaction : event.getTransactions())
			{
				Location<World> location = transaction.getOriginal().getLocation().get();
				Optional<Shop> shop = AdminShop.shops.values().stream().filter(s -> s.getSignLocation().equals(location)).findAny();

				if (shop.isPresent())
				{
					if (player.hasPermission("adminshop.remove"))
					{
						AdminShop.shops.values().remove(shop.get());
						ConfigManager.writeShops();
						player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GREEN, "Shop successfully removed!"));
					}
					else
					{
						player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.RED, "You do not have permission to remove shops."));
						event.setCancelled(true);
					}
				}
			}
		}
		else
		{
			for (Transaction<BlockSnapshot> transaction : event.getTransactions())
			{
				Location<World> location = transaction.getOriginal().getLocation().get();
				Optional<Shop> shop = AdminShop.shops.values().stream().filter(s -> s.getSignLocation().equals(location)).findAny();

				if (shop.isPresent())
				{
					event.setCancelled(true);
				}
			}
		}
	}
}
