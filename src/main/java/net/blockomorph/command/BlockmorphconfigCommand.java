
package net.blockomorph.command;

import net.blockomorph.utils.config.*;

import net.minecraft.commands.Commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandBuildContext;
import com.mojang.brigadier.CommandDispatcher;

public class BlockmorphconfigCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection environment) {
		Config.load();
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("blockmorphconfig").requires((p) -> {
			return p.hasPermission(2) && Config.getInstance().getValue("canOperatorModifyConfig", Boolean.class);
		});

		for (ConfigInstance<?> option : Config.getInstance().OPTIONS) {
			if (option.canEditedByOperators()) {
				LiteralArgumentBuilder<CommandSourceStack> optionName = Commands.literal(option.getName());
				optionName = optionName.executes(option.buildGetter());
				builder = builder.then(option.buildArgument(optionName, commandBuildContext, environment));
			}
		}

		dispatcher.register(builder);
	}
}
