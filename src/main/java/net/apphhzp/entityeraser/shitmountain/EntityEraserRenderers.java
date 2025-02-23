package net.apphhzp.entityeraser.shitmountain;

import apphhzp.lib.ClassHelper;
import apphhzp.lib.ClassOption;
import net.apphhzp.entityeraser.screen.EntityEraserDeathScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
    static {
        try {
            if (ClassHelper.isHotspotJVM){
                if (!ClassHelper.defineClassBypassAgent("net.apphhzp.entityeraser.shitmountain.DeathRenderer",EntityEraserRenderers.class,true,null)){
                    deathRenderer= ClassHelper.defineHiddenClass("net.apphhzp.entityeraser.shitmountain.DeathRenderer", EntityEraserRenderers.class,true,null, ClassOption.NESTMATE,ClassOption.STRONG);
                    staticInit=deathRenderer.findStatic(deathRenderer.lookupClass(),"staticInit", MethodType.methodType(void.class, EntityEraserDeathScreen.class, Minecraft.class,int.class,int.class));
                    staticRender=deathRenderer.findStatic(deathRenderer.lookupClass(),"staticRender",MethodType.methodType(void.class,EntityEraserDeathScreen.class,GuiGraphics.class,int.class,int.class,float.class));
                }else {
                    staticInit=staticRender=null;
                    deathRenderer=null;
                }
            }else {
                deathRenderer= ClassHelper.defineHiddenClass("net.apphhzp.entityeraser.shitmountain.DeathRenderer", EntityEraserRenderers.class,true,null, ClassOption.NESTMATE,ClassOption.STRONG);
                staticInit=deathRenderer.findStatic(deathRenderer.lookupClass(),"staticInit", MethodType.methodType(void.class, EntityEraserDeathScreen.class, Minecraft.class,int.class,int.class));
                staticRender=deathRenderer.findStatic(deathRenderer.lookupClass(),"staticRender",MethodType.methodType(void.class,EntityEraserDeathScreen.class,GuiGraphics.class,int.class,int.class,float.class));
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void staticRender(EntityEraserDeathScreen screen, GuiGraphics guiGraphics, int p_283551_, int p_283002_, float p_281981_){
        if (deathRenderer!=null){
            try {
                staticRender.invoke(screen, guiGraphics, p_283551_, p_283002_, p_281981_);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }else {
            DeathRenderer.staticRender(screen, guiGraphics, p_283551_, p_283002_, p_281981_);
        }

    }
    public static void staticInit(EntityEraserDeathScreen gui, Minecraft p_96607_, int p_96608_, int p_96609_){
        if (deathRenderer!=null) {
            try {
                staticInit.invoke(gui, p_96607_, p_96608_, p_96609_);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }else {
            DeathRenderer.staticInit(gui, p_96607_, p_96608_, p_96609_);
        }
    }
}
