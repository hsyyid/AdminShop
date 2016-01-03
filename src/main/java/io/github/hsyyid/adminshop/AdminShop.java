package io.github.hsyyid.adminshop;

import com.google.inject.Inject;
import io.github.hsyyid.adminshop.cmdexecutors.SetItemShopExecutor;
import io.github.hsyyid.adminshop.listeners.ChangeSignListener;
import io.github.hsyyid.adminshop.listeners.PlayerBreakBlockListener;
import io.github.hsyyid.adminshop.listeners.PlayerInteractBlockListener;
import io.github.hsyyid.adminshop.utils.AdminShopModifierObject;
import io.github.hsyyid.adminshop.utils.AdminShopObject;
import io.github.hsyyid.adminshop.utils.ConfigManager;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@Plugin(id = "AdminShop", name = "AdminShop", version = "1.2")
public class AdminShop
{
	public static Game game;
	public static ConfigurationNode config;
	public static EconomyService economyService;
	public static ConfigurationLoader<CommentedConfigurationNode> configurationManager;
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
	public void onGameInit(GameInitializationEvent event)
	{
		getLogger().info("AdminShop loading...");

		game = Sponge.getGame();

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
			.description(Text.of("Sets Item for a AdminShop"))
			.permission("adminshop.setitem")
			.arguments(GenericArguments.seq(
				GenericArguments.onlyOne(GenericArguments.string(Text.of("item ID"))), 
				GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.integer(Text.of("meta"))))))
			.executor(new SetItemShopExecutor())
			.build();

		game.getCommandManager().register(this, setItemShopCommandSpec, "setitem");

		game.getEventManager().registerListeners(this, new ChangeSignListener());
		game.getEventManager().registerListeners(this, new PlayerBreakBlockListener());
		game.getEventManager().registerListeners(this, new PlayerInteractBlockListener());
		
		getLogger().info("-----------------------------");
		getLogger().info("AdminShop was made by HassanS6000!");
		getLogger().info("Please post all errors on the Sponge Thread or on GitHub!");
		getLogger().info("Have fun, and enjoy! :D");
		getLogger().info("-----------------------------");
		getLogger().info("AdminShop loaded!");
	}
	
	@Listener
	public void onGamePostInit(GamePostInitializationEvent event)
	{
		Optional<EconomyService> optionalEconomyService = game.getServiceManager().provide(EconomyService.class);

		if (!optionalEconomyService.isPresent())
		{
			getLogger().error("There is no Economy Plugin installed on this Server! This plugin will not work correctly!");
			return;
		}
		else
		{
			economyService = optionalEconomyService.get();
		}
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

	public static ConfigurationLoader<CommentedConfigurationNode> getConfigManager()
	{
		return configurationManager;
	}
}
