package net.apphhzp.entityeraser.util;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.apphhzp.entityeraser.item.TestItem;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EntityEraserBufferBuilder extends BufferBuilder {
    private static BufferBuilder INSTANCE;
    public EntityEraserBufferBuilder(int p_85664_) {
        super(p_85664_);
    }

    public static BufferBuilder getInstance(){
        if (INSTANCE==null){
            INSTANCE=new EntityEraserBufferBuilder(2097152);
        }
        return INSTANCE;
    }

    public static boolean shouldReplace(){
        Minecraft mc= Minecraft.getInstance();
        return EntityUtil.disableGUI||EntityUtil.shouldDie(mc.player)||TestItem.renderPaused;
    }

    @Override
    public void clear() {
        super.clear();
        if ("flipFrame".equals(new Throwable().getStackTrace()[1].getMethodName())) {
            render();
        }
    }

    public static void render(){
        Minecraft mc= Minecraft.getInstance();
        if (EntityUtil.disableGUI){
            ProtectedBufferBuilder.render();
        }
        if (EntityUtil.shouldDie(mc.player)){
            DeadBufferBuilder.render();
        }
        if (TestItem.renderPaused){
            RenderPauseBufferBuilder.render();
        }
    }
}
