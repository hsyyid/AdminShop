package io.github.hsyyid.adminshop.listeners;

import io.github.hsyyid.adminshop.AdminShop;
import io.github.hsyyid.adminshop.utils.AdminShopObject;
import io.github.hsyyid.adminshop.utils.ConfigManager;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class PlayerBreakBlockListener
{
	@Listener
	public void onPlayerBreakBlock(ChangeBlockEvent.Break event)
	{
		if (event.getCause().first(Player.class).isPresent())
		{
			Player player = (Player) event.getCause().first(Player.class).get();

			for (Transaction<BlockSnapshot> transaction : event.getTransactions())
			{
				if (transaction.getOriginal().getState() != null && (transaction.getOriginal().getState().getType() == BlockTypes.WALL_SIGN || transaction.getOriginal().getState().getType() == BlockTypes.STANDING_SIGN))
				{
					AdminShopObject thisShop = null;

					for (AdminShopObject shop : AdminShop.adminShops)
					{
						if (shop.getSignLocation().getX() == transaction.getOriginal().getLocation().get().getX() && shop.getSignLocation().getY() == transaction.getOriginal().getLocation().get().getY() && shop.getSignLocation().getZ() == transaction.getOriginal().getLocation().get().getZ())
						{
							thisShop = shop;
						}
					}

					if (thisShop != null && player.hasPermission("adminshop.remove"))
					{
						player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]:", TextColors.GREEN, " AdminShop successfully removed!"));
						AdminShop.adminShops.remove(thisShop);
						ConfigManager.writeAdminShops();
					}
					else if (thisShop != null)
					{
						player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]: Error!", TextColors.RED, " you do not have permission to destroy AdminShops!"));
						event.setCancelled(true);
					}
					else
					{
						AdminShopObject thisBuyShop = null;

						for (AdminShopObject shop : AdminShop.buyAdminShops)
						{
							if (shop.getSignLocation() != null && transaction.getOriginal().getLocation().isPresent() && shop.getSignLocation().getX() == transaction.getOriginal().getLocation().get().getX() && shop.getSignLocation().getY() == transaction.getOriginal().getLocation().get().getY() && shop.getSignLocation().getZ() == transaction.getOriginal().getLocation().get().getZ())
							{
								thisBuyShop = shop;
							}
						}

						if (thisBuyShop != null && player.hasPermission("adminshop.remove"))
						{
							player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]:", TextColors.GREEN, " AdminShop successfully removed!"));
							AdminShop.buyAdminShops.remove(thisBuyShop);
							ConfigManager.writeBuyAdminShops();
						}
						else if (thisBuyShop != null)
						{
							player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]: Error!", TextColors.RED, " you do not have permission to destroy AdminShops!"));
							event.setCancelled(true);
						}
					}
				}
			}
		}
	}
}
