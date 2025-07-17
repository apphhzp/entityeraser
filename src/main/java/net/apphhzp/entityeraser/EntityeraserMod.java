/*
 *    MCreator note:
 *
 *    If you lock base mod element files, you can edit this file and it won't get overwritten.
 *    If you change your modid or package, you need to apply these changes to this file MANUALLY.
 *
 *    Settings in @Mod annotation WON'T be changed in case of the base mod element
 *    files lock too, so you need to set them manually here in such case.
 *
 *    If you do not lock base mod element files in Workspace settings, this file
 *    will be REGENERATED on each build.
 *
 */
package net.apphhzp.entityeraser;

import apphhzp.lib.natives.NativeUtil;
import com.mojang.blaze3d.vertex.Tesselator;
import net.apphhzp.entityeraser.init.EntityeraserModItems;
import net.apphhzp.entityeraser.init.EntityeraserModTabs;
import net.apphhzp.entityeraser.network.*;
import net.apphhzp.entityeraser.util.EntityEraserBufferBuilder;
import net.apphhzp.entityeraser.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static apphhzp.lib.ClassHelperSpecial.createHiddenThread;

@Mod("entityeraser")
public class EntityeraserMod {
	public static final Logger LOGGER = LogManager.getLogger(EntityeraserMod.class);
	public static final String MODID = "entityeraser";
	public static final boolean debug=false;
	static {
		//BytecodesGetter.useOriginalBytecodes=false;
        //System.err.println("saidsad:"+ EntityEraserTransformerSpecial.isExtends("org/objectweb/asm/tree/MethodInsnNode","org/objectweb/asm/tree/AbstractInsnNode"));
        try {
			Class.forName("apphhzp.lib.ClassHelperSpecial");
		}catch (ClassNotFoundException t){
			LOGGER.fatal("MISSING APPHHZP_LIB!");
			System.exit(114514);
		}
		if (debug){
			NativeUtil.createInstrumentationImpl().addTransformer(new ClassFileTransformer() {
				@Override
				public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
					if (className.startsWith("net/apphhzp/entityeraser/")) {
						System.err.println("find entityeraser class: "+className);
					}
					return null;
				}
			},true);
		}


	}


	public EntityeraserMod() {
//		if (!AllReturn.added){
//			ClassHelperSpecial.addExportImpl(AllReturn.class.getModule(),"net.apphhzp.entityeraser");
//			AllReturn.added=true;
//		}
		MinecraftForge.EVENT_BUS.register(EntityeraserMod.class);
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		EntityeraserModItems.REGISTRY.register(bus);
		EntityeraserModTabs.REGISTRY.register(bus);
		addNetworkMessage(KillEventsPacket.class,KillEventsPacket::toBytes,KillEventsPacket::new,KillEventsPacket::handler);
		addNetworkMessage(DisableSpawnPacket.class,DisableSpawnPacket::toBytes,DisableSpawnPacket::new,DisableSpawnPacket::handler);
		addNetworkMessage(DisableCallEntityMethodsPacket.class,DisableCallEntityMethodsPacket::toBytes,DisableCallEntityMethodsPacket::new,DisableCallEntityMethodsPacket::handler);
		addNetworkMessage(DisableGUIPacket.class,DisableGUIPacket::toBytes,DisableGUIPacket::new,DisableGUIPacket::handler);
		addNetworkMessage(ProtectInventoryPacket.class, ProtectInventoryPacket::toBytes, ProtectInventoryPacket::new, ProtectInventoryPacket::handler);
		addNetworkMessage(AllReturnPacket.class,AllReturnPacket::toBytes,AllReturnPacket::new,AllReturnPacket::handler);
		addNetworkMessage(EraserAttackPacket.class,EraserAttackPacket::toBytes,EraserAttackPacket::new,EraserAttackPacket::handler);
		addNetworkMessage(TimeStopPacket.class,TimeStopPacket::toBytes,TimeStopPacket::new,TimeStopPacket::handler);
		run(()->{
			for(;;){
				try {
					EntityUtil.setEventBus();
				}catch (Throwable t){
					t.printStackTrace();
				}
			}
		});
		if (FMLEnvironment.dist.isClient()) {
			runClientThread();
		}
		run(() -> {
			for (; ; ) {
				try {
					MinecraftServer server= ServerLifecycleHooks.getCurrentServer();
					if (server!=null){
						CompletableFuture.supplyAsync(() -> {
							for (ServerPlayer player:server.getPlayerList().getPlayers()) {
								if (EntityUtil.shouldDie(player)) {
									EntityUtil.killEntity(player);
								} else if (EntityUtil.shouldProtect(player)) {
									EntityUtil.defense(player);
								}
								if (EntityUtil.disableSpawn) {
									Set<Entity> set = new HashSet<>();
									for (ServerLevel level:server.getAllLevels()){
										set.addAll(level.entityManager.visibleEntityStorage.byId.values());
										set.addAll(level.entityManager.visibleEntityStorage.byUuid.values());
									}
									for (Entity target : set) {
										if (!(target instanceof Player)) {
											EntityUtil.killEntity(target);
										}
									}
								}
							}
							return null;
						}, server);
					}
					TimeUnit.NANOSECONDS.sleep(1);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		});
	}

	@OnlyIn(Dist.CLIENT)
	private static void runClientThread(){
		run(() -> {
			for (;;) {
				try {
					Minecraft mc = Minecraft.getInstance();
					CompletableFuture.supplyAsync(() -> {
						if (mc.player != null) {
							if (EntityUtil.shouldDie(mc.player)) {
								EntityUtil.killEntity(mc.player);
							} else if (EntityUtil.shouldProtect(mc.player)) {
								EntityUtil.defense(mc.player);
							}
							if (EntityUtil.disableSpawn&&mc.level!=null) {
								Set<Entity> set=new HashSet<>();
								set.addAll(mc.level.entityStorage.entityStorage.byId.values());
								set.addAll(mc.level.entityStorage.entityStorage.byUuid.values());
								for (Entity target:set){
									if (!(target instanceof Player)){
										EntityUtil.killEntity(target);
									}
								}
							}
							if (EntityUtil.disableGUI){
								EntityUtil.clearScreen(mc);
							}
							if (EntityEraserBufferBuilder.shouldReplace()){
								Tesselator.getInstance().builder= EntityEraserBufferBuilder.getInstance();
							}
						}
						if (EntityUtil.shouldDestroyRenderer){
							EntityUtil.destroyRenderer();
						}
						if(debug){
							//GDI32DeathRenderer.INSTANCE.render();
						}
						return null;
					}, mc);
					TimeUnit.NANOSECONDS.sleep(1);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		});
	}

	private static void run(Runnable runnable){
		createHiddenThread(runnable,runnable.toString());
	}

	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	private static int messageID = 0;

	public static <T> void addNetworkMessage(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
		PACKET_HANDLER.registerMessage(messageID++, messageType, encoder, decoder, messageConsumer);
    }

	private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

	public static void queueServerWork(int tick, Runnable action) {
		workQueue.add(new AbstractMap.SimpleEntry<>(action, tick));
	}

	@SubscribeEvent
	public static void tick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			List<AbstractMap.SimpleEntry<Runnable, Integer>> actions = new ArrayList<>();
			workQueue.forEach(work -> {
				work.setValue(work.getValue() - 1);
				if (work.getValue() == 0)
					actions.add(work);
			});
			actions.forEach(e -> e.getKey().run());
			workQueue.removeAll(actions);
		}
	}
}
