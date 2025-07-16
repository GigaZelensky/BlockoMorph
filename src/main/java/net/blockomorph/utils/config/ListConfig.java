package net.blockomorph.utils.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonArray;

import java.util.List;

import com.mojang.brigadier.Command;
import net.blockomorph.screens.config.renderers.StringListConfigRenderer;
import net.minecraft.network.FriendlyByteBuf;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class ListConfig extends ConfigInstance<List<String>> {
	private static StringListConfigRenderer RENDERER;

	public ListConfig(String name, List<String> initialValue, boolean canOperatorModify, @Nullable Component tip) {
		super(name, initialValue, canOperatorModify, tip);
	}

	@Override
	public void readFromStorage(JsonElement option) {
		for (JsonElement el : option.getAsJsonArray()) {
			this.value.add(el.getAsString());
		}
	}

	@Override
	public JsonElement getDataForStorage() {
		JsonArray jsonArray = new JsonArray();
		for (String item : this.value) {
			jsonArray.add(item);
		}
		return jsonArray;
	}

	@Override
	public void readFromNetwork(FriendlyByteBuf buf) {
		this.value.clear();
		int size = buf.readInt();
		for (int i = 0; i < size; i++) {
			String element = buf.readUtf();
			this.value.add(element);
		}
	}

	@Override
	public void writeToNetwork(FriendlyByteBuf buf) {
		buf.writeInt(this.value.size());
		for (String element : this.value) {
			buf.writeUtf(element);
		}
	}

	@Override
	public void parseFromUser(String value) {
		String[] parts = value.split(" ", 2);
		if (parts.length != 2) {
			throw new IllegalArgumentException("Invalid input format: " + value);
		}

		String element = parts[1];
		String action = parts[0];

		switch (action) {
			case "+":
				if (!this.value.contains(element))
					this.value.add(element);
				break;
			case "-":
				this.value.remove(element);
				break;
			case "r":
				this.value.clear();
				break;
			default:
				throw new IllegalArgumentException("Invalid action: " + action);
		}
	}

	public LiteralArgumentBuilder<CommandSourceStack> buildArgument(LiteralArgumentBuilder<CommandSourceStack> optionNameArgument, CommandBuildContext context, Commands.CommandSelection environment) {
		return optionNameArgument
				.then(this.end(Commands.literal("add"), false, context))
				.then(this.end(Commands.literal("remove"), true, context))
				.then(Commands.literal("clear").executes(args -> {
					this.value.clear();
					Config.getInstance().writeAndSend();
					args.getSource().sendSuccess(() -> Component.translatable("blockomorph.commands.option_change.list.clear", this.name), true);
					return 1;
				})
		);
	}

	private LiteralArgumentBuilder<CommandSourceStack> end(LiteralArgumentBuilder<CommandSourceStack> actionString, boolean remove, CommandBuildContext ctx) {
		return actionString.then(Commands.argument("value", ResourceArgument.resource(ctx, Registries.BLOCK)).executes(args -> {
			String name = ResourceArgument.getResource(args, "value", Registries.BLOCK).key().location().toString();
			Component end;
			if (remove) {
				end = Component.translatable("blockomorph.commands.option_change.list.remove", name, this.name);
				this.value.remove(name);
			} else {
				if (!this.value.contains(name))
					this.value.add(name);
				end = Component.translatable("blockomorph.commands.option_change.list.add", name, this.name);
			}
			Config.getInstance().writeAndSend();
			args.getSource().sendSuccess(() -> end, true);
			return 1;
		}));
	}

	@Override
	public Command<CommandSourceStack> buildGetter() {
		return args -> {
			args.getSource().sendSuccess(() -> {
				return Component.translatable("blockomorph.commands.option_get.list", this.name, this.value.size(), this.value.toString());
			}, true);
			return 1;
		};
	}

	@Override
	public StringListConfigRenderer getRenderer() {
		if (RENDERER == null) {
			RENDERER = new StringListConfigRenderer();
		}
		return RENDERER;
	}
}
