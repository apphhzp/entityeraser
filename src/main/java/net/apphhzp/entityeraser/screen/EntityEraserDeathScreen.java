package net.apphhzp.entityeraser.screen;

import net.apphhzp.entityeraser.shitmountain.EntityEraserRenderers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class EntityEraserDeathScreen extends DeathScreen {
    public EntityEraserDeathScreen(Player player) {
        super(Component.translatable("death.deathOfCause",player==null?Component.literal("<null>"):player.getName()) , false);
        if (player != null) {
            this.causeOfDeath=Component.translatable("death.deathOfCause",player.getName());
        }else {
            this.causeOfDeath=Component.translatable("death.deathOfCause","<null>");
        }
    }

    @Override
    public void init() {
        this.delayTicker = 0;
        this.exitButtons.clear();
        Component $$0 = Component.translatable("death.do_not_respawn");
        this.exitButtons.add(this.addRenderableWidget(Button.builder($$0, new OnPress1()).bounds(this.width / 2 - 100, this.height / 4 + 72, 200, 20).build()));
        this.exitToTitleButton = this.addRenderableWidget(Button.builder(Component.translatable("death.do_not_exit"), new OnPress2(this)).bounds(this.width / 2 - 100, this.height / 4 + 96, 200, 20).build());
        this.exitButtons.add(this.exitToTitleButton);
        this.deathScore = Component.translatable("deathScreen.score").append(": ").append(Component.literal(Integer.toString(this.minecraft.player.getScore())).withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(GuiGraphics p_283488_, int p_283551_, int p_283002_, float p_281981_) {
        EntityEraserRenderers.staticRender(this,p_283488_,p_283551_,p_283002_,p_281981_);
    }

    @Override
    public boolean mouseClicked(double p_95914_, double p_95915_, int p_95916_) {
        if (p_95915_ > 85.0) {
            Objects.requireNonNull(this.font);
            if (p_95915_ < (double)(85 + 9)) {
                Style $$3 = this.getClickedComponentStyleAt((int)p_95914_);
                if ($$3 != null && $$3.getClickEvent() != null && $$3.getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) {
                    this.handleComponentClicked($$3);
                    return false;
                }
            }
        }
        Iterator var6 = this.children().iterator();
        GuiEventListener $$3;
        do {
            if (!var6.hasNext()) {
                return false;
            }
            $$3 = (GuiEventListener)var6.next();
        } while(!$$3.mouseClicked(p_95914_, p_95915_, p_95916_));
        this.setFocused($$3);
        if (p_95916_ == 0) {
            this.setDragging(true);
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {}

    @Override
    public void clearWidgets() {}

    @Nullable
    private Style getClickedComponentStyleAt(int p_95918_) {
        int $$1 = this.minecraft.font.width(this.causeOfDeath);
        int $$2 = this.width / 2 - $$1 / 2;
        int $$3 = this.width / 2 + $$1 / 2;
        return p_95918_ >= $$2 && p_95918_ <= $$3 ? this.minecraft.font.getSplitter().componentStyleAtWidth(this.causeOfDeath, p_95918_ - $$2) : null;
    }

    public static class OnPress1 implements Button.OnPress {
        public OnPress1(){

        }
        @Override
        public void onPress(Button p_280794_) {
        }
    }

    public static class OnPress2 implements Button.OnPress {
        private final DeathScreen deathScreen;
        public OnPress2(DeathScreen screen){
            this.deathScreen=screen;
        }
        @Override
        public void onPress(Button p_280796_) {
            deathScreen.minecraft.getReportingContext().draftReportHandled(deathScreen.minecraft, deathScreen, () -> {
            }, true);
        }
    }
}
