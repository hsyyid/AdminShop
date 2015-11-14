package io.github.hsyyid.adminshop;

import com.erigitic.config.AccountManager;
import com.erigitic.main.TotalEconomy;
import com.google.inject.Inject;
import io.github.hsyyid.adminshop.cmdexecutors.SetItemShopExecutor;
import io.github.hsyyid.adminshop.utils.AdminShopModifierObject;
import io.github.hsyyid.adminshop.utils.AdminShopObject;
import io.github.hsyyid.adminshop.utils.ConfigManager;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.config.DefaultConfig;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.args.GenericArguments;
import org.spongepowered.api.util.command.spec.CommandSpec;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.TeleportHelper;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

@Plugin(id = "AdminShop", name = "AdminShop", version = "0.9", dependencies = "required-after:TotalEconomy")
public class AdminShop
{
	public static Game game = null;
	public static ConfigurationNode config = null;
	public static ConfigurationLoader<CommentedConfigurationNode> configurationManager;
	public static TeleportHelper helper;
	public static ArrayList<AdminShopObject> adminShops = new ArrayList<AdminShopObject>();
	public static ArrayList<AdminShopObject> buyAdminShops = new ArrayList<AdminShopObject>();
	public static ArrayList<AdminShopModifierObject> adminShopModifiers = new ArrayList<AdminShopModifierObject>();

	@Inject
	private Logger logger;

	public Logger getLogger()
	{
		return logger;
	}

	@Inject
	@DefaultConfig(sharedRoot = true)
	private File dConfig;

	@Inject
	@DefaultConfig(sharedRoot = true)
	private ConfigurationLoader<CommentedConfigurationNode> confManager;

	@Listener
	public void onServerInit(GameInitializationEvent event)
	{
		getLogger().info("AdminShop loading..");

		game = event.getGame();
		helper = game.getTeleportHelper();

		try
		{
			if (!dConfig.exists())
			{
				dConfig.createNewFile();
				config = confManager.load();
				confManager.save(config);
			}
			configurationManager = confManager;
			config = confManager.load();

		}
		catch (IOException exception)
		{
			getLogger().error("The default configuration could not be loaded or created!");
		}

		CommandSpec setItemShopCommandSpec = CommandSpec.builder()
			.description(Texts.of("Sets Item for a AdminShop"))
			.permission("adminshop.setitem")
			.arguments(GenericArguments.seq(
				GenericArguments.onlyOne(GenericArguments.string(Texts.of("item ID"))),
				GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.integer(Texts.of("meta"))))))
			.executor(new SetItemShopExecutor())
			.build();

		game.getCommandDispatcher().register(this, setItemShopCommandSpec, "setitem");

		getLogger().info("-----------------------------");
		getLogger().info("AdminShop was made by HassanS6000!");
		getLogger().info("Please post all errors on the Sponge Thread or on GitHub!");
		getLogger().info("Have fun, and enjoy! :D");
		getLogger().info("-----------------------------");
		getLogger().info("AdminShop loaded!");
	}

	@Listener
	public void onServerStart(GameStartedServerEvent event)
	{
		getLogger().info("Reading AdminShops from JSON");

		ConfigManager.readAdminShops();
		ConfigManager.readBuyAdminShops();

		getLogger().info("AdminShops read from JSON.");
	}

	@Listener
	public void onServerStopping(GameStoppingServerEvent event)
	{
		ConfigManager.writeAdminShops();
		ConfigManager.writeBuyAdminShops();
	}

	@Listener
	public void onSignChange(ChangeSignEvent event)
	{
		if (event.getCause().first(Player.class).isPresent())
		{
			Player player = (Player) event.getCause().first(Player.class).get();
			Sign sign = event.getTargetTile();
			Location<World> signLocation = sign.getLocation();
			SignData signData = event.getText();
			String line0 = Texts.toPlain(signData.getValue(Keys.SIGN_LINES).get().get(0));
			String line1 = Texts.toPlain(signData.getValue(Keys.SIGN_LINES).get().get(1));
			String line2 = Texts.toPlain(signData.getValue(Keys.SIGN_LINES).get().get(2));
			String line3 = Texts.toPlain(signData.getValue(Keys.SIGN_LINES).get().get(3));

			if (line0.equals("[AdminShop]"))
			{
				if (player != null && player.hasPermission("adminshop.create"))
				{
					int itemAmount = Integer.parseInt(line1);
					double price = Double.parseDouble(line2);
					String itemName = line3;
					AdminShopObject shop = new AdminShopObject(itemAmount, price, itemName, signLocation);
					adminShops.add(shop);
					ConfigManager.writeAdminShops();
					signData = signData.set(signData.getValue(Keys.SIGN_LINES).get().set(0, Texts.of(TextColors.DARK_BLUE, "[AdminShop]")));
					player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GOLD, "Successfully created AdminShop!"));
				}
				else if (player != null)
				{
					player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.DARK_RED, "Error! ", TextColors.RED, "You do not have permission to create an AdminShop!"));
				}
			}
			else if (line0.equals("[AdminShopSell]"))
			{
				if (player != null && player.hasPermission("adminshop.create"))
				{
					int itemAmount = Integer.parseInt(line1);
					double price = Double.parseDouble(line2);
					String itemName = line3;
					AdminShopObject shop = new AdminShopObject(itemAmount, price, itemName, signLocation);
					buyAdminShops.add(shop);
					ConfigManager.writeBuyAdminShops();
					signData = signData.set(signData.getValue(Keys.SIGN_LINES).get().set(0, Texts.of(TextColors.DARK_BLUE, "[AdminShopSell]")));
					player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GOLD, "Successfully created AdminShop!"));
				}
				else if (player != null)
				{
					player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.DARK_RED, "Error! ", TextColors.RED, "You do not have permission to create an AdminShop!"));
				}
			}
		}
	}

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
					for (AdminShopObject shop : adminShops)
					{
						if (shop.getSignLocation().getX() == transaction.getOriginal().getLocation().get().getX() && shop.getSignLocation().getY() == transaction.getOriginal().getLocation().get().getY() && shop.getSignLocation().getZ() == transaction.getOriginal().getLocation().get().getZ())
						{
							thisShop = shop;
						}
					}

					if (thisShop != null && player.hasPermission("adminshop.remove"))
					{
						player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]:", TextColors.GREEN, " AdminShop successfully removed!"));
						adminShops.remove(thisShop);
						ConfigManager.writeAdminShops();
					}
					else if (thisShop != null)
					{
						player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]: Error!", TextColors.RED, " you do not have permission to destroy AdminShops!"));
						event.setCancelled(true);
					}
					else
					{
						AdminShopObject thisBuyShop = null;
						for (AdminShopObject shop : buyAdminShops)
						{
							if (shop.getSignLocation().getX() == transaction.getOriginal().getLocation().get().getX() && shop.getSignLocation().getY() == transaction.getOriginal().getLocation().get().getY() && shop.getSignLocation().getZ() == transaction.getOriginal().getLocation().get().getZ())
							{
								thisBuyShop = shop;
							}
						}

						if (thisBuyShop != null && player.hasPermission("adminshop.remove"))
						{
							player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]:", TextColors.GREEN, " AdminShop successfully removed!"));
							buyAdminShops.remove(thisBuyShop);
							ConfigManager.writeBuyAdminShops();
						}
						else if (thisBuyShop != null)
						{
							player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]: Error!", TextColors.RED, " you do not have permission to destroy AdminShops!"));
							event.setCancelled(true);
						}
					}
				}
			}
		}
	}

	@Listener
	public void onPlayerInteractBlock(InteractBlockEvent event)
	{
		if (event.getCause().first(Player.class).isPresent())
		{
			Player player = (Player) event.getCause().first(Player.class).get();

			if (event.getTargetBlock().getState().getType() != null && (event.getTargetBlock().getState().getType() == BlockTypes.WALL_SIGN || event.getTargetBlock().getState().getType() == BlockTypes.STANDING_SIGN))
			{
				AdminShopObject thisShop = null;

				for (AdminShopObject adminShop : adminShops)
				{
					if (adminShop.getSignLocation() != null &&
						adminShop.getSignLocation().getX() == event.getTargetBlock().getLocation().get().getX() && adminShop.getSignLocation().getY() == event.getTargetBlock().getLocation().get().getY() && adminShop.getSignLocation().getZ() == event.getTargetBlock().getLocation().get().getZ())
					{
						thisShop = adminShop;
					}
				}

				if (thisShop != null)
				{
					AdminShopModifierObject shopModifier = null;
					for (AdminShopModifierObject i : adminShopModifiers)
					{
						if (i.getPlayer().getUniqueId() == player.getUniqueId())
						{
							shopModifier = i;
							break;
						}
					}

					if (shopModifier != null)
					{
						adminShops.remove(thisShop);
						thisShop.setItemName(shopModifier.getItemID());

						if (shopModifier.getMeta() != null)
							thisShop.setMeta(shopModifier.getMeta());

						adminShops.add(thisShop);
						adminShopModifiers.remove(shopModifier);
						ConfigManager.writeAdminShops();
						player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GREEN, "Successfully set new item ID."));
					}
					else
					{
						int itemAmount = thisShop.getItemAmount();
						double price = thisShop.getPrice();
						String itemName = thisShop.getItemName();

						TotalEconomy totalEconomy = (TotalEconomy) game.getPluginManager().getPlugin("TotalEconomy").get().getInstance();
						AccountManager accountManager = totalEconomy.getAccountManager();
						BigDecimal amount = new BigDecimal(price);

						if (accountManager.getBalance(player.getUniqueId()).compareTo(amount) >= 0)
						{
							accountManager.removeFromBalance(player.getUniqueId(), amount);
							player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GOLD, "You have just bought " + itemAmount + " " + itemName + " for " + price + " dollars."));

							if (thisShop.getMeta() != -1)
								game.getCommandDispatcher().process(game.getServer().getConsole(), "minecraft:give" + " " + player.getName() + " " + itemName + " " + itemAmount + " " + thisShop.getMeta());
							else
								game.getCommandDispatcher().process(game.getServer().getConsole(), "minecraft:give" + " " + player.getName() + " " + itemName + " " + itemAmount);
						}
						else
						{
							player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.DARK_RED, "Error! ", TextColors.RED, "You don't have enough money to do that!"));
						}
					}
				}
				else
				{
					AdminShopObject thisBuyShop = null;

					for (AdminShopObject buyAdminShop : buyAdminShops)
					{
						if (buyAdminShop.getSignLocation() != null &&
							buyAdminShop.getSignLocation().getX() == event.getTargetBlock().getLocation().get().getX() && buyAdminShop.getSignLocation().getY() == event.getTargetBlock().getLocation().get().getY() && buyAdminShop.getSignLocation().getZ() == event.getTargetBlock().getLocation().get().getZ())
						{
							thisBuyShop = buyAdminShop;
						}
					}

					if (thisBuyShop != null)
					{
						AdminShopModifierObject shopModifier = null;
						for (AdminShopModifierObject i : adminShopModifiers)
						{
							if (i.getPlayer().getUniqueId() == player.getUniqueId())
							{
								shopModifier = i;
								break;
							}
						}

						if (shopModifier != null)
						{
							buyAdminShops.remove(thisBuyShop);
							thisBuyShop.setItemName(shopModifier.getItemID());
							if (shopModifier.getMeta() != null)
								thisBuyShop.setMeta(shopModifier.getMeta());
							buyAdminShops.add(thisBuyShop);
							adminShopModifiers.remove(shopModifier);
							ConfigManager.writeBuyAdminShops();
							player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GREEN, "Successfully set new item ID."));
						}
						else
						{
							int itemAmount = thisBuyShop.getItemAmount();
							double price = thisBuyShop.getPrice();
							String itemName = thisBuyShop.getItemName();

							TotalEconomy totalEconomy = (TotalEconomy) game.getPluginManager().getPlugin("TotalEconomy").get().getInstance();
							AccountManager accountManager = totalEconomy.getAccountManager();
							BigDecimal amount = new BigDecimal(price);
							int quantityInHand = 0;
							
							if (thisBuyShop.getMeta() != -1)
							{
								int meta = thisBuyShop.getMeta();
								
								if (player.getItemInHand().isPresent() && player.getItemInHand().get().getItem().getName().equals(itemName) && player.getItemInHand().get().getQuantity() == itemAmount && player.getItemInHand().get().toContainer().get(new DataQuery("UnsafeDamage")).isPresent() && (Integer) player.getItemInHand().get().toContainer().get(new DataQuery("UnsafeDamage")).get() == meta)
								{
									player.setItemInHand(null);
									accountManager.addToBalance(player.getUniqueId(), amount, true);
									player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GOLD, "You have just sold " + itemAmount + " " + itemName + " for " + price + " dollars."));
								}
								else if (player.getItemInHand().isPresent() && player.getItemInHand().get().getItem().getName().equals(itemName) && player.getItemInHand().get().getQuantity() > itemAmount && player.getItemInHand().get().toContainer().get(new DataQuery("UnsafeDamage")).isPresent() && (Integer) player.getItemInHand().get().toContainer().get(new DataQuery("UnsafeDamage")).get() == meta)
								{
									quantityInHand = player.getItemInHand().get().getQuantity() - itemAmount;
									player.setItemInHand(game.getRegistry().createBuilder(ItemStack.Builder.class)
										.fromItemStack(player.getItemInHand().get())
										.quantity(quantityInHand)
										.build());
									accountManager.addToBalance(player.getUniqueId(), amount, true);
									player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GOLD, "You have just sold " + itemAmount + " " + itemName + " for " + price + " dollars."));
								}
								else
								{
									player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.DARK_RED, "Error! ", TextColors.RED, "You're not holding this item or the right quantity of this item!"));
								}
							}
							else
							{
								if (player.getItemInHand().isPresent() && player.getItemInHand().get().getItem().getName().equals(itemName) && player.getItemInHand().get().getQuantity() == itemAmount)
								{
									player.setItemInHand(null);
									accountManager.addToBalance(player.getUniqueId(), amount, true);
									player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GOLD, "You have just sold " + itemAmount + " " + itemName + " for " + price + " dollars."));
								}
								else if (player.getItemInHand().isPresent() && player.getItemInHand().get().getItem().getName().equals(itemName) && player.getItemInHand().get().getQuantity() > itemAmount)
								{
									quantityInHand = player.getItemInHand().get().getQuantity() - itemAmount;
									player.setItemInHand(null);
									game.getCommandDispatcher().process(game.getServer().getConsole(), "minecraft:give" + " " + player.getName() + " " + itemName + " " + quantityInHand);
									accountManager.addToBalance(player.getUniqueId(), amount, true);
									player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GOLD, "You have just sold " + itemAmount + " " + itemName + " for " + price + " dollars."));
								}
								else
								{
									player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.DARK_RED, "Error! ", TextColors.RED, "You're not holding this item or the right quantity of this item!"));
								}
							}
						}
					}
				}
			}
		}
	}

	public static ConfigurationLoader<CommentedConfigurationNode> getConfigManager()
	{
		return configurationManager;
	}
}
