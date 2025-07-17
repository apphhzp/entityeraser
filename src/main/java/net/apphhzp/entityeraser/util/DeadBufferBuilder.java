package net.apphhzp.entityeraser.util;

import apphhzp.lib.ClassOption;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.apphhzp.entityeraser.shitmountain.DeathRenderer;
import net.apphhzp.entityeraser.shitmountain.EntityEraserRenderers;
import net.apphhzp.entityeraser.shitmountain.PoseStackHelper;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static apphhzp.lib.ClassHelperSpecial.*;
import static net.apphhzp.entityeraser.shitmountain.MinecraftRenderers.doEntityOutline;
import static net.apphhzp.entityeraser.shitmountain.MinecraftRenderers.renderLevel;

@OnlyIn(Dist.CLIENT)
public class DeadBufferBuilder{
    private static final MethodHandle deathScreenConstructor;
    private static RenderTarget workingTarget;
    private static RenderTarget normalTarget;
    private static DeathScreen deathScreen;
    static {
        try {
            deathScreenConstructor= lookup.findConstructor(
                    defineHiddenClass("net.apphhzp.entityeraser.screen.EntityEraserDeathScreen",DeadBufferBuilder.class,true,null, ClassOption.STRONG,ClassOption.NESTMATE).lookupClass(),
                    MethodType.methodType(void.class, Player.class));
        } catch (Throwable e) {
            throwOriginalException(e);
            throw new RuntimeException(e);
        }
    }

    private DeadBufferBuilder() {}

    private static int framebufferWidth;

    private static int framebufferHeight;

    private static RenderTarget currentTarget() {
        return normalTarget;
    }

    private static void recreate(){
        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();

        if (normalTarget!=null){
            normalTarget.destroyBuffers();
        }
        normalTarget = new MainTarget(window.getWidth(), window.getHeight());
        GL30C.glBindFramebuffer(GL30.GL_FRAMEBUFFER, normalTarget.frameBufferId);
        GlStateManager._viewport(0, 0, normalTarget.viewWidth, normalTarget.viewHeight);
        workingTarget = normalTarget;
        FogRenderer.setupNoFog();
        RenderSystem.enableCull();
        try {
            deathScreen = (DeathScreen) deathScreenConstructor.invoke(mc.player);
        } catch (Throwable e) {
            throwOriginalException(e);
            throw new RuntimeException(e);
        }
        if (BufferUploader.lastImmediateBuffer != null) {
            BufferUploader.lastImmediateBuffer = null;
            GL30C.glBindVertexArray(0);
        }
        EntityEraserRenderers.staticInit(deathScreen,mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
        render(mc.gameRenderer,mc.isPaused() ? mc.pausePartialTick : mc.timer.partialTick, Util.getNanos(), true,-2);
        deathScreen = null;
        GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0);

        framebufferWidth = mc.getMainRenderTarget().viewWidth;
        framebufferHeight = mc.getMainRenderTarget().viewHeight;

        GL30C.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
        GL30C.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
        GL30C.glBlitFramebuffer(0, 0, framebufferWidth, framebufferHeight, 0, 0, framebufferWidth, framebufferHeight, GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
    }

    public static void render(){
        recreate();
        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();
        GLFW.glfwSetInputMode(window.getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        GL11C.glDrawBuffer(GL11.GL_FRONT);
        GL30C.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, currentTarget().frameBufferId);
        GL30C.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
        GL30C.glBlitFramebuffer(0, 0, framebufferWidth, framebufferHeight, 0, 0, mc.getMainRenderTarget().viewWidth, mc.getMainRenderTarget().viewHeight, GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
        GL11C.glFlush();
    }


    private static void render(GameRenderer renderer, float p_109094_, long p_109095_, boolean p_109096_,int index) {
        if (renderer.minecraft.isWindowActive() || !renderer.minecraft.options.pauseOnLostFocus || renderer.minecraft.options.touchscreen().get() && renderer.minecraft.mouseHandler.isRightPressed()) {
            renderer.lastActiveTime = Util.getMillis();
        } else if (Util.getMillis() - renderer.lastActiveTime > 500L) {
            renderer.minecraft.pauseGame(false);
        }

        if (!renderer.minecraft.noRender) {
            int i = (int)(renderer.minecraft.mouseHandler.xpos() * (double)renderer.minecraft.getWindow().getGuiScaledWidth() / (double)renderer.minecraft.getWindow().getScreenWidth());
            int j = (int)(renderer.minecraft.mouseHandler.ypos() * (double)renderer.minecraft.getWindow().getGuiScaledHeight() / (double)renderer.minecraft.getWindow().getScreenHeight());
            RenderSystem.viewport(0, 0, renderer.minecraft.getWindow().getWidth(), renderer.minecraft.getWindow().getHeight());
            if (p_109096_ && renderer.minecraft.level != null) {
                renderer.minecraft.getProfiler().push("level");
                renderLevel(renderer,p_109094_, p_109095_, new PoseStack());
                renderer.tryTakeScreenshotIfNeeded();
                doEntityOutline(renderer.minecraft.levelRenderer);
                if (renderer.postEffect != null && renderer.effectActive) {
                    RenderSystem.disableBlend();
                    RenderSystem.disableDepthTest();
                    RenderSystem.resetTextureMatrix();
                    renderer.postEffect.process(p_109094_);
                }
                GL30C.glBindFramebuffer(GL30.GL_FRAMEBUFFER, workingTarget.frameBufferId);
                GlStateManager._viewport(0, 0, workingTarget.viewWidth, workingTarget.viewHeight);
            }

            Window window = renderer.minecraft.getWindow();
            RenderSystem.clear(256, Minecraft.ON_OSX);
            Matrix4f matrix4f = new Matrix4f().setOrtho(0.0F, (float)((double)window.getWidth() / window.getGuiScale()), (float)((double)window.getHeight() / window.getGuiScale()), 0.0F, 1000.0F, 21000.0f);
            RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
            PoseStack posestack = RenderSystem.modelViewStack;
            PoseStackHelper.pushPose(posestack);
            PoseStackHelper.setIdentity(posestack);
            PoseStackHelper.translate(posestack,0.0, 0.0, -20000.0f);
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

            CrashReportCategory crashreportcategory2;
            Throwable throwable;
            CrashReport crashreport2;
            if (renderer.minecraft.getOverlay() != null) {
                try {
                    renderer.minecraft.getOverlay().render(guigraphics, i, j, renderer.minecraft.getDeltaFrameTime());
                } catch (Throwable var16) {
                    throwable = var16;
                    crashreport2 = CrashReport.forThrowable(throwable, "Rendering overlay");
                    crashreportcategory2 = crashreport2.addCategory("Overlay render details");
                    crashreportcategory2.setDetail("Overlay name", () -> renderer.minecraft.getOverlay().getClass().getCanonicalName());
                    throw new ReportedException(crashreport2);
                }
            }
            EntityEraserRenderers.staticRender2(deathScreen,guigraphics, i, j, renderer.minecraft.getDeltaFrameTime(),index);
            DeathRenderer.flush(guigraphics);
            PoseStackHelper.popPose(posestack);
            RenderSystem.applyModelViewMatrix();
        }
    }


}
