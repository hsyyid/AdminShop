package io.github.hsyyid.adminshop.listeners;

import io.github.hsyyid.adminshop.AdminShop;
import io.github.hsyyid.adminshop.utils.AdminShopObject;
import io.github.hsyyid.adminshop.utils.ConfigManager;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class ChangeSignListener
{
	@Listener
	public void onChangeSign(ChangeSignEvent event)
	{
		if (event.getCause().first(Player.class).isPresent())
		{
			Player player = (Player) event.getCause().first(Player.class).get();
			Sign sign = event.getTargetTile();
			Location<World> signLocation = sign.getLocation();
			SignData signData = event.getText();
			String line0 = signData.getValue(Keys.SIGN_LINES).get().get(0).toPlain();
			String line1 = signData.getValue(Keys.SIGN_LINES).get().get(1).toPlain();
			String line2 = signData.getValue(Keys.SIGN_LINES).get().get(2).toPlain();
			String line3 = signData.getValue(Keys.SIGN_LINES).get().get(3).toPlain();

			if (line0.equals("[AdminShop]"))
			{
				if (player != null && player.hasPermission("adminshop.create"))
				{
					int itemAmount = Integer.parseInt(line1);
					double price = Double.parseDouble(line2);
					String itemName = line3;
					AdminShopObject shop = new AdminShopObject(itemAmount, price, itemName, signLocation);
					AdminShop.adminShops.add(shop);
					ConfigManager.writeAdminShops();
					signData = signData.set(signData.getValue(Keys.SIGN_LINES).get().set(0, Text.of(TextColors.DARK_BLUE, "[AdminShop]")));
					player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GOLD, "Successfully created AdminShop!"));
				}
				else if (player != null)
				{
					player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.DARK_RED, "Error! ", TextColors.RED, "You do not have permission to create an AdminShop!"));
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
					AdminShop.buyAdminShops.add(shop);
					ConfigManager.writeBuyAdminShops();
					signData = signData.set(signData.getValue(Keys.SIGN_LINES).get().set(0, Text.of(TextColors.DARK_BLUE, "[AdminShopSell]")));
					player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GOLD, "Successfully created AdminShop!"));
				}
				else if (player != null)
				{
					player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.DARK_RED, "Error! ", TextColors.RED, "You do not have permission to create an AdminShop!"));
				}
			}
		}
	}
}
