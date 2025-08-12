package net.blockomorph;

import net.blockomorph.core.MainBus;
import net.fabricmc.api.DedicatedServerModInitializer;

public class BlockomorphServer implements DedicatedServerModInitializer {
	public static final String MOD_ID = "blockomorph";

	@Override
	public void onInitializeServer() {
		MainBus.registerServer();
	}
}
