package net.blockomorph;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import net.blockomorph.core.MainBus;

public class BlockomorphServer implements DedicatedServerModInitializer {
	public static final String MOD_ID = "blockomorph";

	@Override
	public void onInitializeServer() {

		MainBus.registerServer();
	}
}
