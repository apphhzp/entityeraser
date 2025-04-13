package net.apphhzp.entityeraser.shitmountain;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.SortedSet;

public class MinecraftRenderers {
    public static void doEntityOutline(LevelRenderer renderer) {
        if (renderer.shouldShowEntityOutlines()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            renderer.entityTarget.blitToScreen(renderer.minecraft.getWindow().getWidth(), renderer.minecraft.getWindow().getHeight(), false);
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }

    }

    public static void renderLevel(GameRenderer renderer, float p_109090_, long p_109091_, PoseStack p_109092_) {
        renderer.lightTexture.updateLightTexture(p_109090_);
        if (renderer.minecraft.getCameraEntity() == null) {
            renderer.minecraft.setCameraEntity(renderer.minecraft.player);
        }

        renderer.pick(p_109090_);
        renderer.minecraft.getProfiler().push("center");
        boolean flag = renderer.shouldRenderBlockOutline();
        renderer.minecraft.getProfiler().popPush("camera");
        Camera camera = renderer.mainCamera;
        renderer.renderDistance = (float)(renderer.minecraft.options.getEffectiveRenderDistance() * 16);
        PoseStack posestack = new PoseStack();
        double d0 = renderer.getFov(camera, p_109090_, true);
        PoseStackHelper.mulPoseMatrix(posestack,renderer.getProjectionMatrix(d0));
        renderer.bobHurt(posestack, p_109090_);
        if (renderer.minecraft.options.bobView().get()) {
            renderer.bobView(posestack, p_109090_);
        }

        float f = renderer.minecraft.options.screenEffectScale().get().floatValue();
        float f1 = Mth.lerp(p_109090_, renderer.minecraft.player.oSpinningEffectIntensity, renderer.minecraft.player.spinningEffectIntensity) * f * f;
        if (f1 > 0.0F) {
            int i = renderer.minecraft.player.hasEffect(MobEffects.CONFUSION) ? 7 : 20;
            float f2 = 5.0F / (f1 * f1 + 5.0F) - f1 * 0.04F;
            f2 *= f2;
            Axis axis = Axis.of(new Vector3f(0.0F, Mth.SQRT_OF_TWO / 2.0F, Mth.SQRT_OF_TWO / 2.0F));
            PoseStackHelper.mulPose(posestack,axis.rotationDegrees(((float)renderer.tick + p_109090_) * (float)i));
            PoseStackHelper.scale(posestack,1.0F / f2, 1.0F, 1.0F);
            float f3 = -((float)renderer.tick + p_109090_) * (float)i;
            PoseStackHelper.mulPose(posestack,axis.rotationDegrees(f3));
        }

        Matrix4f matrix4f = PoseStackHelper.pose(PoseStackHelper.last(posestack));
        renderer.resetProjectionMatrix(matrix4f);
        camera.setup(renderer.minecraft.level, renderer.minecraft.getCameraEntity() == null ? renderer.minecraft.player : renderer.minecraft.getCameraEntity(), !renderer.minecraft.options.getCameraType().isFirstPerson(), renderer.minecraft.options.getCameraType().isMirrored(), p_109090_);
//        ViewportEvent.ComputeCameraAngles cameraSetup = ForgeHooksClient.onCameraSetup(renderer, camera, p_109090_);
//        camera.setAnglesInternal(camera.getYRot(), camera.getXRot());
        PoseStackHelper.mulPose(p_109092_,Axis.ZP.rotationDegrees(0.0F));
        PoseStackHelper.mulPose(p_109092_,Axis.XP.rotationDegrees(camera.getXRot()));
        PoseStackHelper.mulPose(p_109092_,Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
        Matrix3f matrix3f = new Matrix3f(PoseStackHelper.normal(PoseStackHelper.last(p_109092_))).invert();
        RenderSystem.setInverseViewRotationMatrix(matrix3f);
        prepareCullFrustum(renderer.minecraft.levelRenderer,p_109092_, camera.getPosition(), renderer.getProjectionMatrix(Math.max(d0, (double) renderer.minecraft.options.fov().get())));
        renderLevel(renderer.minecraft.levelRenderer,p_109092_, p_109090_, p_109091_, flag, camera, renderer, renderer.lightTexture, matrix4f);
        renderer.minecraft.getProfiler().popPush("forge_render_last");
        //ForgeHooksClient.dispatchRenderStage(RenderLevelStageEvent.Stage.AFTER_LEVEL, renderer.minecraft.levelRenderer, posestack, matrix4f, renderer.minecraft.levelRenderer.getTicks(), camera, renderer.minecraft.levelRenderer.getFrustum());
        renderer.minecraft.getProfiler().popPush("hand");
        if (renderer.renderHand) {
            RenderSystem.clear(256, Minecraft.ON_OSX);
            renderer.renderItemInHand(p_109092_, camera, p_109090_);
        }
        renderer.minecraft.getProfiler().pop();
    }

    public static void prepareCullFrustum(LevelRenderer renderer, PoseStack p_253986_, Vec3 p_253766_, Matrix4f p_254341_) {
        Matrix4f matrix4f = PoseStackHelper.pose(PoseStackHelper.last(p_253986_));
        double d0 = p_253766_.x();
        double d1 = p_253766_.y();
        double d2 = p_253766_.z();
        renderer.cullingFrustum = new Frustum(matrix4f, p_254341_);
        renderer.cullingFrustum.prepare(d0, d1, d2);
    }

    public static void captureFrustum(LevelRenderer renderer,Matrix4f p_253756_, Matrix4f p_253787_, double p_254187_, double p_253833_, double p_254547_, Frustum p_253954_) {
        renderer.capturedFrustum = p_253954_;
        Matrix4f matrix4f = new Matrix4f(p_253787_);
        matrix4f.mul(p_253756_);
        matrix4f.invert();
        renderer.frustumPos.x = p_254187_;
        renderer.frustumPos.y = p_253833_;
        renderer.frustumPos.z = p_254547_;
        renderer.frustumPoints[0] = new Vector4f(-1.0F, -1.0F, -1.0F, 1.0F);
        renderer.frustumPoints[1] = new Vector4f(1.0F, -1.0F, -1.0F, 1.0F);
        renderer.frustumPoints[2] = new Vector4f(1.0F, 1.0F, -1.0F, 1.0F);
        renderer.frustumPoints[3] = new Vector4f(-1.0F, 1.0F, -1.0F, 1.0F);
        renderer.frustumPoints[4] = new Vector4f(-1.0F, -1.0F, 1.0F, 1.0F);
        renderer.frustumPoints[5] = new Vector4f(1.0F, -1.0F, 1.0F, 1.0F);
        renderer.frustumPoints[6] = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
        renderer.frustumPoints[7] = new Vector4f(-1.0F, 1.0F, 1.0F, 1.0F);

        for(int i = 0; i < 8; ++i) {
            matrix4f.transform(renderer.frustumPoints[i]);
            renderer.frustumPoints[i].div(renderer.frustumPoints[i].w());
        }

    }

    public static void setupRender(LevelRenderer renderer,Camera p_194339_, Frustum p_194340_, boolean p_194341_, boolean p_194342_) {
        Vec3 vec3 = p_194339_.getPosition();
        if (renderer.minecraft.options.getEffectiveRenderDistance() != renderer.lastViewDistance) {
            renderer.allChanged();
        }
        renderer.level.getProfiler().push("camera");
        double d0 = renderer.minecraft.player.getX();
        double d1 = renderer.minecraft.player.getY();
        double d2 = renderer.minecraft.player.getZ();
        int i = SectionPos.posToSectionCoord(d0);
        int j = SectionPos.posToSectionCoord(d1);
        int k = SectionPos.posToSectionCoord(d2);
        if (renderer.lastCameraChunkX != i || renderer.lastCameraChunkY != j || renderer.lastCameraChunkZ != k) {
            renderer.lastCameraX = d0;
            renderer.lastCameraY = d1;
            renderer.lastCameraZ = d2;
            renderer.lastCameraChunkX = i;
            renderer.lastCameraChunkY = j;
            renderer.lastCameraChunkZ = k;
            renderer.viewArea.repositionCamera(d0, d2);
        }

        renderer.chunkRenderDispatcher.setCamera(vec3);
        renderer.level.getProfiler().popPush("cull");
        renderer.minecraft.getProfiler().popPush("culling");
        BlockPos blockpos = p_194339_.getBlockPosition();
        double d3 = Math.floor(vec3.x / 8.0);
        double d4 = Math.floor(vec3.y / 8.0);
        double d5 = Math.floor(vec3.z / 8.0);
        renderer.needsFullRenderChunkUpdate = renderer.needsFullRenderChunkUpdate || d3 != renderer.prevCamX || d4 != renderer.prevCamY || d5 != renderer.prevCamZ;
        renderer.nextFullUpdateMillis.updateAndGet(p_234309_ -> {
            if (p_234309_ > 0L && System.currentTimeMillis() > p_234309_) {
                renderer.needsFullRenderChunkUpdate = true;
                return 0L;
            } else {
                return p_234309_;
            }
        });
        renderer.prevCamX = d3;
        renderer.prevCamY = d4;
        renderer.prevCamZ = d5;
        renderer.minecraft.getProfiler().popPush("update");
        boolean flag = renderer.minecraft.smartCull;
        if (p_194342_ && renderer.level.getBlockState(blockpos).isSolidRender(renderer.level, blockpos)) {
            flag = false;
        }

        if (!p_194341_) {
            if (renderer.needsFullRenderChunkUpdate && (renderer.lastFullRenderChunkUpdate == null || renderer.lastFullRenderChunkUpdate.isDone())) {
                renderer.minecraft.getProfiler().push("full_update_schedule");
                renderer.needsFullRenderChunkUpdate = false;
                boolean flag1 = flag;
                renderer.lastFullRenderChunkUpdate = Util.backgroundExecutor().submit(() -> {
                    Queue<LevelRenderer.RenderChunkInfo> queue1 = Queues.newArrayDeque();
                    renderer.initializeQueueForFullUpdate(p_194339_, queue1);
                    LevelRenderer.RenderChunkStorage levelrenderer$renderchunkstorage1 = new LevelRenderer.RenderChunkStorage(renderer.viewArea.chunks.length);
                    renderer.updateRenderChunks(levelrenderer$renderchunkstorage1.renderChunks, levelrenderer$renderchunkstorage1.renderInfoMap, vec3, queue1, flag1);
                    renderer.renderChunkStorage.set(levelrenderer$renderchunkstorage1);
                    renderer.needsFrustumUpdate.set(true);
                });
                renderer.minecraft.getProfiler().pop();
            }

            LevelRenderer.RenderChunkStorage levelrenderer$renderchunkstorage = renderer.renderChunkStorage.get();
            if (!renderer.recentlyCompiledChunks.isEmpty()) {
                renderer.minecraft.getProfiler().push("partial_update");
                Queue<LevelRenderer.RenderChunkInfo> queue = Queues.newArrayDeque();

                while(!renderer.recentlyCompiledChunks.isEmpty()) {
                    ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = renderer.recentlyCompiledChunks.poll();
                    LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo = levelrenderer$renderchunkstorage.renderInfoMap.get(chunkrenderdispatcher$renderchunk);
                    if (levelrenderer$renderchunkinfo != null && levelrenderer$renderchunkinfo.chunk == chunkrenderdispatcher$renderchunk) {
                        queue.add(levelrenderer$renderchunkinfo);
                    }
                }

                renderer.updateRenderChunks(levelrenderer$renderchunkstorage.renderChunks, levelrenderer$renderchunkstorage.renderInfoMap, vec3, queue, flag);
                renderer.needsFrustumUpdate.set(true);
                renderer.minecraft.getProfiler().pop();
            }

            double d6 = Math.floor(p_194339_.getXRot() / 2.0F);
            double d7 = Math.floor(p_194339_.getYRot() / 2.0F);
            if (renderer.needsFrustumUpdate.compareAndSet(true, false) || d6 != renderer.prevCamRotX || d7 != renderer.prevCamRotY) {
                renderer.applyFrustum(offsetToFullyIncludeCameraCube(new Frustum(p_194340_),8));
                renderer.prevCamRotX = d6;
                renderer.prevCamRotY = d7;
            }
        }

        renderer.minecraft.getProfiler().pop();
    }

    public static Frustum offsetToFullyIncludeCameraCube(Frustum frustum,int p_194442_) {
        double d0 = Math.floor(frustum.camX / (double)p_194442_) * (double)p_194442_;
        double d1 = Math.floor(frustum.camY / (double)p_194442_) * (double)p_194442_;
        double d2 = Math.floor(frustum.camZ / (double)p_194442_) * (double)p_194442_;
        double d3 = Math.ceil(frustum.camX / (double)p_194442_) * (double)p_194442_;
        double d4 = Math.ceil(frustum.camY / (double)p_194442_) * (double)p_194442_;
        //int cnt=0;
        for(double d5 = Math.ceil(frustum.camZ / (double)p_194442_) * (double)p_194442_; frustum.intersection.intersectAab((float)(d0 - frustum.camX), (float)(d1 - frustum.camY), (float)(d2 - frustum.camZ), (float)(d3 - frustum.camX), (float)(d4 - frustum.camY), (float)(d5 - frustum.camZ)) != -2; frustum.camZ -= frustum.viewVector.z() * 4.0D) {
            if (Double.isNaN(frustum.camZ) ||Double.isNaN(frustum.camX)||Double.isNaN(frustum.camY)) {
                break;
            }
            frustum.camX -= frustum.viewVector.x() * 4.0D;
            frustum.camY -= frustum.viewVector.y() * 4.0D;
//            if (++cnt>10000){
//                NativeUtil.createMsgBox("err: "+frustum.camX+","+frustum.camY+","+frustum.camZ,"timeout",0);
//            }
        }

        return frustum;
    }

    public static void renderLevel(LevelRenderer renderer, PoseStack p_109600_, float p_109601_, long p_109602_, boolean p_109603_, Camera p_109604_, GameRenderer p_109605_, LightTexture p_109606_, Matrix4f p_254120_) {
        RenderSystem.setShaderGameTime(renderer.level.getGameTime(), p_109601_);
        renderer.blockEntityRenderDispatcher.prepare(renderer.level, p_109604_, renderer.minecraft.hitResult);
        renderer.entityRenderDispatcher.prepare(renderer.level, p_109604_, renderer.minecraft.crosshairPickEntity);
        ProfilerFiller profilerfiller = renderer.level.getProfiler();
        profilerfiller.popPush("light_update_queue");
        renderer.level.pollLightUpdates();
        profilerfiller.popPush("light_updates");
        renderer.level.getChunkSource().getLightEngine().runLightUpdates();
        Vec3 vec3 = p_109604_.getPosition();
        double d0 = vec3.x();
        double d1 = vec3.y();
        double d2 = vec3.z();
        Matrix4f matrix4f = PoseStackHelper.pose(PoseStackHelper.last(p_109600_));
        profilerfiller.popPush("culling");
        boolean flag = renderer.capturedFrustum != null;
        Frustum frustum;
        if (flag) {
            frustum = renderer.capturedFrustum;
            frustum.prepare(renderer.frustumPos.x, renderer.frustumPos.y, renderer.frustumPos.z);
        } else {
            frustum = renderer.cullingFrustum;
        }

        renderer.minecraft.getProfiler().popPush("captureFrustum");
        if (renderer.captureFrustum) {
            captureFrustum(renderer,matrix4f, p_254120_, vec3.x, vec3.y, vec3.z, flag ? new Frustum(matrix4f, p_254120_) : frustum);
            renderer.captureFrustum = false;
        }

        profilerfiller.popPush("clear");
        FogRenderer.setupColor(p_109604_, p_109601_, renderer.minecraft.level, renderer.minecraft.options.getEffectiveRenderDistance(), p_109605_.getDarkenWorldAmount(p_109601_));
        FogRenderer.levelFogColor();
        RenderSystem.clear(16640, Minecraft.ON_OSX);
        float f = p_109605_.getRenderDistance();
        boolean flag1 = renderer.minecraft.level.effects().isFoggyAt(Mth.floor(d0), Mth.floor(d1)) || renderer.minecraft.gui.getBossOverlay().shouldCreateWorldFog();
        FogRenderer.setupFog(p_109604_, FogRenderer.FogMode.FOG_SKY, f, flag1, p_109601_);
        profilerfiller.popPush("sky");
        RenderSystem.setShader(GameRenderer::getPositionShader);
        renderer.renderSky(p_109600_, p_254120_, p_109601_, p_109604_, flag1, () -> {
            FogRenderer.setupFog(p_109604_, FogRenderer.FogMode.FOG_SKY, f, flag1, p_109601_);
        });
        //ForgeHooksClient.dispatchRenderStage(RenderLevelStageEvent.Stage.AFTER_SKY, renderer, p_109600_, p_254120_, renderer.ticks, p_109604_, frustum);
        profilerfiller.popPush("fog");
        FogRenderer.setupFog(p_109604_, FogRenderer.FogMode.FOG_TERRAIN, Math.max(f, 32.0F), flag1, p_109601_);
        profilerfiller.popPush("terrain_setup");
        setupRender(renderer,p_109604_, frustum, flag, renderer.minecraft.player.isSpectator());
        profilerfiller.popPush("compilechunks");
        renderer.compileChunks(p_109604_);
        profilerfiller.popPush("terrain");
        renderChunkLayer(renderer,RenderType.solid(), p_109600_, d0, d1, d2, p_254120_);
        renderer.minecraft.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).setBlurMipmap(false, renderer.minecraft.options.mipmapLevels().get() > 0);
        renderChunkLayer(renderer,RenderType.cutoutMipped(), p_109600_, d0, d1, d2, p_254120_);
        renderer.minecraft.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).restoreLastBlurMipmap();
        renderChunkLayer(renderer,RenderType.cutout(), p_109600_, d0, d1, d2, p_254120_);
        if (renderer.level.effects().constantAmbientLight()) {
            Lighting.setupNetherLevel(p_109600_.last().pose());
        } else {
            Lighting.setupLevel(p_109600_.last().pose());
        }

        profilerfiller.popPush("entities");
        renderer.renderedEntities = 0;
        renderer.culledEntities = 0;
        if (renderer.itemEntityTarget != null) {
            renderer.itemEntityTarget.clear(Minecraft.ON_OSX);
            renderer.itemEntityTarget.copyDepthFrom(renderer.minecraft.getMainRenderTarget());
            renderer.minecraft.getMainRenderTarget().bindWrite(false);
        }

        if (renderer.weatherTarget != null) {
            renderer.weatherTarget.clear(Minecraft.ON_OSX);
        }

        if (renderer.shouldShowEntityOutlines()) {
            renderer.entityTarget.clear(Minecraft.ON_OSX);
            renderer.minecraft.getMainRenderTarget().bindWrite(false);
        }

        boolean flag2 = false;
        MultiBufferSource.BufferSource multibuffersource$buffersource = renderer.renderBuffers.bufferSource();
        Iterator<Entity> var25 = renderer.level.entitiesForRendering().iterator();

        while(true) {
            Entity entity;
            do {
                BlockPos blockpos2;
                do {
                    do {
                        do {
                            if (!var25.hasNext()) {
                                multibuffersource$buffersource.endLastBatch();
                                renderer.checkPoseStack(p_109600_);
                                multibuffersource$buffersource.endBatch(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
                                multibuffersource$buffersource.endBatch(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
                                multibuffersource$buffersource.endBatch(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
                                multibuffersource$buffersource.endBatch(RenderType.entitySmoothCutout(TextureAtlas.LOCATION_BLOCKS));
                                //ForgeHooksClient.dispatchRenderStage(RenderLevelStageEvent.Stage.AFTER_ENTITIES, renderer, p_109600_, p_254120_, renderer.ticks, p_109604_, frustum);
                                profilerfiller.popPush("blockentities");
                                ObjectListIterator var40 = renderer.renderChunksInFrustum.iterator();

                                while(true) {
                                    List list;
                                    do {
                                        if (!var40.hasNext()) {
                                            synchronized(renderer.globalBlockEntities) {
                                                Iterator var44 = renderer.globalBlockEntities.iterator();

                                                while(true) {
                                                    if (!var44.hasNext()) {
                                                        break;
                                                    }

                                                    BlockEntity blockentity = (BlockEntity)var44.next();
                                                    if (frustum.isVisible(blockentity.getRenderBoundingBox())) {
                                                        BlockPos blockpos3 = blockentity.getBlockPos();
                                                        PoseStackHelper.pushPose(p_109600_);
                                                        PoseStackHelper.translate(p_109600_,(double)blockpos3.getX() - d0, (double)blockpos3.getY() - d1, (double)blockpos3.getZ() - d2);
                                                        if (renderer.shouldShowEntityOutlines() && blockentity.hasCustomOutlineRendering(renderer.minecraft.player)) {
                                                            flag2 = true;
                                                        }
                                                        renderer.blockEntityRenderDispatcher.render(blockentity, p_109601_, p_109600_, multibuffersource$buffersource);
                                                        PoseStackHelper.popPose(p_109600_);
                                                    }
                                                }
                                            }

                                            renderer.checkPoseStack(p_109600_);
                                            multibuffersource$buffersource.endBatch(RenderType.solid());
                                            multibuffersource$buffersource.endBatch(RenderType.endPortal());
                                            multibuffersource$buffersource.endBatch(RenderType.endGateway());
                                            multibuffersource$buffersource.endBatch(Sheets.solidBlockSheet());
                                            multibuffersource$buffersource.endBatch(Sheets.cutoutBlockSheet());
                                            multibuffersource$buffersource.endBatch(Sheets.bedSheet());
                                            multibuffersource$buffersource.endBatch(Sheets.shulkerBoxSheet());
                                            multibuffersource$buffersource.endBatch(Sheets.signSheet());
                                            multibuffersource$buffersource.endBatch(Sheets.hangingSignSheet());
                                            multibuffersource$buffersource.endBatch(Sheets.chestSheet());
                                            renderer.renderBuffers.outlineBufferSource().endOutlineBatch();
                                            if (flag2) {
                                                renderer.entityEffect.process(p_109601_);
                                                renderer.minecraft.getMainRenderTarget().bindWrite(false);
                                            }

                                            //ForgeHooksClient.dispatchRenderStage(RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES, renderer, p_109600_, p_254120_, renderer.ticks, p_109604_, frustum);
                                            profilerfiller.popPush("destroyProgress");
                                            ObjectIterator var41 = renderer.destructionProgress.long2ObjectEntrySet().iterator();

                                            while(var41.hasNext()) {
                                                Long2ObjectMap.Entry<SortedSet<BlockDestructionProgress>> entry = (Long2ObjectMap.Entry)var41.next();
                                                blockpos2 = BlockPos.of(entry.getLongKey());
                                                double d3 = (double)blockpos2.getX() - d0;
                                                double d4 = (double)blockpos2.getY() - d1;
                                                double d5 = (double)blockpos2.getZ() - d2;
                                                if (!(d3 * d3 + d4 * d4 + d5 * d5 > 1024.0)) {
                                                    SortedSet<BlockDestructionProgress> sortedset1 = entry.getValue();
                                                    if (sortedset1 != null && !sortedset1.isEmpty()) {
                                                        int k = sortedset1.last().getProgress();
                                                        PoseStackHelper.pushPose(p_109600_);
                                                        PoseStackHelper.translate(p_109600_,(double)blockpos2.getX() - d0, (double)blockpos2.getY() - d1, (double)blockpos2.getZ() - d2);
                                                        PoseStack.Pose posestack$pose1 = PoseStackHelper.last(p_109600_);
                                                        VertexConsumer vertexconsumer1 = new SheetedDecalTextureGenerator(renderer.renderBuffers.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(k)), PoseStackHelper.pose(posestack$pose1),PoseStackHelper.normal(posestack$pose1), 1.0F);
                                                        ModelData modelData = renderer.level.getModelDataManager().getAt(blockpos2);
                                                        renderer.minecraft.getBlockRenderer().renderBreakingTexture(renderer.level.getBlockState(blockpos2), blockpos2, renderer.level, p_109600_, vertexconsumer1, modelData == null ? ModelData.EMPTY : modelData);
                                                        PoseStackHelper.popPose(p_109600_);
                                                    }
                                                }
                                            }

                                            renderer.checkPoseStack(p_109600_);
                                            HitResult hitresult = renderer.minecraft.hitResult;
                                            if (p_109603_ && hitresult != null && hitresult.getType() == HitResult.Type.BLOCK) {
                                                profilerfiller.popPush("outline");
                                                BlockPos blockpos1 = ((BlockHitResult)hitresult).getBlockPos();
                                                BlockState blockstate = renderer.level.getBlockState(blockpos1);
                                                if (!blockstate.isAir() && renderer.level.getWorldBorder().isWithinBounds(blockpos1)) {
                                                    VertexConsumer vertexconsumer2 = multibuffersource$buffersource.getBuffer(RenderType.lines());
                                                    renderer.renderHitOutline(p_109600_, vertexconsumer2, p_109604_.getEntity(), d0, d1, d2, blockpos1, blockstate);
                                                }
                                            } else if (hitresult != null && hitresult.getType() == HitResult.Type.ENTITY) {
                                                //ForgeHooksClient.onDrawHighlight(renderer, p_109604_, hitresult, p_109601_, p_109600_, multibuffersource$buffersource);
                                            }

                                            renderer.minecraft.debugRenderer.render(p_109600_, multibuffersource$buffersource, d0, d1, d2);
                                            multibuffersource$buffersource.endLastBatch();
                                            PoseStack posestack = RenderSystem.getModelViewStack();
                                            RenderSystem.applyModelViewMatrix();
                                            multibuffersource$buffersource.endBatch(Sheets.translucentCullBlockSheet());
                                            multibuffersource$buffersource.endBatch(Sheets.bannerSheet());
                                            multibuffersource$buffersource.endBatch(Sheets.shieldSheet());
                                            multibuffersource$buffersource.endBatch(RenderType.armorGlint());
                                            multibuffersource$buffersource.endBatch(RenderType.armorEntityGlint());
                                            multibuffersource$buffersource.endBatch(RenderType.glint());
                                            multibuffersource$buffersource.endBatch(RenderType.glintDirect());
                                            multibuffersource$buffersource.endBatch(RenderType.glintTranslucent());
                                            multibuffersource$buffersource.endBatch(RenderType.entityGlint());
                                            multibuffersource$buffersource.endBatch(RenderType.entityGlintDirect());
                                            multibuffersource$buffersource.endBatch(RenderType.waterMask());
                                            renderer.renderBuffers.crumblingBufferSource().endBatch();
                                            if (renderer.transparencyChain != null) {
                                                multibuffersource$buffersource.endBatch(RenderType.lines());
                                                multibuffersource$buffersource.endBatch();
                                                renderer.translucentTarget.clear(Minecraft.ON_OSX);
                                                renderer.translucentTarget.copyDepthFrom(renderer.minecraft.getMainRenderTarget());
                                                profilerfiller.popPush("translucent");
                                                renderChunkLayer(renderer,RenderType.translucent(), p_109600_, d0, d1, d2, p_254120_);
                                                profilerfiller.popPush("string");
                                                renderChunkLayer(renderer,RenderType.tripwire(), p_109600_, d0, d1, d2, p_254120_);
                                                renderer.particlesTarget.clear(Minecraft.ON_OSX);
                                                renderer.particlesTarget.copyDepthFrom(renderer.minecraft.getMainRenderTarget());
                                                RenderStateShard.PARTICLES_TARGET.setupRenderState();
                                                profilerfiller.popPush("particles");
                                                renderer.minecraft.particleEngine.render(p_109600_, multibuffersource$buffersource, p_109606_, p_109604_, p_109601_, frustum);
                                                //ForgeHooksClient.dispatchRenderStage(RenderLevelStageEvent.Stage.AFTER_PARTICLES, renderer, p_109600_, p_254120_, renderer.ticks, p_109604_, frustum);
                                                RenderStateShard.PARTICLES_TARGET.clearRenderState();
                                            } else {
                                                profilerfiller.popPush("translucent");
                                                if (renderer.translucentTarget != null) {
                                                    renderer.translucentTarget.clear(Minecraft.ON_OSX);
                                                }

                                                renderChunkLayer(renderer,RenderType.translucent(), p_109600_, d0, d1, d2, p_254120_);
                                                multibuffersource$buffersource.endBatch(RenderType.lines());
                                                multibuffersource$buffersource.endBatch();
                                                profilerfiller.popPush("string");
                                                renderChunkLayer(renderer,RenderType.tripwire(), p_109600_, d0, d1, d2, p_254120_);
                                                profilerfiller.popPush("particles");
                                                renderer.minecraft.particleEngine.render(p_109600_, multibuffersource$buffersource, p_109606_, p_109604_, p_109601_, frustum);
                                                //ForgeHooksClient.dispatchRenderStage(RenderLevelStageEvent.Stage.AFTER_PARTICLES, renderer, p_109600_, p_254120_, renderer.ticks, p_109604_, frustum);
                                            }

                                            PoseStackHelper.pushPose(posestack);
                                            PoseStackHelper.mulPoseMatrix(posestack,PoseStackHelper.pose(PoseStackHelper.last(p_109600_)));
                                            RenderSystem.applyModelViewMatrix();
                                            if (renderer.minecraft.options.getCloudsType() != CloudStatus.OFF) {
                                                if (renderer.transparencyChain != null) {
                                                    renderer.cloudsTarget.clear(Minecraft.ON_OSX);
                                                    RenderStateShard.CLOUDS_TARGET.setupRenderState();
                                                    profilerfiller.popPush("clouds");
                                                    renderer.renderClouds(p_109600_, p_254120_, p_109601_, d0, d1, d2);
                                                    RenderStateShard.CLOUDS_TARGET.clearRenderState();
                                                } else {
                                                    profilerfiller.popPush("clouds");
                                                    RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
                                                    renderer.renderClouds(p_109600_, p_254120_, p_109601_, d0, d1, d2);
                                                }
                                            }

                                            if (renderer.transparencyChain != null) {
                                                RenderStateShard.WEATHER_TARGET.setupRenderState();
                                                profilerfiller.popPush("weather");
                                                renderer.renderSnowAndRain(p_109606_, p_109601_, d0, d1, d2);
                                                //ForgeHooksClient.dispatchRenderStage(RenderLevelStageEvent.Stage.AFTER_WEATHER, renderer, p_109600_, p_254120_, renderer.ticks, p_109604_, frustum);
                                                renderer.renderWorldBorder(p_109604_);
                                                RenderStateShard.WEATHER_TARGET.clearRenderState();
                                                renderer.transparencyChain.process(p_109601_);
                                                renderer.minecraft.getMainRenderTarget().bindWrite(false);
                                            } else {
                                                RenderSystem.depthMask(false);
                                                profilerfiller.popPush("weather");
                                                renderer.renderSnowAndRain(p_109606_, p_109601_, d0, d1, d2);
                                                //ForgeHooksClient.dispatchRenderStage(RenderLevelStageEvent.Stage.AFTER_WEATHER, renderer, p_109600_, p_254120_, renderer.ticks, p_109604_, frustum);
                                                renderer.renderWorldBorder(p_109604_);
                                                RenderSystem.depthMask(true);
                                            }

                                            PoseStackHelper.popPose(posestack);
                                            RenderSystem.applyModelViewMatrix();
                                            renderer.renderDebug(p_109600_, multibuffersource$buffersource, p_109604_);
                                            multibuffersource$buffersource.endLastBatch();
                                            RenderSystem.depthMask(true);
                                            RenderSystem.disableBlend();
                                            FogRenderer.setupNoFog();
                                            return;
                                        }

                                        LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo = (LevelRenderer.RenderChunkInfo)var40.next();
                                        list = levelrenderer$renderchunkinfo.chunk.getCompiledChunk().getRenderableBlockEntities();
                                    } while(list.isEmpty());

                                    Iterator var51 = list.iterator();

                                    while(var51.hasNext()) {
                                        BlockEntity blockentity1 = (BlockEntity)var51.next();
                                        if (frustum.isVisible(blockentity1.getRenderBoundingBox())) {
                                            BlockPos blockpos4 = blockentity1.getBlockPos();
                                            MultiBufferSource multibuffersource1 = multibuffersource$buffersource;
                                            PoseStackHelper.pushPose(p_109600_);
                                            PoseStackHelper.translate(p_109600_,(double)blockpos4.getX() - d0, (double)blockpos4.getY() - d1, (double)blockpos4.getZ() - d2);
                                            SortedSet<BlockDestructionProgress> sortedset = renderer.destructionProgress.get(blockpos4.asLong());
                                            if (sortedset != null && !sortedset.isEmpty()) {
                                                int j = sortedset.last().getProgress();
                                                if (j >= 0) {
                                                    PoseStack.Pose posestack$pose = p_109600_.last();
                                                    VertexConsumer vertexconsumer = new SheetedDecalTextureGenerator(renderer.renderBuffers.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(j)), posestack$pose.pose(), posestack$pose.normal(), 1.0F);
                                                    multibuffersource1 = p_234298_ -> {
                                                        VertexConsumer vertexconsumer3 = multibuffersource$buffersource.getBuffer(p_234298_);
                                                        return p_234298_.affectsCrumbling() ? VertexMultiConsumer.create(vertexconsumer, vertexconsumer3) : vertexconsumer3;
                                                    };
                                                }
                                            }

                                            if (renderer.shouldShowEntityOutlines() && blockentity1.hasCustomOutlineRendering(renderer.minecraft.player)) {
                                                flag2 = true;
                                            }

                                            renderer.blockEntityRenderDispatcher.render(blockentity1, p_109601_, p_109600_, multibuffersource1);
                                            PoseStackHelper.popPose(p_109600_);
                                        }
                                    }
                                }
                            }

                            entity = var25.next();
                        } while(!renderer.entityRenderDispatcher.shouldRender(entity, frustum, d0, d1, d2) && !entity.hasIndirectPassenger(renderer.minecraft.player));

                        blockpos2 = entity.blockPosition();
                    } while(!renderer.level.isOutsideBuildHeight(blockpos2.getY()) && !renderer.isChunkCompiled(blockpos2));
                } while(entity == p_109604_.getEntity() && !p_109604_.isDetached() && (!(p_109604_.getEntity() instanceof LivingEntity) || !((LivingEntity)p_109604_.getEntity()).isSleeping()));
            } while(entity instanceof LocalPlayer && p_109604_.getEntity() != entity && (entity != renderer.minecraft.player || renderer.minecraft.player.isSpectator()));

            ++renderer.renderedEntities;
            if (entity.tickCount == 0) {
                entity.xOld = entity.getX();
                entity.yOld = entity.getY();
                entity.zOld = entity.getZ();
            }

            Object multibuffersource;
            if (renderer.shouldShowEntityOutlines() && renderer.minecraft.shouldEntityAppearGlowing(entity)) {
                flag2 = true;
                OutlineBufferSource outlinebuffersource = renderer.renderBuffers.outlineBufferSource();
                multibuffersource = outlinebuffersource;
                int i = entity.getTeamColor();
                outlinebuffersource.setColor(FastColor.ARGB32.red(i), FastColor.ARGB32.green(i), FastColor.ARGB32.blue(i), 255);
            } else {
                if (renderer.shouldShowEntityOutlines() && entity.hasCustomOutlineRendering(renderer.minecraft.player)) {
                    flag2 = true;
                }

                multibuffersource = multibuffersource$buffersource;
            }

            renderer.renderEntity(entity, d0, d1, d2, p_109601_, p_109600_, (MultiBufferSource)multibuffersource);
        }
    }

    public static void tryTakeScreenshotIfNeeded(GameRenderer renderer) {
        if (!renderer.hasWorldScreenshot && renderer.minecraft.isLocalServer()) {
            long i = Util.getMillis();
            if (i - renderer.lastScreenshotAttempt >= 1000L) {
                renderer.lastScreenshotAttempt = i;
                IntegratedServer integratedserver = renderer.minecraft.getSingleplayerServer();
                if (integratedserver != null && !integratedserver.isStopped()) {
                    integratedserver.getWorldScreenshotFile().ifPresent((p_234239_) -> {
                        if (Files.isRegularFile(p_234239_)) {
                            renderer.hasWorldScreenshot = true;
                        } else {
                            takeAutoScreenshot(renderer,p_234239_);
                        }
                    });
                }
            }
        }

    }

    public static void takeAutoScreenshot(GameRenderer renderer,Path p_182643_) {
        if (renderer.minecraft.levelRenderer.countRenderedChunks() > 10 && renderer.minecraft.levelRenderer.hasRenderedAllChunks()) {
            NativeImage nativeimage = Screenshot.takeScreenshot(renderer.minecraft.getMainRenderTarget());
            Util.ioPool().execute(() -> {
                int i = nativeimage.getWidth();
                int j = nativeimage.getHeight();
                int k = 0;
                int l = 0;
                if (i > j) {
                    k = (i - j) / 2;
                    i = j;
                } else {
                    l = (j - i) / 2;
                    j = i;
                }

                try {
                    NativeImage nativeimage1 = new NativeImage(64, 64, false);
                    try {
                        nativeimage.resizeSubRectTo(k, l, i, j, nativeimage1);
                        nativeimage1.writeToFile(p_182643_);
                    } catch (Throwable var15) {
                        try {
                            nativeimage1.close();
                        } catch (Throwable var14) {
                            var15.addSuppressed(var14);
                        }
                        throw var15;
                    }
                    nativeimage1.close();
                } catch (IOException var16) {
                    IOException ioexception = var16;
                    GameRenderer.LOGGER.warn("Couldn't save auto screenshot", ioexception);
                } finally {
                    nativeimage.close();
                }
            });
        }

    }

    public static void renderChunkLayer(LevelRenderer renderer,RenderType p_172994_, PoseStack p_172995_, double p_172996_, double p_172997_, double p_172998_, Matrix4f p_254039_) {

        p_172994_.setupRenderState();
        if (p_172994_ == RenderType.translucent()) {
            renderer.minecraft.getProfiler().push("translucent_sort");
            double d0 = p_172996_ - renderer.xTransparentOld;
            double d1 = p_172997_ - renderer.yTransparentOld;
            double d2 = p_172998_ - renderer.zTransparentOld;
            if (d0 * d0 + d1 * d1 + d2 * d2 > 1.0) {
                int j = SectionPos.posToSectionCoord(p_172996_);
                int k = SectionPos.posToSectionCoord(p_172997_);
                int l = SectionPos.posToSectionCoord(p_172998_);
                boolean flag = j != SectionPos.posToSectionCoord(renderer.xTransparentOld) || l != SectionPos.posToSectionCoord(renderer.zTransparentOld) || k != SectionPos.posToSectionCoord(renderer.yTransparentOld);
                renderer.xTransparentOld = p_172996_;
                renderer.yTransparentOld = p_172997_;
                renderer.zTransparentOld = p_172998_;
                int i1 = 0;
                ObjectListIterator var21 = renderer.renderChunksInFrustum.iterator();

                label126:
                while(true) {
                    LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo;
                    do {
                        do {
                            if (!var21.hasNext()) {
                                break label126;
                            }

                            levelrenderer$renderchunkinfo = (LevelRenderer.RenderChunkInfo)var21.next();
                        } while(i1 >= 15);
                    } while(!flag && !levelrenderer$renderchunkinfo.isAxisAlignedWith(j, k, l));

                    if (levelrenderer$renderchunkinfo.chunk.resortTransparency(p_172994_, renderer.chunkRenderDispatcher)) {
                        ++i1;
                    }
                }
            }

            renderer.minecraft.getProfiler().pop();
        }

        renderer.minecraft.getProfiler().push("filterempty");
        renderer.minecraft.getProfiler().popPush(() -> {
            return "render_" + p_172994_;
        });
        boolean flag1 = p_172994_ != RenderType.translucent();
        ObjectListIterator<LevelRenderer.RenderChunkInfo> objectlistiterator = renderer.renderChunksInFrustum.listIterator(flag1 ? 0 : renderer.renderChunksInFrustum.size());
        ShaderInstance shaderinstance = RenderSystem.getShader();

        for(int i = 0; i < 12; ++i) {
            int j1 = RenderSystem.getShaderTexture(i);
            shaderinstance.setSampler("Sampler" + i, j1);
        }

        if (shaderinstance.MODEL_VIEW_MATRIX != null) {
            shaderinstance.MODEL_VIEW_MATRIX.set(p_172995_.last().pose());
        }

        if (shaderinstance.PROJECTION_MATRIX != null) {
            shaderinstance.PROJECTION_MATRIX.set(p_254039_);
        }

        if (shaderinstance.COLOR_MODULATOR != null) {
            shaderinstance.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
        }

        if (shaderinstance.GLINT_ALPHA != null) {
            shaderinstance.GLINT_ALPHA.set(RenderSystem.getShaderGlintAlpha());
        }

        if (shaderinstance.FOG_START != null) {
            shaderinstance.FOG_START.set(RenderSystem.getShaderFogStart());
        }

        if (shaderinstance.FOG_END != null) {
            shaderinstance.FOG_END.set(RenderSystem.getShaderFogEnd());
        }

        if (shaderinstance.FOG_COLOR != null) {
            shaderinstance.FOG_COLOR.set(RenderSystem.getShaderFogColor());
        }

        if (shaderinstance.FOG_SHAPE != null) {
            shaderinstance.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
        }

        if (shaderinstance.TEXTURE_MATRIX != null) {
            shaderinstance.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
        }

        if (shaderinstance.GAME_TIME != null) {
            shaderinstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
        }

        RenderSystem.setupShaderLights(shaderinstance);
        shaderinstance.apply();
        Uniform uniform = shaderinstance.CHUNK_OFFSET;

        while(true) {
            if (flag1) {
                if (!objectlistiterator.hasNext()) {
                    break;
                }
            } else if (!objectlistiterator.hasPrevious()) {
                break;
            }

            LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo1 = flag1 ? objectlistiterator.next() : objectlistiterator.previous();
            ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = levelrenderer$renderchunkinfo1.chunk;
            if (!chunkrenderdispatcher$renderchunk.getCompiledChunk().isEmpty(p_172994_)) {
                VertexBuffer vertexbuffer = chunkrenderdispatcher$renderchunk.getBuffer(p_172994_);
                BlockPos blockpos = chunkrenderdispatcher$renderchunk.getOrigin();
                if (uniform != null) {
                    uniform.set((float)((double)blockpos.getX() - p_172996_), (float)((double)blockpos.getY() - p_172997_), (float)((double)blockpos.getZ() - p_172998_));
                    uniform.upload();
                }
                BufferUploader.lastImmediateBuffer = null;
                GL30C.glBindVertexArray(vertexbuffer.arrayObjectId);
                GL11C.glDrawElements(vertexbuffer.mode.asGLMode, vertexbuffer.indexCount, getIndexType(vertexbuffer).asGLType,0L);
            }
        }

        if (uniform != null) {
            uniform.set(0.0F, 0.0F, 0.0F);
        }

        shaderinstance.clear();
        BufferUploader.lastImmediateBuffer = null;
        GlStateManager._glBindVertexArray(0);
        renderer.minecraft.getProfiler().pop();
        p_172994_.clearRenderState();
    }
    private static VertexFormat.IndexType getIndexType(VertexBuffer buffer) {
        RenderSystem.AutoStorageIndexBuffer $$0 = buffer.sequentialIndices;
        return $$0 != null ? $$0.type() : buffer.indexType;
    }
}
