package net.blockomorph.screens;

import net.blockomorph.screens.morphConfig.nbtEditor.NbtEditorScreen;
import net.blockomorph.screens.utils.GuiUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;

public class TestScreen extends NbtEditorScreen {
	private static CompoundTag tag = new CompoundTag();
	private final CompoundTag primaryTag;
	public TestScreen(BlockState state, boolean blockEntity) {
		this.primaryTag = get(state, blockEntity);
	}

	public TestScreen(boolean init) {
		if (init) {
			this.primaryTag = get2();
		} else this.primaryTag = null;
	}

	@Override
	protected void onTagEdited(CompoundTag tag2) {
		tag = tag2;
	}

	private static CompoundTag get(BlockState state, boolean BE) {
		if (!BE) {
			CompoundTag main = new CompoundTag();
			main.put("maska", NbtUtils.writeBlockState(state));
			return main;
		}
		return state.getBlock() instanceof EntityBlock ent ? ent.newBlockEntity(BlockPos.ZERO, state).saveCustomOnly(GuiUtils.MC.level.registryAccess()) : new CompoundTag();
	}

	private static CompoundTag get2() {
		if (true) {
			CompoundTag tag2 = new CompoundTag();
			tag2.putString("opa", "ttsetys");
			for (int i = 0; i < 14; i++) {
				tag2.putString(i + "", "value" + i);
			}
			tag2.putString("iejikjiejijeikfjiejefejfoe", "e");
			tag2.putString("jkmolkkhlklhknlkhlnkhlnklhkn", "e2");

			tag.putString("0", "test");
			tag.putInt("1", 2392);
			tag.putLong("2", 3849384938L);
			tag.putDouble("3", 23.4d);
			tag.putFloat("4", 96.7f);
			tag.putShort("5", (short) 9);
			tag.putByte("6", (byte) 1);
			tag.put("internal", tag2);

			ListTag list = new ListTag();
			list.add(IntTag.valueOf(0));
			list.add(StringTag.valueOf("kjjef"));
			list.add(tag2.copy());

			IntArrayTag array = new IntArrayTag(new int[3]);
			array.addTag(0, IntTag.valueOf(378483));
			list.add(array);

			tag.put("listing", list);
			return tag;
		}

		tag.putByte("opa", (byte) 127);
		tag.putByte("opa2", (byte) 0);
		tag.putByte("InWater", (byte) 1);
		tag.putByte("enabled", (byte) 5);
		tag.putByte("HasGlowingInk", (byte) 0);
		tag.putByte("locked", (byte) 1);

		return tag;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float tick) {
		super.render(guiGraphics, mouseX, mouseY, tick);
		this.gui.drawString(Component.literal(tag.toString()), this.width / 2 - (this.font.width(tag.toString()) / 2), this.topPos - 20, -1, false);
	}

	@Override
	protected void init() {
		super.init();
		this.setNewTag(this.primaryTag);
	}
}
