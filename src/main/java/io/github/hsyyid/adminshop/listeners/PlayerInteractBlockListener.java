package io.github.hsyyid.adminshop.listeners;

import io.github.hsyyid.adminshop.AdminShop;
import io.github.hsyyid.adminshop.utils.AdminShopModifierObject;
import io.github.hsyyid.adminshop.utils.AdminShopObject;
import io.github.hsyyid.adminshop.utils.ConfigManager;
import io.github.hsyyid.adminshop.utils.ItemUtil;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;

public class PlayerInteractBlockListener
{

	@Listener
	public void onPlayerInteractBlock(InteractBlockEvent event)
	{
		if (event.getCause().first(Player.class).isPresent())
		{
			Player player = (Player) event.getCause().first(Player.class).get();

			if (event.getTargetBlock().getState().getType() != null && (event.getTargetBlock().getState().getType() == BlockTypes.WALL_SIGN || event.getTargetBlock().getState().getType() == BlockTypes.STANDING_SIGN))
			{
				AdminShopObject thisShop = null;

				for (AdminShopObject adminShop : AdminShop.adminShops)
				{
					if (adminShop.getSignLocation() != null && adminShop.getSignLocation().getX() == event.getTargetBlock().getLocation().get().getX() && adminShop.getSignLocation().getY() == event.getTargetBlock().getLocation().get().getY() && adminShop.getSignLocation().getZ() == event.getTargetBlock().getLocation().get().getZ())
					{
						thisShop = adminShop;
					}
				}

				if (thisShop != null)
				{
					AdminShopModifierObject shopModifier = null;
					for (AdminShopModifierObject i : AdminShop.adminShopModifiers)
					{
						if (i.getPlayer().getUniqueId() == player.getUniqueId())
						{
							shopModifier = i;
							break;
						}
					}

					if (shopModifier != null)
					{
						AdminShop.adminShops.remove(thisShop);
						thisShop.setItemName(shopModifier.getItemID());

						if (shopModifier.getMeta() != null)
							thisShop.setMeta(shopModifier.getMeta());

						AdminShop.adminShops.add(thisShop);
						AdminShop.adminShopModifiers.remove(shopModifier);
						ConfigManager.writeAdminShops();
						player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GREEN, "Successfully set new item ID."));
					}
					else
					{
						int itemAmount = thisShop.getItemAmount();
						double price = thisShop.getPrice();
						String itemName = thisShop.getItemName();

						BigDecimal amount = new BigDecimal(price);

						if (!AdminShop.economyService.getAccount(player.getUniqueId()).isPresent())
						{
							AdminShop.economyService.createAccount(player.getUniqueId());
						}

						UniqueAccount playerAccount = AdminShop.economyService.getAccount(player.getUniqueId()).get();
						ResultType result = playerAccount.withdraw(AdminShop.economyService.getDefaultCurrency(), amount, Cause.of(this)).getResult();

						if (result == ResultType.SUCCESS)
						{
							player.sendMessage(Text.builder().append(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GOLD, "You have just bought " + itemAmount + " " + itemName + " for " + price + " ")).append(AdminShop.economyService.getDefaultCurrency().getPluralDisplayName()).build());

							if (thisShop.getMeta() != -1)
							{
								ItemUtil.givePlayerItem(player, itemName, itemAmount, thisShop.getMeta());
							}
							else
							{
								ItemUtil.givePlayerItem(player, itemName, itemAmount);
							}
						}
						else if (result == ResultType.ACCOUNT_NO_FUNDS)
						{
							player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.DARK_RED, "Error! ", TextColors.RED, "You don't have enough money to do that!"));
						}
						else if (result == ResultType.FAILED)
						{
							player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.DARK_RED, "Error! ", TextColors.RED, "Transaction failed!"));
						}
					}
				}
				else
				{
					AdminShopObject thisBuyShop = null;

					for (AdminShopObject buyAdminShop : AdminShop.buyAdminShops)
					{
						if (buyAdminShop.getSignLocation() != null && buyAdminShop.getSignLocation().getX() == event.getTargetBlock().getLocation().get().getX() && buyAdminShop.getSignLocation().getY() == event.getTargetBlock().getLocation().get().getY() && buyAdminShop.getSignLocation().getZ() == event.getTargetBlock().getLocation().get().getZ())
						{
							thisBuyShop = buyAdminShop;
						}
					}

					if (thisBuyShop != null)
					{
						AdminShopModifierObject shopModifier = null;
						for (AdminShopModifierObject i : AdminShop.adminShopModifiers)
						{
							if (i.getPlayer().getUniqueId() == player.getUniqueId())
							{
								shopModifier = i;
								break;
							}
						}

						if (shopModifier != null)
						{
							AdminShop.buyAdminShops.remove(thisBuyShop);
							thisBuyShop.setItemName(shopModifier.getItemID());
							if (shopModifier.getMeta() != null)
								thisBuyShop.setMeta(shopModifier.getMeta());
							AdminShop.buyAdminShops.add(thisBuyShop);
							AdminShop.adminShopModifiers.remove(shopModifier);
							ConfigManager.writeBuyAdminShops();
							player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GREEN, "Successfully set new item ID."));
						}
						else
						{
							int itemAmount = thisBuyShop.getItemAmount();
							double price = thisBuyShop.getPrice();
							String itemName = thisBuyShop.getItemName();

							if (!AdminShop.economyService.getAccount(player.getUniqueId()).isPresent())
							{
								AdminShop.economyService.createAccount(player.getUniqueId());
							}

							UniqueAccount playerAccount = AdminShop.economyService.getAccount(player.getUniqueId()).get();
							BigDecimal amount = new BigDecimal(price);
							int quantityInHand = 0;

							if (thisBuyShop.getMeta() != -1)
							{
								int meta = thisBuyShop.getMeta();

								if (player.getItemInHand().isPresent() && player.getItemInHand().get().getItem().getName().equals(itemName) && player.getItemInHand().get().getQuantity() == itemAmount && player.getItemInHand().get().toContainer().get(DataQuery.of("UnsafeDamage")).isPresent() && (Integer) player.getItemInHand().get().toContainer().get(DataQuery.of("UnsafeDamage")).get() == meta)
								{
									ResultType result = playerAccount.deposit(AdminShop.economyService.getDefaultCurrency(), amount, Cause.of(this)).getResult();

									if (result == ResultType.SUCCESS)
									{
										player.setItemInHand(null);
										player.sendMessage(Text.builder().append(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GOLD, "You have just sold " + itemAmount + " " + itemName + " for " + price + " ")).append(AdminShop.economyService.getDefaultCurrency().getPluralDisplayName()).build());
									}
									else if (result == ResultType.ACCOUNT_NO_SPACE)
									{
										player.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "Your account has no space for this!"));
									}
									else if (result == ResultType.FAILED)
									{
										player.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "Transaction failed!"));
									}
								}
								else if (player.getItemInHand().isPresent() && player.getItemInHand().get().getItem().getName().equals(itemName) && player.getItemInHand().get().getQuantity() > itemAmount && player.getItemInHand().get().toContainer().get(DataQuery.of("UnsafeDamage")).isPresent() && (Integer) player.getItemInHand().get().toContainer().get(DataQuery.of("UnsafeDamage")).get() == meta)
								{
									ResultType result = playerAccount.deposit(AdminShop.economyService.getDefaultCurrency(), amount, Cause.of(this)).getResult();

									if (result == ResultType.SUCCESS)
									{
										quantityInHand = player.getItemInHand().get().getQuantity() - itemAmount;
										player.getItemInHand().get().setQuantity(quantityInHand);
										player.sendMessage(Text.builder().append(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GOLD, "You have just sold " + itemAmount + " " + itemName + " for " + price + " ")).append(AdminShop.economyService.getDefaultCurrency().getPluralDisplayName()).build());
									}
									else if (result == ResultType.ACCOUNT_NO_SPACE)
									{
										player.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "Your account has no space for this!"));
									}
									else if (result == ResultType.FAILED)
									{
										player.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "Transaction failed!"));
									}
								}
								else
								{
									player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.DARK_RED, "Error! ", TextColors.RED, "You're not holding this item or the right quantity of this item!"));
								}
							}
							else
							{
								if (player.getItemInHand().isPresent() && player.getItemInHand().get().getItem().getName().equals(itemName) && player.getItemInHand().get().getQuantity() == itemAmount)
								{
									ResultType result = playerAccount.deposit(AdminShop.economyService.getDefaultCurrency(), amount, Cause.of(this)).getResult();

									if (result == ResultType.SUCCESS)
									{
										player.setItemInHand(null);
										player.sendMessage(Text.builder().append(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GOLD, "You have just sold " + itemAmount + " " + itemName + " for " + price + " ")).append(AdminShop.economyService.getDefaultCurrency().getPluralDisplayName()).build());
									}
									else if (result == ResultType.ACCOUNT_NO_SPACE)
									{
										player.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "Your account has no space for this!"));
									}
									else if (result == ResultType.FAILED)
									{
										player.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "Transaction failed!"));
									}
								}
								else if (player.getItemInHand().isPresent() && player.getItemInHand().get().getItem().getName().equals(itemName) && player.getItemInHand().get().getQuantity() > itemAmount)
								{
									ResultType result = playerAccount.deposit(AdminShop.economyService.getDefaultCurrency(), amount, Cause.of(this)).getResult();

									if (result == ResultType.SUCCESS)
									{
										quantityInHand = player.getItemInHand().get().getQuantity() - itemAmount;
										player.setItemInHand(null);
										ItemUtil.givePlayerItem(player, itemName, quantityInHand);
										player.sendMessage(Text.builder().append(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GOLD, "You have just sold " + itemAmount + " " + itemName + " for " + price + " ")).append(AdminShop.economyService.getDefaultCurrency().getPluralDisplayName()).build());
									}
									else if (result == ResultType.ACCOUNT_NO_SPACE)
									{
										player.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "Your account has no space for this!"));
									}
									else if (result == ResultType.FAILED)
									{
										player.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "Transaction failed!"));
									}
								}
								else
								{
									player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.DARK_RED, "Error! ", TextColors.RED, "You're not holding this item or the right quantity of this item!"));
								}
							}
						}
					}
				}
			}
		}
	}
}
