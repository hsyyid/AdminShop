package io.github.hsyyid.adminshop;

import com.erigitic.config.AccountManager;
import com.erigitic.main.TotalEconomy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import io.github.hsyyid.adminshop.cmdexecutors.SetItemShopExecutor;
import io.github.hsyyid.adminshop.utils.AdminShop;
import io.github.hsyyid.adminshop.utils.LocationAdapter;
import io.github.hsyyid.adminshop.utils.ShopItem;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.block.BlockTransaction;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.config.DefaultConfig;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.args.GenericArguments;
import org.spongepowered.api.util.command.spec.CommandSpec;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.TeleportHelper;
import org.spongepowered.api.world.World;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

@Plugin(id = "AdminShop", name = "AdminShop", version = "0.6", dependencies = "required-after:TotalEconomy")
public class Main
{
	public static Game game = null;
	public static ConfigurationNode config = null;
	public static ConfigurationLoader<CommentedConfigurationNode> configurationManager;
	public static TeleportHelper helper;
	public static ArrayList<AdminShop> adminShops = new ArrayList<AdminShop>();
	public static ArrayList<AdminShop> buyAdminShops = new ArrayList<AdminShop>();
	public static ArrayList<ShopItem> items = new ArrayList<ShopItem>();
	private Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(Location.class, new LocationAdapter()).create();

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
	public void onServerStart(GameStartedServerEvent event)
	{
		getLogger().info("AdminShop loading..");

		game = event.getGame();
		helper = game.getTeleportHelper();

		// Config File
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
			.arguments(GenericArguments.onlyOne(GenericArguments.string(Texts.of("item ID"))))
			.executor(new SetItemShopExecutor())
			.build();

		game.getCommandDispatcher().register(this, setItemShopCommandSpec, "setitem");

		String json = null;

		try
		{
			json = readFile("AdminShops.json", StandardCharsets.UTF_8);
		}
		catch (IOException e)
		{
			getLogger().error("Could not read JSON file!");
		}

		if (json != null)
		{
			adminShops = new ArrayList<AdminShop>(Arrays.asList(gson.fromJson(json, AdminShop[].class)));
		}
		else
		{
			getLogger().error("Could not read JSON file!");
		}

		try
		{
			json = readFile("BuyAdminShops.json", StandardCharsets.UTF_8);
		}
		catch (IOException e)
		{
			getLogger().error("Could not read JSON file!");
		}

		if (json != null)
		{
			buyAdminShops = new ArrayList<AdminShop>(Arrays.asList(gson.fromJson(json, AdminShop[].class)));
		}
		else
		{
			getLogger().error("Could not read JSON file!");
		}

		getLogger().info("-----------------------------");
		getLogger().info("AdminShop was made by HassanS6000!");
		getLogger().info("Please post all errors on the Sponge Thread or on GitHub!");
		getLogger().info("Have fun, and enjoy! :D");
		getLogger().info("-----------------------------");
		getLogger().info("AdminShop loaded!");
	}

	static String readFile(String path, Charset encoding) throws IOException
	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	@Listener
	public void onServerStopping(GameStoppingServerEvent event)
	{
		String json = gson.toJson(adminShops);
		String j = gson.toJson(buyAdminShops);
		try
		{
			// Assume default encoding.
			FileWriter fileWriter = new FileWriter("AdminShops.json");

			// Always wrap FileWriter in BufferedWriter.
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			bufferedWriter.write(json);

			bufferedWriter.flush();
			// Always close files.
			bufferedWriter.close();
		}
		catch (IOException ex)
		{
			getLogger().error("Could not save JSON file!");
		}

		try
		{
			// Assume default encoding.
			FileWriter fileWriter = new FileWriter("BuyAdminShops.json");

			// Always wrap FileWriter in BufferedWriter.
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			bufferedWriter.write(j);

			bufferedWriter.flush();
			// Always close files.
			bufferedWriter.close();
		}
		catch (IOException ex)
		{
			getLogger().error("Could not save JSON file!");
		}
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
					AdminShop shop = new AdminShop(itemAmount, price, itemName, signLocation);
					adminShops.add(shop);
					String json = gson.toJson(adminShops);

					try
					{
						// Assume default encoding.
						FileWriter fileWriter = new FileWriter("AdminShops.json");

						// Always wrap FileWriter in BufferedWriter.
						BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

						bufferedWriter.write(json);

						bufferedWriter.flush();
						// Always close files.
						bufferedWriter.close();
					}
					catch (IOException ex)
					{
						getLogger().error("Could not save JSON file!");
					}

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
					AdminShop shop = new AdminShop(itemAmount, price, itemName, signLocation);
					buyAdminShops.add(shop);
					String j = gson.toJson(buyAdminShops);

					try
					{
						// Assume default encoding.
						FileWriter fileWriter = new FileWriter("BuyAdminShops.json");

						// Always wrap FileWriter in BufferedWriter.
						BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

						bufferedWriter.write(j);

						bufferedWriter.flush();
						// Always close files.
						bufferedWriter.close();
					}
					catch (IOException ex)
					{
						getLogger().error("Could not save JSON file!");
					}
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

			for (BlockTransaction transaction : event.getTransactions())
			{
				if (transaction.getOriginal().getState() != null && (transaction.getOriginal().getState().getType() == BlockTypes.WALL_SIGN || transaction.getOriginal().getState().getType() == BlockTypes.STANDING_SIGN))
				{
					AdminShop thisShop = null;
					for (AdminShop shop : adminShops)
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

						String json = gson.toJson(adminShops);
						try
						{
							// Assume default encoding.
							FileWriter fileWriter = new FileWriter("AdminShops.json");

							// Always wrap FileWriter in BufferedWriter.
							BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

							bufferedWriter.write(json);

							bufferedWriter.flush();
							// Always close files.
							bufferedWriter.close();
						}
						catch (IOException ex)
						{
							getLogger().error("Could not save JSON file!");
						}
					}
					else if (thisShop != null)
					{
						player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]: Error!", TextColors.RED, " you do not have permission to destroy AdminShops!"));
						event.setCancelled(true);
					}
					else
					{
						AdminShop thisBuyShop = null;
						for (AdminShop shop : buyAdminShops)
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
							String j = gson.toJson(buyAdminShops);

							try
							{
								// Assume default encoding.
								FileWriter fileWriter = new FileWriter("BuyAdminShops.json");

								// Always wrap FileWriter in BufferedWriter.
								BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

								bufferedWriter.write(j);

								bufferedWriter.flush();
								// Always close files.
								bufferedWriter.close();
							}
							catch (IOException ex)
							{
								getLogger().error("Could not save JSON file!");
							}
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
				AdminShop thisShop = null;
				
				for (AdminShop chestShop : adminShops)
				{
					if (chestShop.getSignLocation()!= null &&
					       chestShop.getSignLocation().getX() == event.getTargetBlock().getLocation().get().getX() && chestShop.getSignLocation().getY() == event.getTargetBlock().getLocation().get().getY() && chestShop.getSignLocation().getZ() == event.getTargetBlock().getLocation().get().getZ())
					{
						thisShop = chestShop;
					}
				}

				if (thisShop != null)
				{
					ShopItem item = null;
					for (ShopItem i : items)
					{
						if (i.getPlayer().getUniqueId() == player.getUniqueId())
						{
							item = i;
							break;
						}
					}

					if (item != null)
					{
						adminShops.remove(thisShop);
						thisShop.setItemName(item.getItemID());
						adminShops.add(thisShop);
						items.remove(item);
						String json = gson.toJson(adminShops);

						try
						{
							// Assume default encoding.
							FileWriter fileWriter = new FileWriter("AdminShops.json");

							// Always wrap FileWriter in BufferedWriter.
							BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

							bufferedWriter.write(json);

							bufferedWriter.flush();
							// Always close files.
							bufferedWriter.close();
						}
						catch (IOException ex)
						{
							getLogger().error("Could not save JSON file!");
						}

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

						if (accountManager.getBalance(player.getUniqueId()).intValue() > amount.intValue())
						{
							accountManager.removeFromBalance(player.getUniqueId(), amount);
							player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GOLD, "You have just bought " + itemAmount + " " + itemName + " for " + price + " dollars."));
							game.getCommandDispatcher().process(game.getServer().getConsole(), "give" + " " + player.getName() + " " + itemName + " " + itemAmount);
						}
						else
						{
							player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.DARK_RED, "Error! ", TextColors.RED, "You don't have enough money to do that!"));
						}
					}
				}
				else
				{
					AdminShop thisBuyShop = null;
					for (AdminShop chestShop : buyAdminShops)
					{
						if (chestShop.getSignLocation().getX() == event.getTargetBlock().getLocation().get().getX() && chestShop.getSignLocation().getY() == event.getTargetBlock().getLocation().get().getY() && chestShop.getSignLocation().getZ() == event.getTargetBlock().getLocation().get().getZ())
						{
							thisBuyShop = chestShop;
						}
					}

					if (thisBuyShop != null)
					{
						ShopItem item = null;
						for (ShopItem i : items)
						{
							if (i.getPlayer().getUniqueId() == player.getUniqueId())
							{
								item = i;
								break;
							}
						}

						if (item != null)
						{
							buyAdminShops.remove(thisBuyShop);
							thisBuyShop.setItemName(item.getItemID());
							buyAdminShops.add(thisBuyShop);
							items.remove(item);
							String j = gson.toJson(buyAdminShops);

							// Assume default encoding.
							try
							{
								FileWriter fileWriter = new FileWriter("BuyAdminShops.json");
								BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
								bufferedWriter.write(j);
								bufferedWriter.flush();
								bufferedWriter.close();
								fileWriter.close();
							}
							catch (IOException ex)
							{
								getLogger().error("Could not save JSON file!");
							}

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
								game.getCommandDispatcher().process(game.getServer().getConsole(), "give" + " " + player.getName() + " " + itemName + " " + quantityInHand);
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

	public static ConfigurationLoader<CommentedConfigurationNode> getConfigManager()
	{
		return configurationManager;
	}
}
