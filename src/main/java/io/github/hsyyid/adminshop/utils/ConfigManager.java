package io.github.hsyyid.adminshop.utils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.hsyyid.adminshop.AdminShop;
import io.github.hsyyid.adminshop.config.Config;
import io.github.hsyyid.adminshop.config.Configs;
import io.github.hsyyid.adminshop.config.Configurable;
import io.github.hsyyid.adminshop.config.ShopConfig;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ConfigManager
{
	private static Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(Location.class, new LocationAdapter()).create();
	private static Configurable mainConfig = Config.getConfig();
	private static Configurable shopConfig = ShopConfig.getConfig();

	public static void readShops()
	{
		AdminShop.shops.clear();

		ConfigManager.readLegacyAdminShops();
		ConfigManager.readLegacyBuyAdminShops();

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

	public static void readLegacyAdminShops()
	{
		String json = null;

		try
		{
			json = readFile("AdminShops.json", StandardCharsets.UTF_8);
			Files.delete(Paths.get("AdminShops.json"));
		}
		catch (IOException e)
		{
			return;
		}

		if (json != null)
		{
			AdminShop.getAdminShop().getLogger().info("Reading Legacy AdminShops...");

			List<AdminShopObject> adminShops = new ArrayList<AdminShopObject>(Arrays.asList(gson.fromJson(json, AdminShopObject[].class)));

			for (AdminShopObject adminShop : adminShops)
			{
				Optional<ItemType> itemType = Sponge.getRegistry().getType(ItemType.class, adminShop.getItemName());

				if (itemType.isPresent())
				{
					ItemStack stack = ItemStack.builder().itemType(itemType.get()).quantity(adminShop.getItemAmount()).build();

					if (adminShop.getMeta() == -1)
					{
						DataContainer container = stack.toContainer().set(DataQuery.of("UnsafeDamage"), adminShop.getMeta());
						stack = ItemStack.builder().fromContainer(container).build();
					}

					Shop shop = new Shop(adminShop.getSignLocation(), stack.createSnapshot(), adminShop.getPrice(), false);
					AdminShop.shops.put(UUID.randomUUID(), shop);
				}
			}

			AdminShop.getAdminShop().getLogger().info("Read Legacy AdminShops.");
		}
	}

	public static void readLegacyBuyAdminShops()
	{
		String json = null;

		try
		{
			json = readFile("BuyAdminShops.json", StandardCharsets.UTF_8);
			Files.delete(Paths.get("BuyAdminShops.json"));
		}
		catch (IOException e)
		{
			return;
		}

		if (json != null)
		{
			AdminShop.getAdminShop().getLogger().info("Reading Legacy BuyAdminShops...");

			List<AdminShopObject> buyShops = new ArrayList<AdminShopObject>(Arrays.asList(gson.fromJson(json, AdminShopObject[].class)));

			for (AdminShopObject buyShop : buyShops)
			{
				Optional<ItemType> itemType = Sponge.getRegistry().getType(ItemType.class, buyShop.getItemName());

				if (itemType.isPresent())
				{
					ItemStack stack = ItemStack.builder().itemType(itemType.get()).quantity(buyShop.getItemAmount()).build();

					if (buyShop.getMeta() != -1)
					{
						DataContainer container = stack.toContainer().set(DataQuery.of("UnsafeDamage"), buyShop.getMeta());
						stack = ItemStack.builder().fromContainer(container).build();
					}

					Shop shop = new Shop(buyShop.getSignLocation(), stack.createSnapshot(), buyShop.getPrice(), true);
					AdminShop.shops.put(UUID.randomUUID(), shop);
				}
			}

			AdminShop.getAdminShop().getLogger().info("Read Legacy BuyAdminShops.");
		}
	}

	private static String readFile(String path, Charset encoding) throws IOException
	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
}
