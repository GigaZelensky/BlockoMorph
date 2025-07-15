package net.blockomorph.utils.config;

import com.mojang.brigadier.Command;
import net.blockomorph.screens.config.ConfigRenderer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.FriendlyByteBuf;
import com.google.gson.JsonElement;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.chat.Component;

public abstract class ConfigInstance<T> {
	protected final String name;
	protected T value;
	private final Component tip;
	private final boolean canOperatorModify;

	protected ConfigInstance(String name, T initialValue, boolean canOperatorModify, @Nullable Component tip) {
		this.name = name;
		this.value = initialValue;
		this.tip = tip;
		this.canOperatorModify = canOperatorModify;
	}

	public String getName() {
		return this.name;
	}

	@Nullable
	public Component getTooltip() {
		return this.tip;
	}

	public abstract void readFromStorage(JsonElement option);
	public abstract JsonElement getDataForStorage();
	public abstract void parseFromUser(String value);
	public abstract void readFromNetwork(FriendlyByteBuf buf);
	public abstract void writeToNetwork(FriendlyByteBuf buf);
	public boolean canEditedByOperators() {
		return this.canOperatorModify;
	}
	public abstract LiteralArgumentBuilder<CommandSourceStack> buildArgument(LiteralArgumentBuilder<CommandSourceStack> optionNameArgument, CommandBuildContext context, Commands.CommandSelection environment);
	public Command<CommandSourceStack> buildGetter() {
		return args -> {
			args.getSource().sendSuccess(() -> {
				return Component.translatable("blockomorph.commands.option_get.default", this.name, this.value.toString());
			}, true);
			return 1;
		};
	}
	public abstract <CFG extends ConfigInstance<T>> ConfigRenderer<CFG> getRenderer();
	public abstract <CFG extends ConfigInstance<T>> Class<CFG> getType();

	public T setValue(T value) {
		this.value = value;
		return this.value;
	}
	public T getValue() {
		return this.value;
	}
}
