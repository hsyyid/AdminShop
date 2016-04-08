package io.github.hsyyid.adminshop.listeners;

import io.github.hsyyid.adminshop.AdminShop;
import io.github.hsyyid.adminshop.utils.ConfigManager;
import io.github.hsyyid.adminshop.utils.Shop;
import io.github.hsyyid.adminshop.utils.ShopModifier;
import net.minecraft.entity.EntityHanging;
import net.minecraft.util.EnumFacing;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class PlayerInteractBlockListener
{
	@Listener
	public void onPlayerRightClickBlock(InteractBlockEvent.Secondary event, @First Player player)
	{
		if (event.getTargetBlock().getState().getType() == BlockTypes.WALL_SIGN || event.getTargetBlock().getState().getType() == BlockTypes.STANDING_SIGN)
		{
			Location<World> location = event.getTargetBlock().getLocation().get();
			Optional<Shop> foundShop = AdminShop.shops.values().stream().filter(s -> s.getSignLocation().equals(location)).findAny();
			Optional<ShopModifier> shopModifier = AdminShop.shopModifiers.stream().filter(s -> s.getUuid().equals(player.getUniqueId())).findAny();

			if (!foundShop.isPresent())
			{
				if (shopModifier.isPresent())
				{
					if (player.hasPermission("adminshop.create"))
					{
						AdminShop.shops.put(UUID.randomUUID(), new Shop(location, shopModifier.get().getItem(), shopModifier.get().getPrice(), shopModifier.get().isBuyShop()));
						AdminShop.shopModifiers.remove(shopModifier.get());

						if (ConfigManager.shouldAddItemFrames() && location.getBlock().getType() == BlockTypes.WALL_SIGN)
						{
							Location<World> frameLocation = location.add(0, 1, 0);
							Optional<Entity> itemFrame = location.getExtent().createEntity(EntityTypes.ITEM_FRAME, frameLocation.getPosition());

							if (itemFrame.isPresent())
							{
								ItemFrame entity = (ItemFrame) itemFrame.get();
								entity.offer(Keys.REPRESENTED_ITEM, shopModifier.get().getItem());
								// TODO: Update this when Sponge supports it
								// entity.offer(Keys.DIRECTION, location.getBlock().get(Keys.DIRECTION).get());
								((EntityHanging) entity).updateFacingWithBoundingBox(EnumFacing.byName(location.getBlock().get(Keys.DIRECTION).get().name()));

								if (((EntityHanging) entity).onValidSurface())
								{
									location.getExtent().spawnEntity(entity, Cause.of(NamedCause.source(player)));
								}
							}
						}

						player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GREEN, "Created shop!"));
					}
					else
					{
						player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.RED, "You do not have permission to create AdminShops."));
					}

					return;
				}
			}
			else if (shopModifier.isPresent())
			{
				if (player.hasPermission("adminshop.modify"))
				{
					AdminShop.shops.values().remove(foundShop.get());
					AdminShop.shops.put(UUID.randomUUID(), new Shop(location, shopModifier.get().getItem(), shopModifier.get().getPrice(), shopModifier.get().isBuyShop()));
					AdminShop.shopModifiers.remove(shopModifier.get());
					ConfigManager.writeShops();

					if (!shopModifier.get().getItem().equals(foundShop.get().getItem()) && ConfigManager.shouldAddItemFrames() && location.getBlock().getType() == BlockTypes.WALL_SIGN)
					{
						Location<World> frameLocation = location.add(0, 1, 0);

						// Remove Previous ItemFrames if existing
						Collection<Entity> foundItemFrames = location.getExtent().getEntities(e -> e.getLocation().equals(frameLocation) && e.getType() == EntityTypes.ITEM_FRAME);
						foundItemFrames.forEach(e -> e.remove());

						Optional<Entity> itemFrame = location.getExtent().createEntity(EntityTypes.ITEM_FRAME, frameLocation.getPosition());

						if (itemFrame.isPresent())
						{
							ItemFrame entity = (ItemFrame) itemFrame.get();
							entity.offer(Keys.REPRESENTED_ITEM, shopModifier.get().getItem());
							// TODO: Update this when Sponge supports it
							// entity.offer(Keys.DIRECTION, location.getBlock().get(Keys.DIRECTION).get());
							((EntityHanging) entity).updateFacingWithBoundingBox(EnumFacing.byName(location.getBlock().get(Keys.DIRECTION).get().name()));

							if (((EntityHanging) entity).onValidSurface())
							{
								location.getExtent().spawnEntity(entity, Cause.of(NamedCause.source(player)));
							}
						}
					}

					player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GREEN, "Updated shop."));
				}
				else
				{
					player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.RED, "You do not have permission to modify AdminShops."));
				}

				return;
			}
			else
			{
				Shop shop = foundShop.get();
				BigDecimal price = new BigDecimal(shop.getPrice());
				UniqueAccount playerAccount = AdminShop.economyService.getOrCreateAccount(player.getUniqueId()).get();

				if (shop.isBuyShop())
				{
					if (player.getItemInHand().isPresent() && player.getItemInHand().get().getItem() == shop.getItem().getType() && player.getItemInHand().get().getQuantity() == shop.getItem().getCount())
					{
						player.setItemInHand(null);
					}
					else if (player.getItemInHand().isPresent() && player.getItemInHand().get().getItem() == shop.getItem().getType() && player.getItemInHand().get().getQuantity() > shop.getItem().getCount())
					{
						ItemStack stack = player.getItemInHand().get();
						int quantityInHand = stack.getQuantity() - shop.getItem().getCount();
						stack.setQuantity(quantityInHand);
						player.setItemInHand(stack);
					}
					else
					{
						player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.DARK_RED, "Error! ", TextColors.RED, "You're not holding this item or the right quantity of this item!"));
						return;
					}

					ResultType result = playerAccount.deposit(AdminShop.economyService.getDefaultCurrency(), price, Cause.of(NamedCause.source(player))).getResult();

					if (result == ResultType.SUCCESS)
					{
						player.sendMessage(Text.builder().append(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GOLD, "You have just sold " + shop.getItem().getCount() + " " + shop.getItem().getType().getTranslation().get() + " for " + price + " ")).append(AdminShop.economyService.getDefaultCurrency().getPluralDisplayName()).build());
					}
					else if (result == ResultType.ACCOUNT_NO_SPACE)
					{
						player.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "Your account has no space for this!"));
					}
					else
					{
						player.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "Transaction failed!"));
					}
				}
				else
				{
					ResultType result = playerAccount.withdraw(AdminShop.economyService.getDefaultCurrency(), price, Cause.of(NamedCause.source(player))).getResult();

					if (result == ResultType.SUCCESS)
					{
						player.sendMessage(Text.builder().append(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GOLD, "You have just bought " + shop.getItem().getCount() + " " + shop.getItem().getType().getTranslation().get() + " for " + price + " ")).append(AdminShop.economyService.getDefaultCurrency().getPluralDisplayName()).build());
						player.getInventory().offer(shop.getItem().createStack());
					}
					else if (result == ResultType.ACCOUNT_NO_FUNDS)
					{
						player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.DARK_RED, "Error! ", TextColors.RED, "You don't have enough money to do that!"));
					}
					else
					{
						player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.DARK_RED, "Error! ", TextColors.RED, "Transaction failed!"));
					}
				}
			}
		}
	}
}
