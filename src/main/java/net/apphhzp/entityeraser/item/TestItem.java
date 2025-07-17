package net.apphhzp.entityeraser.item;


import apphhzp.lib.ClassHelperSpecial;
import net.apphhzp.entityeraser.util.OpenGL2GDI;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL21C.GL_PIXEL_PACK_BUFFER;
import static org.lwjgl.opengl.GL21C.GL_PIXEL_UNPACK_BUFFER;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL32C.*;

public class TestItem extends Item {
    public TestItem() {
        super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.COMMON));
    }

    @OnlyIn(Dist.CLIENT)
    private static MultiBufferSource.BufferSource old;
    private static  int textureId,fboId,pboId,width,height;
    private static long sync;
    private static void createFBO() {
        // 创建纹理附件
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8,width ,height, 0,
                GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // 创建FBO
        fboId = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fboId);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                GL_TEXTURE_2D, textureId, 0);

        // 检查FBO完整性
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("FBO不完整: 0x" +
                    Integer.toHexString(glCheckFramebufferStatus(GL_FRAMEBUFFER)));
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private static void createPBO() {
        pboId = glGenBuffers();
        glBindBuffer(GL_PIXEL_PACK_BUFFER, pboId);
        glBufferData(GL_PIXEL_PACK_BUFFER, (long) width * height * 4, GL_STREAM_READ);
        glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
    }

    private static void copyFrontBufferToFBO() {
        // 等待主线程完成渲染
        if (sync != 0) {
            int waitResult = glClientWaitSync(sync, 0, 1000000000); // 1秒超时
            if (waitResult == GL_TIMEOUT_EXPIRED) {
                System.err.println("等待同步超时");
                return;
            }
            glDeleteSync(sync);
            sync = 0;
        }

        // 1. 读取FRONT buffer到PBO
        glBindBuffer(GL_PIXEL_PACK_BUFFER, pboId);
        glReadBuffer(GL_FRONT);
        glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, 0);

        // 2. 创建新同步点
        //sync = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0);

        // 3. 复制PBO数据到FBO纹理
        glBindBuffer(GL_PIXEL_UNPACK_BUFFER, pboId);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height,
                GL_RGBA, GL_UNSIGNED_BYTE, 0);

        // 4. 解绑资源
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);
        glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);

    }

    public static boolean renderPaused=false;
    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        if (p_41432_.isClientSide) {
//            renderPaused=!renderPaused;
//            if (renderPaused){
//                RenderPauseBufferBuilder.clean();
//            }
//            Tesselator.getInstance().builder= EntityEraserBufferBuilder.getInstance();
            OpenGL2GDI.updPixels();
            ClassHelperSpecial.createHiddenThread(()->{
                while (true){
                    OpenGL2GDI.render();
                }
            },"test");
        }
        return super.use(p_41432_, p_41433_, p_41434_);
    }


    public Object afuhad(){
        return null;
    }
}
