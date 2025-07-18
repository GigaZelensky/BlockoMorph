package net.blockomorph;

import net.fabricmc.api.DedicatedServerModInitializer;

import net.blockomorph.core.MainBus;

public class BlockomorphServer implements DedicatedServerModInitializer {
	public static final String MOD_ID = "blockomorph";

	@Override
	public void onInitializeServer() {

		MainBus.registerServer();
	}
}
