package io.github.hsyyid.adminshop.utils;

import com.google.common.reflect.TypeToken;
import io.github.hsyyid.adminshop.AdminShop;
import io.github.hsyyid.adminshop.config.Config;
import io.github.hsyyid.adminshop.config.Configs;
import io.github.hsyyid.adminshop.config.Configurable;
import io.github.hsyyid.adminshop.config.ShopConfig;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.util.UUID;

public class ConfigManager
{
	private static Configurable mainConfig = Config.getConfig();
	private static Configurable shopConfig = ShopConfig.getConfig();

	public static void readShops()
	{
		AdminShop.shops.clear();

		CommentedConfigurationNode node = shopConfig.get().getNode("shops");

		node.getChildrenMap().forEach((k, v) -> {
			try
			{
				AdminShop.shops.put(UUID.fromString(String.valueOf(k)), v.getValue(TypeToken.of(Shop.class), new Shop()));
			}
			catch (ObjectMappingException e)
			{
				AdminShop.getAdminShop().getLogger().error(e.getMessage());
			}
		});
	}

	public static void writeShops()
	{
		Configs.removeChildren(shopConfig, new Object[] { "shops" });

		AdminShop.shops.forEach((u, s) -> {
			try
			{
				shopConfig.get().getNode("shops", u.toString()).setValue(TypeToken.of(Shop.class), s);
				Configs.saveConfig(shopConfig);
			}
			catch (ObjectMappingException e)
			{
				AdminShop.getAdminShop().getLogger().error(e.getMessage());
			}
		});
	}
}
