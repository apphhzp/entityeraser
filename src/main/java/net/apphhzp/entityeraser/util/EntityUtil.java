package net.apphhzp.entityeraser.util;

import apphhzp.lib.ClassHelper;
import apphhzp.lib.natives.NativeUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.apphhzp.entityeraser.AllReturn;
import net.apphhzp.entityeraser.EntityeraserMod;
import net.apphhzp.entityeraser.init.EntityeraserModItems;
import net.apphhzp.entityeraser.item.EntityProtectorItem;
import net.apphhzp.entityeraser.screen.EntityEraserDeathScreen;
import net.apphhzp.entityeraser.shitmountain.EntityEraserRenderers;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.PacketDistributor;
import org.apache.commons.lang3.mutable.MutableObject;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;
import org.lwjgl.system.JNI;
import org.lwjgl.system.SharedLibrary;
import org.lwjgl.system.windows.GDI32;
import org.lwjgl.system.windows.User32;
import org.lwjgl.system.windows.WinBase;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import static apphhzp.lib.ClassHelper.unsafe;
import static net.apphhzp.entityeraser.util.EntityUtil.Kernel32.PAGE_EXECUTE_READWRITE;
import static net.minecraft.server.level.ChunkMap.isChunkInRange;

@SuppressWarnings("SameParameterValue")
public final class EntityUtil {
    private static final Set<GameProfile> death=new DisableRemoveSet<>();

    private static final Set<GameProfile> defense=new DisableRemoveSet<>();
    private static final WeakHashMap<Entity,Object> deadEntities=new WeakHashMap<>();
    private static final Object NULL_OBJECT=new Object();
    @OnlyIn(Dist.CLIENT)
    private static Set<Minecraft> death_client;

    @OnlyIn(Dist.CLIENT)
    private static Set<Minecraft> defense_client;

    static {
        if (FMLEnvironment.dist.isClient()) {
            death_client = new DisableRemoveSet<>();
            defense_client = new DisableRemoveSet<>();
            disableGUI = false;
            shouldDestroyRenderer = false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean disableGUI;

    public static boolean protectInventory =false;

    public static boolean shouldDie(Entity entity){
        if (entity instanceof Player player){
            boolean flag=death.contains(player.getGameProfile());
            if (player.level.isClientSide){
                if (player instanceof RemotePlayer){
                    return false;
                }
                if (player.level instanceof ClientLevel clientLevel){
                      flag=flag||death_client.contains(clientLevel.minecraft);
                }
            }
            return flag;
        }
        return deadEntities.containsKey(entity);
    }

    public static boolean isDeadEntity(Entity entity){
        if (entity.level.isClientSide){
            if (entity instanceof RemotePlayer){
                return false;
            }
        }
        return deadEntities.containsKey(entity);
    }

    public static boolean shouldProtect(Entity entity){
        if (entity instanceof Player player){
            boolean flag=defense.contains(player.getGameProfile());
            if (player.level.isClientSide){
                if (player instanceof RemotePlayer){
                    return false;
                }
                if (player.level instanceof ClientLevel clientLevel){
                    flag=flag||defense_client.contains(clientLevel.minecraft);
                }
            }
            return flag;
        }
        return false;
    }


    public static final AtomicBoolean killEvents=new AtomicBoolean(false);

    public static boolean disableSpawn=false;
    public static boolean disableCallEntityMethods=false;

    @OnlyIn(Dist.CLIENT)
    public static boolean shouldDestroyRenderer;

    public static <MSG> Object sendPacketToP(ServerPlayer player, MSG packet) {
        EntityeraserMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), packet);
        return null;
    }

    public static <MSG> Object sendPacketToAllP(MSG packet, MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendPacketToP(player, packet);
        }
        return null;
    }

    public static int getSmoothColor(int alpha, double offset,double len){
        return Mth.hsvToRgb((float)Mth.clamp((Math.cos(Util.getMillis()/len+offset)/2D+0.5D),0.0001,0.9999),1,1)|(alpha<<24);
    }

    public static int getColor(int alpha, double offset,double len){
        return Mth.hsvToRgb((float)Mth.clamp((Math.abs(Math.cos(Util.getMillis()/len+offset))),0.0001,0.9999),1,1)|(alpha<<24);
    }

    public static void setEventBus() {
        try {
            if (!(MinecraftForge.EVENT_BUS instanceof EntityEraserEventBus)) {
                ClassHelper.forceSetField(null,MinecraftForge.class.getDeclaredField("EVENT_BUS"),EntityEraserEventBus.INSTANCE);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    public static void killEntity(Entity entity){
        if (entity!=null){
            //boolean pre=killEvents.get();
            try {
                setEventBus();
                //killEvents.set(true);
                if (entity.level.isClientSide){
                    if (!(entity instanceof RemotePlayer)){
                        deadEntities.put(entity,NULL_OBJECT);
                    }
                }else {
                    deadEntities.put(entity,NULL_OBJECT);
                }
                Level level=entity.level;
                entity.invulnerable=false;
                completeRemove(entity);
                if (entity instanceof LivingEntity living){
                    die(living);
                    living.deathTime=19;
                    living.hurtTime=0;
                    living.entityData.set(LivingEntity.DATA_HEALTH_ID,0F);
                    //noinspection DataFlowIssue
                    living.attributes.getInstance(Attributes.MAX_HEALTH).setBaseValue(0);
                    if (living instanceof Player player){
                        setEventBus();
                        death.add(player.getGameProfile());
                        if (EntityeraserMod.enableAllReturnKill) {
                            AllReturn.allReturn = true;
                        }
                        if (player.getAbilities().invulnerable) {
                            player.getAbilities().invulnerable = false;
                            player.onUpdateAbilities();
                        }
                        Inventory inventory=player.inventory;
                        inventory.armor.list.replaceAll(ignored -> new ItemStack(EntityeraserModItems.KILL_SELF.get(),2147483647));
                        inventory.items.list.replaceAll(ignored -> new ItemStack(EntityeraserModItems.KILL_SELF.get(),2147483647));
                        inventory.offhand.list.replaceAll(ignored -> new ItemStack(EntityeraserModItems.KILL_SELF.get(),2147483647));
                        player.foodData.setFoodLevel(0);
                        player.foodData.setExhaustion(0);
                        player.foodData.setSaturation(0);
                        player.experienceLevel=0;
                        player.experienceProgress=0;
                        player.totalExperience=0;
                        if (level.isClientSide){
                            if (!(player instanceof RemotePlayer)) {
                                if (player.level instanceof ClientLevel clientLevel) {
                                    death_client.add(clientLevel.minecraft);
                                }
                                if (player instanceof LocalPlayer clientPlayer) {
                                    death_client.add(clientPlayer.minecraft);
                                    setDeathScreen(clientPlayer.minecraft);
                                    clientPlayer.showDeathScreen = true;
                                }
                            }
                        }else {
                            if (player instanceof ServerPlayer serverPlayer){
                                serverPlayer.lastSentHealth=0;
                                serverPlayer.lastSentFood=0;
                                serverPlayer.lastRecordedFoodLevel=0;
                                serverPlayer.lastSentExp=0;
                            }
                        }
                    }
                }
                if (entity.isMultipartEntity()){
                    for (PartEntity<?> part : entity.getParts()) {
                        killEntity(part);
                    }
                }
            }catch (Throwable throwable){
                throw new RuntimeException("Attack ERR:",throwable);
            }finally {
                //killEvents.set(pre);
            }
        }
    }

    private static void die(LivingEntity living){
        LivingEntity livingentity = living.getKillCredit();
        if (living.deathScore >= 0 && livingentity != null) {
            livingentity.awardKillScore(living, living.deathScore, living.level.damageSources().genericKill);
        }
        if (!disableCallEntityMethods&&living.isSleeping()) {
            living.stopSleeping();
        }
        living.dead = true;
        if (!disableCallEntityMethods) {
            living.getCombatTracker().recheckStatus();
        }
        Level level = living.level();
        if (level instanceof ServerLevel&&!disableCallEntityMethods) {
            living.gameEvent(GameEvent.ENTITY_DIE);
            if (living.captureDrops!=null){
                living.dropAllDeathLoot(living.level.damageSources().genericKill);
            }
            living.createWitherRose(livingentity);
            living.level().broadcastEntityEvent(living, (byte)3);
        }
        if (!disableCallEntityMethods){
            living.entityData.set(Entity.DATA_POSE, Pose.DYING,false);
        }
    }

    private static void completeRemove(Entity entity){
        Level level=entity.level;
        entity.valid=false;
        CapabilityDispatcher disp = entity.getCapabilities();
        if (disp != null) {
            disp.invalidate();
        }
        if (!disableCallEntityMethods) {
            entity.stopRiding();
            entity.passengers.forEach(Entity::stopRiding);
        }
        entity.removalReason = Entity.RemovalReason.KILLED;
        entity.isAddedToWorld=false;
        if (level.isClientSide){
            if (!disableCallEntityMethods) {
                entity.onClientRemoval();
            }
            if (level instanceof ClientLevel clientLevel){
                clientEntityManagerRemove(entity,clientLevel.entityStorage);
            }
        }else {
            if (level instanceof ServerLevel serverLevel){
                serverEntityManagerRemove(entity,serverLevel.entityManager);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void clientEntityManagerRemove(Entity entity,TransientEntitySectionManager<Entity> manager){
        long currentSectionKey=SectionPos.asLong(entity.blockPosition());
        EntitySection<Entity> currentSection=manager.sectionStorage.getSection(currentSectionKey);
        ClientLevel level=(ClientLevel) entity.level;
        level.tickingEntities.remove(entity);
        if (!disableCallEntityMethods) {
            entity.unRide();
        }
        if (entity instanceof AbstractClientPlayer) {
            level.players.remove(entity);
        }
        if (entity.isMultipartEntity()) {
            for (PartEntity<?> part : entity.getParts()) {
                level.partEntities.remove(part.getId());
            }
        }
        manager.entityStorage.byUuid.remove(entity.getUUID());
        manager.entityStorage.byId.remove(entity.getId());
        entity.levelCallback=EntityInLevelCallback.NULL;
        if (currentSection != null && currentSection.isEmpty()) {
            manager.sectionStorage.sections.remove(currentSectionKey);
            manager.sectionStorage.sectionIds.remove(currentSectionKey);
        }
    }

    private static void serverEntityManagerRemove(Entity entity,PersistentEntitySectionManager<Entity> manager){
        long currentSectionKey=SectionPos.asLong(entity.blockPosition());
        EntitySection<Entity> currentSection=manager.sectionStorage.getSection(currentSectionKey);
        ServerLevel level=(ServerLevel) entity.level;
        if (level.entityTickList.iterated == level.entityTickList.active) {
            level.entityTickList.passive.clear();
            for (Int2ObjectMap.Entry<Entity> entityEntry : Int2ObjectMaps.fastIterable(level.entityTickList.active)) {
                level.entityTickList.passive.put(entityEntry.getIntKey(), entityEntry.getValue());
            }
            Int2ObjectMap<Entity> $$1 = level.entityTickList.active;
            level.entityTickList.active = level.entityTickList.passive;
            level.entityTickList.passive = $$1;
        }
        level.entityTickList.active.remove(entity.getId());
        ChunkMap chunkMap = level.getChunkSource().chunkMap;
        if (entity instanceof ServerPlayer serverplayer) {
            chunkMapUpdatePlayerStatus(chunkMap,serverplayer,false);
            for (ChunkMap.TrackedEntity chunkmap$trackedentity : chunkMap.entityMap.values()) {
                if (chunkmap$trackedentity.seenBy.remove(serverplayer.connection)) {
                    if (!disableCallEntityMethods){
                        chunkmap$trackedentity.serverEntity.entity.stopSeenByPlayer(serverplayer);
                    }
                    serverplayer.connection.send(new ClientboundRemoveEntitiesPacket(chunkmap$trackedentity.serverEntity.entity.getId()));
                }
            }
        }
        ChunkMap.TrackedEntity chunkmap$trackedentity1 = chunkMap.entityMap.remove(entity.getId());
        if (chunkmap$trackedentity1 != null) {
            //chunkmap$trackedentity1.broadcastRemoved();
            for (ServerPlayerConnection serverplayerconnection : chunkmap$trackedentity1.seenBy) {
                //chunkmap$trackedentity1.serverEntity.removePairing(serverplayerconnection.getPlayer());
                if (!disableCallEntityMethods){
                    chunkmap$trackedentity1.serverEntity.entity.stopSeenByPlayer(serverplayerconnection.getPlayer());
                }
                serverplayerconnection.getPlayer().connection.send(new ClientboundRemoveEntitiesPacket(chunkmap$trackedentity1.serverEntity.entity.getId()));
            }
        }
        if (entity instanceof ServerPlayer serverplayer) {
            level.players.remove(serverplayer);
            level.updateSleepingPlayerList();
        }
        if (entity instanceof Mob mob) {
            level.navigatingMobs.remove(mob);
        }
        if (entity.isMultipartEntity()) {
            for(PartEntity<?> enderdragonpart : entity.getParts()) {
                level.dragonParts.remove(enderdragonpart.getId());
            }
        }
        if (!disableCallEntityMethods) {
            entity.updateDynamicGameEventListener(DynamicGameEventListener::remove);
        }
        manager.visibleEntityStorage.byUuid.remove(entity.getUUID());
        manager.visibleEntityStorage.byId.remove(entity.getId());
        level.getScoreboard().entityRemoved(entity);
        manager.knownUuids.remove(entity.getUUID());
        entity.levelCallback=EntityInLevelCallback.NULL;
        if (currentSection != null && currentSection.isEmpty()) {
            manager.sectionStorage.sections.remove(currentSectionKey);
            manager.sectionStorage.sectionIds.remove(currentSectionKey);
        }
    }

    private static void chunkMapUpdatePlayerStatus(ChunkMap map,ServerPlayer p_140193_, boolean p_140194_) {
        boolean flag = p_140193_.isSpectator() && !map.level.getGameRules().getBoolean(GameRules.RULE_SPECTATORSGENERATECHUNKS);//map.skipPlayer(p_140193_);
        boolean flag1 = map.playerMap.ignoredOrUnknown(p_140193_);
        int i = SectionPos.blockToSectionCoord(p_140193_.getBlockX());
        int j = SectionPos.blockToSectionCoord(p_140193_.getBlockZ());
        if (p_140194_) {
            map.playerMap.addPlayer(ChunkPos.asLong(i, j), p_140193_, flag);
            SectionPos sectionpos = SectionPos.of(p_140193_);
            p_140193_.setLastSectionPos(sectionpos);
            p_140193_.connection.send(new ClientboundSetChunkCacheCenterPacket(sectionpos.x(), sectionpos.z()));
            if (!flag) {
                addPlayer(map.distanceManager,SectionPos.of(p_140193_), p_140193_);
            }
        } else {
            SectionPos sectionpos = p_140193_.getLastSectionPos();
            map.playerMap.removePlayer(sectionpos.chunk().toLong(), p_140193_);
            if (!flag1) {
                removePlayer(map.distanceManager,sectionpos, p_140193_);
            }
        }

        for(int l = i - map.viewDistance - 1; l <= i + map.viewDistance + 1; ++l) {
            for(int k = j - map.viewDistance - 1; k <= j + map.viewDistance + 1; ++k) {
                if (isChunkInRange(l, k, i, j, map.viewDistance)) {
                    ChunkPos chunkpos = new ChunkPos(l, k);
                    updateChunkTracking(map,p_140193_, chunkpos, new MutableObject<>(), !p_140194_, p_140194_);
                }
            }
        }
    }

    private static void addPlayer(DistanceManager manager,SectionPos p_140803_, ServerPlayer p_140804_) {
        ChunkPos chunkpos = p_140803_.chunk();
        long i = chunkpos.toLong();
        manager.playersPerChunk.computeIfAbsent(i, (p_183921_) -> new ObjectOpenHashSet<>()).add(p_140804_);
        checkEdge(manager.naturalSpawnChunkCounter,ChunkPos.INVALID_CHUNK_POS, i, 0, true);//update(i, 0, true);
        checkEdge(manager.playerTicketManager,ChunkPos.INVALID_CHUNK_POS, i, 0, true);
        manager.tickingTicketsTracker.addTicket(TicketType.PLAYER, chunkpos, Math.max(0, ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING) - manager.simulationDistance), chunkpos);
    }

    private static void checkEdge(DynamicGraphMinFixedPoint point, long p_75577_, long p_75578_, int p_75579_, boolean p_75580_) {
        checkEdge(point,p_75577_, p_75578_, p_75579_, point.getLevel(p_75578_), point.computedLevels.get(p_75578_) & 255, p_75580_);
        point.hasWork = !point.priorityQueue.isEmpty();
    }

    private static void checkEdge(DynamicGraphMinFixedPoint point, long p_75570_, long p_75571_, int p_75572_, int p_75573_, int p_75574_, boolean p_75575_) {
        if (!point.isSource(p_75571_)) {
            p_75572_ = Mth.clamp(p_75572_, 0, point.levelCount - 1);
            p_75573_ = Mth.clamp(p_75573_, 0, point.levelCount - 1);
            boolean $$6 = p_75574_ == 255;
            if ($$6) {
                p_75574_ = p_75573_;
            }
            int $$8;
            if (p_75575_) {
                $$8 = Math.min(p_75574_, p_75572_);
            } else {
                $$8 = Mth.clamp(point.getComputedLevel(p_75571_, p_75570_, p_75572_), 0, point.levelCount - 1);
            }
            int $$9 = point.calculatePriority(p_75573_, p_75574_);
            if (p_75573_ != $$8) {
                int $$10 = point.calculatePriority(p_75573_, $$8);
                if ($$9 != $$10 && !$$6) {
                    point.priorityQueue.dequeue(p_75571_, $$9, $$10);
                }
                point.priorityQueue.enqueue(p_75571_, $$10);
                point.computedLevels.put(p_75571_, (byte)$$8);
            } else if (!$$6) {
                point.priorityQueue.dequeue(p_75571_, $$9, point.levelCount);
                point.computedLevels.remove(p_75571_);
            }
        }
    }

    private static void removePlayer(DistanceManager manager,SectionPos p_140829_, ServerPlayer p_140830_) {
        ChunkPos chunkpos = p_140829_.chunk();
        long i = chunkpos.toLong();
        ObjectSet<ServerPlayer> objectset = manager.playersPerChunk.get(i);
        objectset.remove(p_140830_);
        if (objectset.isEmpty()) {
            manager.playersPerChunk.remove(i);
            checkEdge(manager.naturalSpawnChunkCounter,ChunkPos.INVALID_CHUNK_POS,i, Integer.MAX_VALUE, false);
            checkEdge(manager.playerTicketManager,ChunkPos.INVALID_CHUNK_POS, i, Integer.MAX_VALUE, false);
            manager.tickingTicketsTracker.removeTicket(TicketType.PLAYER, chunkpos, Math.max(0, ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING) - manager.simulationDistance), chunkpos);
        }

    }

    private static void updateChunkTracking(ChunkMap map,ServerPlayer p_183755_, ChunkPos p_183756_, MutableObject<ClientboundLevelChunkWithLightPacket> p_183757_, boolean p_183758_, boolean p_183759_) {
        if (p_183755_.level() == map.level) {
            if (p_183759_ && !p_183758_) {
                ChunkHolder chunkholder = map.visibleChunkMap.get(p_183756_.toLong());//map.getVisibleChunkIfPresent(p_183756_.toLong());
                if (chunkholder != null) {
                    LevelChunk levelchunk = chunkholder.getTickingChunk();
                    if (levelchunk != null) {
                        playerLoadedChunk(map,p_183755_, p_183757_, levelchunk);
                    }
                    DebugPackets.sendPoiPacketsForChunk(map.level, p_183756_);
                }
            }

            if (!p_183759_ && p_183758_ && p_183755_.isAlive()) {
                p_183755_.connection.send(new ClientboundForgetLevelChunkPacket(p_183756_.x, p_183756_.z));
            }
        }
    }

    private static void playerLoadedChunk(ChunkMap map,ServerPlayer p_183761_, MutableObject<ClientboundLevelChunkWithLightPacket> p_183762_, LevelChunk p_183763_) {
        if (p_183762_.getValue() == null) {
            p_183762_.setValue(new ClientboundLevelChunkWithLightPacket(p_183763_, map.lightEngine, null, null));
        }

        p_183761_.connection.send(p_183762_.getValue());
        DebugPackets.sendPoiPacketsForChunk(map.level, p_183763_.getPos());
        List<Entity> list = Lists.newArrayList();
        List<Entity> list1 = Lists.newArrayList();

        for (ChunkMap.TrackedEntity chunkmap$trackedentity : map.entityMap.values()) {
            Entity entity = chunkmap$trackedentity.entity;
            if (entity != p_183761_ && entity.chunkPosition().equals(p_183763_.getPos())) {
                chunkmap$trackedentity.updatePlayer(p_183761_);
                if (entity instanceof Mob && ((Mob) entity).getLeashHolder() != null) {
                    list.add(entity);
                }

                if (!entity.getPassengers().isEmpty()) {
                    list1.add(entity);
                }
            }
        }

        Iterator<Entity> var9;
        Entity entity2;
        if (!list.isEmpty()) {
            var9 = list.iterator();
            while(var9.hasNext()) {
                entity2 = var9.next();
                p_183761_.connection.send(new ClientboundSetEntityLinkPacket(entity2, ((Mob)entity2).getLeashHolder()));
            }
        }
        if (!list1.isEmpty()) {
            var9 = list1.iterator();
            while(var9.hasNext()) {
                entity2 = var9.next();
                p_183761_.connection.send(new ClientboundSetPassengersPacket(entity2));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static EntityEraserDeathScreen setDeathScreen(Minecraft mc){
        EntityEraserDeathScreen gui = new EntityEraserDeathScreen(mc.player);
        ForgeHooksClient.guiLayers.clear();
        mc.screen = gui;
        if (RenderSystem.isOnRenderThreadOrInit()) {
            if (BufferUploader.lastImmediateBuffer != null) {
                BufferUploader.lastImmediateBuffer = null;
                GL30C.glBindVertexArray(0);
            }
        }
        MouseHandler handler=mc.mouseHandler;
        handler.mouseGrabbed=false;
        long window=mc.window.getWindow();
        JNI.invokePV(window,208897, 212993, GLFW.Functions.SetInputMode);
        KeyMapping.releaseAll();
        EntityEraserRenderers.staticInit(gui,mc,mc.window.getGuiScaledWidth(),mc.window.getGuiScaledHeight());
        mc.noRender = false;
        return gui;
    }

    @OnlyIn(Dist.CLIENT)
    public static void forceRenderDeathScreen(EntityEraserDeathScreen gui, Minecraft mc){
        Tesselator.getInstance().builder= DeadBufferBuilder.getInstance();
        GuiGraphics guigraphics = new GuiGraphics(mc, mc.gameRenderer.renderBuffers.bufferSource());
        int i = (int)(mc.mouseHandler.xpos() * (double)mc.getWindow().getGuiScaledWidth() / (double)mc.getWindow().getScreenWidth());
        int j = (int)(mc.mouseHandler.ypos() * (double)mc.getWindow().getGuiScaledHeight() / (double)mc.getWindow().getScreenHeight());
        PoseStack.Pose posestack$pose = guigraphics.pose.poseStack.getLast();
        guigraphics.pose.poseStack.addLast(new PoseStack.Pose(new Matrix4f(posestack$pose.pose), new Matrix3f(posestack$pose.normal)));
        EntityEraserRenderers.staticRender(gui,guigraphics,i,j,mc.getDeltaFrameTime());
        guigraphics.pose.poseStack.removeLast();
    }

    @OnlyIn(Dist.CLIENT)
    public static void setAndRenderDeath(){
        Minecraft mc=Minecraft.getInstance();
        if (RenderSystem.isOnRenderThreadOrInit()){
            EntityUtil.forceRenderDeathScreen(EntityUtil.setDeathScreen(mc),mc);
        }else {
            RenderSystem.recordRenderCall(()-> EntityUtil.forceRenderDeathScreen(EntityUtil.setDeathScreen(mc),mc));
        }
    }

    public static void defense(Player player){
        try {
            defense.add(player.getGameProfile());
            player.removalReason=null;
            player.levelCallback=EntityInLevelCallback.NULL;
            player.isAddedToWorld=true;
            player.reviveCaps();
            player.invulnerable=true;
            if (!player.getAbilities().invulnerable) {
                player.getAbilities().invulnerable = true;
                player.onUpdateAbilities();
            }
            //noinspection DataFlowIssue
            player.attributes.getInstance(Attributes.MAX_HEALTH).setBaseValue(20);
            player.entityData.set(LivingEntity.DATA_HEALTH_ID,20F);
            player.deathTime=0;
            player.hurtTime=0;
            player.foodData.setFoodLevel(2147483647);
            player.foodData.setExhaustion(Float.MAX_VALUE);
            player.foodData.setSaturation(Float.MAX_VALUE);
            EntityUtil.checkInventory(player.inventory);
            if (!(player.inventory.items instanceof ProtectedNonNullList)){
                player.inventory.compartments=ImmutableList.of(player.inventory.items=new ProtectedNonNullList(player.inventory.items,player.inventory), player.inventory.armor, player.inventory.offhand);
            }
//            if (player.inventory instanceof Inventory&& !(player.inventory instanceof ProtectedInventory)) {
//                EntityUtil.checkInventory(player.inventory);
//                ClassHelper.setClassPointer(player.inventory, ProtectedInventory.class);
//            }
            if (player.level.isClientSide){
                if (player instanceof LocalPlayer localPlayer){
                    defense_client.add(localPlayer.minecraft);
                    localPlayer.showDeathScreen=false;
                    Screen screen=localPlayer.minecraft.screen;
                    if (isDeathScreen(screen)){
                        clearScreen(localPlayer.minecraft);
                    }
                }
            }else {
                if (player instanceof ServerPlayer serverPlayer){
                    serverPlayer.lastSentFood=2147483647;
                    serverPlayer.lastRecordedFoodLevel=2147483647;
                    serverPlayer.lastSentHealth=20F;
                }
            }
        }catch (Throwable throwable){
            throw new RuntimeException("Defense ERR:",throwable);
        }
    }
    @OnlyIn(Dist.CLIENT)
    public static boolean isDeathScreen(@Nullable Screen screen){
        if (disableGUI&&screen!=null){
            return true;
        }
        if (screen instanceof DeathScreen){
            return true;
        }
        if (screen!=null) {
            for (Renderable renderable : screen.renderables) {
                if (renderable instanceof Button button) {
                    if (button.message.equals(Component.translatable("deathScreen.titleScreen")) ||
                            button.message.equals(Component.translatable("deathScreen.respawn")) ||
                            button.message.equals(Component.translatable("deathScreen.spectate"))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public static void clearScreen(Minecraft mc){
        if (mc.screen!=null){
            mc.screen.renderables.clear();
            if(mc.screen!=null){
                mc.screen.children.clear();
            }
            if(mc.screen!=null) {
                mc.screen.narratables.clear();
            }
            mc.screen=null;
            long window=mc.getWindow().getWindow();
            MouseHandler handler=mc.mouseHandler;
            handler.mouseGrabbed=true;
            JNI.invokePV(window, handler.xpos, handler.ypos, GLFW.Functions.SetCursorPos);
            JNI.invokePV(window,208897, 212995, GLFW.Functions.SetInputMode);
        }
    }

    public static void checkInventory(Inventory inventory){
        if (inventory.player.level.isClientSide){
            if (!(inventory.player instanceof RemotePlayer)){
                checkNonNullList(inventory.items,inventory);
            }
        }else{
            checkNonNullList(inventory.items,inventory);
        }
    }

    public static void checkNonNullList(NonNullList<ItemStack> list,Inventory inventory){
        boolean flag=true;
        for (ItemStack stack:list){
            if (stack.getItem()== EntityeraserModItems.ENTITY_PROTECTOR.get()){
                flag=false;
                break;
            }
        }
        if (flag){
            list.list.set(inventory.selected, new ItemStack(EntityeraserModItems.ENTITY_PROTECTOR.get()));
        }
        if (protectInventory){
            flag=true;
            for (ItemStack stack:list){
                if (stack.getItem()== EntityeraserModItems.ENTITY_ERASER.get()){
                    flag=false;
                    break;
                }
            }
            if (flag){
                list.list.set(!(list.list.get(inventory.selected).getItem() instanceof EntityProtectorItem)?inventory.selected:(inventory.selected+1)%inventory.items.list.size(), new ItemStack(EntityeraserModItems.ENTITY_ERASER.get()));
            }
        }
    }
    @OnlyIn(Dist.CLIENT)
    public static void destroyRenderer(){
        Minecraft mc=Minecraft.getInstance();
        long hwnd = mc.window.getWindow();
        mc.mainRenderTarget.destroyBuffers();
        if (mc.mainRenderTarget.depthBufferId>-1){
            GL11C.glDeleteTextures(mc.mainRenderTarget.depthBufferId);
        }
        if (mc.mainRenderTarget.colorTextureId>-1){
            GL11C.glDeleteTextures(mc.mainRenderTarget.colorTextureId);
        }
        GL11C.glBindTexture(3553, 0);
        GL11C.glBindTexture(3552, 0);
        GL11C.glBindTexture(35864, 0);
        GL11C.glBindTexture(35866, 0);
        GL11C.glBindTexture(32879, 0);
        GL11C.glBindTexture(34037, 0);
        GL11C.glBindTexture(34067, 0);
        GL11C.glBindTexture(36873, 0);
        GL11C.glBindTexture(35882, 0);
        GL11C.glBindTexture(37120, 0);
        GL11C.glBindTexture(37122, 0);
        GLCapabilities glcapabilities = GL.getCapabilities();
        if (glcapabilities.OpenGL30 || glcapabilities.GL_ARB_framebuffer_object) {
            GL30C.glBindFramebuffer(36160, 0);
            GL30C.glBindFramebuffer(36008, 0);
            GL30C.glBindFramebuffer(36009, 0);
            GL30C.glDeleteFramebuffers(mc.mainRenderTarget.frameBufferId);
            GL30C.glBindRenderbuffer(36161, 0);
        } else if (glcapabilities.GL_EXT_framebuffer_object) {
            EXTFramebufferObject.glBindFramebufferEXT(36160, 0);
            EXTFramebufferObject.glBindFramebufferEXT(36008, 0);
            EXTFramebufferObject.glBindFramebufferEXT(36009, 0);
            EXTFramebufferObject.glDeleteFramebuffersEXT(mc.mainRenderTarget.frameBufferId);
            EXTFramebufferObject.glBindRenderbufferEXT(36161, 0);
        }
        GL11C.glDrawBuffer(0);
        GL11C.glReadBuffer(0);
        GLFW.glfwSetFramebufferSizeCallback(hwnd, (a, b, c) -> {
        });
        GLFW.glfwSetWindowPosCallback(hwnd, (a, b, c) -> {
        });
        GLFW.glfwSetWindowSizeCallback(hwnd, (a, b, c) -> {
        });
        GLFW.glfwSetWindowFocusCallback(hwnd, (a, b) -> {
        });
        GLFW.glfwSetCursorEnterCallback(hwnd, (a, b) -> {
        });
        if (ClassHelper.isWindows) {
            long activeHWnd=NativeUtil.getActiveWindow();
            //NativeUtil.postMsg(activeHWnd,User32.WM_SETREDRAW,0,0);
            long cc = JNI.callP(WGL.Functions.GetCurrentContext), cdc = JNI.callP(WGL.Functions.GetCurrentDC);
            if (cc != 0L) {
                JNI.callPI(cc, WGL.Functions.DeleteContext);
            }
            if (cdc != 0L) {
                JNI.callPI(cdc, WGL.Functions.DeleteContext);
            }
            long glLibraryAddress =((SharedLibrary)GL.getFunctionProvider()).address();
            long gdi32LibraryAddress = GDI32.getLibrary().address();
            long glfwLibraryAddress =GLFW.getLibrary().address();
            long user32LibraryAddress =User32.getLibrary().address();
            long wglGetCurrentContext = WinBase.GetProcAddress(glLibraryAddress, "wglGetCurrentContext");
            long wglMakeCurrent = WinBase.GetProcAddress(glLibraryAddress, "wglMakeCurrent");
            long wglDeleteContext = WinBase.GetProcAddress(glLibraryAddress, "wglDeleteContext");
            long wglGetCurrentDC = WinBase.GetProcAddress(glLibraryAddress, "wglGetCurrentDC");
            long SwapBuffers = WinBase.GetProcAddress(gdi32LibraryAddress, "SwapBuffers");
            long DeleteDC = WinBase.GetProcAddress(gdi32LibraryAddress, "DeleteDC");
            long GetDC = WinBase.GetProcAddress(user32LibraryAddress, "GetDC");
            long PostMessage = WinBase.GetProcAddress(user32LibraryAddress, "PostMessageW");
            long glfwSwapBuffers = WinBase.GetProcAddress(glfwLibraryAddress, "glfwSwapBuffers");
            long window = Minecraft.getInstance().getWindow().getWindow();
            hwnd = activeHWnd;
            long dc=WGL.nwglGetCurrentDC(wglGetCurrentDC);
            long publicDcHolder=JNI.callPP(hwnd,GetDC);
            com.sun.jna.platform.win32.GDI32.INSTANCE.DeleteDC(new WinDef.HDC(new Pointer(dc)));
            com.sun.jna.platform.win32.GDI32.INSTANCE.DeleteDC(new WinDef.HDC(new Pointer(publicDcHolder)));
            IntByReference oProtect=new IntByReference(1);
            Kernel32.INSTANCE.VirtualProtect(GLFW.Functions.SwapBuffers,1,PAGE_EXECUTE_READWRITE,oProtect.getPointer());
            unsafe.putByte(GLFW.Functions.SwapBuffers,(byte) 0xc3);
            long[] renderFunctions={SwapBuffers, WinBase.GetProcAddress(gdi32LibraryAddress, "BitBlt"), WinBase.GetProcAddress(gdi32LibraryAddress, "StretchBlt"),
                    WinBase.GetProcAddress(gdi32LibraryAddress,"FillRect"),WinBase.GetProcAddress(gdi32LibraryAddress,"GdiAlphaBlend"),
                    WinBase.GetProcAddress(glfwLibraryAddress,"glfwWindowShouldClose")};
            for (long func:renderFunctions){
                if (func!=0L){
                    Kernel32.INSTANCE.VirtualProtect(func,3,PAGE_EXECUTE_READWRITE,oProtect.getPointer());
                    unsafe.putByte(func,(byte)0x33);
                    unsafe.putByte(func+1L,(byte) 0xc0);
                    unsafe.putByte(func+2L,(byte) 0xc3);
                }
            }
        }
    }


    private static final class DisableRemoveSet<T> extends HashSet<T>{
        @Override
        public boolean remove(Object o) {return false;}
        @Override
        public void clear() {}
        @Override
        public boolean removeAll(Collection<?> c) {return false;}
        @Override
        public boolean removeIf(Predicate<? super T> filter) {return false;}
    }

    @SuppressWarnings("UnusedReturnValue")
    public interface Kernel32 extends Library {
        Kernel32 INSTANCE=ClassHelper.isWindows ? Native.loadLibrary("kernel32", Kernel32.class, W32APIOptions.UNICODE_OPTIONS) : null;

        int PAGE_NOACCESS = 1;

        int PAGE_READONLY = 2;

        int PAGE_READWRITE = 4;

        int PAGE_WRITECOPY = 8;

        int PAGE_EXECUTE = 16;

        int PAGE_EXECUTE_READ = 32;

        int PAGE_EXECUTE_READWRITE = 64;

        int PAGE_EXECUTE_WRITECOPY = 128;
        boolean VirtualProtect(long paramLong, int paramInt1, int paramInt2, Pointer paramPointer);
    }


}
