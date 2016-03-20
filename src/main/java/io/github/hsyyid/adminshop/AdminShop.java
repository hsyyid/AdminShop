package io.github.hsyyid.adminshop;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import io.github.hsyyid.adminshop.cmdexecutors.SetShopExecutor;
import io.github.hsyyid.adminshop.config.Config;
import io.github.hsyyid.adminshop.config.ShopConfig;
import io.github.hsyyid.adminshop.listeners.PlayerBreakBlockListener;
import io.github.hsyyid.adminshop.listeners.PlayerInteractBlockListener;
import io.github.hsyyid.adminshop.utils.ConfigManager;
import io.github.hsyyid.adminshop.utils.Shop;
import io.github.hsyyid.adminshop.utils.ShopModifier;
import ninja.leaping.configurate.ConfigurationNode;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Plugin(id = "io.github.hsyyid.adminshop", name = "AdminShop", description = "This plugin adds sign shops for users to buy items.", version = "1.6c")
public class AdminShop
{
	protected AdminShop()
	{
		;
	}

	private static AdminShop adminShop;
	public static ConfigurationNode config;
	public static EconomyService economyService;
	public static Map<UUID, Shop> shops = Maps.newHashMap();
	public static Set<ShopModifier> shopModifiers = Sets.newHashSet();

	public static AdminShop getAdminShop()
	{
		return adminShop;
	}

	@Inject
	private Logger logger;

	public Logger getLogger()
	{
		return logger;
	}

	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;

	public Path getConfigDir()
	{
		return configDir;
	}

	@Listener
	public void init(GameInitializationEvent event)
	{
		adminShop = this;
		getLogger().info("AdminShop loading...");

		// Create Config Directory for AdminShop
		if (!Files.exists(configDir))
		{
			try
			{
				Files.createDirectories(configDir);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		// Create data Directory for AdminShop
		if (!Files.exists(configDir.resolve("data")))
		{
			try
			{
				Files.createDirectories(configDir.resolve("data"));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		// Create config.conf
		Config.getConfig().setup();
		// Create shops.conf
		ShopConfig.getConfig().setup();

		CommandSpec setItemShopCommandSpec = CommandSpec.builder()
			.description(Text.of("Creates AdminShops"))
			.permission("adminshop.command.setshop")
			.arguments(GenericArguments.seq(
				GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("price"))), 
				GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.bool(Text.of("buy shop"))))))
			.executor(new SetShopExecutor())
			.build();

		Sponge.getCommandManager().register(this, setItemShopCommandSpec, "setshop");

		Sponge.getEventManager().registerListeners(this, new PlayerBreakBlockListener());
		Sponge.getEventManager().registerListeners(this, new PlayerInteractBlockListener());

		getLogger().info("-----------------------------");
		getLogger().info("AdminShop was made by HassanS6000!");
		getLogger().info("Please post all errors on the Sponge Thread or on GitHub!");
		getLogger().info("Have fun, and enjoy! :D");
		getLogger().info("-----------------------------");
		getLogger().info("AdminShop loaded!");
	}

	@Listener
	public void postInit(GamePostInitializationEvent event)
	{
		Optional<EconomyService> optionalEconomyService = Sponge.getServiceManager().provide(EconomyService.class);

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
		getLogger().info("Reading Shops...");

		ConfigManager.readShops();

		getLogger().info("Finished reading Shops.");
	}

	@Listener
	public void onServerStopping(GameStoppingServerEvent event)
	{
		ConfigManager.writeShops();
	}
}
