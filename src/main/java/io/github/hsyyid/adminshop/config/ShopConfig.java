package io.github.hsyyid.adminshop.config;

import io.github.hsyyid.adminshop.AdminShop;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handles the shops.conf file
 */
public class ShopConfig implements Configurable
{
	private static ShopConfig config = new ShopConfig();

	private ShopConfig()
	{
		;
	}

	public static ShopConfig getConfig()
	{
		return config;
	}

	private Path configFile = Paths.get(AdminShop.getAdminShop().getConfigDir().resolve("data") + "/shops.conf");
	private ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(configFile).build();
	private CommentedConfigurationNode configNode;

	@Override
	public void setup()
	{
		if (!Files.exists(configFile))
		{
			try
			{
				Files.createFile(configFile);
				load();
				populate();
				save();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			load();
		}
	}

	@Override
	public void load()
	{
		try
		{
			configNode = configLoader.load();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void save()
	{
		try
		{
			configLoader.save(configNode);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void populate()
	{
		get().getNode("shops").setComment("Contains all shop data.");
	}

	@Override
	public CommentedConfigurationNode get()
	{
		return configNode;
	}
}
