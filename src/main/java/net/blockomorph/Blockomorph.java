package net.blockomorph;

import net.blockomorph.network.*;
import net.blockomorph.utils.MorphUtils;

import net.blockomorph.utils.dataSyncher.IntSyncedData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.neoforged.bus.EventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@Mod(Blockomorph.MODID)
public class Blockomorph {
	public static final String MODID = "blockomorph";

	public Blockomorph(IEventBus modEventBus) {
		modEventBus.addListener((RegisterPayloadHandlersEvent event) -> {
			final PayloadRegistrar registrar = event.registrar(MODID);
			registrar.playBidirectional(MainPacket.ID, MainPacket.STREAM_CODEC, MainPacket::apply, MainPacket::apply);
		});
		MorphUtils.registerPacket(ClientBoundConfigUpdatePacket.ID, ClientBoundConfigUpdatePacket::new, true);
		MorphUtils.registerPacket(ClientBoundBlockPosBoundPacket.ID, ClientBoundBlockPosBoundPacket::new, true);
		MorphUtils.registerPacket(ClientBoundMorphUpdatePacket.ID, ClientBoundMorphUpdatePacket::new, true);
		MorphUtils.registerPacket(ClientBoundServerBlockEntityTagPacket.ID, ClientBoundServerBlockEntityTagPacket::new, true);
		MorphUtils.registerPacket(ClientBoundApplyBlockMorphPacket.ID, ClientBoundApplyBlockMorphPacket::new, true);
		MorphUtils.registerPacket(ClientBoundEntityDataSyncPacket.ID, ClientBoundEntityDataSyncPacket::new, true);
		MorphUtils.registerPacket(ServerBoundBlockMorphPacket.ID, ServerBoundBlockMorphPacket::new, false);
		MorphUtils.registerPacket(ServerBoundConfigUpdatePacket.ID, ServerBoundConfigUpdatePacket::new, false);
		MorphUtils.registerPacket(ServerBoundSelfNbtRequestPacket.ID, ServerBoundSelfNbtRequestPacket::new, false);
		MorphUtils.registerPacket(ServerBoundMorphActionPacket.ID, ServerBoundMorphActionPacket::new, false);
	}

	public static class Pig extends Mob {
		private final IntSyncedData data = new IntSyncedData(this, ResourceLocation.fromNamespaceAndPath(MODID, "myData"), -1, this::doAfterUpdate);

		public Pig(EntityType<? extends Mob> p_21368_, Level p_21369_) {
			super(p_21368_, p_21369_);
			data.set(4);//уже готово к работе
		}

		public void doAfterUpdate() {
			if (this.level().isClientSide) {
				//do client
			} else {
				//do server, пример:
				System.out.println(data.get());
			}
		}
	}

	public static class Pig2 extends Mob {
		private final EntityDataAccessor<Integer> data = SynchedEntityData.defineId(Pig2.class, EntityDataSerializers.INT);

		public Pig2(EntityType<? extends Mob> p_21368_, Level p_21369_) {
			super(p_21368_, p_21369_);
		}

		@Override
		protected void defineSynchedData(SynchedEntityData.Builder p_326499_) {
			p_326499_.define(data, -1);
			this.entityData.set(data, 4);//готово после define
		}

		@Override
		public void onSyncedDataUpdated(EntityDataAccessor<?> p_21104_) {//doAfterUpdate
			if (p_21104_.equals(data)) {
				if (this.level().isClientSide) {
					//do client
				} else {
					//do server, пример:
					System.out.println(this.entityData.get(data));
				}
			}
		}
	}

	public static class Pig3 extends Mob {
		private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);
		public static final Supplier<AttachmentType<Integer>> WITH_SYNC_HANDLER = ATTACHMENT_TYPES.register(
				"with_sync_handler", () -> AttachmentType.builder(() -> -1)
						.sync(new ExampleSyncHandler())
						.build()
		);

		public static void register(EventBus bus) { //дополнительный хук с главного класса мода
			bus.register(ATTACHMENT_TYPES);
		}

		public static class ExampleSyncHandler implements AttachmentSyncHandler<java.lang.Integer> {
			@Override
			public void write(RegistryFriendlyByteBuf buf, java.lang.Integer attachment, boolean initialSync) {
				buf.writeInt(attachment);
			}

			@Override
			public java.lang.@Nullable Integer read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, java.lang.@Nullable Integer previousValue) {
				return buf.readInt();
			}
		}

		public Pig3(EntityType<? extends Mob> p_21368_, Level p_21369_) {
			super(p_21368_, p_21369_);
			this.setData(WITH_SYNC_HANDLER, 4); //не привязано к сущности, работает по типу ForgeData, можеть быть привязано к любой сущности
		}

		//doAfterUpdate неизвестно как подключить
	}

}
