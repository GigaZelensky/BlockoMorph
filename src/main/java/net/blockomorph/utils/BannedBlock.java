package net.blockomorph.utils;

import net.blockomorph.utils.config.Config;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record BannedBlock(String reason, Component text, boolean systemLock) {
	public static final List<BanPredicate> TESTS = new ArrayList<>();
	public static final BannedBlock ALWAYS_ON = new BannedBlock(null, null, true);

	public BannedBlock(String reason, Component text) {
		this(reason, text, false);
	}

	public static final BannedBlock ALREADY_MORPHED = new BannedBlock(
			"You have already been turned into this block.",
			Component.translatable("blockomorph.bannedBlock.same")
	);

	static {
		TESTS.add((state, player, source) -> {
			if (state.isAir()) return ALWAYS_ON;
			return null;
		});

		TESTS.add((state, player, source) -> {
			if (state.getBlock() instanceof LiquidBlock) return new BannedBlock(
					"Morphing in liquids in development!",
					Component.translatable("blockomorph.bannedBlock.liquid"), true
			);
			return null;
		});

		TESTS.add((state, player, source) -> {
			if (player != null && source == Source.NETWORK) {
				if (player.getTnt() != null) {
					return new BannedBlock("No access to morph when TNT is lit!", Component.translatable("blockomorph.bannedBlock.tnt"));
				}
			}
			return null;
		});

		TESTS.add((state, player, source) -> {
			if ((!state.isSolid() || state.getRenderShape() == RenderShape.INVISIBLE) && Config.getInstance().getValue("solidBlocksOnly", Boolean.class)) {
				return new BannedBlock("Not solid blocks not allowed!", Component.translatable("blockomorph.bannedBlock.solid"));
			}
			return null;
		});

		TESTS.add((state, player, source) -> {
			String name = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
			Config cfg = Config.getInstance();
			Config.Mode mode = cfg.getValue("listMode", Config.Mode.class);
			switch (mode) {
				case WHITELIST -> {
					if (!cfg.getValue("allowedBlocks", List.class).contains(name)) {
						return new BannedBlock("Block " + name + " not allowed because it not in whitelist!", Component.translatable("blockomorph.bannedBlock.whitelist"));
					}
				}
				case BLACKLIST -> {
					if (cfg.getValue("bannedBlocks", List.class).contains(name)) {
						return new BannedBlock("Block " + name + " not allowed because it in blacklist!", Component.translatable("blockomorph.bannedBlock.blacklist"));
					}
				}
			}
			return null;
		});
	}




	@Nullable
	public static BannedBlock isBannedBlock(BlockState state, @Nullable PlayerAccessor player, Source source) {
		for (BanPredicate predicate : TESTS) {
			BannedBlock reason = predicate.checkBanned(state, player, source);
			if (reason != null) {
				if (reason == ALWAYS_ON) return null;
				return reason;
			}
		}
		return null;
	}

	@FunctionalInterface
	public interface BanPredicate {
		BannedBlock checkBanned(BlockState state, @Nullable PlayerAccessor player, Source source);
	}

	public enum Source {
		NETWORK,
		COMMAND,
		SYSTEM
	}
}
