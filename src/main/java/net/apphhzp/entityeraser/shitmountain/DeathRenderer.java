package net.apphhzp.entityeraser.shitmountain;

import apphhzp.lib.ClassHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Divisor;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.apphhzp.entityeraser.screen.EntityEraserDeathScreen;
import net.apphhzp.entityeraser.util.EntityUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joml.*;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nullable;
import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings({"UnusedReturnValue", "JavaExistingMethodCanBeUsed", "unused"})
public final class DeathRenderer {
    private DeathRenderer() {
    }

    public static void fillGradient(GuiGraphics guiGraphics, int p_283290_, int p_283278_, int p_282670_, int p_281698_, int p_283374_, int p_283076_) {
        fillGradient(guiGraphics, p_283290_, p_283278_, p_282670_, p_281698_, 0, p_283374_, p_283076_);
    }

    public static void fillGradient(GuiGraphics guiGraphics, int p_282702_, int p_282331_, int p_281415_, int p_283118_, int p_282419_, int p_281954_, int p_282607_) {
        fillGradient(guiGraphics, RenderType.gui(), p_282702_, p_282331_, p_281415_, p_283118_, p_281954_, p_282607_, p_282419_);
    }

    public static void fillGradient(GuiGraphics guiGraphics, RenderType p_286522_, int p_286535_, int p_286839_, int p_286242_, int p_286856_, int p_286809_, int p_286833_, int p_286706_) {
        BufferBuilder bufferBuilder = getBuffer(guiGraphics.bufferSource, p_286522_);
        fillGradient(guiGraphics, bufferBuilder, p_286535_, p_286839_, p_286242_, p_286856_, p_286706_, p_286809_, p_286833_);
        if (!guiGraphics.managed) {
            flush(guiGraphics);
        }
    }

    public static void staticInit(EntityEraserDeathScreen gui, Minecraft p_96607_, int p_96608_, int p_96609_) {
        gui.minecraft = p_96607_;
        gui.font = p_96607_.font;
        gui.width = p_96608_;
        gui.height = p_96609_;
        if (gui.initialized) {
            gui.renderables.clear();
            gui.children.clear();
            gui.narratables.clear();
            gui.clearFocus();
        }
        gui.delayTicker = 0;
        gui.exitButtons.clear();
        Component $$0 = Component.translatable("death.do_not_respawn");
        gui.exitButtons.add(gui.addRenderableWidget(Button.builder($$0, new EmptyOnPress()).bounds(gui.width / 2 - 100, gui.height / 4 + 72, 200, 20).build()));
        gui.exitToTitleButton = gui.addRenderableWidget(Button.builder(Component.translatable("death.do_not_exit"), new OnPress1(gui)).bounds(gui.width / 2 - 100, gui.height / 4 + 96, 200, 20).build());
        gui.exitButtons.add(gui.exitToTitleButton);
        gui.deathScore = Component.translatable("deathScreen.score").append(": ").append(Component.literal(Integer.toString(gui.minecraft.player.getScore())).withStyle(ChatFormatting.YELLOW));
        gui.initialized = true;
        gui.triggerImmediateNarration(false);
        gui.suppressNarration(Screen.NARRATE_SUPPRESS_AFTER_INIT_TIME);
    }

    public static void staticRender(EntityEraserDeathScreen screen, GuiGraphics guiGraphics, int p_283551_, int p_283002_, float p_281981_) {
        int colour = EntityUtil.getSmoothColor(128, 0, 2000D), c2 = EntityUtil.getSmoothColor(255, Mth.PI / 2, 2000D);
        fillGradient(guiGraphics, 0, 0, screen.width, screen.height, EntityUtil.getSmoothColor(64, 0, 2000D), colour);
        {
            PoseStack.Pose posestack$pose = guiGraphics.pose.poseStack.getLast();
            guiGraphics.pose.poseStack.addLast(new PoseStack.Pose(new Matrix4f(posestack$pose.pose), new Matrix3f(posestack$pose.normal)));
        }
        {
            PoseStack.Pose posestack$pose = guiGraphics.pose.poseStack.getLast();
            posestack$pose.pose.scale(2.0F, 2.0F, 2.0F);
        }
        drawCenteredString(guiGraphics, screen.font, screen.title, screen.width / 2 / 2, 30, c2);
        guiGraphics.pose.poseStack.removeLast();
        drawCenteredString(guiGraphics, screen.font, screen.causeOfDeath, screen.width / 2, 85, c2);
        drawCenteredString(guiGraphics, screen.font, screen.deathScore, screen.width / 2, 100, c2);
        if (p_283002_ > 85) {
            Objects.requireNonNull(screen.font);
            if (p_283002_ < 85 + 9) {
                Style $$4 = getClickedComponentStyleAt(screen, p_283551_);
                renderComponentHoverEffect(guiGraphics, screen.font, $$4, p_283551_, p_283002_);
            }
        }
        Iterator<Renderable> var5 = screen.renderables.iterator();
        while (var5.hasNext()) {
            Renderable renderable = var5.next();
            if (renderable instanceof Button button) {
                render(button, guiGraphics, p_283551_, p_283002_, p_281981_, screen);
            }
        }
        if (screen.exitToTitleButton != null && screen.minecraft.reportingContext.hasDraftReport()) {
            blit(guiGraphics, AbstractWidget.WIDGETS_LOCATION, screen.exitToTitleButton.getX() + screen.exitToTitleButton.getWidth() - 17, screen.exitToTitleButton.getY() + 3, 182, 24, 15, 15);
        }
    }

    public static void render(Button button, GuiGraphics p_282421_, int p_93658_, int p_93659_, float p_93660_, EntityEraserDeathScreen deathScreen) {
        button.isHovered = p_93658_ >= button.x && p_93659_ >= button.y && p_93658_ < button.x + button.width && p_93659_ < button.y + button.height;
        renderWidget(button, p_282421_, p_93658_, p_93659_, p_93660_);
        updateTooltip(button, deathScreen);
    }

    public static void renderWidget(Button button, GuiGraphics p_281670_, int p_282682_, int p_281714_, float p_282542_) {
        Minecraft minecraft = Minecraft.getInstance();
        setColor(p_281670_, 1.0F, 1.0F, 1.0F, button.alpha);
        setEnabled(GlStateManager.BLEND.mode, true);
        setEnabled(GlStateManager.DEPTH.mode, true);
        blitNineSliced(p_281670_, AbstractWidget.WIDGETS_LOCATION, button.x, button.y, button.width, button.height, 20, 4, 200, 20, 0, getTextureY(button));
        setColor(p_281670_, 1.0F, 1.0F, 1.0F, 1.0F);
        int i = getFGColor(button);
        renderString(button, p_281670_, minecraft.font, i | Mth.ceil(button.alpha * 255.0F) << 24);
    }

    public static int getFGColor(Button button) {
        if (button.packedFGColor != -1) {
            return button.packedFGColor;
        } else {
            return button.active ? 16777215 : 10526880;
        }
    }

    public static void blitNineSliced(GuiGraphics guiGraphics, ResourceLocation p_282543_, int p_281513_, int p_281865_, int p_282482_, int p_282661_, int p_282068_, int p_281294_, int p_281681_, int p_281957_, int p_282300_, int p_282769_) {
        blitNineSliced(guiGraphics, p_282543_, p_281513_, p_281865_, p_282482_, p_282661_, p_282068_, p_281294_, p_282068_, p_281294_, p_281681_, p_281957_, p_282300_, p_282769_);
    }

    public static void blitNineSliced(GuiGraphics guiGraphics, ResourceLocation p_282712_, int p_283509_, int p_283259_, int p_283273_, int p_282043_, int p_281430_, int p_281412_, int p_282566_, int p_281971_, int p_282879_, int p_281529_, int p_281924_, int p_281407_) {
        p_281430_ = Math.min(p_281430_, p_283273_ / 2);
        p_282566_ = Math.min(p_282566_, p_283273_ / 2);
        p_281412_ = Math.min(p_281412_, p_282043_ / 2);
        p_281971_ = Math.min(p_281971_, p_282043_ / 2);
        if (p_283273_ == p_282879_ && p_282043_ == p_281529_) {
            blit(guiGraphics, p_282712_, p_283509_, p_283259_, p_281924_, p_281407_, p_283273_, p_282043_);
        } else if (p_282043_ == p_281529_) {
            blit(guiGraphics, p_282712_, p_283509_, p_283259_, p_281924_, p_281407_, p_281430_, p_282043_);
            blitRepeating(guiGraphics, p_282712_, p_283509_ + p_281430_, p_283259_, p_283273_ - p_282566_ - p_281430_, p_282043_, p_281924_ + p_281430_, p_281407_, p_282879_ - p_282566_ - p_281430_, p_281529_);
            blit(guiGraphics, p_282712_, p_283509_ + p_283273_ - p_282566_, p_283259_, p_281924_ + p_282879_ - p_282566_, p_281407_, p_282566_, p_282043_);
        } else if (p_283273_ == p_282879_) {
            blit(guiGraphics, p_282712_, p_283509_, p_283259_, p_281924_, p_281407_, p_283273_, p_281412_);
            blitRepeating(guiGraphics, p_282712_, p_283509_, p_283259_ + p_281412_, p_283273_, p_282043_ - p_281971_ - p_281412_, p_281924_, p_281407_ + p_281412_, p_282879_, p_281529_ - p_281971_ - p_281412_);
            blit(guiGraphics, p_282712_, p_283509_, p_283259_ + p_282043_ - p_281971_, p_281924_, p_281407_ + p_281529_ - p_281971_, p_283273_, p_281971_);
        } else {
            blit(guiGraphics, p_282712_, p_283509_, p_283259_, p_281924_, p_281407_, p_281430_, p_281412_);
            blitRepeating(guiGraphics, p_282712_, p_283509_ + p_281430_, p_283259_, p_283273_ - p_282566_ - p_281430_, p_281412_, p_281924_ + p_281430_, p_281407_, p_282879_ - p_282566_ - p_281430_, p_281412_);
            blit(guiGraphics, p_282712_, p_283509_ + p_283273_ - p_282566_, p_283259_, p_281924_ + p_282879_ - p_282566_, p_281407_, p_282566_, p_281412_);
            blit(guiGraphics, p_282712_, p_283509_, p_283259_ + p_282043_ - p_281971_, p_281924_, p_281407_ + p_281529_ - p_281971_, p_281430_, p_281971_);
            blitRepeating(guiGraphics, p_282712_, p_283509_ + p_281430_, p_283259_ + p_282043_ - p_281971_, p_283273_ - p_282566_ - p_281430_, p_281971_, p_281924_ + p_281430_, p_281407_ + p_281529_ - p_281971_, p_282879_ - p_282566_ - p_281430_, p_281971_);
            blit(guiGraphics, p_282712_, p_283509_ + p_283273_ - p_282566_, p_283259_ + p_282043_ - p_281971_, p_281924_ + p_282879_ - p_282566_, p_281407_ + p_281529_ - p_281971_, p_282566_, p_281971_);
            blitRepeating(guiGraphics, p_282712_, p_283509_, p_283259_ + p_281412_, p_281430_, p_282043_ - p_281971_ - p_281412_, p_281924_, p_281407_ + p_281412_, p_281430_, p_281529_ - p_281971_ - p_281412_);
            blitRepeating(guiGraphics, p_282712_, p_283509_ + p_281430_, p_283259_ + p_281412_, p_283273_ - p_282566_ - p_281430_, p_282043_ - p_281971_ - p_281412_, p_281924_ + p_281430_, p_281407_ + p_281412_, p_282879_ - p_282566_ - p_281430_, p_281529_ - p_281971_ - p_281412_);
            blitRepeating(guiGraphics, p_282712_, p_283509_ + p_283273_ - p_282566_, p_283259_ + p_281412_, p_281430_, p_282043_ - p_281971_ - p_281412_, p_281924_ + p_282879_ - p_282566_, p_281407_ + p_281412_, p_282566_, p_281529_ - p_281971_ - p_281412_);
        }
    }

    public static void blitRepeating(GuiGraphics guiGraphics, ResourceLocation p_283059_, int p_283575_, int p_283192_, int p_281790_, int p_283642_, int p_282691_, int p_281912_, int p_281728_, int p_282324_) {
        blitRepeating(guiGraphics, p_283059_, p_283575_, p_283192_, p_281790_, p_283642_, p_282691_, p_281912_, p_281728_, p_282324_, 256, 256);
    }

    public static void blitRepeating(GuiGraphics guiGraphics, ResourceLocation p_283059_, int p_283575_, int p_283192_, int p_281790_, int p_283642_, int p_282691_, int p_281912_, int p_281728_, int p_282324_, int textureWidth, int textureHeight) {
        int i = p_283575_;
        int j;
        for (IntIterator intiterator = slices(p_281790_, p_281728_); intiterator.hasNext(); i += j) {
            j = intiterator.nextInt();
            int k = (p_281728_ - j) / 2;
            int l = p_283192_;
            int i1;
            for (IntIterator intiterator1 = slices(p_283642_, p_282324_); intiterator1.hasNext(); l += i1) {
                i1 = intiterator1.nextInt();
                int j1 = (p_282324_ - i1) / 2;
                blit(guiGraphics, p_283059_, i, l, (float) (p_282691_ + k), (float) (p_281912_ + j1), j, i1, textureWidth, textureHeight);
            }
        }
    }

    private static IntIterator slices(int p_282197_, int p_282161_) {
        int i = Mth.positiveCeilDiv(p_282197_, p_282161_);
        return new Divisor(p_282197_, i);
    }

    public static void setColor(GuiGraphics graphics, float p_281272_, float p_281734_, float p_282022_, float p_281752_) {
        if (graphics.managed) {
            flush(graphics);
        }
        setShaderColor(p_281272_, p_281734_, p_282022_, p_281752_);
    }

    public static void setShaderColor(float p_157430_, float p_157431_, float p_157432_, float p_157433_) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(new RenderCall2(p_157430_, p_157431_, p_157432_, p_157433_));
        } else {
            RenderSystem.shaderColor[0] = p_157430_;
            RenderSystem.shaderColor[1] = p_157431_;
            RenderSystem.shaderColor[2] = p_157432_;
            RenderSystem.shaderColor[3] = p_157433_;
        }
    }

    public static int getTextureY(Button button) {
        int i = 1;
        if (!button.active) {
            i = 0;
        } else if (button.isHovered() || button.isFocused()) {
            i = 2;
        }
        return 46 + i * 20;
    }

    private static void updateTooltip(Button button, EntityEraserDeathScreen screen) {
        if (button.tooltip != null) {
            boolean flag = button.isHovered || button.isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard();
            if (flag != button.wasHoveredOrFocused) {
                if (flag) {
                    button.hoverOrFocusedStartTime = Util.getMillis();
                }
                button.wasHoveredOrFocused = flag;
            }
            if (flag && Util.getMillis() - button.hoverOrFocusedStartTime > (long) button.tooltipMsDelay) {
                if (screen != null) {
                    screen.setTooltipForNextRenderPass(button.tooltip, createTooltipPositioner(button), button.focused);
                }
            }
        }
    }

    public static ClientTooltipPositioner createTooltipPositioner(Button button) {
        return !button.isHovered && button.isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard() ? new BelowOrAboveWidgetTooltipPositioner(button) : new MenuTooltipPositioner(button);
    }

    public static void renderString(Button button, GuiGraphics p_283366_, Font p_283054_, int p_281656_) {
        renderScrollingString(button, p_283366_, p_283054_, 2, p_281656_);
    }

    public static void renderScrollingString(Button button, GuiGraphics p_281857_, Font p_282790_, int p_282664_, int p_282944_) {
        renderScrollingString(p_281857_, p_282790_, button.message, button.x + p_282664_, button.y, button.x + button.width - p_282664_, button.y + button.height, p_282944_);
    }

    public static void renderScrollingString(GuiGraphics p_281620_, Font p_282651_, Component p_281467_, int p_283621_, int p_282084_, int p_283398_, int p_281938_, int p_283471_) {
        int i = width(p_282651_, p_281467_);
        int j = (p_282084_ + p_281938_ - 9) / 2 + 1;
        int k = p_283398_ - p_283621_;
        if (i > k) {
            int l = i - k;
            double d0 = (double) Util.getMillis() / 1000.0;
            double d1 = Math.max((double) l * 0.5, 3.0);
            double d2 = Math.sin(1.5707963267948966 * Math.cos(6.283185307179586 * d0 / d1)) / 2.0 + 0.5;
            double d3 = Mth.lerp(d2, 0.0, l);
            enableScissor(p_281620_, p_283621_, p_282084_, p_283398_, p_281938_);
            drawString(p_281620_, p_282651_, p_281467_, p_283621_ - (int) d3, j, p_283471_);
            disableScissor(p_281620_);
        } else {
            drawCenteredString(p_281620_, p_282651_, p_281467_, (p_283621_ + p_283398_) / 2, j, p_283471_);
        }
    }

    public static void enableScissor(GuiGraphics guiGraphics, int p_281479_, int p_282788_, int p_282924_, int p_282826_) {
        applyScissor(guiGraphics, push(guiGraphics.scissorStack, new ScreenRectangle(p_281479_, p_282788_, p_282924_ - p_281479_, p_282826_ - p_282788_)));
    }

    public static void disableScissor(GuiGraphics guiGraphics) {
        applyScissor(guiGraphics, pop(guiGraphics.scissorStack));
    }

    private static void applyScissor(GuiGraphics guiGraphics, @Nullable ScreenRectangle p_281569_) {
        if (guiGraphics.managed) {
            flush(guiGraphics);
        }
        if (p_281569_ != null) {
            Window window = Minecraft.getInstance().window;
            int i = window.getHeight();
            double d0 = window.getGuiScale();
            double d1 = (double) p_281569_.left() * d0;
            double d2 = (double) i - (double) p_281569_.bottom() * d0;
            double d3 = (double) p_281569_.width() * d0;
            double d4 = (double) p_281569_.height() * d0;
            setEnabled(GlStateManager.SCISSOR.mode, true);
            GL11C.glScissor((int) d1, (int) d2, Math.max(0, (int) d3), Math.max(0, (int) d4));
        } else {
            setEnabled(GlStateManager.SCISSOR.mode, false);
        }
    }

    public static ScreenRectangle push(GuiGraphics.ScissorStack scissorStack, ScreenRectangle p_281812_) {
        ScreenRectangle screenrectangle = scissorStack.stack.peekLast();
        if (screenrectangle != null) {
            ScreenRectangle screenrectangle1 = Objects.requireNonNullElse(p_281812_.intersection(screenrectangle), ScreenRectangle.empty());
            scissorStack.stack.addLast(screenrectangle1);
            return screenrectangle1;
        } else {
            scissorStack.stack.addLast(p_281812_);
            return p_281812_;
        }
    }

    public static @Nullable ScreenRectangle pop(GuiGraphics.ScissorStack scissorStack) {
        if (scissorStack.stack.isEmpty()) {
            throw new IllegalStateException("Scissor stack underflow");
        } else {
            scissorStack.stack.removeLast();
            return scissorStack.stack.peekLast();
        }
    }

    public static int width(Font font, FormattedText p_92853_) {
        return Mth.ceil(font.splitter.stringWidth(p_92853_));
    }

    private static Style getClickedComponentStyleAt(EntityEraserDeathScreen screen, int p_95918_) {
        int $$1 = screen.minecraft.font.width(screen.causeOfDeath);
        int $$2 = screen.width / 2 - $$1 / 2;
        int $$3 = screen.width / 2 + $$1 / 2;
        return p_95918_ >= $$2 && p_95918_ <= $$3 ? screen.minecraft.font.getSplitter().componentStyleAtWidth(screen.causeOfDeath, p_95918_ - $$2) : null;
    }

    public static void flush(GuiGraphics guiGraphics) {
        setEnabled(GlStateManager.DEPTH.mode, false);
        endBatch(guiGraphics.bufferSource);
        setEnabled(GlStateManager.DEPTH.mode, true);
    }

    private static BufferBuilder getBuffer(MultiBufferSource.BufferSource bufferSource, RenderType p_109919_) {
        Optional<RenderType> $$1 = p_109919_.asOptional();
        BufferBuilder $$2 = getBuilderRaw(bufferSource, p_109919_);
        if (!Objects.equals(bufferSource.lastState, $$1) || !p_109919_.canConsolidateConsecutiveGeometry()) {
            if (bufferSource.lastState.isPresent()) {
                RenderType $$3 = bufferSource.lastState.get();
                if (!bufferSource.fixedBuffers.containsKey($$3)) {
                    endBatch(bufferSource, $$3);
                }
            }
            if (bufferSource.startedBuffers.add($$2)) {
                begin($$2, p_109919_.mode(), p_109919_.format());
            }
            bufferSource.lastState = $$1;
        }
        return $$2;
    }

    private static void endBatch(MultiBufferSource.BufferSource bufferSource, RenderType p_109913_) {
        BufferBuilder $$1 = getBuilderRaw(bufferSource, p_109913_);
        boolean $$2 = Objects.equals(bufferSource.lastState, p_109913_.asOptional());
        if ($$2 || $$1 != bufferSource.builder) {
            if (bufferSource.startedBuffers.remove($$1)) {
                end(p_109913_, $$1, RenderSystem.getVertexSorting());
                if ($$2) {
                    bufferSource.lastState = Optional.empty();
                }

            }
        }
    }

    public static void end(RenderType type, BufferBuilder p_277996_, VertexSorting p_277677_) {
        if (p_277996_.building) {
            if (type.sortOnUpload) {
                setQuadSorting(p_277996_, p_277677_);
            }
            BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = end(p_277996_);
            type.setupState.run();
            drawWithShader(bufferbuilder$renderedbuffer);
            type.clearState.run();
        }
    }

    private static void endBatch(MultiBufferSource.BufferSource bufferSource) {
        if (!bufferSource.lastState.isEmpty()) {
            RenderType type = bufferSource.lastState.get();
            BufferBuilder $$1 = getBuffer(bufferSource, type);
            if ($$1 == bufferSource.builder) {
                endBatch(bufferSource, type);
            }
        }
        Iterator var1 = bufferSource.fixedBuffers.keySet().iterator();
        while (var1.hasNext()) {
            RenderType $$0 = (RenderType) var1.next();
            endBatch(bufferSource, $$0);
        }

    }

    private static BufferBuilder getBuilderRaw(MultiBufferSource.BufferSource bufferSource, RenderType p_109915_) {
        return bufferSource.fixedBuffers.getOrDefault(p_109915_, bufferSource.builder);
    }

    private static void fillGradient(GuiGraphics guiGraphics, BufferBuilder p_286862_, int p_283414_, int p_281397_, int p_283587_, int p_281521_, int p_283505_, int p_283131_, int p_282949_) {
        float f = (float) FastColor.ARGB32.alpha(p_283131_) / 255.0F;
        float f1 = (float) FastColor.ARGB32.red(p_283131_) / 255.0F;
        float f2 = (float) FastColor.ARGB32.green(p_283131_) / 255.0F;
        float f3 = (float) FastColor.ARGB32.blue(p_283131_) / 255.0F;
        float f4 = (float) FastColor.ARGB32.alpha(p_282949_) / 255.0F;
        float f5 = (float) FastColor.ARGB32.red(p_282949_) / 255.0F;
        float f6 = (float) FastColor.ARGB32.green(p_282949_) / 255.0F;
        float f7 = (float) FastColor.ARGB32.blue(p_282949_) / 255.0F;
        Matrix4f matrix4f = guiGraphics.pose.poseStack.getLast().pose;
        endVertex(colorVertexConsumer(vertexVertexConsumer(p_286862_, matrix4f, (float) p_283414_, (float) p_281397_, (float) p_283505_), f1, f2, f3, f));
        endVertex(colorVertexConsumer(vertexVertexConsumer(p_286862_, matrix4f, (float) p_283414_, (float) p_281521_, (float) p_283505_), f5, f6, f7, f4));
        endVertex(colorVertexConsumer(vertexVertexConsumer(p_286862_, matrix4f, (float) p_283587_, (float) p_281521_, (float) p_283505_), f5, f6, f7, f4));
        endVertex(colorVertexConsumer(vertexVertexConsumer(p_286862_, matrix4f, (float) p_283587_, (float) p_281397_, (float) p_283505_), f1, f2, f3, f));
    }

    public static void drawCenteredString(GuiGraphics guiGraphics, Font p_282901_, Component p_282456_, int p_283083_, int p_282276_, int p_281457_) {
        FormattedCharSequence formattedcharsequence = getVisualOrderText(p_282456_);
        drawString(guiGraphics, p_282901_, formattedcharsequence, p_283083_ - p_282901_.width(formattedcharsequence) / 2, p_282276_, p_281457_);
    }

    public static FormattedCharSequence getVisualOrderText(Component component) {
        if (component instanceof MutableComponent mutable) {
            Language $$0 = Language.getInstance();
            if (mutable.decomposedWith != $$0) {
                mutable.visualOrderText = $$0.getVisualOrder(mutable);
                mutable.decomposedWith = $$0;
            }
            return mutable.visualOrderText;
        }
        return component.getVisualOrderText();
    }

    public static int drawString(GuiGraphics graphics, Font p_281653_, Component p_283140_, int p_283102_, int p_282347_, int p_281429_) {
        return drawString(graphics, p_281653_, p_283140_, p_283102_, p_282347_, p_281429_, true);
    }

    public static int drawString(GuiGraphics graphics, Font p_281547_, Component p_282131_, int p_282857_, int p_281250_, int p_282195_, boolean p_282791_) {
        return drawString(graphics, p_281547_, p_282131_.getVisualOrderText(), p_282857_, p_281250_, p_282195_, p_282791_);
    }

    public static int drawString(GuiGraphics guiGraphics, Font p_283019_, FormattedCharSequence p_283376_, int p_283379_, int p_283346_, int p_282119_) {
        return drawString(guiGraphics, p_283019_, p_283376_, p_283379_, p_283346_, p_282119_, true);
    }

    public static int drawString(GuiGraphics guiGraphics, Font p_282636_, FormattedCharSequence p_281596_, int p_281586_, int p_282816_, int p_281743_, boolean p_282394_) {
        return drawString(guiGraphics, p_282636_, p_281596_, (float) p_281586_, (float) p_282816_, p_281743_, p_282394_);
    }

    public static int drawString(GuiGraphics guiGraphics, Font p_282636_, FormattedCharSequence p_281596_, float p_281586_, float p_282816_, int p_281743_, boolean p_282394_) {
        int i = drawInBatch(p_282636_, p_281596_, p_281586_, p_282816_, p_281743_, p_282394_, guiGraphics.pose.poseStack.getLast().pose, guiGraphics.bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        if (!guiGraphics.managed) {
            flush(guiGraphics);
        }
        return i;
    }

    public static int drawInBatch(Font font, FormattedCharSequence p_273262_, float p_273006_, float p_273254_, int p_273375_, boolean p_273674_, Matrix4f p_273525_, MultiBufferSource p_272624_, Font.DisplayMode p_273418_, int p_273330_, int p_272981_) {
        return drawInternal(font, p_273262_, p_273006_, p_273254_, p_273375_, p_273674_, p_273525_, p_272624_, p_273418_, p_273330_, p_272981_);
    }

    private static final Vector3f SHADOW_OFFSET = new Vector3f(0.0F, 0.0F, 0.03F);

    public static int drawInternal(Font font, FormattedCharSequence p_273025_, float p_273121_, float p_272717_, int p_273653_, boolean p_273531_, Matrix4f p_273265_, MultiBufferSource p_273560_, Font.DisplayMode p_273342_, int p_273373_, int p_273266_) {
        p_273653_ = adjustColor(p_273653_);
        Matrix4f matrix4f = new Matrix4f(p_273265_);
        if (p_273531_) {
            renderText(font, p_273025_, p_273121_, p_272717_, p_273653_, true, p_273265_, p_273560_, p_273342_, p_273373_, p_273266_);
            matrix4f.translate(SHADOW_OFFSET);
        }
        p_273121_ = renderText(font, p_273025_, p_273121_, p_272717_, p_273653_, false, matrix4f, p_273560_, p_273342_, p_273373_, p_273266_);
        return (int) p_273121_ + (p_273531_ ? 1 : 0);
    }

    public static float renderText(Font font, FormattedCharSequence p_273322_, float p_272632_, float p_273541_, int p_273200_, boolean p_273312_, Matrix4f p_273276_, MultiBufferSource p_273392_, Font.DisplayMode p_272625_, int p_273774_, int p_273371_) {
        Font.StringRenderOutput font$stringrenderoutput = font.new StringRenderOutput(p_273392_, p_272632_, p_273541_, p_273200_, p_273312_, p_273276_, p_272625_, p_273371_);
        p_273322_.accept(font$stringrenderoutput);
        return finish(font, font$stringrenderoutput, p_273774_, p_272632_);
    }

    public static float finish(Font font, Font.StringRenderOutput output, int p_92962_, float p_92963_) {
        if (p_92962_ != 0) {
            float f = (float) (p_92962_ >> 24 & 255) / 255.0F;
            float f1 = (float) (p_92962_ >> 16 & 255) / 255.0F;
            float f2 = (float) (p_92962_ >> 8 & 255) / 255.0F;
            float f3 = (float) (p_92962_ & 255) / 255.0F;
            addEffect(output, new BakedGlyph.Effect(p_92963_ - 1.0F, output.y + 9.0F, output.x + 1.0F, output.y - 1.0F, 0.01F, f1, f2, f3, f));
        }
        if (output.effects != null) {
            BakedGlyph bakedglyph = getFontSet(font, Style.DEFAULT_FONT).whiteGlyph;
            VertexConsumer vertexconsumer =
                    output.bufferSource instanceof MultiBufferSource.BufferSource source ?
                            getBuffer(source, bakedglyph.renderType(output.mode)) :
                            output.bufferSource.getBuffer(bakedglyph.renderType(output.mode));
            for (BakedGlyph.Effect bakedglyph$effect : output.effects) {
                renderEffect(bakedglyph, bakedglyph$effect, output.pose, vertexconsumer, output.packedLightCoords);
            }
        }
        return output.x;
    }

    public static FontSet getFontSet(Font font, ResourceLocation p_92864_) {
        return font.fonts.apply(p_92864_);
    }

    public static void renderEffect(BakedGlyph glyph, BakedGlyph.Effect p_95221_, Matrix4f p_254370_, VertexConsumer p_95223_, int p_95224_) {
        if (p_95223_ instanceof BufferBuilder builder) {
            endVertex(uv2VertexConsumer(uvBufferVertexConsumer(colorVertexConsumer(vertexVertexConsumer(builder, p_254370_, p_95221_.x0, p_95221_.y0, p_95221_.depth), p_95221_.r, p_95221_.g, p_95221_.b, p_95221_.a), glyph.u0, glyph.v0), p_95224_));
            endVertex(uv2VertexConsumer(uvBufferVertexConsumer(colorVertexConsumer(vertexVertexConsumer(builder, p_254370_, p_95221_.x1, p_95221_.y0, p_95221_.depth), p_95221_.r, p_95221_.g, p_95221_.b, p_95221_.a), glyph.u0, glyph.v1), p_95224_));
            endVertex(uv2VertexConsumer(uvBufferVertexConsumer(colorVertexConsumer(vertexVertexConsumer(builder, p_254370_, p_95221_.x1, p_95221_.y1, p_95221_.depth), p_95221_.r, p_95221_.g, p_95221_.b, p_95221_.a), glyph.u1, glyph.v1), p_95224_));
            endVertex(uv2VertexConsumer(uvBufferVertexConsumer(colorVertexConsumer(vertexVertexConsumer(builder, p_254370_, p_95221_.x0, p_95221_.y1, p_95221_.depth), p_95221_.r, p_95221_.g, p_95221_.b, p_95221_.a), glyph.u1, glyph.v0), p_95224_));
        } else {
            p_95223_.vertex(p_254370_, p_95221_.x0, p_95221_.y0, p_95221_.depth).color(p_95221_.r, p_95221_.g, p_95221_.b, p_95221_.a).uv(glyph.u0, glyph.v0).uv2(p_95224_).endVertex();
            p_95223_.vertex(p_254370_, p_95221_.x1, p_95221_.y0, p_95221_.depth).color(p_95221_.r, p_95221_.g, p_95221_.b, p_95221_.a).uv(glyph.u0, glyph.v1).uv2(p_95224_).endVertex();
            p_95223_.vertex(p_254370_, p_95221_.x1, p_95221_.y1, p_95221_.depth).color(p_95221_.r, p_95221_.g, p_95221_.b, p_95221_.a).uv(glyph.u1, glyph.v1).uv2(p_95224_).endVertex();
            p_95223_.vertex(p_254370_, p_95221_.x0, p_95221_.y1, p_95221_.depth).color(p_95221_.r, p_95221_.g, p_95221_.b, p_95221_.a).uv(glyph.u1, glyph.v0).uv2(p_95224_).endVertex();
        }
    }

    public static void addEffect(Font.StringRenderOutput output, BakedGlyph.Effect p_92965_) {
        if (output.effects == null) {
            output.effects = Lists.newArrayList();
        }
        output.effects.add(p_92965_);
    }

    public static int adjustColor(int p_92720_) {
        return (p_92720_ & -67108864) == 0 ? p_92720_ | -16777216 : p_92720_;
    }

    public static void blit(GuiGraphics guiGraphics, ResourceLocation p_283377_, int p_281970_, int p_282111_, int p_283134_, int p_282778_, int p_281478_, int p_281821_) {
        blit(guiGraphics, p_283377_, p_281970_, p_282111_, 0, (float) p_283134_, (float) p_282778_, p_281478_, p_281821_, 256, 256);
    }

    public static void blit(GuiGraphics guiGraphics, ResourceLocation p_283573_, int p_283574_, int p_283670_, int p_283545_, float p_283029_, float p_283061_, int p_282845_, int p_282558_, int p_282832_, int p_281851_) {
        blit(guiGraphics, p_283573_, p_283574_, p_283574_ + p_282845_, p_283670_, p_283670_ + p_282558_, p_283545_, p_282845_, p_282558_, p_283029_, p_283061_, p_282832_, p_281851_);
    }

    public static void blit(GuiGraphics guiGraphics, ResourceLocation p_283272_, int p_283605_, int p_281879_, float p_282809_, float p_282942_, int p_281922_, int p_282385_, int p_282596_, int p_281699_) {
        blit(guiGraphics, p_283272_, p_283605_, p_281879_, p_281922_, p_282385_, p_282809_, p_282942_, p_281922_, p_282385_, p_282596_, p_281699_);
    }

    public static void blit(GuiGraphics guiGraphics, ResourceLocation p_282034_, int p_283671_, int p_282377_, int p_282058_, int p_281939_, float p_282285_, float p_283199_, int p_282186_, int p_282322_, int p_282481_, int p_281887_) {
        blit(guiGraphics, p_282034_, p_283671_, p_283671_ + p_282058_, p_282377_, p_282377_ + p_281939_, 0, p_282186_, p_282322_, p_282285_, p_283199_, p_282481_, p_281887_);
    }

    public static void blit(GuiGraphics guiGraphics, ResourceLocation p_282639_, int p_282732_, int p_283541_, int p_281760_, int p_283298_, int p_283429_, int p_282193_, int p_281980_, float p_282660_, float p_281522_, int p_282315_, int p_281436_) {
        innerBlit(guiGraphics, p_282639_, p_282732_, p_283541_, p_281760_, p_283298_, p_283429_, (p_282660_ + 0.0F) / (float) p_282315_, (p_282660_ + (float) p_282193_) / (float) p_282315_, (p_281522_ + 0.0F) / (float) p_281436_, (p_281522_ + (float) p_281980_) / (float) p_281436_);
    }

    private static void innerBlit(GuiGraphics guiGraphics, ResourceLocation p_283461_, int p_281399_, int p_283222_, int p_283615_, int p_283430_, int p_281729_, float p_283247_, float p_282598_, float p_282883_, float p_283017_) {
        RenderSystem.setShaderTexture(0, p_283461_);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix4f = guiGraphics.pose.poseStack.getLast().pose;
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        begin(bufferbuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        endVertex(uvBufferVertexConsumer(vertexVertexConsumer(bufferbuilder, matrix4f, (float) p_281399_, (float) p_283615_, (float) p_281729_), p_283247_, p_282883_));
        endVertex(uvBufferVertexConsumer(vertexVertexConsumer(bufferbuilder, matrix4f, (float) p_281399_, (float) p_283430_, (float) p_281729_), p_283247_, p_283017_));
        endVertex(uvBufferVertexConsumer(vertexVertexConsumer(bufferbuilder, matrix4f, (float) p_283222_, (float) p_283430_, (float) p_281729_), p_282598_, p_283017_));
        endVertex(uvBufferVertexConsumer(vertexVertexConsumer(bufferbuilder, matrix4f, (float) p_283222_, (float) p_283615_, (float) p_281729_), p_282598_, p_282883_));
        drawWithShader(end(bufferbuilder));
    }

    public static void drawWithShader(BufferBuilder.RenderedBuffer p_231203_) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(new RenderCall1(p_231203_));
        } else {
            _drawWithShader(p_231203_);
        }
    }


    public static void _drawWithShader(BufferBuilder.RenderedBuffer p_231212_) {
        //System.out.println("isafhasfhhsa");
        VertexBuffer $$1 = upload(p_231212_);
        if ($$1 != null) {
            drawWithShader($$1, RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
        }
    }


    public static void drawWithShader(VertexBuffer buffer, Matrix4f p_254480_, Matrix4f p_254555_, ShaderInstance p_253993_) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                _drawWithShader(buffer, new Matrix4f(p_254480_), new Matrix4f(p_254555_), p_253993_);
            });
        } else {
            _drawWithShader(buffer, p_254480_, p_254555_, p_253993_);
        }
    }

    private static void _drawWithShader(VertexBuffer buffer, Matrix4f p_253705_, Matrix4f p_253737_, ShaderInstance p_166879_) {
        for (int $$3 = 0; $$3 < 12; ++$$3) {
            int $$4 = RenderSystem.getShaderTexture($$3);
            p_166879_.setSampler("Sampler" + $$3, $$4);
        }

        if (p_166879_.MODEL_VIEW_MATRIX != null) {
            p_166879_.MODEL_VIEW_MATRIX.set(p_253705_);
        }

        if (p_166879_.PROJECTION_MATRIX != null) {
            p_166879_.PROJECTION_MATRIX.set(p_253737_);
        }

        if (p_166879_.INVERSE_VIEW_ROTATION_MATRIX != null) {
            p_166879_.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
        }

        if (p_166879_.COLOR_MODULATOR != null) {
            p_166879_.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
        }

        if (p_166879_.GLINT_ALPHA != null) {
            p_166879_.GLINT_ALPHA.set(RenderSystem.getShaderGlintAlpha());
        }

        if (p_166879_.FOG_START != null) {
            p_166879_.FOG_START.set(RenderSystem.getShaderFogStart());
        }

        if (p_166879_.FOG_END != null) {
            p_166879_.FOG_END.set(RenderSystem.getShaderFogEnd());
        }

        if (p_166879_.FOG_COLOR != null) {
            p_166879_.FOG_COLOR.set(RenderSystem.getShaderFogColor());
        }

        if (p_166879_.FOG_SHAPE != null) {
            p_166879_.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
        }

        if (p_166879_.TEXTURE_MATRIX != null) {
            p_166879_.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
        }

        if (p_166879_.GAME_TIME != null) {
            p_166879_.GAME_TIME.set(RenderSystem.getShaderGameTime());
        }

        if (p_166879_.SCREEN_SIZE != null) {
            Window $$5 = Minecraft.getInstance().getWindow();
            p_166879_.SCREEN_SIZE.set((float) $$5.getWidth(), (float) $$5.getHeight());
        }

        if (p_166879_.LINE_WIDTH != null && (buffer.mode == VertexFormat.Mode.LINES || buffer.mode == VertexFormat.Mode.LINE_STRIP)) {
            p_166879_.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
        }
        RenderSystem.setupShaderLights(p_166879_);
        p_166879_.apply();
        GL11C.nglDrawElements(buffer.mode.asGLMode, buffer.indexCount, getIndexType(buffer).asGLType, 0);
        p_166879_.clear();
    }

    private static VertexFormat.IndexType getIndexType(VertexBuffer buffer) {
        RenderSystem.AutoStorageIndexBuffer $$0 = buffer.sequentialIndices;
        return $$0 != null ? $$0.type() : buffer.indexType;
    }


    private static VertexBuffer upload(BufferBuilder.RenderedBuffer p_231214_) {
        //RenderSystem.assertOnRenderThread();
        if (p_231214_.drawState.vertexCount == 0) {
            release(p_231214_);
            return null;
        } else {
            VertexBuffer $$1 = bindImmediateBuffer(p_231214_.drawState.format);
            upload($$1, p_231214_);
            return $$1;
        }
    }

    public static void upload(VertexBuffer buffer, BufferBuilder.RenderedBuffer p_231222_) {
        if (buffer.arrayObjectId != -1) {
            try {
                BufferBuilder.DrawState $$1 = p_231222_.drawState;
                buffer.format = uploadVertexBuffer(buffer, $$1, vertexBuffer(p_231222_));
                buffer.sequentialIndices = uploadIndexBuffer(buffer, $$1, indexBuffer(p_231222_));
                buffer.indexCount = $$1.indexCount;
                buffer.indexType = $$1.indexType;
                buffer.mode = $$1.mode;
            } finally {
                release(p_231222_);
            }
        }
    }

    private static VertexFormat uploadVertexBuffer(VertexBuffer buffer, BufferBuilder.DrawState p_231219_, ByteBuffer p_231220_) {
        boolean $$2 = false;
        if (!p_231219_.format.equals(buffer.format)) {
            if (buffer.format != null) {
                buffer.format.clearBufferState();
            }
            GL15C.glBindBuffer(34962, buffer.vertexBufferId);
            p_231219_.format.setupBufferState();
            $$2 = true;
        }
        if (!p_231219_.indexOnly) {
            if (!$$2) {
                GL15C.glBindBuffer(34962, buffer.vertexBufferId);
            }
            GL15C.nglBufferData(34962, p_231220_.remaining(), MemoryUtil.memAddress(p_231220_), buffer.usage.id);
        }
        return p_231219_.format();
    }

    private static RenderSystem.AutoStorageIndexBuffer uploadIndexBuffer(VertexBuffer buffer, BufferBuilder.DrawState p_231224_, ByteBuffer p_231225_) {
        if (!p_231224_.sequentialIndex()) {
            GL15C.glBindBuffer(34963, buffer.indexBufferId);
            GL15C.glBufferData(34963, p_231225_, buffer.usage.id);
            return null;
        } else {
            RenderSystem.AutoStorageIndexBuffer $$2 = RenderSystem.getSequentialBuffer(p_231224_.mode);
            if ($$2 != buffer.sequentialIndices || !$$2.hasStorage(p_231224_.indexCount)) {
                $$2.bind(p_231224_.indexCount);
            }
            return $$2;
        }
    }

    private static VertexBuffer bindImmediateBuffer(VertexFormat p_231207_) {
        VertexBuffer $$1 = p_231207_.getImmediateDrawVertexBuffer();
        bindImmediateBuffer($$1);
        return $$1;
    }

    private static void bindImmediateBuffer(VertexBuffer p_231205_) {
        if (p_231205_ != BufferUploader.lastImmediateBuffer) {
            bind(p_231205_);
            BufferUploader.lastImmediateBuffer = p_231205_;
        }

    }

    public static void bind(VertexBuffer buffer) {
        BufferUploader.lastImmediateBuffer = null;
        GL30C.glBindVertexArray(buffer.arrayObjectId);
    }

    public static void renderComponentHoverEffect(GuiGraphics guiGraphics, Font p_282584_, @Nullable Style p_282156_, int p_283623_, int p_282114_) {
        if (p_282156_ != null && p_282156_.getHoverEvent() != null) {
            HoverEvent hoverevent = p_282156_.getHoverEvent();
            HoverEvent.ItemStackInfo hoverevent$itemstackinfo = hoverevent.getValue(HoverEvent.Action.SHOW_ITEM);
            if (hoverevent$itemstackinfo != null) {
                renderTooltip(guiGraphics, p_282584_, hoverevent$itemstackinfo.getItemStack(), p_283623_, p_282114_);
            } else {
                HoverEvent.EntityTooltipInfo hoverevent$entitytooltipinfo = hoverevent.getValue(HoverEvent.Action.SHOW_ENTITY);
                if (hoverevent$entitytooltipinfo != null) {
                    if (guiGraphics.minecraft.options.advancedItemTooltips) {
                        renderComponentTooltip(guiGraphics, p_282584_, hoverevent$entitytooltipinfo.getTooltipLines(), p_283623_, p_282114_);
                    }
                } else {
                    Component component = hoverevent.getValue(HoverEvent.Action.SHOW_TEXT);
                    if (component != null) {
                        renderTooltip(guiGraphics, p_282584_, p_282584_.split(component, Math.max(guiGraphics.guiWidth() / 2, 200)), p_283623_, p_282114_);
                    }
                }
            }
        }

    }

    private static void renderTooltip(GuiGraphics guiGraphics, Font p_282308_, ItemStack p_282781_, int p_282687_, int p_282292_) {
        guiGraphics.tooltipStack = p_282781_;
        renderTooltip(guiGraphics, p_282308_, Screen.getTooltipFromItem(guiGraphics.minecraft, p_282781_), p_282781_.getTooltipImage(), p_282687_, p_282292_);
        guiGraphics.tooltipStack = ItemStack.EMPTY;
    }

    private static void renderTooltip(GuiGraphics guiGraphics, Font p_282192_, List<? extends FormattedCharSequence> p_282297_, int p_281680_, int p_283325_) {
        renderTooltipInternal(guiGraphics, p_282192_, p_282297_.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()), p_281680_, p_283325_, DefaultTooltipPositioner.INSTANCE);
    }

    private static void renderComponentTooltip(GuiGraphics guiGraphics, Font p_282739_, List<Component> p_281832_, int p_282191_, int p_282446_) {
        List<ClientTooltipComponent> components = ForgeHooksClient.gatherTooltipComponents(guiGraphics.tooltipStack, p_281832_, p_282191_, guiGraphics.guiWidth(), guiGraphics.guiHeight(), p_282739_);
        renderTooltipInternal(guiGraphics, p_282739_, components, p_282191_, p_282446_, DefaultTooltipPositioner.INSTANCE);
    }

    public static void renderTooltip(GuiGraphics guiGraphics, Font p_283128_, List<Component> p_282716_, Optional<TooltipComponent> p_281682_, int p_283678_, int p_281696_) {
        List<ClientTooltipComponent> list = ForgeHooksClient.gatherTooltipComponents(guiGraphics.tooltipStack, p_282716_, p_281682_, p_283678_, guiGraphics.guiWidth(), guiGraphics.guiHeight(), p_283128_);
        renderTooltipInternal(guiGraphics, p_283128_, list, p_283678_, p_281696_, DefaultTooltipPositioner.INSTANCE);
    }

    public static void renderTooltip(GuiGraphics guiGraphics, Font p_281627_, List<FormattedCharSequence> p_283313_, ClientTooltipPositioner p_283571_, int p_282367_, int p_282806_) {
        renderTooltipInternal(guiGraphics, p_281627_, p_283313_.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()), p_282367_, p_282806_, p_283571_);
    }

    private static void renderTooltipInternal(GuiGraphics guiGraphics, Font p_282675_, List<ClientTooltipComponent> p_282615_, int p_283230_, int p_283417_, ClientTooltipPositioner p_282442_) {
        if (!p_282615_.isEmpty()) {
            int i = 0;
            int j = p_282615_.size() == 1 ? -2 : 0;
            for (ClientTooltipComponent clienttooltipcomponent : p_282615_) {
                int k = clienttooltipcomponent.getWidth(p_282675_);
                if (k > i) {
                    i = k;
                }

                j += clienttooltipcomponent.getHeight();
            }

            int i2 = i;
            int j2 = j;
            Vector2ic vector2ic = p_282442_.positionTooltip(guiGraphics.guiWidth(), guiGraphics.guiHeight(), p_283230_, p_283417_, i2, j2);
            int l = vector2ic.x();
            int i1 = vector2ic.y();
            {
                PoseStack.Pose posestack$pose = guiGraphics.pose.poseStack.getLast();
                guiGraphics.pose.poseStack.addLast(new PoseStack.Pose(new Matrix4f(posestack$pose.pose), new Matrix3f(posestack$pose.normal)));
            }
            flush(guiGraphics);
            guiGraphics.managed = true;
            TooltipRenderUtil.renderTooltipBackground(guiGraphics, l, i1, i2, j2, 400, 0xf0100010, 0xf0100010, 0x505000FF, 0x5028007f);
            guiGraphics.managed = false;
            flush(guiGraphics);
            {
                PoseStack.Pose posestack$pose = guiGraphics.pose.poseStack.getLast();
                posestack$pose.pose.translate(0, 0, 400F);
            }
            int k1 = i1;
            for (int l1 = 0; l1 < p_282615_.size(); ++l1) {
                ClientTooltipComponent clienttooltipcomponent1 = p_282615_.get(l1);
                clienttooltipcomponent1.renderText(p_282675_, l, k1, guiGraphics.pose.poseStack.getLast().pose, guiGraphics.bufferSource);
                k1 += clienttooltipcomponent1.getHeight() + (l1 == 0 ? 2 : 0);
            }
            k1 = i1;
            for (int k2 = 0; k2 < p_282615_.size(); ++k2) {
                ClientTooltipComponent clienttooltipcomponent2 = p_282615_.get(k2);
                clienttooltipcomponent2.renderImage(p_282675_, l, k1, guiGraphics);
                k1 += clienttooltipcomponent2.getHeight() + (k2 == 0 ? 2 : 0);
            }
            guiGraphics.pose.poseStack.removeLast();
        }
    }

    public static void setEnabled(GlStateManager.BooleanState booleanState, boolean p_84591_) {
        if (p_84591_ != booleanState.enabled) {
            booleanState.enabled = p_84591_;
            if (p_84591_) {
                GL11C.glEnable(booleanState.state);
            } else {
                GL11C.glDisable(booleanState.state);
            }
        }
    }

    /*=
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * =
     * */
    public static void ensureVertexCapacity(BufferBuilder builder) {
        ensureCapacity(builder, builder.format.getVertexSize());
    }

    public static void ensureCapacity(BufferBuilder builder, int p_85723_) {
        if (builder.nextElementByte + p_85723_ > builder.buffer.capacity()) {
            int i = builder.buffer.capacity();
            int j = i + roundUp(p_85723_);
            BufferBuilder.LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", i, j);
            ByteBuffer bytebuffer = MemoryTracker.resize(builder.buffer, j);
            bytebuffer.rewind();
            builder.buffer = bytebuffer;
        }
    }

    public static int roundUp(int p_85726_) {
        int i = 2097152;
        if (p_85726_ == 0) {
            return i;
        } else {
            if (p_85726_ < 0) {
                i *= -1;
            }
            int j = p_85726_ % i;
            return j == 0 ? p_85726_ : p_85726_ + i - j;
        }
    }

    public static void setQuadSorting(BufferBuilder builder, VertexSorting p_277454_) {
        if (builder.mode == VertexFormat.Mode.QUADS) {
            builder.sorting = p_277454_;
            if (builder.sortingPoints == null) {
                builder.sortingPoints = makeQuadSortingPoints(builder);
            }

        }
    }

    public static BufferBuilder.SortState getSortState(BufferBuilder builder) {
        return new BufferBuilder.SortState(builder.mode, builder.vertices, builder.sortingPoints, builder.sorting);
    }

    public static void restoreSortState(BufferBuilder builder, BufferBuilder.SortState p_166776_) {
        builder.buffer.rewind();
        builder.mode = p_166776_.mode;
        builder.vertices = p_166776_.vertices;
        builder.nextElementByte = builder.renderedBufferPointer;
        builder.sortingPoints = p_166776_.sortingPoints;
        builder.sorting = p_166776_.sorting;
        builder.indexOnly = true;
    }

    public static void begin(BufferBuilder builder, VertexFormat.Mode p_166780_, VertexFormat p_166781_) {
        if (builder.building) {
            throw new IllegalStateException("Already building!");
        } else {
            builder.building = true;
            builder.mode = p_166780_;
            switchFormat(builder, p_166781_);
            builder.currentElement = p_166781_.getElements().get(0);
            builder.elementIndex = 0;
            builder.buffer.rewind();
        }
    }

    public static void switchFormat(BufferBuilder builder, VertexFormat p_85705_) {
        if (builder.format != p_85705_) {
            builder.format = p_85705_;
            boolean flag = p_85705_ == DefaultVertexFormat.NEW_ENTITY;
            boolean flag1 = p_85705_ == DefaultVertexFormat.BLOCK;
            builder.fastFormat = flag || flag1;
            builder.fullFormat = flag;
        }
    }


    public static Vector3f[] makeQuadSortingPoints(BufferBuilder builder) {
        FloatBuffer floatbuffer = builder.buffer.asFloatBuffer();
        int i = builder.renderedBufferPointer / 4;
        int j = builder.format.getIntegerSize();
        int k = j * builder.mode.primitiveStride;
        int l = builder.vertices / builder.mode.primitiveStride;
        Vector3f[] avector3f = new Vector3f[l];
        for (int i1 = 0; i1 < l; ++i1) {
            float f = floatbuffer.get(i + i1 * k);
            float f1 = floatbuffer.get(i + i1 * k + 1);
            float f2 = floatbuffer.get(i + i1 * k + 2);
            float f3 = floatbuffer.get(i + i1 * k + j * 2);
            float f4 = floatbuffer.get(i + i1 * k + j * 2 + 1);
            float f5 = floatbuffer.get(i + i1 * k + j * 2 + 2);
            float f6 = (f + f3) / 2.0F;
            float f7 = (f1 + f4) / 2.0F;
            float f8 = (f2 + f5) / 2.0F;
            avector3f[i1] = new Vector3f(f6, f7, f8);
        }
        return avector3f;
    }

    public static IntConsumer intConsumer(BufferBuilder builder, int p_231159_, VertexFormat.IndexType p_231160_) {
        MutableInt mutableint = new MutableInt(p_231159_);
        IntConsumer intconsumer;
        return switch (p_231160_) {
            case SHORT -> new IntConsumer1(builder, mutableint);
            case INT -> new IntConsumer2(builder, mutableint);
        };
    }

    public static void putSortedQuadIndices(BufferBuilder builder, VertexFormat.IndexType p_166787_) {
        if (builder.sortingPoints != null && builder.sorting != null) {
            int[] aint = builder.sorting.sort(builder.sortingPoints);
            IntConsumer intconsumer = intConsumer(builder, builder.nextElementByte, p_166787_);
            for (int i : aint) {
                intconsumer.accept(i * builder.mode.primitiveStride);
                intconsumer.accept(i * builder.mode.primitiveStride + 1);
                intconsumer.accept(i * builder.mode.primitiveStride + 2);
                intconsumer.accept(i * builder.mode.primitiveStride + 2);
                intconsumer.accept(i * builder.mode.primitiveStride + 3);
                intconsumer.accept(i * builder.mode.primitiveStride);
            }
        } else {
            throw new IllegalStateException("Sorting state uninitialized");
        }
    }

    public static boolean isCurrentBatchEmpty(BufferBuilder builder) {
        return builder.vertices == 0;
    }

    @Nullable
    public static BufferBuilder.RenderedBuffer endOrDiscardIfEmpty(BufferBuilder builder) {
        ensureDrawing(builder);
        if (isCurrentBatchEmpty(builder)) {
            reset(builder);
            return null;
        } else {
            BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = storeRenderedBuffer(builder);
            reset(builder);
            return bufferbuilder$renderedbuffer;
        }
    }

    public static BufferBuilder.RenderedBuffer end(BufferBuilder builder) {
        ensureDrawing(builder);
        BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = storeRenderedBuffer(builder);
        reset(builder);
        return bufferbuilder$renderedbuffer;
    }

    public static void ensureDrawing(BufferBuilder builder) {
        if (!builder.building) {
            throw new IllegalStateException("Not building!");
        }
    }

    public static BufferBuilder.RenderedBuffer storeRenderedBuffer(BufferBuilder builder) {
        int i = builder.mode.indexCount(builder.vertices);
        int j = !builder.indexOnly ? builder.vertices * builder.format.getVertexSize() : 0;
        VertexFormat.IndexType vertexformat$indextype = VertexFormat.IndexType.least(i);
        boolean flag;
        int k;
        if (builder.sortingPoints != null) {
            int l = Mth.roundToward(i * vertexformat$indextype.bytes, 4);
            ensureCapacity(builder, l);
            putSortedQuadIndices(builder, vertexformat$indextype);
            flag = false;
            builder.nextElementByte += l;
            k = j + l;
        } else {
            flag = true;
            k = j;
        }

        int i1 = builder.renderedBufferPointer;
        builder.renderedBufferPointer += k;
        ++builder.renderedBufferCount;
        BufferBuilder.DrawState bufferbuilder$drawstate = new BufferBuilder.DrawState(builder.format, builder.vertices, i, builder.mode, vertexformat$indextype, builder.indexOnly, flag);
        return builder.new RenderedBuffer(i1, bufferbuilder$drawstate);
    }

    public static void reset(BufferBuilder builder) {
        builder.building = false;
        builder.vertices = 0;
        builder.currentElement = null;
        builder.elementIndex = 0;
        builder.sortingPoints = null;
        builder.sorting = null;
        builder.indexOnly = false;
    }

    public static void putByte(BufferBuilder builder, int p_85686_, byte p_85687_) {
        builder.buffer.put(builder.nextElementByte + p_85686_, p_85687_);
    }

    public static void putShort(BufferBuilder builder, int p_85700_, short p_85701_) {
        builder.buffer.putShort(builder.nextElementByte + p_85700_, p_85701_);
    }

    public static void putFloat(BufferBuilder builder, int p_85689_, float p_85690_) {
        builder.buffer.putFloat(builder.nextElementByte + p_85689_, p_85690_);
    }

    public static void endVertex(BufferBuilder builder) {
        if (builder.elementIndex != 0) {
            throw new IllegalStateException("Not filled all elements of the vertex");
        } else {
            ++builder.vertices;
            ensureVertexCapacity(builder);
            if (builder.mode == VertexFormat.Mode.LINES || builder.mode == VertexFormat.Mode.LINE_STRIP) {
                int i = builder.format.getVertexSize();
                builder.buffer.put(builder.nextElementByte, builder.buffer, builder.nextElementByte - i, i);
                builder.nextElementByte += i;
                ++builder.vertices;
                ensureVertexCapacity(builder);
            }

        }
    }

    public static void nextElement(BufferBuilder builder) {
        ImmutableList<VertexFormatElement> immutablelist = builder.format.getElements();
        builder.elementIndex = (builder.elementIndex + 1) % immutablelist.size();
        builder.nextElementByte += builder.currentElement.getByteSize();
        VertexFormatElement vertexformatelement = immutablelist.get(builder.elementIndex);
        builder.currentElement = vertexformatelement;
        if (vertexformatelement.getUsage() == VertexFormatElement.Usage.PADDING) {
            nextElement(builder);
        }

        if (builder.defaultColorSet && builder.currentElement.getUsage() == VertexFormatElement.Usage.COLOR) {
            colorBufferVertexConsumer(builder, builder.defaultR, builder.defaultG, builder.defaultB, builder.defaultA);
        }

    }

    public static BufferBuilder color(BufferBuilder builder, int p_85692_, int p_85693_, int p_85694_, int p_85695_) {
        if (builder.defaultColorSet) {
            throw new IllegalStateException();
        } else {
            return colorBufferVertexConsumer(builder, p_85692_, p_85693_, p_85694_, p_85695_);
        }
    }

    public static void vertex(BufferBuilder builder, float p_85671_, float p_85672_, float p_85673_, float p_85674_, float p_85675_, float p_85676_, float p_85677_, float p_85678_, float p_85679_, int p_85680_, int p_85681_, float p_85682_, float p_85683_, float p_85684_) {
        if (builder.defaultColorSet) {
            throw new IllegalStateException();
        } else if (builder.fastFormat) {
            putFloat(builder, 0, p_85671_);
            putFloat(builder, 4, p_85672_);
            putFloat(builder, 8, p_85673_);
            putByte(builder, 12, (byte) ((int) (p_85674_ * 255.0F)));
            putByte(builder, 13, (byte) ((int) (p_85675_ * 255.0F)));
            putByte(builder, 14, (byte) ((int) (p_85676_ * 255.0F)));
            putByte(builder, 15, (byte) ((int) (p_85677_ * 255.0F)));
            putFloat(builder, 16, p_85678_);
            putFloat(builder, 20, p_85679_);
            int i;
            if (builder.fullFormat) {
                putShort(builder, 24, (short) (p_85680_ & '\uffff'));
                putShort(builder, 26, (short) (p_85680_ >> 16 & '\uffff'));
                i = 28;
            } else {
                i = 24;
            }

            putShort(builder, i, (short) (p_85681_ & '\uffff'));
            putShort(builder, i + 2, (short) (p_85681_ >> 16 & '\uffff'));
            putByte(builder, i + 4, normalIntValue(p_85682_));
            putByte(builder, i + 5, normalIntValue(p_85683_));
            putByte(builder, i + 6, normalIntValue(p_85684_));
            builder.nextElementByte += i + 8;
            endVertex(builder);
        } else {
            superVertex(builder, p_85671_, p_85672_, p_85673_, p_85674_, p_85675_, p_85676_, p_85677_, p_85678_, p_85679_, p_85680_, p_85681_, p_85682_, p_85683_, p_85684_);
        }
    }

    public static void releaseRenderedBuffer(BufferBuilder builder) {
        if (builder.renderedBufferCount > 0 && --builder.renderedBufferCount == 0) {
            clear(builder);
        }

    }

    public static void clear(BufferBuilder builder) {
        if (builder.renderedBufferCount > 0) {
            BufferBuilder.LOGGER.warn("Clearing BufferBuilder with unused batches");
        }

        discard(builder);
    }

    public static void discard(BufferBuilder builder) {
        builder.renderedBufferCount = 0;
        builder.renderedBufferPointer = 0;
        builder.nextElementByte = 0;
    }

    public static VertexFormatElement currentElement(BufferBuilder builder) {
        if (builder.currentElement == null) {
            throw new IllegalStateException("BufferBuilder not started");
        } else {
            return builder.currentElement;
        }
    }

    public static boolean building(BufferBuilder builder) {
        return builder.building;
    }

    static ByteBuffer bufferSlice(BufferBuilder builder, int p_231170_, int p_231171_) {
        return MemoryUtil.memSlice(builder.buffer, p_231170_, p_231171_ - p_231170_);
    }

    public static void putBulkData(BufferBuilder builder, ByteBuffer buffer) {
        ensureCapacity(builder, buffer.limit() + builder.format.getVertexSize());
        builder.buffer.position(builder.nextElementByte);
        builder.buffer.put(buffer);
        builder.buffer.position(0);
        builder.vertices += buffer.limit() / builder.format.getVertexSize();
        builder.nextElementByte += buffer.limit();
    }

    public static ByteBuffer vertexBuffer(BufferBuilder.RenderedBuffer buffer) {
        int i = buffer.pointer + buffer.drawState.vertexBufferStart();
        int j = buffer.pointer + buffer.drawState.vertexBufferEnd();
        return bufferSlice(ClassHelper.getOuterInstance(buffer, BufferBuilder.class, "f_231188_"), i, j);
    }

    public static ByteBuffer indexBuffer(BufferBuilder.RenderedBuffer buffer) {
        int i = buffer.pointer + buffer.drawState.indexBufferStart();
        int j = buffer.pointer + buffer.drawState.indexBufferEnd();
        return bufferSlice(ClassHelper.getOuterInstance(buffer, BufferBuilder.class, "f_231188_"), i, j);
    }


    public static boolean isEmpty(BufferBuilder.RenderedBuffer buffer) {
        return buffer.drawState.vertexCount == 0;
    }

    public static void release(BufferBuilder.RenderedBuffer buffer) {
        if (buffer.released) {
            throw new IllegalStateException("Buffer has already been released!");
        } else {
            releaseRenderedBuffer(ClassHelper.getOuterInstance(buffer, BufferBuilder.class, "f_231188_"));
            buffer.released = true;
        }
    }

    //================super=======================
    //================super=======================
    //================super=======================
    //================super=======================
    //================super=======================
    //================super=======================
    //================super=======================
    //================super=======================

    public static void superVertex(BufferBuilder builder, float p_85955_, float p_85956_, float p_85957_, float p_85958_, float p_85959_, float p_85960_, float p_85961_, float p_85962_, float p_85963_, int p_85964_, int p_85965_, float p_85966_, float p_85967_, float p_85968_) {
        vertexBufferVertexConsumer(builder, (double) p_85955_, (double) p_85956_, (double) p_85957_);
        colorVertexConsumer(builder, p_85958_, p_85959_, p_85960_, p_85961_);
        uvBufferVertexConsumer(builder, p_85962_, p_85963_);
        overlayCoordsVertexConsumer(builder, p_85964_);
        uv2VertexConsumer(builder, p_85965_);
        normalBufferVertexConsumer(builder, p_85966_, p_85967_, p_85968_);
        endVertex(builder);
    }

    public static BufferBuilder colorVertexConsumer(BufferBuilder builder, float p_85951_, float p_85952_, float p_85953_, float p_85954_) {
        return color(builder, (int) (p_85951_ * 255.0F), (int) (p_85952_ * 255.0F), (int) (p_85953_ * 255.0F), (int) (p_85954_ * 255.0F));
    }

    public static BufferBuilder vertexBufferVertexConsumer(BufferBuilder builder, double p_85771_, double p_85772_, double p_85773_) {
        if (currentElement(builder).getUsage() != VertexFormatElement.Usage.POSITION) {
            return builder;
        } else if (currentElement(builder).getType() == VertexFormatElement.Type.FLOAT && currentElement(builder).getCount() == 3) {
            putFloat(builder, 0, (float) p_85771_);
            putFloat(builder, 4, (float) p_85772_);
            putFloat(builder, 8, (float) p_85773_);
            nextElement(builder);
            return builder;
        } else {
            throw new IllegalStateException();
        }
    }

    public static BufferBuilder uvBufferVertexConsumer(BufferBuilder builder, float p_85777_, float p_85778_) {
        VertexFormatElement vertexformatelement = currentElement(builder);
        if (vertexformatelement.getUsage() == VertexFormatElement.Usage.UV && vertexformatelement.getIndex() == 0) {
            if (vertexformatelement.getType() == VertexFormatElement.Type.FLOAT && vertexformatelement.getCount() == 2) {
                putFloat(builder, 0, p_85777_);
                putFloat(builder, 4, p_85778_);
                nextElement(builder);
                return builder;
            } else {
                throw new IllegalStateException();
            }
        } else {
            return builder;
        }
    }

    public static BufferBuilder uv2VertexConsumer(BufferBuilder builder, int p_85970_) {
        return uv2BufferVertexConsumer(builder, p_85970_ & '\uffff', p_85970_ >> 16 & '\uffff');
    }

    public static BufferBuilder uv2BufferVertexConsumer(BufferBuilder builder, int p_85802_, int p_85803_) {
        return uvShortBufferVertexConsumer(builder, (short) p_85802_, (short) p_85803_, 2);
    }

    public static BufferBuilder overlayCoordsVertexConsumer(BufferBuilder builder, int p_86009_) {
        return overlayCoordsBufferVertexConsumer(builder, p_86009_ & '\uffff', p_86009_ >> 16 & '\uffff');
    }

    public static BufferBuilder overlayCoordsBufferVertexConsumer(BufferBuilder builder, int p_85784_, int p_85785_) {
        return uvShortBufferVertexConsumer(builder, (short) p_85784_, (short) p_85785_, 1);
    }

    public static BufferBuilder uvShortBufferVertexConsumer(BufferBuilder builder, short p_85794_, short p_85795_, int p_85796_) {
        VertexFormatElement vertexformatelement = currentElement(builder);
        if (vertexformatelement.getUsage() == VertexFormatElement.Usage.UV && vertexformatelement.getIndex() == p_85796_) {
            if (vertexformatelement.getType() == VertexFormatElement.Type.SHORT && vertexformatelement.getCount() == 2) {
                putShort(builder, 0, p_85794_);
                putShort(builder, 2, p_85795_);
                nextElement(builder);
                return builder;
            } else {
                throw new IllegalStateException();
            }
        } else {
            return builder;
        }
    }

    public static BufferBuilder normalBufferVertexConsumer(BufferBuilder builder, float p_85798_, float p_85799_, float p_85800_) {
        VertexFormatElement vertexformatelement = currentElement(builder);
        if (vertexformatelement.getUsage() != VertexFormatElement.Usage.NORMAL) {
            return builder;
        } else if (vertexformatelement.getType() == VertexFormatElement.Type.BYTE && vertexformatelement.getCount() == 3) {
            putByte(builder, 0, normalIntValue(p_85798_));
            putByte(builder, 1, normalIntValue(p_85799_));
            putByte(builder, 2, normalIntValue(p_85800_));
            nextElement(builder);
            return builder;
        } else {
            throw new IllegalStateException();
        }
    }

    public static byte normalIntValue(float p_85775_) {
        return (byte) ((int) (Mth.clamp(p_85775_, -1.0F, 1.0F) * 127.0F) & 255);
    }

    public static BufferBuilder colorBufferVertexConsumer(BufferBuilder builder, int p_85787_, int p_85788_, int p_85789_, int p_85790_) {
        VertexFormatElement vertexformatelement = currentElement(builder);
        if (vertexformatelement.getUsage() != VertexFormatElement.Usage.COLOR) {
            return builder;
        } else if (vertexformatelement.getType() == VertexFormatElement.Type.UBYTE && vertexformatelement.getCount() == 4) {
            putByte(builder, 0, (byte) p_85787_);
            putByte(builder, 1, (byte) p_85788_);
            putByte(builder, 2, (byte) p_85789_);
            putByte(builder, 3, (byte) p_85790_);
            nextElement(builder);
            return builder;
        } else {
            throw new IllegalStateException();
        }
    }

    public static BufferBuilder vertexVertexConsumer(BufferBuilder builder, Matrix4f p_254075_, float p_254519_, float p_253869_, float p_253980_) {
        Vector4f vector4f = p_254075_.transform(new Vector4f(p_254519_, p_253869_, p_253980_, 1.0F));
        return vertexBufferVertexConsumer(builder, (double) vector4f.x(), (double) vector4f.y(), (double) vector4f.z());
    }
}
