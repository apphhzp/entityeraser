package net.apphhzp.entityeraser;

import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import net.apphhzp.entityeraser.init.EntityeraserModItems;
import net.apphhzp.entityeraser.shitmountain.MinecraftRenderers;
import net.apphhzp.entityeraser.util.EntityEraserEventBus;
import net.apphhzp.entityeraser.util.EntityUtil;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.entity.*;
import net.minecraft.world.level.lighting.LightEventListener;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.extensions.IForgeEntity;
import net.minecraftforge.common.extensions.IForgeItemStack;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.IEventBusInvokeDispatcher;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static net.minecraft.world.entity.Entity.DATA_SHARED_FLAGS_ID;

@SuppressWarnings("unused")
public final class MethodUtil {
    static {
//        if(ClassHelperSpecial.isHotspotJVM){
//            ClassHelperSpecial.defineClassBypassAgent("net.apphhzp.entityeraser.util.EntityUtil$DisableRemoveSet",MethodUtil.class,true,null);
//            ClassHelperSpecial.defineClassBypassAgent("net.apphhzp.entityeraser.util.EntityUtil",MethodUtil.class,true,null);
//            ClassHelperSpecial.defineClassBypassAgent("net.apphhzp.entityeraser.event.Events",MethodUtil.class,true,null);
//            if (FMLEnvironment.dist.isClient()){
//                ClassHelperSpecial.defineClassBypassAgent("net.apphhzp.entityeraser.shitmountain.PoseStackHelper",MethodUtil.class,true,null);
//                ClassHelperSpecial.defineClassBypassAgent("net.apphhzp.entityeraser.shitmountain.MinecraftRenderers",MethodUtil.class,true,null);
//                ClassHelperSpecial.defineClassBypassAgent("net.apphhzp.entityeraser.util.DeadBufferBuilder",MethodUtil.class,true,null);
//                ClassHelperSpecial.defineClassBypassAgent("net.apphhzp.entityeraser.shitmountain.EntityEraserRenderers",MethodUtil.class,true,null);
//            }
//        }else {
//            try {
//                defineClass("net.apphhzp.entityeraser.util.EntityUtil$DisableRemoveSet");
//                defineClass("net.apphhzp.entityeraser.util.EntityUtil");
//                defineClass("net.apphhzp.entityeraser.event.Events");
//                defineClass("net.apphhzp.entityeraser.util.EntityEraserEventBus");
//                if (FMLEnvironment.dist.isClient()) {
//                    defineClass("net.apphhzp.entityeraser.shitmountain.PoseStackHelper");
//                    defineClass("net.apphhzp.entityeraser.shitmountain.MinecraftRenderers");
//                    defineClass("net.apphhzp.entityeraser.util.DeadBufferBuilder");
//                    defineClass("net.apphhzp.entityeraser.shitmountain.EntityEraserRenderers");
//                }
//            }catch (Throwable t){
//                throw new RuntimeException(t);
//            }
//        }
    }

    public static float getHealth(LivingEntity entity) {
        if (EntityUtil.shouldDie(entity)) {
            return 0;
        }
        if (EntityUtil.shouldProtect(entity)) {
            return 20F;
        }
        return entity.getHealth();
    }

    public static float getMaxHealth(LivingEntity entity) {
        if (EntityUtil.shouldDie(entity)) {
            return 0;
        }
        if (EntityUtil.shouldProtect(entity)) {
            return 20F;
        }
        return entity.getMaxHealth();
    }

    public static boolean isEmptyStack(ItemStack stack) {
        if (EntityUtil.shouldDie(stack.entityRepresentation)) {
            if (stack.delegate != null) {
                return !(stack.delegate.get() == EntityeraserModItems.KILL_SELF.get());
            }
        }
        return stack.isEmpty();
    }

    public static boolean isAlive(Entity entity) {
        if (EntityUtil.shouldDie(entity)) {
            return false;
        }
        if (EntityUtil.shouldProtect(entity)) {
            return true;
        }
        return entity.isAlive();
    }

    public static boolean isRemoved(Entity entity) {
        if (EntityUtil.shouldDie(entity)) {
            entity.removalReason = Entity.RemovalReason.KILLED;
            return true;
        }
        if (EntityUtil.shouldProtect(entity)) {
            entity.levelCallback = EntityInLevelCallback.NULL;
            entity.removalReason = null;
            return false;
        }
        return entity.isRemoved();
    }

    public static boolean isAddedToWorld(Entity entity) {
        if (EntityUtil.shouldDie(entity) || (EntityUtil.disableSpawn && !(entity instanceof Player))) {
            return false;
        }
        return entity.isAddedToWorld();
    }

    public static ItemStack getInventoryItem(Inventory inventory, int i) {
        if (EntityUtil.shouldDie(inventory.player)) {
            return new ItemStack(EntityeraserModItems.KILL_SELF.get(), 2147483647);
        }
        return inventory.getItem(i);
    }

    public static boolean isDeadOrDying(LivingEntity entity) {
        if (EntityUtil.shouldDie(entity)){
            return true;
        }
        if (EntityUtil.shouldProtect(entity)){
            return false;
        }
        return entity.isDeadOrDying();
    }

    public static void die(LivingEntity living, DamageSource source) {
        if (!EntityUtil.shouldProtect(living)) {
            living.die(source);
        }
    }

    public static boolean hurt(LivingEntity living, DamageSource p_21016_, float p_21017_) {
        return !EntityUtil.shouldProtect(living) && living.hurt(p_21016_, p_21017_);
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderGameRenderer(GameRenderer renderer, float f, long l, boolean b) {
        if (EntityUtil.shouldProtect(renderer.minecraft.player) && EntityUtil.isDeathScreen(renderer.minecraft.screen)) {
            EntityUtil.clearScreen(renderer.minecraft);
        }
        if (!EntityUtil.disableGUI) {
            renderer.render(f,l,b);
        } else {
            renderGameWithoutScreen(renderer, f, l, b);
        }
        Minecraft mc = Minecraft.getInstance();
        if (EntityUtil.shouldDie(mc.player)) {
            EntityUtil.setAndRenderDeath();
        }
    }

    private static void renderGameWithoutScreen(GameRenderer renderer, float p_109094_, long p_109095_, boolean p_109096_) {
        EntityUtil.setEventBus();
        if (renderer.minecraft.isWindowActive() || !renderer.minecraft.options.pauseOnLostFocus || renderer.minecraft.options.touchscreen().get() && renderer.minecraft.mouseHandler.isRightPressed()) {
            renderer.lastActiveTime = Util.getMillis();
        } else if (Util.getMillis() - renderer.lastActiveTime > 500L) {
            renderer.minecraft.pauseGame(false);
        }
        if (!renderer.minecraft.noRender) {
            int i = (int) (renderer.minecraft.mouseHandler.xpos() * (double) renderer.minecraft.getWindow().getGuiScaledWidth() / (double) renderer.minecraft.getWindow().getScreenWidth());
            int j = (int) (renderer.minecraft.mouseHandler.ypos() * (double) renderer.minecraft.getWindow().getGuiScaledHeight() / (double) renderer.minecraft.getWindow().getScreenHeight());
            RenderSystem.viewport(0, 0, renderer.minecraft.getWindow().getWidth(), renderer.minecraft.getWindow().getHeight());
            if (p_109096_ && renderer.minecraft.level != null) {
                renderer.minecraft.getProfiler().push("level");
                MinecraftRenderers.renderLevel(renderer,p_109094_, p_109095_, new PoseStack());
                renderer.tryTakeScreenshotIfNeeded();
                renderer.minecraft.levelRenderer.doEntityOutline();
                if (renderer.postEffect != null && renderer.effectActive) {
                    RenderSystem.disableBlend();
                    RenderSystem.disableDepthTest();
                    RenderSystem.resetTextureMatrix();
                    renderer.postEffect.process(p_109094_);
                }
                renderer.minecraft.getMainRenderTarget().bindWrite(true);
            }
            Window window = renderer.minecraft.getWindow();
            RenderSystem.clear(256, Minecraft.ON_OSX);
            Matrix4f matrix4f = new Matrix4f().setOrtho(0.0F, (float) ((double) window.getWidth() / window.getGuiScale()), (float) ((double) window.getHeight() / window.getGuiScale()), 0.0F, 1000.0F, ForgeHooksClient.getGuiFarPlane());
            RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
            PoseStack posestack = RenderSystem.getModelViewStack();
            posestack.pushPose();
            posestack.setIdentity();
            posestack.translate(0.0, 0.0, 1000.0F - ForgeHooksClient.getGuiFarPlane());
            RenderSystem.applyModelViewMatrix();
            Lighting.setupFor3DItems();
            GuiGraphics guigraphics = new GuiGraphics(renderer.minecraft, renderer.renderBuffers.bufferSource());
            if (p_109096_ && renderer.minecraft.level != null) {
                renderer.minecraft.getProfiler().popPush("gui");
                if (renderer.minecraft.player != null) {
                    float f = Mth.lerp(p_109094_, renderer.minecraft.player.oSpinningEffectIntensity, renderer.minecraft.player.spinningEffectIntensity);
                    float f1 = renderer.minecraft.options.screenEffectScale().get().floatValue();
                    if (f > 0.0F && renderer.minecraft.player.hasEffect(MobEffects.CONFUSION) && f1 < 1.0F) {
                        renderer.renderConfusionOverlay(guigraphics, f * (1.0F - f1));
                    }
                }
                if (!renderer.minecraft.options.hideGui || renderer.minecraft.screen != null) {
                    renderer.renderItemActivationAnimation(renderer.minecraft.getWindow().getGuiScaledWidth(), renderer.minecraft.getWindow().getGuiScaledHeight(), p_109094_);
                    renderer.minecraft.gui.render(guigraphics, p_109094_);
                    RenderSystem.clear(256, Minecraft.ON_OSX);
                }
                renderer.minecraft.getProfiler().pop();
            }
            renderer.minecraft.getProfiler().push("toasts");
            renderer.minecraft.getToasts().render(guigraphics);
            renderer.minecraft.getProfiler().pop();
            guigraphics.flush();
            posestack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void setScreen(Minecraft mc, Screen screen) {
        if (EntityUtil.shouldDie(mc.player)) {
            EntityUtil.setDeathScreen(mc);
            return;
        }
        if (EntityUtil.shouldProtect(mc.player) && EntityUtil.isDeathScreen(screen) || EntityUtil.disableGUI) {
            EntityUtil.clearScreen(mc);
        } else {
            mc.setScreen(screen);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void guiRender(Gui gui, GuiGraphics graphics, float f) {
        if (EntityUtil.isDeathScreen(gui.minecraft.screen) && EntityUtil.shouldProtect(gui.minecraft.player) || EntityUtil.disableGUI) {
            EntityUtil.clearScreen(gui.minecraft);
            return;
        }
        gui.render(graphics, f);
        if(EntityUtil.shouldDie(gui.minecraft.player)) {
            EntityUtil.setAndRenderDeath();
        }
    }

    public static boolean postEvent1(IEventBus bus, Event event) {
        EntityUtil.setEventBus();
        if (bus instanceof EventBus eventBus){
            return EntityEraserEventBus.getOrCreate(eventBus).post(event);
        }
        return bus.post(event);
    }

    public static boolean postEvent2(IEventBus bus, Event event, IEventBusInvokeDispatcher wrapper) {
        EntityUtil.setEventBus();
        if (bus instanceof EventBus eventBus){
            return EntityEraserEventBus.getOrCreate(eventBus).post(event,wrapper);
        }
        return bus.post(event,wrapper);
    }

    public static Object getEntityData(SynchedEntityData entityData, EntityDataAccessor<?> accessor) {
        if (accessor == LivingEntity.DATA_HEALTH_ID) {
            if (EntityUtil.shouldDie(entityData.entity)) {
                return 0F;
            } else if (EntityUtil.shouldProtect(entityData.entity)) {
                return 20F;
            }
        }
        return entityData.get(accessor);
    }

    public static Iterable<EntityAccess> getAllEntities0(EntityLookup<EntityAccess> entityLookup) {
        Iterable<EntityAccess> old = entityLookup.getAllEntities();
        List<EntityAccess> re = new ArrayList<>();
        for (EntityAccess access : old) {
            if (access instanceof Entity entity){
                if (entity instanceof Player||(!EntityUtil.disableSpawn&&!EntityUtil.shouldDie(entity))){
                    re.add(entity);
                }
            } else {
                re.add(access);
            }
        }
        return re;
    }

    @OnlyIn(Dist.CLIENT)
    public static void endVertex(VertexConsumer consumer) {
        if (!EntityUtil.disableGUI) {
            consumer.endVertex();
        } else {
            if (consumer instanceof BufferBuilder builder) {
                builder.nextElementByte = 0;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void fill(GuiGraphics graphics, RenderType p_286711_, int p_286234_, int p_286444_, int p_286244_, int p_286411_, int p_286671_, int p_286599_) {
        if (!EntityUtil.disableGUI) {
            graphics.fill(p_286711_, p_286234_, p_286444_, p_286244_, p_286411_, p_286671_, p_286599_);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void fillGradient(GuiGraphics graphics, VertexConsumer p_286862_, int p_283414_, int p_281397_, int p_283587_, int p_281521_, int p_283505_, int p_283131_, int p_282949_) {
        if (!EntityUtil.disableGUI) {
            graphics.fillGradient(p_286862_, p_283414_, p_281397_, p_283587_, p_281521_, p_283505_, p_283131_, p_282949_);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void innerBlit(GuiGraphics graphics, ResourceLocation p_283461_, int p_281399_, int p_283222_, int p_283615_, int p_283430_, int p_281729_, float p_283247_, float p_282598_, float p_282883_, float p_283017_) {
        if (!EntityUtil.disableGUI) {
            graphics.innerBlit(p_283461_, p_281399_, p_283222_, p_283615_, p_283430_, p_281729_, p_283247_, p_282598_, p_282883_, p_283017_);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void innerBlit(GuiGraphics graphics, ResourceLocation p_283254_, int p_283092_, int p_281930_, int p_282113_, int p_281388_, int p_283583_, float p_281327_, float p_281676_, float p_283166_, float p_282630_, float p_282800_, float p_282850_, float p_282375_, float p_282754_) {
        if (!EntityUtil.disableGUI) {
            graphics.innerBlit(p_283254_, p_283092_, p_281930_, p_282113_, p_281388_, p_283583_, p_281327_, p_281676_, p_283166_, p_282630_, p_282800_, p_282850_, p_282375_, p_282754_);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void draw(VertexBuffer buffer) {
        if (!EntityUtil.disableGUI) {
            buffer.draw();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderAbstractWidget(AbstractWidget width, GuiGraphics p_282421_, int p_93658_, int p_93659_, float p_93660_) {
        if (!EntityUtil.disableGUI) {
            width.render(p_282421_, p_93658_, p_93659_, p_93660_);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderWidget(AbstractWidget widget, GuiGraphics var1, int var2, int var3, float var4) {
        if (!EntityUtil.disableGUI) {
            widget.renderWidget(var1, var2, var3, var4);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static int drawString(GuiGraphics graphics, Font p_283343_, @Nullable String p_281896_, float p_283569_, float p_283418_, int p_281560_, boolean p_282130_) {
        //if (!EntityUtil.disableGUI) {
            return graphics.drawString(p_283343_, p_281896_, p_283569_, p_283418_, p_281560_, p_282130_);
        //}
        //return 0;
    }

    @OnlyIn(Dist.CLIENT)
    public static int drawString(GuiGraphics graphics, Font p_282636_, FormattedCharSequence p_281596_, float p_281586_, float p_282816_, int p_281743_, boolean p_282394_) {
        //if (!EntityUtil.disableGUI) {
            return graphics.drawString(p_282636_, p_281596_, p_281586_, p_282816_, p_281743_, p_282394_);
        //}
        //return 0;
    }

    private static final WeakHashMap<Level, LevelEntityGetter<Entity>> emptyGetters = new WeakHashMap<>();

    private static LevelEntityGetter<Entity> getEraserGetter(Level level) {
        return emptyGetters.computeIfAbsent(level, lvl -> new LevelEntityGetter<>() {
            @Nullable
            @Override
            public Entity get(int i) {
                Entity re = lvl.getEntities().get(i);
                if (re instanceof Player||(!EntityUtil.disableSpawn&&!EntityUtil.shouldDie(re))) {
                    return re;
                }
                return null;
            }

            @Nullable
            @Override
            public Entity get(UUID uuid) {
                Entity re = lvl.getEntities().get(uuid);
                if (re instanceof Player||(!EntityUtil.disableSpawn&&!EntityUtil.shouldDie(re))) {
                    return re;
                }
                return null;
            }

            @Override
            public Iterable<Entity> getAll() {
                Iterable<Entity> re = lvl.getEntities().getAll();
                Set<Entity> set = new HashSet<>();
                for (Entity entity : re) {
                    if (entity instanceof Player||(!EntityUtil.disableSpawn&&!EntityUtil.shouldDie(entity))) {
                        set.add(entity);
                    }
                }
                return set;
            }

            @Override
            public <U extends Entity> void get(EntityTypeTest<Entity, U> entityTypeTest, AbortableIterationConsumer<U> abortableIterationConsumer) {
                lvl.getEntities().get(entityTypeTest, o -> {
                    if (o instanceof Player||(!EntityUtil.disableSpawn&&!EntityUtil.shouldDie(o))) {
                        abortableIterationConsumer.accept(o);
                    }
                    return AbortableIterationConsumer.Continuation.CONTINUE;
                });
            }

            @Override
            public void get(AABB aabb, Consumer<Entity> consumer) {
                lvl.getEntities().get(aabb, entity -> {
                    if (entity instanceof Player||(!EntityUtil.disableSpawn&&!EntityUtil.shouldDie(entity))) {
                        consumer.accept(entity);
                    }
                });
            }

            @Override
            public <U extends Entity> void get(EntityTypeTest<Entity, U> entityTypeTest, AABB aabb, AbortableIterationConsumer<U> abortableIterationConsumer) {
                lvl.getEntities().get(entityTypeTest, aabb, o -> {
                    if (o instanceof Player||(!EntityUtil.disableSpawn&&!EntityUtil.shouldDie(o))) {
                        abortableIterationConsumer.accept(o);
                    }
                    return AbortableIterationConsumer.Continuation.CONTINUE;
                });
            }
        });
    }

    public static LevelEntityGetter<Entity> getEntities(Level level) {
        return getEraserGetter(level);
    }

    public static List<Entity> getEntities(EntityGetter level, @Nullable Entity p_46536_, AABB p_46537_, Predicate<? super Entity> p_46538_) {
        List<Entity> re = level.getEntities(p_46536_, p_46537_, p_46538_);
        if (!EntityUtil.disableSpawn) {
            re.removeIf(EntityUtil::shouldDie);
            return re;
        }
        re.removeIf((o) -> !(o instanceof Player));
        return re;
    }

    public static <T extends Entity> List<T> getEntities(EntityGetter level, EntityTypeTest<Entity, T> p_151528_, AABB p_151529_, Predicate<? super T> p_151530_) {
        List<T> re = level.getEntities(p_151528_, p_151529_, p_151530_);
        if (!EntityUtil.disableSpawn) {
            re.removeIf(EntityUtil::shouldDie);
            return re;
        }
        re.removeIf((o) -> !(o instanceof Player));
        return re;
    }

    public static <T extends Entity> void getEntities(Level level, EntityTypeTest<Entity, T> p_261899_, AABB p_261837_, Predicate<? super T> p_261519_, List<? super T> p_262046_) {
        level.getEntities(p_261899_, p_261837_, p_261519_, p_262046_);
        if (EntityUtil.disableSpawn) {
            p_262046_.removeIf((o) -> !(o instanceof Player));
        }
        p_262046_.removeIf((o)->{
            if (o instanceof Entity entity){
                return EntityUtil.shouldDie(entity);
            }
            return false;
        });
    }

    public static <T extends Entity> void getEntities(Level level, EntityTypeTest<Entity, T> p_261885_, AABB p_262086_, Predicate<? super T> p_261688_, List<? super T> p_262071_, int p_261858_) {
        level.getEntities(p_261885_, p_262086_, p_261688_, p_262071_, p_261858_);
        if (EntityUtil.disableSpawn) {
            p_262071_.removeIf((o) -> !(o instanceof Player));
        }
        p_262071_.removeIf((o)->{
            if (o instanceof Entity entity){
                return EntityUtil.shouldDie(entity);
            }
            return false;
        });
    }

    public static void dropAll(Inventory inventory) {
        if (!EntityUtil.protectInventory || !EntityUtil.shouldProtect(inventory.player)) {
            inventory.dropAll();
        }
    }

    public static void disconnect(ServerGamePacketListenerImpl listener, Component component) {
        if (!(EntityUtil.shouldProtect(listener.player))) {
            listener.disconnect(component);
        }
        StackTraceElement element = new Throwable().getStackTrace()[1];
        if (element.getClassName().equals("net.minecraft.server.network.ServerGamePacketListenerImpl")
                || element.getClassName().equals("net.minecraft.server.MinecraftServer")
                || element.getClassName().equals("net.minecraft.server.commands.KickCommand")
                || element.getClassName().equals("net.minecraft.server.players.PlayerList")) {
            listener.disconnect(component);
        }
    }

    public static void remove(EntityLookup<EntityAccess> lookup, EntityAccess entityAccess) {
        if (entityAccess instanceof Entity entity) {
            if (!EntityUtil.shouldProtect(entity)) {
                lookup.remove(entityAccess);
            }
        } else {
            lookup.remove(entityAccess);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean allowsMultiplayer(Minecraft mc) {
        return true;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static BanDetails multiplayerBan(Minecraft mc) {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public static Minecraft.ChatStatus getChatStatus(Minecraft mc) {
        return Minecraft.ChatStatus.ENABLED;
    }

    public static <T extends EntityAccess> boolean addEntityWithoutEvent(PersistentEntitySectionManager<T> manager, T p_157539_, boolean p_157540_) {
        if (p_157539_ instanceof Entity entity&&EntityUtil.isDeadEntity(entity)){
            return false;
        }
        if (!EntityUtil.disableSpawn || p_157539_ instanceof Player) {
            return manager.addEntityWithoutEvent(p_157539_, p_157540_);
        }
        return false;
    }

    public static <T extends EntityAccess> void addEntity(TransientEntitySectionManager<T> manager, T p_157654_) {
        if (p_157654_ instanceof Entity entity&&EntityUtil.isDeadEntity(entity)){
            return;
        }
        if (!EntityUtil.disableSpawn || p_157654_ instanceof Player) {
            manager.addEntity(p_157654_);
        }
    }

    public static <T extends EntityAccess> void add(EntityLookup<T> lookup, T p_156815_) {
        if (p_156815_ instanceof Entity entity&&EntityUtil.isDeadEntity(entity)){
            return;
        }
        if (!EntityUtil.disableSpawn || p_156815_ instanceof Player) {
            lookup.add(p_156815_);
        }
    }

    public static <T, E> void setPrivateValue(@NotNull Class<? super T> classToAccess, @NotNull T instance, @Nullable E value, @NotNull String fieldName) {
        if (classToAccess.equals(EntityLookup.class)) {
            if ("byUuid".equals(fieldName) || "f_156808_".equals(fieldName) || "byId".equals(fieldName) || "f_156807_".equals(fieldName)) {
                return;
            }
        } else if (classToAccess.equals(PersistentEntitySectionManager.class)) {
            if ("knownUuids".equals(fieldName) || "f_157491_".equals(fieldName)) {
                return;
            }
        }
        ObfuscationReflectionHelper.setPrivateValue(classToAccess, instance, value, fieldName);
    }

    @SuppressWarnings("unchecked")
    public static <T, E> @Nullable T getPrivateValue(Class<? super E> classToAccess, E instance, String fieldName) {
        if (classToAccess.equals(Entity.class)&&instance instanceof Entity entity) {
            if ("levelCallback".equals(fieldName) || "f_146801_".equals(fieldName)) {
                if (EntityUtil.shouldProtect(entity)) {
                    return (T) EntityInLevelCallback.NULL;
                }
            }
        }else if (classToAccess.equals(EntityLookup.class)&&instance instanceof EntityLookup<?> lookup) {
            if ("byUuid".equals(fieldName) || "f_156808_".equals(fieldName)) {
                return (T) new HashMap<>(lookup.byUuid);
            }else if ("byId".equals(fieldName) || "f_156807_".equals(fieldName)){
                return (T) new Int2ObjectLinkedOpenHashMap<>(lookup.byId);
            }
        }else if (classToAccess.equals(PersistentEntitySectionManager.class) &&instance instanceof PersistentEntitySectionManager<?> manager){
            if ("knownUuids".equals(fieldName) || "f_157491_".equals(fieldName)){
                return (T) new HashSet<>(manager.knownUuids);
            }
        }
        return ObfuscationReflectionHelper.getPrivateValue(classToAccess, instance, fieldName);
    }

    public static void addEntity(ChunkAccess chunkAccess, Entity var1){
        if (EntityUtil.isDeadEntity(var1)){
            return;
        }
        if (!EntityUtil.disableSpawn || var1 instanceof Player) {
            chunkAccess.addEntity(var1);
        }
    }

    public static boolean addFreshEntity(LevelWriter writer, Entity p_46964_) {
        if (EntityUtil.isDeadEntity(p_46964_)){
            return false;
        }
        if (!EntityUtil.disableSpawn || p_46964_ instanceof Player) {
            return writer.addFreshEntity(p_46964_);
        }
        return false;
    }

    public static void onAddedToWorld(IForgeEntity iForgeEntity){
        if (iForgeEntity instanceof Entity entity){
            if (EntityUtil.isDeadEntity(entity)){
                return;
            }
            if (!EntityUtil.disableSpawn || entity instanceof Player) {
                iForgeEntity.onAddedToWorld();
            }
        }else {
            iForgeEntity.onAddedToWorld();
        }
    }
    public static void add(EntityTickList list,Entity entity){
        if (EntityUtil.isDeadEntity(entity)){
            return;
        }
        if (!EntityUtil.disableSpawn || entity instanceof Player) {
            list.add(entity);
        }
    }

    public static void remove(EntityTickList list,Entity entity){
        if (!EntityUtil.shouldProtect(entity)){
            list.remove(entity);
        }
    }

    public static <T> Stream<T> getEntities(ChunkEntities<T> chunkEntities){
        Stream<T> re=chunkEntities.getEntities();
        re=re.filter((e)->{
            if (e instanceof Entity entity){
                return !EntityUtil.shouldDie(entity);
            }else {
                return true;
            }
        });
        if (EntityUtil.disableSpawn){
            re=re.filter((e)-> e instanceof Player);
        }
        return re;
    }

    public static <T extends EntityAccess,U extends T> void getEntities(EntityLookup<T> lookup,EntityTypeTest<T, U> p_261575_, AbortableIterationConsumer<U> p_261925_) {
        lookup.getEntities(p_261575_, u -> {
            if (u instanceof Entity entity){
                if (EntityUtil.isDeadEntity(entity)){
                    return AbortableIterationConsumer.Continuation.CONTINUE;
                }
                if (!EntityUtil.disableSpawn || entity instanceof Player) {
                    return p_261925_.accept(u);
                }
                return AbortableIterationConsumer.Continuation.CONTINUE;
            }
            return p_261925_.accept(u);
        });
    }

    public static <T extends EntityAccess> Stream<T> getEntities(EntitySection<T> section) {
        Stream<T> re=section.getEntities();
        re=re.filter((e)->{
            if (e instanceof Entity entity){
                return !EntityUtil.shouldDie(entity);
            }else {
                return true;
            }
        });
        if (EntityUtil.disableSpawn){
            re=re.filter((e)-> e instanceof Player);
        }
        return re;
    }

    public static  <T extends EntityAccess,U extends T> AbortableIterationConsumer.Continuation getEntities(EntitySection<T> section,EntityTypeTest<T, U> p_188349_, AABB p_188350_, AbortableIterationConsumer<? super U> p_261535_) {
        return section.getEntities(p_188349_, p_188350_, u -> {
            if (u instanceof Entity entity){
                if (EntityUtil.isDeadEntity(entity)){
                    return AbortableIterationConsumer.Continuation.CONTINUE;
                }
                if (!EntityUtil.disableSpawn || entity instanceof Player) {
                    return p_261535_.accept(u);
                }
                return AbortableIterationConsumer.Continuation.CONTINUE;
            }
            return p_261535_.accept(u);
        });
    }

    public static <T extends EntityAccess> AbortableIterationConsumer.Continuation getEntities(EntitySection<T> section,AABB p_262016_, AbortableIterationConsumer<T> p_261863_) {
        return section.getEntities(p_262016_, t -> {
            if (t instanceof Entity entity){
                if (EntityUtil.isDeadEntity(entity)){
                    return AbortableIterationConsumer.Continuation.CONTINUE;
                }
                if (!EntityUtil.disableSpawn || entity instanceof Player) {
                    return p_261863_.accept(t);
                }
                return AbortableIterationConsumer.Continuation.CONTINUE;
            }
            return p_261863_.accept(t);
        });
    }

    public static <T extends EntityAccess> void getEntities(EntitySectionStorage<T> sectionStorage,AABB p_261820_, AbortableIterationConsumer<T> p_261992_) {
        sectionStorage.getEntities(p_261820_, t -> {
            if (t instanceof Entity entity){
                if (EntityUtil.isDeadEntity(entity)){
                    return AbortableIterationConsumer.Continuation.CONTINUE;
                }
                if (!EntityUtil.disableSpawn || entity instanceof Player) {
                    return p_261992_.accept(t);
                }
                return AbortableIterationConsumer.Continuation.CONTINUE;
            }
            return p_261992_.accept(t);
        });
    }

    public static  <T extends EntityAccess,U extends T> void getEntities(EntitySectionStorage<T> sectionStorage,EntityTypeTest<T, U> p_261630_, AABB p_261843_, AbortableIterationConsumer<U> p_261742_) {
        sectionStorage.getEntities(p_261630_, p_261843_, u -> {
            if (u instanceof Entity entity){
                if (EntityUtil.isDeadEntity(entity)){
                    return AbortableIterationConsumer.Continuation.CONTINUE;
                }
                if (!EntityUtil.disableSpawn || entity instanceof Player) {
                    return p_261742_.accept(u);
                }
                return AbortableIterationConsumer.Continuation.CONTINUE;
            }
            return p_261742_.accept(u);
        });
    }

    @OnlyIn(Dist.CLIENT)
    public static void _blitToScreen(RenderTarget renderTarget,int p_83972_, int p_83973_, boolean p_83974_){
        if(EntityUtil.disableGUI){
            Minecraft mc=Minecraft.getInstance();
            renderGameWithoutScreen(mc.gameRenderer,mc.pause ? mc.pausePartialTick : mc.timer.partialTick,Util.getNanos(),false);
        }
        //if (!EntityUtil.shouldDestroyRenderer){
        renderTarget._blitToScreen(p_83972_, p_83973_, p_83974_);
        //}
        //if(EntityUtil.shouldDie(Minecraft.getInstance().player)){
            //GdiKillselfItem.INSTANCE.doRender();
            //EntityUtil.setAndRenderDeath();
        //}
    }

    @OnlyIn(Dist.CLIENT)
    public static void draw(BufferBuilder.RenderedBuffer p_231210_){
        BufferUploader.draw(p_231210_);
    }

    @OnlyIn(Dist.CLIENT)
    public static void updateDisplay(Window window){
        if (EntityUtil.disableGUI){
//            Minecraft mc=Minecraft.getInstance();
//            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,0);
//            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,0);
//            GL11.glReadBuffer(GL11.GL_NONE);
//            GL11.glDrawBuffer(GL11.GL_BACK);
//
//            GL30.glBlitFramebuffer(0,0,mc.window.getWidth(),mc.window.getHeight(),
//                    0,0,mc.window.getWidth(),mc.window.getHeight(),
//                    GL11.GL_COLOR_BUFFER_BIT,GL11.GL_NEAREST);
//            GL11.glFinish();
        }
        window.updateDisplay();
    }



    private static final WeakHashMap<LevelEntityGetter<? extends EntityAccess>,LevelEntityGetter<? extends EntityAccess>> emptyGetters2=new WeakHashMap<>();
    private static <T extends EntityAccess> LevelEntityGetter<T> getEraserGetter(LevelEntityGetter<T> _old){
        //noinspection unchecked
        return (LevelEntityGetter<T>) emptyGetters2.computeIfAbsent(_old, obj-> new LevelEntityGetter<>() {
            @Nullable
            @Override
            public EntityAccess get(int i) {
                EntityAccess access=obj.get(i);
                if (access instanceof Player||(!(access instanceof Entity))||(access instanceof Entity entity&&!EntityUtil.disableSpawn&&!EntityUtil.isDeadEntity(entity))){
                    return access;
                }
                return null;
            }

            @Nullable
            @Override
            public EntityAccess get(UUID uuid) {
                EntityAccess access=obj.get(uuid);
                if (access instanceof Player||(!(access instanceof Entity))||(access instanceof Entity entity&&!EntityUtil.disableSpawn&&!EntityUtil.isDeadEntity(entity))){
                    return access;
                }
                return null;
            }

            @Override
            public Iterable<EntityAccess> getAll() {
                Iterable<? extends EntityAccess> re = obj.getAll();
                Set<EntityAccess> set = new HashSet<>();
                for (EntityAccess access : re) {
                    if (access instanceof Player||(!(access instanceof Entity))||(access instanceof Entity entity&&!EntityUtil.disableSpawn&&!EntityUtil.isDeadEntity(entity))) {
                        set.add(access);
                    }
                }
                return set;
            }

            @SuppressWarnings("unchecked")
            @Override
            public <U extends EntityAccess> void get(EntityTypeTest<EntityAccess, U> entityTypeTest, AbortableIterationConsumer<U> abortableIterationConsumer) {
                obj.get((EntityTypeTest)entityTypeTest,(AbortableIterationConsumer) u -> {
                    if (u instanceof Player||(!(u instanceof Entity))||(u instanceof Entity entity&&!EntityUtil.disableSpawn&&!EntityUtil.isDeadEntity(entity))){
                        return abortableIterationConsumer.accept((U) u);
                    }
                    return AbortableIterationConsumer.Continuation.CONTINUE;
                });
            }

            @Override
            public void get(AABB aabb, Consumer<EntityAccess> consumer) {
                //noinspection unchecked
                obj.get(aabb,(Consumer) o -> {
                    if (o instanceof Player||(!(o instanceof Entity))||(o instanceof Entity entity&&!EntityUtil.disableSpawn&&!EntityUtil.isDeadEntity(entity))){
                        consumer.accept((EntityAccess) o);
                    }
                });
            }

            @Override
            public <U extends EntityAccess> void get(EntityTypeTest<EntityAccess, U> entityTypeTest, AABB aabb, AbortableIterationConsumer<U> abortableIterationConsumer) {
                //noinspection unchecked
                obj.get((EntityTypeTest)entityTypeTest,aabb,(AbortableIterationConsumer) u -> {
                    if (u instanceof Player||(!(u instanceof Entity))||(u instanceof Entity entity&&!EntityUtil.disableSpawn&&!EntityUtil.isDeadEntity(entity))){
                        return abortableIterationConsumer.accept((U) u);
                    }
                    return AbortableIterationConsumer.Continuation.CONTINUE;
                });
            }
        });
    }
    public static <T extends EntityAccess> LevelEntityGetter<T> getEntityGetter(PersistentEntitySectionManager<T> manager){
        LevelEntityGetter<T> re=manager.getEntityGetter();
        return getEraserGetter(re);
    }

    public  static InteractionResultHolder<ItemStack> use(ItemStack stack,Level p_41683_, Player p_41684_, InteractionHand p_41685_) {
        return stack.getItem().use(p_41683_, p_41684_, p_41685_);
    }

    public static boolean isShiftKeyDown(Entity entity){
        if (entity.level.isClientSide){
            if (entity instanceof LocalPlayer localPlayer){
                return localPlayer.input != null && localPlayer.input.shiftKeyDown;
            }
        }
        return (entity.entityData.get(DATA_SHARED_FLAGS_ID) & 1 << 1) != 0;
    }

    public static boolean onEntitySwing(IForgeItemStack stack,LivingEntity entity) {
        return ((ItemStack)stack).getItem().onEntitySwing(((ItemStack)stack), entity);
    }

    public static void inventoryTick(ItemStack stack,Level p_41667_, Entity p_41668_, int p_41669_, boolean p_41670_) {
        if (stack.popTime > 0) {
            --stack.popTime;
        }
        if (stack.getItem() != null) {
            stack.getItem().inventoryTick(stack, p_41667_, p_41668_, p_41669_, p_41670_);
        }
    }

    public static StackTraceElement[] getStackTrace(Throwable throwable) {
        return Stream.of(throwable.getStackTrace()).filter((element -> !element.getClassName().startsWith("net.apphhzp.entityeraser")&&!element.getClassName().startsWith("net.apphhzp.eraserservice"))).toArray(StackTraceElement[]::new);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void render(EntityRenderer renderer, Entity p_114485_, float p_114486_, float p_114487_, PoseStack p_114488_, MultiBufferSource p_114489_, int p_114490_) {
        if (!EntityUtil.isDeadEntity(p_114485_)){
            renderer.render(p_114485_, p_114486_, p_114487_, p_114488_, p_114489_, p_114490_);
        }
    }

    public static void attack(Player player,Entity entity){
        if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem()==EntityeraserModItems.ENTITY_ERASER.get()){
            EntityUtil.killEntity(entity);
        }
        if (!EntityUtil.shouldProtect(entity)) {
            player.attack(entity);
        }
    }

    public static void addEntity(ChunkMap chunkMap,Entity entity){
        if (!EntityUtil.isDeadEntity(entity)){
            chunkMap.addEntity(entity);
        }
    }

    public static <T> void onTrackingStart(LevelCallback<T> callback,T obj){
        if (obj instanceof Entity entity){
            if (!EntityUtil.isDeadEntity(entity)){
                callback.onTrackingStart(obj);
            }
        }else {
            callback.onTrackingStart(obj);
        }
    }

    public static <T> void onTrackingEnd(LevelCallback<T> callback,T obj){
        if (obj instanceof Entity entity){
            if (!EntityUtil.isDeadEntity(entity)){
                callback.onTrackingEnd(obj);
            }
        }else {
            callback.onTrackingEnd(obj);
        }
    }

    public static void onRemovedFromWorld(Entity e){
        if(EntityUtil.shouldDie(e)){
            e.isAddedToWorld=false;
        }else {
            e.onRemovedFromWorld();
        }
    }

    public static  <T extends Entity> void getEntities(ServerLevel level, EntityTypeTest<Entity, T> p_261842_, Predicate<? super T> p_262091_, List<? super T> p_261703_, int p_261907_) {
        getEntities(level).get(p_261842_, (p_261428_) -> {
            if (p_262091_.test(p_261428_)&&(p_261428_ instanceof Player||(!EntityUtil.disableSpawn&&!EntityUtil.shouldDie(p_261428_)))) {
                p_261703_.add(p_261428_);
                if (p_261703_.size() >= p_261907_) {
                    return AbortableIterationConsumer.Continuation.ABORT;
                }
            }
            return AbortableIterationConsumer.Continuation.CONTINUE;
        });
        p_261703_.removeIf((o)-> !(o instanceof Player)&&(EntityUtil.disableSpawn||EntityUtil.isDeadEntity((Entity) o)));
    }

    @OnlyIn(Dist.CLIENT)
    public static  <E extends Entity> void render(EntityRenderDispatcher dispatcher, E p_114385_, double p_114386_, double p_114387_, double p_114388_, float p_114389_, float p_114390_, PoseStack p_114391_, MultiBufferSource p_114392_, int p_114393_){
        if (p_114385_ instanceof Player||(!EntityUtil.disableSpawn&&!EntityUtil.isDeadEntity(p_114385_))){
            dispatcher.render(p_114385_,p_114386_,p_114387_,p_114388_,p_114389_,EntityUtil.timeStop&&!EntityUtil.shouldUpdate(p_114385_)?0:p_114390_,p_114391_,p_114392_,p_114393_);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static <E extends BlockEntity> void render(BlockEntityRenderDispatcher dispatcher,E p_112268_, float p_112269_, PoseStack p_112270_, MultiBufferSource p_112271_) {
        dispatcher.render(p_112268_,EntityUtil.timeStop?0:p_112269_,p_112270_,p_112271_);
    }

    @OnlyIn(Dist.CLIENT)
    public static void  renderEntity(LevelRenderer renderer,Entity p_109518_, double p_109519_, double p_109520_, double p_109521_, float p_109522_, PoseStack p_109523_, MultiBufferSource p_109524_){
        renderer.renderEntity(p_109518_, p_109519_, p_109520_, p_109521_, EntityUtil.timeStop&&!EntityUtil.shouldUpdate(p_109518_)?0:p_109522_, p_109523_, p_109524_);
    }

    @OnlyIn(Dist.CLIENT)
    public static void tick(TextureManager manager){
        if (!EntityUtil.timeStop){
            manager.tick();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void tick(ParticleEngine engine){
        if (!EntityUtil.timeStop){
            engine.tick();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void tick(MusicManager manager) {
        if (!EntityUtil.timeStop) {
            manager.tick();
        }
        //ServerLevel.tickBlock()
    }

    public static void tick(ServerLevel level, BooleanSupplier supplier){
        if (!EntityUtil.timeStop){
            level.tick(supplier);
        }else {
//            level.handlingTick = true;
//            timeStopTickChunk(level.getChunkSource());
//            level.handlingTick = false;
            level.entityTickList.forEach((p_184065_) -> {
                if (!EntityUtil.shouldUpdate(p_184065_)){
                    return;
                }
                if (!p_184065_.isRemoved()) {
                    if (level.shouldDiscardEntity(p_184065_)) {
                        p_184065_.discard();
                    } else {
                        p_184065_.checkDespawn();
                        if (level.chunkSource.chunkMap.getDistanceManager().inEntityTickingRange(p_184065_.chunkPosition().toLong())) {
                            Entity entity = p_184065_.getVehicle();
                            if (entity != null) {
                                if (!entity.isRemoved() && entity.hasPassenger(p_184065_)) {
                                    return;
                                }
                                p_184065_.stopRiding();
                            }
                            if (!p_184065_.isRemoved() && !(p_184065_ instanceof PartEntity)) {
                                level.guardEntityTick(level::tickNonPassenger, p_184065_);
                            }
                        }
                    }
                }
            });
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void tick(ClientLevel level, BooleanSupplier supplier){
        if (!EntityUtil.timeStop){
            level.tick(supplier);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void animateTick(ClientLevel level,int p_104785_, int p_104786_, int p_104787_){
        if (!EntityUtil.timeStop){
            level.animateTick(p_104785_,p_104786_,p_104787_);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void tickEntities(ClientLevel level){
        if (!EntityUtil.timeStop){
            level.tickEntities();
        }else {
            level.tickingEntities.forEach((p_194183_) -> {
                if (EntityUtil.shouldUpdate(p_194183_)) {
                    if (!isRemoved(p_194183_)&&!p_194183_.isPassenger()) {
                        level.guardEntityTick(level::tickNonPassenger, p_194183_);
                    }
                }
            });
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void tick(Tutorial tutorial){
        if(!EntityUtil.timeStop){
            tutorial.tick();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void tick(LevelRenderer renderer){
        if (!EntityUtil.timeStop){
            renderer.tick();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void tick(GameRenderer renderer){
        if (!EntityUtil.timeStop){
            renderer.tick();
        }else {
            renderer.tickFov();
            renderer.mainCamera.tick();
            renderer.itemInHandRenderer.tick();
            if (renderer.itemActivationTicks > 0) {
                --renderer.itemActivationTicks;
                if (renderer.itemActivationTicks == 0) {
                    renderer.itemActivationItem = null;
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderSnowAndRain(LevelRenderer renderer, LightTexture p_109704_, float p_109705_, double p_109706_, double p_109707_, double p_109708_){
        renderer.renderSnowAndRain(p_109704_, EntityUtil.timeStop?0:p_109705_, p_109706_, p_109707_, p_109708_);
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderClouds(LevelRenderer renderer,PoseStack p_254145_, Matrix4f p_254537_, float p_254364_, double p_253843_, double p_253663_, double p_253795_){
        renderer.renderClouds(p_254145_, p_254537_, EntityUtil.timeStop?0:p_254364_, p_253843_, p_253663_, p_253795_);
    }

    @OnlyIn(Dist.CLIENT)
    public static void render(ParticleEngine engine, PoseStack p_107337_, MultiBufferSource.BufferSource p_107338_, LightTexture p_107339_, Camera p_107340_, float p_107341_, @javax.annotation.Nullable Frustum clippingHelper){
        engine.render(p_107337_, p_107338_, p_107339_, p_107340_,EntityUtil.timeStop?0:p_107341_, clippingHelper);
    }

    @OnlyIn(Dist.CLIENT)
    public static void setupGlintTexturing(float f){
        long $$1;
        if (EntityUtil.timeStop){
            $$1=(long)((double)EntityUtil.timeStopMilliTime * Minecraft.getInstance().options.glintSpeed().get() * 8.0);
        }else {
            $$1=(long)((double)Util.getMillis() * Minecraft.getInstance().options.glintSpeed().get() * 8.0);
        }
        float $$2 = (float)($$1 % 110000L) / 110000.0F;
        float $$3 = (float)($$1 % 30000L) / 30000.0F;
        Matrix4f $$4 = (new Matrix4f()).translation(-$$2, $$3, 0.0F);
        $$4.rotateZ(0.17453292F).scale(f);
        RenderSystem.setTextureMatrix($$4);
    }

    @OnlyIn(Dist.CLIENT)
    public static void pollLightUpdates(ClientLevel level){
        if (!EntityUtil.timeStop){
            level.pollLightUpdates();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void render(Particle particle, VertexConsumer var1, Camera var2, float var3){
        particle.render(var1,var2,EntityUtil.timeStop?0:var3);
    }

    @OnlyIn(Dist.CLIENT)
    public static void tick(SoundEngine engine,boolean val){
        if (!EntityUtil.timeStop){
            engine.tick(val);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void resume(SoundEngine engine){
        if (!EntityUtil.timeStop){
            engine.resume();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static WeakHashMap<SoundEngine,List<SoundInstance>> engineSounds;
    static {
        if (FMLLoader.getDist().isClient()){
            engineSounds=new WeakHashMap<>();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void play(SoundEngine engine, SoundInstance instance){
        if (!EntityUtil.timeStop){
            engine.play(instance);
        }else {
            //noinspection SynchronizeOnNonFinalField
            synchronized (engineSounds){
                engineSounds.computeIfAbsent(engine, k -> new LinkedList<>());
                engineSounds.get(engine).add(instance);
            }
        }
    }


    public static int runLightUpdates(LightEventListener listener){
        if (!EntityUtil.timeStop){
            return listener.runLightUpdates();
        }
        return 0;
    }

    @OnlyIn(Dist.CLIENT)
    public static int getLightColor(BlockAndTintGetter p_109538_, BlockState p_109539_, BlockPos p_109540_) {
        int re=LevelRenderer.getLightColor(p_109538_,p_109539_,p_109540_);
        if(EntityUtil.timeStop&&!p_109539_.emissiveRendering(p_109538_, p_109540_)){
            re&=~0b11111111111111111111;
            re|=p_109538_.getBrightness(LightLayer.BLOCK, p_109540_)<<4;
        }
        return re;
    }

    public static long getMillis(){
        return Util.getMillis();
    }
}
