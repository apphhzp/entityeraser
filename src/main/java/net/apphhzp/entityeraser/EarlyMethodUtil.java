package net.apphhzp.entityeraser;

import apphhzp.lib.ClassHelper;
import net.apphhzp.entityeraser.util.DeadBufferBuilder;
import net.apphhzp.entityeraser.util.EntityUtil;
import net.apphhzp.entityeraser.util.ProtectedBufferBuilder;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

public class EarlyMethodUtil {
    @OnlyIn(Dist.CLIENT)
    public static void glfwSwapBuffers(long window){
        if (ClassHelper.findLoadedClass(Thread.currentThread().getContextClassLoader(),"net.minecraft.client.Minecraft")!=null){
            if (EntityUtil.shouldDie(Minecraft.getInstance().player)){
                DeadBufferBuilder.render();
            }else if (EntityUtil.disableGUI){
                ProtectedBufferBuilder.render();
            }
            GLFW.glfwSwapBuffers(window);
            if (EntityUtil.shouldDie(Minecraft.getInstance().player)){
                DeadBufferBuilder.render();
            }else if (EntityUtil.disableGUI){
                ProtectedBufferBuilder.render();
            }
        }else {
            GLFW.glfwSwapBuffers(window);
        }
    }
}
