package net.apphhzp.entityeraser.item;


import net.minecraft.client.Minecraft;
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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class TestItem extends Item {
    public TestItem() {
        super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.COMMON));
    }

    @OnlyIn(Dist.CLIENT)
    private static MultiBufferSource.BufferSource old;
    private static boolean val=false;
    public static int fboId;
    private static int colorTextureId;
    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        //Minecraft.getInstance().doRunTask(GDI32DeathRenderer.INSTANCE::doRender);
        if (p_41432_.isClientSide){
            if (fboId!=0){
                glDeleteTextures(colorTextureId);
                glDeleteFramebuffers(fboId);
            }
            Minecraft mc= Minecraft.getInstance();
            int width=mc.window.getWidth(), height=mc.window.getHeight();
            fboId = GL30.glGenFramebuffers();
            if (fboId==0){
                throw new RuntimeException("Shit！");
            }
            GL30.glBindFramebuffer(GL_FRAMEBUFFER, fboId);

            // 创建颜色纹理附件
            colorTextureId = GL30.glGenTextures();
            GL11.glBindTexture(GL_TEXTURE_2D, colorTextureId);
            GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
            GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            // 将纹理附加到帧缓冲
            GL30.glFramebufferTexture2D(GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorTextureId, 0);

            // 检查帧缓冲完整性
            if (GL30.glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
                throw new RuntimeException("Framebuffer is not complete!");
            }

            // 解绑帧缓冲和纹理
            GL30.glBindFramebuffer(GL_FRAMEBUFFER, 0);
            GL11.glBindTexture(GL_TEXTURE_2D, 0);

            GL30.glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
            // 绑定新帧缓冲为写入目标
            GL30.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fboId);
            // 执行像素复制
            GL30.glBlitFramebuffer(
                    0, 0, width, height,          // 源区域
                    0, 0, width, height,          // 目标区域
                    GL_COLOR_BUFFER_BIT,          // 复制颜色缓冲
                    GL_NEAREST                    // 缩放过滤方式
            );

            // 恢复默认帧缓冲绑定
            GL30.glBindFramebuffer(GL_FRAMEBUFFER, 0);
            new ScheduledThreadPoolExecutor(1).execute(()->{
                while (true){

                    mc.submitAsync(() -> {
                        System.err.println("fjsaifsa!");
//                        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,0);
//                        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,0);
//                        GL11.glReadBuffer(GL11.GL_NONE);
//                        GL11.glDrawBuffer(GL11.GL_FRONT_AND_BACK);
//
//                        GL30.glBlitFramebuffer(0,0,mc.window.getWidth(),mc.window.getHeight(),
//                                0,0,mc.window.getWidth(),mc.window.getHeight(),
//                                GL11.GL_COLOR_BUFFER_BIT,GL11.GL_NEAREST);
//                        GL11.glFinish();
                    });
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    //mc.window.updateDisplay();

                }

            });
            //
            //WGL.wglDeleteContext(oldHGLRC);
        }

//        if (!p_41432_.isClientSide){
//            EntityUtil.allReturn=!EntityUtil.allReturn;
//        }

//        if (p_41432_.isClientSide){
//            Minecraft mc=Minecraft.getInstance();
//            val=!val;
//            if (val){
//                old= mc.renderBuffers.bufferSource;
//                Map<RenderType, BufferBuilder> map=new HashMap<>();
//                for (Map.Entry<RenderType,BufferBuilder> entry:mc.renderBuffers.fixedBuffers.entrySet()){
//                    map.put(entry.getKey(),new BufferBuilder(entry.getValue().buffer.limit()/6){
//                        @Override
//                        public void endVertex() {
//                            this.nextElementByte=0;
//                        }
//                    });
//                }
//                mc.renderBuffers.bufferSource=new MultiBufferSource.BufferSource(new BufferBuilder(256){
//                    @Override
//                    public void endVertex() {
//                        this.nextElementByte=0;
//                    }
//                },map);
//            }else {
//                mc.renderBuffers.bufferSource=old;
//            }
//            p_41433_.sendSystemMessage(Component.literal("测试:"+val));
//        }
        try {
            //Test.out();
//            if (!p_41432_.isClientSide) {
//                //instImpl.appendToSystemClassLoaderSearch(new JarFile(new File(ClassHelper.getJarPath(TestItem.class))));
//                instImpl.addTransformer(new ClassFileTransformer() {
//                    @Override
//                    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
//                        if (classfileBuffer != null&&!className.startsWith("java/lang/")&&
//                                !className.startsWith("it/unimi/dsi/fastutil/")&&!className.startsWith("io/netty/")&&
//                                !className.startsWith("org/apache/")) {
//                            ClassNode classNode = CoremodHelper.bytes2ClassNote(classfileBuffer, className);
//                            boolean flag = false;
//                            for (MethodNode method : classNode.methods) {
//                                if(method.desc.endsWith(")V") && !method.name.equals("<init>") && !method.name.equals("<clinit>")&&!Modifier.isAbstract(method.access)&&!Modifier.isNative(method.access)) {
//                                    for (AbstractInsnNode insn:method.instructions){
//                                        System.err.println(insn.getClass().getName());
//                                    }
//                                    System.err.println("-------");
//                                    InsnList list = new InsnList();
//                                    list.add(new LabelNode());
//                                    LabelNode label = new LabelNode();
//                                    list.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/apphhzp/entityeraser/item/TestItem", "allReturn", "Z"));
//                                    list.add(new JumpInsnNode(Opcodes.IFEQ, label));
//                                    list.add(new InsnNode(Opcodes.RETURN));
//                                    list.add(label);
//                                    list.add(new FrameNode(Opcodes.F_SAME,0,null,0,null));
//                                    method.instructions.insert(list);
//                                    System.err.println("FIXED:" + className + "." + method.name + method.desc);
//                                    flag = true;
//                                    for (AbstractInsnNode insn:method.instructions){
//                                        System.err.println(insn.getClass().getName());
//                                    }
//                                }
//                            }
//
//                            if (flag) {
//                                byte[] bytes=CoremodHelper.classNote2bytes(classNode,false);
//                                try {
//                                    String[] s = className.split("/");
//                                    File f = new File(s[s.length - 1] + ".class");
//                                    FileOutputStream fos = new FileOutputStream(f);
//                                    fos.write(bytes);
//                                    fos.close();
//                                } catch (Throwable t) {
//                                    throw new RuntimeException(t);
//                                }
//                                return bytes;
//                            }
//                        }
//                        return classfileBuffer;
//                    }
//                }, true);
//            }
//            instImpl.retransformClasses(Test.class);
            //Test.out();
        }catch (Throwable t){
            throw new RuntimeException(t);
        }
        return super.use(p_41432_, p_41433_, p_41434_);
    }


    public Object afuhad(){
        return null;
    }
}
