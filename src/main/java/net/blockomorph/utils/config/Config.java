package net.blockomorph.utils.config;

import java.io.*;
import java.util.ArrayList;

import com.google.gson.*;

import java.util.List;
import java.nio.file.Files;

import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.MorphUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.Main;
import net.minecraft.server.MinecraftServer;
import net.blockomorph.network.ClientBoundConfigUpdatePacket;
import net.minecraft.world.level.block.state.properties.RailShape;

public class Config {
	private static final Gson WRITER = new GsonBuilder().setPrettyPrinting().create();
	private static final File CONFIG_FILE = MorphUtils.getGameDir().resolve("config").resolve("blockomorph.json").toFile();
	private static Config INSTANCE;
	private static MinecraftServer SERVER;

	public final List<ConfigInstance<?>> OPTIONS = List.of(
			new EnumConfig<>("listMode", Mode.NONE, true, null),
			new BooleanConfig("solidBlocksOnly", false, true, null),
			new BooleanConfig("offUnbreakableBlocks", false, true, null),
			new BlockListConfig("allowedBlocks", new ArrayList<>(), true, null, new BlockListConfig.ListOptionContext(MorphUtils.res("textures/screens/sel_good.png"), MorphUtils.blockPredicate(), MorphUtils.blockPredicate(), true)),
			new BlockListConfig("bannedBlocks", new ArrayList<>(), true, null, new BlockListConfig.ListOptionContext(MorphUtils.res("textures/screens/sel_bad.png"), MorphUtils.blockPredicate(), MorphUtils.blockPredicate(), true)),
			new BooleanConfig("playerDieAfterDestroy", true, true, null),
			new EnumConfig<>("useMode", UseMode.ALL, true, null),
			new EnumConfig<>("placeMode", PlaceMode.OUT, true, null),
			new BooleanConfig("canOperatorModifyConfig", true, false, null),
			new EnumConfig<>("screenAccess", ScreenAccess.ALL, true, null)
	);

	private Config() {}

	public static Config getInstance() {
		if (INSTANCE == null) 
			throw new IllegalAccessError("Config not loaded!");
		return INSTANCE;
	}

	public static void loadExternal(Config cfg) {
		INSTANCE = cfg;
	}

	public static MinecraftServer getServer() {
		return SERVER;
	}
	
	public static void setServer(MinecraftServer sv) {
		SERVER = sv;
	}

	public void writeAndSend() {
		this.write();
		MorphUtils.sendAll(new ClientBoundConfigUpdatePacket(this));
	}

	public static void load() {
		INSTANCE = new Config();
		if (!Files.exists(CONFIG_FILE.toPath())) {
			INSTANCE.write();
			return;
		}
		try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE))) {
			JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
			for (ConfigInstance<?> option : INSTANCE.OPTIONS) {
				JsonElement property = jsonObject.get(option.getName());
				if (property != null) option.readFromStorage(property);
			}
		} catch (Exception e) {
			MorphUtils.LOGGER.error("Cannot read Blockomorph config: ", e);
		}
	}

	protected void write() {
		JsonObject jsonObject = new JsonObject();
		for (ConfigInstance<?> option : OPTIONS) {
			jsonObject.add(option.getName(), option.getDataForStorage());
		}

		try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
			WRITER.toJson(jsonObject, writer);
		} catch (IOException e) {
			MorphUtils.LOGGER.error("Cannot write Blockomorph config, changes lost: ", e);
		}
	}

	public void writeInBuffer(FriendlyByteBuf buf) {
		for (ConfigInstance<?> con : OPTIONS) {
			con.writeToNetwork(buf);
		}
	}

	public static Config readFromBuffer(FriendlyByteBuf buf) {
		Config cfg = new Config();
		for (ConfigInstance<?> con : cfg.OPTIONS) {
			con.readFromNetwork(buf);
		}
		return cfg;
	}

	public void parse(String optionName, String value, boolean fromNetwork) {
		ConfigInstance<?> option = this.getOption(optionName);
		if (option.getName().equals(optionName) && option.canEditedByOperators()) {
			option.parseFromUser(value);
			this.writeAndSend();
			return;
		}
		if (fromNetwork)
			throw new IllegalStateException("Invalid option name: " + optionName);
	}

	@SuppressWarnings("unchecked")
	public <T> T getValue(String optionName, Class<T> valueType) {
		Object value = this.getOption(optionName).getValue();
		if (valueType.isAssignableFrom(value.getClass())) {
			return (T) value;
		}
		throw new IllegalArgumentException("Irregular value type: " + valueType.getTypeName() + " for option: " + optionName);
	}

	private ConfigInstance<?> getOption(String name) {
		for (ConfigInstance<?> option : OPTIONS) {
			if (option.getName().equals(name)) {
				return option;
			}
		}
		throw new IllegalArgumentException("Option not found: " + name);
	}







	public enum Mode {
		NONE,
		BLACKLIST,
		WHITELIST
	}

	public enum UseMode {
		DISABLED,
		VANILLA,
		ALL
	}

	public enum PlaceMode {
		DISABLED,
		IN,
		OUT
	}

	public enum ScreenAccess {
		NONE(false, false),
		MORPH_SCREEN(true, false),
		CONFIG_MORPH_SCREEN(false, true),
		ALL(true, true);

		public final boolean morph;
		public final boolean config;

		ScreenAccess(boolean morph, boolean config) {
			this.morph = morph;
			this.config = config;
		}
	}
}
