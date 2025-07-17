package net.apphhzp.entityeraser.shitmountain;

import apphhzp.lib.ClassHelperSpecial;
import apphhzp.lib.ClassOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@OnlyIn(Dist.CLIENT)
public final class EntityEraserRenderers {
    public static final MethodHandles.Lookup deathRenderer;
    private static final MethodHandle staticInit;
    private static final MethodHandle staticRender;
    private static final MethodHandle staticRender2;
    static {
        try {
            deathRenderer= ClassHelperSpecial.defineHiddenClass("net.apphhzp.entityeraser.shitmountain.DeathRenderer", EntityEraserRenderers.class,true,null, ClassOption.NESTMATE,ClassOption.STRONG);
            staticInit=deathRenderer.findStatic(deathRenderer.lookupClass(),"staticInit", MethodType.methodType(void.class, DeathScreen.class, Minecraft.class,int.class,int.class));
            staticRender=deathRenderer.findStatic(deathRenderer.lookupClass(),"staticRender",MethodType.methodType(void.class,DeathScreen.class,GuiGraphics.class,int.class,int.class,float.class));
            staticRender2=deathRenderer.findStatic(deathRenderer.lookupClass(),"staticRender",MethodType.methodType(void.class,DeathScreen.class,GuiGraphics.class,int.class,int.class,float.class,int.class));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void staticRender(DeathScreen screen, GuiGraphics guiGraphics, int p_283551_, int p_283002_, float p_281981_){
        try {
            staticRender.invoke(screen, guiGraphics, p_283551_, p_283002_, p_281981_);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    public static void staticRender2(DeathScreen screen, GuiGraphics guiGraphics, int p_283551_, int p_283002_, float p_281981_,int index){
        try {
            staticRender2.invoke(screen, guiGraphics, p_283551_, p_283002_, p_281981_,index);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    public static void staticInit(DeathScreen gui, Minecraft p_96607_, int p_96608_, int p_96609_){
        try {
            staticInit.invoke(gui, p_96607_, p_96608_, p_96609_);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
