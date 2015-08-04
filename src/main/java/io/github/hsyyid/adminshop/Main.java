package io.github.hsyyid.adminshop;

import io.github.hsyyid.adminshop.utils.AdminShop;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.manipulator.tileentity.SignData;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.block.tileentity.SignChangeEvent;
import org.spongepowered.api.event.entity.player.PlayerBreakBlockEvent;
import org.spongepowered.api.event.entity.player.PlayerInteractBlockEvent;
import org.spongepowered.api.event.state.ServerStartedEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.config.DefaultConfig;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.TeleportHelper;

import com.erigitic.config.AccountManager;
import com.erigitic.main.TotalEconomy;
import com.google.inject.Inject;

@Plugin(id = "AdminShop", name = "AdminShop", version = "0.1", dependencies = "required-after:TotalEconomy")
public class Main
{
	public static Game game = null;
	public static ConfigurationNode config = null;
	public static ConfigurationLoader<CommentedConfigurationNode> configurationManager;
	public static TeleportHelper helper;
	public static ArrayList<AdminShop> adminShops = new ArrayList<AdminShop>();

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

	@Subscribe
	public void onServerStart(ServerStartedEvent event)
	{
		getLogger().info("AdminShop loading...");
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

		getLogger().info("-----------------------------");
		getLogger().info("AdminShop was made by HassanS6000!");
		getLogger().info("Please post all errors on the Sponge Thread or on GitHub!");
		getLogger().info("Have fun, and enjoy! :D");
		getLogger().info("-----------------------------");
		getLogger().info("AdminShop loaded!");
	}

	@Subscribe
	public void onSignChange(SignChangeEvent event)
	{
		Player player = null;
		if(event.getCause().isPresent() && event.getCause().get().getCause() instanceof Player)
		{
			player = (Player) event.getCause().get().getCause();
		}

		Sign sign = event.getTile();
		Location signLocation = sign.getBlock();
		SignData signData = event.getNewData();
		String line0 = Texts.toPlain(signData.getLine(0));
		String line1 = Texts.toPlain(signData.getLine(1));
		String line2 = Texts.toPlain(signData.getLine(2));
		String line3 = Texts.toPlain(signData.getLine(3));

		if (line0.equals("[AdminShop]"))
		{
			if(player != null && player.hasPermission("adminshop.create"))
			{
				int itemAmount = Integer.parseInt(line1);
				double price = Double.parseDouble(line2);
				String itemName = line3;
				AdminShop shop = new AdminShop(itemAmount, price, itemName, signLocation);
				adminShops.add(shop);
				signData.setLine(0, Texts.of(TextColors.DARK_BLUE, "[AdminShop]"));
				player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GOLD, "Successfully created AdminShop!"));
			}
			else if(player != null)
			{
				player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.DARK_RED, "Error! ", TextColors.RED, "You do not have permission to create an AdminShop!"));
			}
		}

		event.setNewData(signData);
	}

	@Subscribe
	public void onPlayerBreakBlock(PlayerBreakBlockEvent event)
	{
		if (event.getBlock().getBlock() != null && (event.getBlock().getBlock().getType() == BlockTypes.WALL_SIGN || event.getBlock().getBlock().getType() == BlockTypes.STANDING_SIGN))
		{
			AdminShop thisShop = null;
			for (AdminShop shop : adminShops)
			{
				if (shop.getSignLocation().getX() == event.getBlock().getX() && shop.getSignLocation().getY() == event.getBlock().getY() && shop.getSignLocation().getZ() == event.getBlock().getZ())
				{
					thisShop = shop;
				}
			}

			if (thisShop != null && event.getEntity().hasPermission("adminshop.remove"))
			{
				event.getEntity().sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]:", TextColors.GREEN, " AdminShop successfully removed!"));
				adminShops.remove(thisShop);
			}
			else if(thisShop != null)
			{
				event.getEntity().sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]: Error!", TextColors.RED, " you do not have permission to destroy AdminShops!"));
				event.setCancelled(true);
			}
		}
	}

	@Subscribe
	public void onPlayerInteractBlock(PlayerInteractBlockEvent event)
	{
		if (event.getBlock().getBlock() != null && (event.getBlock().getBlock().getType() == BlockTypes.WALL_SIGN || event.getBlock().getBlock().getType() == BlockTypes.STANDING_SIGN))
		{
			AdminShop thisShop = null;
			for (AdminShop chestShop : adminShops)
			{
				if (chestShop.getSignLocation().getX() == event.getBlock().getX() && chestShop.getSignLocation().getY() == event.getBlock().getY() && chestShop.getSignLocation().getZ() == event.getBlock().getZ())
				{
					thisShop = chestShop;
				}
			}

			if (thisShop != null)
			{
				int itemAmount = thisShop.getItemAmount();
				double price = thisShop.getPrice();
				String itemName = thisShop.getItemName();

				Player player = event.getEntity();
				TotalEconomy totalEconomy = (TotalEconomy) game.getPluginManager().getPlugin("TotalEconomy").get().getInstance();
				AccountManager accountManager = totalEconomy.getAccountManager();
				BigDecimal amount = new BigDecimal(price);

				if (accountManager.getBalance(player).intValue() > amount.intValue())
				{
					accountManager.removeFromBalance(player, amount);
					player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GOLD, "You have just bought " + itemAmount + " " + itemName + " for " + price + " dollars."));
					game.getCommandDispatcher().process(game.getServer().getConsole(), "give" + " " + player.getName() + " " + itemName + " " + itemAmount);
				}
				else
				{
					player.sendMessage(Texts.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.DARK_RED, "Error! ", TextColors.RED, "You don't have enough money to do that!"));
				}

			}
		}
	}

	public static ConfigurationLoader<CommentedConfigurationNode> getConfigManager()
	{
		return configurationManager;
	}
}
