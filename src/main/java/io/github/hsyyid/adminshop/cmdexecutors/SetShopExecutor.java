package io.github.hsyyid.adminshop.cmdexecutors;

import io.github.hsyyid.adminshop.AdminShop;
import io.github.hsyyid.adminshop.utils.ShopModifier;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class SetShopExecutor implements CommandExecutor
{
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		double price = ctx.<Double> getOne("price").get();
		Optional<Boolean> buyShop = ctx.<Boolean> getOne("buy shop");

		if (src instanceof Player)
		{
			Player player = (Player) src;
			Optional<ShopModifier> shopModifier = AdminShop.shopModifiers.stream().filter(s -> s.getUuid().equals(player.getUniqueId())).findAny();

			if (shopModifier.isPresent())
			{
				AdminShop.shopModifiers.remove(shopModifier.get());
			}

			if (player.getItemInHand(HandTypes.MAIN_HAND).isPresent())
			{
				AdminShop.shopModifiers.add(new ShopModifier(player.getUniqueId(), player.getItemInHand(HandTypes.MAIN_HAND).get().createSnapshot(), price, buyShop.isPresent() ? buyShop.get() : false));
				player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.GREEN, "Right click a sign!"));
			}
			else
			{
				player.sendMessage(Text.of(TextColors.DARK_RED, "[AdminShop]: ", TextColors.RED, "You must be holding the item you want the shop to use."));
			}
		}
		else
		{
			src.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "Must be an in-game player to use /setshop!"));
		}

		return CommandResult.success();
	}
}
