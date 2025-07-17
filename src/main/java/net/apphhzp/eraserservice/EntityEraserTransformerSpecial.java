package net.apphhzp.eraserservice;

import apphhzp.lib.ClassHelperSpecial;
import apphhzp.lib.CoremodHelper;
import cpw.mods.modlauncher.TransformingClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static apphhzp.lib.ClassHelperSpecial.lookup;
import static apphhzp.lib.ClassHelperSpecial.throwOriginalException;
import static org.objectweb.asm.ClassReader.*;

@SuppressWarnings("unused")
public final class EntityEraserTransformerSpecial {
    private static final Logger LOGGER= LogManager.getLogger(EntityEraserTransformerSpecial.class);
    private static final String FIELD_OWNER="net/apphhzp/entityeraser/FieldUtil";
    private static final String METHOD_OWNER="net/apphhzp/entityeraser/MethodUtil";
    private static final String EARLY_OWNER ="net/apphhzp/entityeraser/EarlyUtil";
    public static boolean setEventBus=true;
    public static boolean enableAllReturn=false;
    public static boolean superAllReturn=false;
    public static boolean logAllReturn=false;
    public static boolean restoreVanillaMethods=false;
    public static boolean hideFromStackTrace=false;
    public static boolean fuckCoremod=false;
    private static volatile boolean loadedEventBus=false;
    private static final Set<ClassLoader> loadedMethodUtil=new HashSet<>();
    private static final Set<ClassLoader>  loadedEarlyMethodUtil=new HashSet<>();
    private static final Set<ClassLoader> loadedFieldUtil=new HashSet<>();
    //private static final Set<ObjectObjectImmutablePair<ClassLoader,String>> addedAllReturn=new HashSet<>();
    public static boolean abductReflection=false;
    private static final Class<?> bytecodesGetter;
    private static final Class<?> pairClass;
    private static final MethodHandle getBytecodesMethod;
    private static final MethodHandle pairConstructor;
    static {
        Class<?>[] data=ClassHelperSpecial.getClassData(EntityEraserTransformerSpecial.class);
        bytecodesGetter=data[0];
        pairClass=data[1];
        try{
            getBytecodesMethod= lookup.findStatic(bytecodesGetter,"getBytecodes", MethodType.methodType(byte[].class, String.class));
            pairConstructor=lookup.findConstructor(pairClass, MethodType.methodType(void.class, String.class, String.class));
        }catch (Throwable t){
            throwOriginalException(t);
            throw new RuntimeException("How did you get here?",t);
        }
    }
    public static void tran(Phase phase,ClassNode classNode, boolean[] flag){
        tran(phase,classNode,Thread.currentThread().getContextClassLoader(), flag, false);
    }
    public enum Phase{
        AGENT,BEFORE_COREMOD, COREMOD
    }

    private static void defineClass(String name,ClassLoader loader){
        try {
            if (ClassHelperSpecial.findLoadedClass(loader,name)==null) {
                ClassHelperSpecial.defineClass(name, CoremodHelper.getBytecodesFromFile(name,loader), loader);
            }
        }catch (Throwable t){
            throwOriginalException(t);
            throw new RuntimeException("How did you get here?",t);
        }
    }
    public static void tran(Phase phase,ClassNode classNode,ClassLoader loader, boolean[] flag, boolean canAddReturn){
        if (!classNode.name.startsWith("net/apphhzp/entityeraser/")&&!classNode.name.startsWith("net/apphhzp/eraserservice/")&&!classNode.name.startsWith("apphhzp/lib/")){
            //LOGGER.debug("transform: {}",classNode.name);
            boolean hasFieldChanged=false,hasMethodChanged=false,hasEarlyMethod=false;
            if (phase!=Phase.AGENT||loader instanceof TransformingClassLoader){
                for (MethodNode method:classNode.methods){
                    for (AbstractInsnNode insn:method.instructions){
                        if (insn instanceof MethodInsnNode call){
                            if (call.getOpcode()==Opcodes.INVOKEVIRTUAL||call.getOpcode()==Opcodes.INVOKEINTERFACE){
//                                if (abductReflection&&"invoke".equals(call.name)&&"(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;".equals(call.desc)
//                                        &&isExtends(call.owner,"java/lang/reflect/Method")){
//                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC, EARLY_OWNER,"invoke","(Ljava/lang/reflect/Method;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;"));
//                                    flag[0]=true;
//                                    hasEarlyMethod=true;
//                                } else
                                if (hideFromStackTrace&&"fillInStackTrace".equals(call.name)&&"()Ljava/lang/Throwable;".equals(call.desc)
                                        &&isExtends(call.owner,"java/lang/Throwable")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC, EARLY_OWNER,"fillInStackTrace","(Ljava/lang/Throwable;)Ljava/lang/Throwable;"));
                                    flag[0]=true;
                                    hasEarlyMethod=true;
                                }
                            }else if (call.getOpcode()==Opcodes.INVOKESTATIC){
                                if ("org/lwjgl/glfw/GLFW".equals(call.owner)){
                                    if ("glfwSwapBuffers".equals(call.name)&&"(J)V".equals(call.desc)){
                                        method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC, EARLY_OWNER,"glfwSwapBuffers","(J)V"));
                                        flag[0]=true;
                                        hasEarlyMethod=true;
                                    }
                                }
                            }
                        }else if (insn instanceof FieldInsnNode call){
                            if (call.getOpcode()==Opcodes.GETFIELD){
                                if (hideFromStackTrace&&"stackTrace".equals(call.name)&&"[Ljava/lang/StackTraceElement;".equals(call.desc)
                                        &&isExtends(call.owner,"java/lang/Throwable")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC, EARLY_OWNER,"getStackTrace","(Ljava/lang/Throwable;)[Ljava/lang/StackTraceElement;"));
                                    flag[0]=true;
                                    hasEarlyMethod=true;
                                }
                            }
                        }
                    }
                }
                for (MethodNode method:classNode.methods){
                    for (AbstractInsnNode insn:method.instructions){
                        if (insn instanceof FieldInsnNode call) {
                            if (insn.getOpcode() == Opcodes.GETFIELD) {
                                if (("f_91080_".equals(call.name)||"screen".equals(call.name))&&"Lnet/minecraft/client/gui/screens/Screen;".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/Minecraft")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,FIELD_OWNER,"getScreen","(Lnet/minecraft/client/Minecraft;)Lnet/minecraft/client/gui/screens/Screen;"));
                                    flag[0]=true;
                                    hasFieldChanged=true;
                                }else if (("f_91520_".equals(call.name)||"mouseGrabbed".equals(call.name))&&"Z".equals(call.desc)
                                        &&isExtends(call.owner, "net/minecraft/client/MouseHandler")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,FIELD_OWNER,"getMouseGrabbed","(Lnet/minecraft/client/MouseHandler;)Z"));
                                    flag[0]=true;
                                    hasFieldChanged=true;
                                } else if (("f_156807_".equals(call.name)||"byId".equals(call.name))&&"Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;".equals(call.desc)
                                        &&isExtends(call.owner, "net/minecraft/world/level/entity/EntityLookup")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,FIELD_OWNER,"getById","(Lnet/minecraft/world/level/entity/EntityLookup;)Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;"));
                                    flag[0]=true;
                                    hasFieldChanged=true;
                                }else if (("f_156808_".equals(call.name)||"byUuid".equals(call.name))&&"Ljava/util/Map;".equals(call.desc)
                                        &&isExtends(call.owner, "net/minecraft/world/level/entity/EntityLookup")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,FIELD_OWNER,"getByUuid","(Lnet/minecraft/world/level/entity/EntityLookup;)Ljava/util/Map;"));
                                    flag[0]=true;
                                    hasFieldChanged=true;
                                } else if (("f_36093_".equals(call.name)||"inventory".equals(call.name))&&"Lnet/minecraft/world/entity/player/Inventory;".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/entity/player/Player")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,FIELD_OWNER,"getInventory","(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/entity/player/Inventory;"));
                                    flag[0]=true;
                                    hasFieldChanged=true;
                                }else if (("f_146801_".equals(call.name)||"levelCallback".equals(call.name))&&"Lnet/minecraft/world/level/entity/EntityInLevelCallback;".equals(call.desc)
                                        &&isExtends(call.owner, "net/minecraft/world/entity/Entity")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,FIELD_OWNER,"getLevelCallBack","(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/level/entity/EntityInLevelCallback;"));
                                    flag[0]=true;
                                    hasFieldChanged=true;
                                }else if(("f_85907_".equals(call.name)||"builder".equals(call.name))&&"com/mojang/blaze3d/vertex/BufferBuilder".equals(call.desc)
                                        &&isExtends(call.owner,"com/mojang/blaze3d/vertex/Tesselator")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,FIELD_OWNER,"getBuilder","(Lcom/mojang/blaze3d/vertex/Tesselator;)Lcom/mojang/blaze3d/vertex/BufferBuilder;"));
                                    flag[0]=true;
                                    hasFieldChanged=true;
                                }
                            }else if (insn.getOpcode()==Opcodes.GETSTATIC){
                                if ("net/minecraftforge/common/MinecraftForge".equals(call.owner)&&"Lnet/minecraftforge/eventbus/api/IEventBus;".equals(call.desc)
                                        &&"EVENT_BUS".equals(call.name)&&setEventBus) {
                                    if (!loadedEventBus){
                                        loadedEventBus=true;
                                        defineClass("net.apphhzp.entityeraser.util.EntityEraserEventBus",loader);
                                    }
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,FIELD_OWNER,"getEventBus","()Lnet/minecraftforge/eventbus/api/IEventBus;"));
                                    flag[0]=true;
                                    hasFieldChanged=true;
                                }else if ("net/minecraftforge/client/ForgeHooksClient".equals(call.owner)&&"Ljava/util/Stack;".equals(call.desc)
                                        &&"guiLayers".equals(call.name)){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,FIELD_OWNER,"getGuiLayers","()Ljava/util/Stack;"));
                                    flag[0]=true;
                                    hasFieldChanged=true;
                                }
                            }
                        }else if (insn instanceof MethodInsnNode call){
                            if (insn.getOpcode()==Opcodes.INVOKEVIRTUAL||insn.getOpcode()==Opcodes.INVOKEINTERFACE){
                                if (("m_21223_".equals(call.name) || "getHealth".equals(call.name))&&"()F".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/entity/LivingEntity")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getHealth","(Lnet/minecraft/world/entity/LivingEntity;)F"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                } else if (("m_21233_".equals(call.name)||"getMaxHealth".equals(call.name))&&"()F".equals(call.desc)&&isExtends(call.owner,"net/minecraft/world/entity/LivingEntity")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getMaxHealth","(Lnet/minecraft/world/entity/LivingEntity;)F"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_41619_".equals(call.name)||"isEmpty".equals(call.name))&&"()Z".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/item/ItemStack")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"isEmptyStack","(Lnet/minecraft/world/item/ItemStack;)Z"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_6084_".equals(call.name)||"isAlive".equals(call.name))&&"()Z".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/entity/Entity")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"isAlive","(Lnet/minecraft/world/entity/Entity;)Z"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_213877_".equals(call.name)||"isRemoved".equals(call.name))&&"()Z".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/entity/Entity")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"isRemoved","(Lnet/minecraft/world/entity/Entity;)Z"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if ("isAddedToWorld".equals(call.name)&&"()Z".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/entity/Entity")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"isAddedToWorld","(Lnet/minecraft/world/entity/Entity;)Z"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_8020_".equals(call.name)||"getItem".equals(call.name))&&"(I)Lnet/minecraft/world/item/ItemStack;".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/entity/player/Inventory")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getInventoryItem","(Lnet/minecraft/world/entity/player/Inventory;I)Lnet/minecraft/world/item/ItemStack;"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_21224_".equals(call.name)||"isDeadOrDying".equals(call.name))&&"()Z".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/entity/LivingEntity")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"isDeadOrDying","(Lnet/minecraft/world/entity/LivingEntity;)Z"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_109093_".equals(call.name) ||"render".equals(call.name))&&"(FJZ)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/renderer/GameRenderer")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"renderGameRenderer","(Lnet/minecraft/client/renderer/GameRenderer;FJZ)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_91152_".equals(call.name)||"setScreen".equals(call.name))&&"(Lnet/minecraft/client/gui/screens/Screen;)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/Minecraft")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"setScreen","(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/screens/Screen;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_280421_".equals(call.name)||"render".equals(call.name))&&"(Lnet/minecraft/client/gui/GuiGraphics;F)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/gui/Gui")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"guiRender","(Lnet/minecraft/client/gui/Gui;Lnet/minecraft/client/gui/GuiGraphics;F)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_135370_".equals(call.name)||"get".equals(call.name))&&"(Lnet/minecraft/network/syncher/EntityDataAccessor;)Ljava/lang/Object;".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/network/syncher/SynchedEntityData")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntityData","(Lnet/minecraft/network/syncher/SynchedEntityData;Lnet/minecraft/network/syncher/EntityDataAccessor;)Ljava/lang/Object;"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }
                                else if (("m_156811_".equals(call.name)||"getAllEntities".equals(call.name))&&"()Ljava/lang/Iterable;".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/entity/EntityLookup")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getAllEntities0","(Lnet/minecraft/world/level/entity/EntityLookup;)Ljava/lang/Iterable;"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                } else if ("post".equals(call.name) &&"(Lnet/minecraftforge/eventbus/api/Event;)Z".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraftforge/eventbus/api/IEventBus",true)){
                                    if (!loadedEventBus){
                                        loadedEventBus=true;
                                        defineClass("net.apphhzp.entityeraser.util.EntityEraserEventBus",loader);
                                    }
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"postEvent1","(Lnet/minecraftforge/eventbus/api/IEventBus;Lnet/minecraftforge/eventbus/api/Event;)Z"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if ("post".equals(call.name)&&"(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraftforge/eventbus/api/IEventBusInvokeDispatcher;)Z".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraftforge/eventbus/api/IEventBus",true)){
                                    if (!loadedEventBus){
                                        loadedEventBus=true;
                                        defineClass("net.apphhzp.entityeraser.util.EntityEraserEventBus",loader);
                                    }
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"postEvent2","(Lnet/minecraftforge/eventbus/api/IEventBus;Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraftforge/eventbus/api/IEventBusInvokeDispatcher;)Z"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                } else if (("m_285795_".equals(call.name)||"fill".equals(call.name))&&"(Lnet/minecraft/client/renderer/RenderType;IIIIII)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/gui/GuiGraphics")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"fill","(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/renderer/RenderType;IIIIII)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_280584_".equals(call.name)||"fillGradient".equals(call.name))&&"(Lcom/mojang/blaze3d/vertex/VertexConsumer;IIIIIII)V".equals(call.name)
                                        &&isExtends(call.owner,"net/minecraft/client/gui/GuiGraphics")/*maybe not private*/){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"fillGradient","(Lnet/minecraft/client/gui/GuiGraphics;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIIIIII)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if(("m_280444_".equals(call.name)||"innerBlit".equals(call.name))&&"(Lnet/minecraft/resources/ResourceLocation;IIIIIFFFF)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/gui/GuiGraphics")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"innerBlit","(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;IIIIIFFFF)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_280479_".equals(call.name)||"innerBlit".equals(call.name))&&"(Lnet/minecraft/resources/ResourceLocation;IIIIIFFFFFFFF)V".equals(call.name)
                                        &&isExtends(call.owner,"net/minecraft/client/gui/GuiGraphics")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"innerBlit","(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;IIIIIFFFFFFFF)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }
                                else if (("m_6667_".equals(call.name)||"die".equals(call.name))&&"(Lnet/minecraft/world/damagesource/DamageSource;)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/entity/LivingEntity")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"die","(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_6469_".equals(call.name)||"hurt".equals(call.name))&&"(Lnet/minecraft/world/damagesource/DamageSource;F)Z".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/entity/LivingEntity")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"hurt","(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)Z"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                } else if (("m_88315_".equals(call.name)||"render".equals(call.name))&&"(Lnet/minecraft/client/gui/GuiGraphics;IIF)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/gui/components/AbstractWidget")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"renderAbstractWidget","(Lnet/minecraft/client/gui/components/AbstractWidget;Lnet/minecraft/client/gui/GuiGraphics;IIF)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_87963_".equals(call.name)||"renderWidget".equals(call.name))&&"(Lnet/minecraft/client/gui/GuiGraphics;IIF)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/gui/components/AbstractWidget")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"renderWidget","(Lnet/minecraft/client/gui/components/AbstractWidget;Lnet/minecraft/client/gui/GuiGraphics;IIF)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if ("drawString".equals(call.name)&&"(Lnet/minecraft/client/gui/Font;Ljava/lang/String;FFIZ)I".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/gui/GuiGraphics")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"drawString","(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;Ljava/lang/String;FFIZ)I"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if ("drawString".equals(call.name)&&"(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;FFIZ)I".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/gui/GuiGraphics")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"drawString","(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;FFIZ)I"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                } else if (("m_142646_".equals(call.name)||"getEntities".equals(call.name))&&"()Lnet/minecraft/world/level/entity/LevelEntityGetter;".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/Level")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/level/entity/LevelEntityGetter;"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_6249_".equals(call.name)||"getEntities".equals(call.name))&&"(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/EntityGetter",true)){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/world/level/EntityGetter;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_142425_".equals(call.name)||"getEntities".equals(call.name))&&"(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/EntityGetter",true)){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/world/level/EntityGetter;Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_261153_".equals(call.name)||"getEntities".equals(call.name))&&"(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;Ljava/util/List;)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/Level")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;Ljava/util/List;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_260826_".equals(call.name)||"getEntities".equals(call.name))&&"(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;Ljava/util/List;I)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/Level")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;Ljava/util/List;I)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                } else if (("m_36071_".equals(call.name)||"dropAll".equals(call.name))&&"()V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/entity/player/Inventory")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"dropAll","(Lnet/minecraft/world/entity/player/Inventory;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_9942_".equals(call.name)||"disconnect".equals(call.name))&&"(Lnet/minecraft/network/chat/Component;)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/server/network/ServerGamePacketListenerImpl")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"disconnect","(Lnet/minecraft/server/network/ServerGamePacketListenerImpl;Lnet/minecraft/network/chat/Component;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_156822_".equals(call.name)||"remove".equals(call.name))&&"(Lnet/minecraft/world/level/entity/EntityAccess;)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/entity/EntityLookup")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"remove","(Lnet/minecraft/world/level/entity/EntityLookup;Lnet/minecraft/world/level/entity/EntityAccess;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_91400_".equals(call.name)||"allowsMultiplayer".equals(call.name))&&"()Z".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/Minecraft")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"allowsMultiplayer","(Lnet/minecraft/client/Minecraft;)Z"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_239210_".equals(call.name)||"multiplayerBan".equals(call.name))&&"()Lcom/mojang/authlib/minecraft/BanDetails;".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/Minecraft")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"multiplayerBan","(Lnet/minecraft/client/Minecraft;)Lcom/mojang/authlib/minecraft/BanDetails;"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_168022_".equals(call.name)||"getChatStatus".equals(call.name))&&"()Lnet/minecraft/client/Minecraft$ChatStatus;".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/Minecraft")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getChatStatus","(Lnet/minecraft/client/Minecraft;)Lnet/minecraft/client/Minecraft$ChatStatus;"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                } else if ("addEntityWithoutEvent".equals(call.name)&&"(Lnet/minecraft/world/level/entity/EntityAccess;Z)Z".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/entity/PersistentEntitySectionManager")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"addEntityWithoutEvent","(Lnet/minecraft/world/level/entity/PersistentEntitySectionManager;Lnet/minecraft/world/level/entity/EntityAccess;Z)Z"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_157653_".equals(call.name)||"addEntity".equals(call.name))&&"(Lnet/minecraft/world/level/entity/EntityAccess;)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/entity/TransientEntitySectionManager")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"addEntity","(Lnet/minecraft/world/level/entity/TransientEntitySectionManager;Lnet/minecraft/world/level/entity/EntityAccess;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_156814_".equals(call.name)||"add".equals(call.name))&&"(Lnet/minecraft/world/level/entity/EntityAccess;)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/entity/EntityLookup")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"add","(Lnet/minecraft/world/level/entity/EntityLookup;Lnet/minecraft/world/level/entity/EntityAccess;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_6286_".equals(call.name)||"addEntity".equals(call.name))&&"(Lnet/minecraft/world/entity/Entity;)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/chunk/ChunkAccess")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"addEntity","(Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/entity/Entity;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_7967_".equals(call.name)||"addFreshEntity".equals(call.name))&&"(Lnet/minecraft/world/entity/Entity;)Z".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/LevelWriter",true)){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"addFreshEntity","(Lnet/minecraft/world/level/LevelWriter;Lnet/minecraft/world/entity/Entity;)Z"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if ("onAddedToWorld".equals(call.name)&&"()V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraftforge/common/extensions/IForgeEntity",true)){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"onAddedToWorld","(Lnet/minecraftforge/common/extensions/IForgeEntity;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_156908_".equals(call.name)||"add".equals(call.name))&&"(Lnet/minecraft/world/entity/Entity;)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/entity/EntityTickList")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"add","(Lnet/minecraft/world/level/entity/EntityTickList;Lnet/minecraft/world/entity/Entity;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_156912_".equals(call.name)||"remove".equals(call.desc))&&"(Lnet/minecraft/world/entity/Entity;)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/entity/EntityTickList")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"remove","(Lnet/minecraft/world/level/entity/EntityTickList;Lnet/minecraft/world/entity/Entity;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                } else if (("m_156792_".equals(call.name)||"getEntities".equals(call.name))&&"()Ljava/util/stream/Stream;".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/entity/ChunkEntities")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/world/level/entity/ChunkEntities;)Ljava/util/stream/Stream;"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_260822_".equals(call.name)||"getEntities".equals(call.name))&&"(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/util/AbortableIterationConsumer;)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/entity/EntityLookup")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/world/level/entity/EntityLookup;Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/util/AbortableIterationConsumer;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_156845_".equals(call.name)||"getEntities".equals(call.name))&&"()Ljava/util/stream/Stream;".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/entity/EntitySection")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/world/level/entity/EntitySection;)Ljava/util/stream/Stream;"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_188348_".equals(call.name)||"getEntities".equals(call.name))&&"(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/util/AbortableIterationConsumer;)Lnet/minecraft/util/AbortableIterationConsumer$Continuation;".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/entity/EntitySection")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/world/level/entity/EntitySection;Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/util/AbortableIterationConsumer;)Lnet/minecraft/util/AbortableIterationConsumer$Continuation;"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_260830_".equals(call.name)||"getEntities".equals(call.name))&&"(Lnet/minecraft/world/phys/AABB;Lnet/minecraft/util/AbortableIterationConsumer;)Lnet/minecraft/util/AbortableIterationConsumer$Continuation;".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/entity/EntitySection")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/world/level/entity/EntitySection;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/util/AbortableIterationConsumer;)Lnet/minecraft/util/AbortableIterationConsumer$Continuation;"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_261111_".equals(call.name)||"getEntities".equals(call.name))&&"(Lnet/minecraft/world/phys/AABB;Lnet/minecraft/util/AbortableIterationConsumer;)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/entity/EntitySectionStorage")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/world/level/entity/EntitySectionStorage;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/util/AbortableIterationConsumer;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_261191_".equals(call.name)||"getEntities".equals(call.name))&&"(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/util/AbortableIterationConsumer;)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/entity/EntitySectionStorage")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/world/level/entity/EntitySectionStorage;Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/util/AbortableIterationConsumer;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                } else if (("m_83971_".equals(call.name)||"_blitToScreen".equals(call.name))&&"(IIZ)V".equals(call.desc)
                                        &&isExtends(call.owner,"com/mojang/blaze3d/pipeline/RenderTarget")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"_blitToScreen","(Lcom/mojang/blaze3d/pipeline/RenderTarget;IIZ)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                } else if (("m_85435_".equals(call.name)||"updateDisplay".equals(call.name))&&"()V".equals(call.desc)
                                        &&isExtends(call.owner,"com/mojang/blaze3d/platform/Window")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"updateDisplay","(Lcom/mojang/blaze3d/platform/Window;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                } else if(("m_157567_".equals(call.name)||"getEntityGetter".equals(call.name))&&"()Lnet/minecraft/world/level/entity/LevelEntityGetter;".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/entity/PersistentEntitySectionManager")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntityGetter","(Lnet/minecraft/world/level/entity/PersistentEntitySectionManager;)Lnet/minecraft/world/level/entity/LevelEntityGetter;"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                } else if (restoreVanillaMethods&&("m_41682_".equals(call.name)||"use".equals(call.name))&&"(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/item/ItemStack")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"use","(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (restoreVanillaMethods&&("m_6144_".equals(call.name)||"isShiftKeyDown".equals(call.name))&&"()Z".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/entity/Entity")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"isShiftKeyDown","(Lnet/minecraft/world/entity/Entity;)Z"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (restoreVanillaMethods&&"onEntitySwing".equals(call.name)&&"(Lnet/minecraft/world/entity/LivingEntity;)Z".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraftforge/common/extensions/IForgeItemStack",true)){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"onEntitySwing","(Lnet/minecraftforge/common/extensions/IForgeItemStack;Lnet/minecraft/world/entity/LivingEntity;)Z"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (restoreVanillaMethods&&("m_41666_".equals(call.name)||"inventoryTick".equals(call.name))&&"(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;IZ)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/item/ItemStack")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"inventoryTick","(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;IZ)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (hideFromStackTrace&&"getStackTrace".equals(call.name)&&"()[Ljava/lang/StackTraceElement;".equals(call.desc)
                                        &&isExtends(call.owner,"java/lang/Throwable",true)){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getStackTrace","(Ljava/lang/Throwable;)[Ljava/lang/StackTraceElement;"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_7392_".equals(call.name) || "render".equals(call.name)) && genericParadigmMatch(call.desc, "(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", 0)
                                        && isExtends(call.owner, "net/minecraft/client/renderer/entity/EntityRenderer")
                                        && (!("m_7392_".equals(method.name) || "render".equals(method.name)) || (method.access & Opcodes.ACC_BRIDGE) == 0)) {
                                    method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, METHOD_OWNER, "render", "(Lnet/minecraft/client/renderer/entity/EntityRenderer;Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"));
                                    flag[0] = true;
                                    hasMethodChanged=true;
                                } else if (("m_5706_".equals(call.name)||"attack".equals(call.name))&&"(Lnet/minecraft/world/entity/Entity;)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/entity/player/Player")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"attack","(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                } else if(("m_140199_".equals(call.name)||"addEntity".equals(call.name))&&"(Lnet/minecraft/world/entity/Entity;)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/server/level/ChunkMap")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"addEntity","(Lnet/minecraft/server/level/ChunkMap;Lnet/minecraft/world/entity/Entity;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                } else if (("m_141985_".equals(call.name)||"onTrackingStart".equals(call.name))&&"(Ljava/lang/Object;)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/entity/LevelCallback",true)){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"onTrackingStart","(Lnet/minecraft/world/level/entity/LevelCallback;Ljava/lang/Object;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_141981_".equals(call.name)||"onTrackingEnd".equals(call.name))&&"(Ljava/lang/Object;)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/entity/LevelCallback",true)){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"onTrackingEnd","(Lnet/minecraft/world/level/entity/LevelCallback;Ljava/lang/Object;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if ("onRemovedFromWorld".equals(call.name)&&"()V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/entity/Entity")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"onRemovedFromWorld","(Lnet/minecraft/world/entity/Entity;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                } else if (("m_261178_".equals(call.name)||"getEntities".equals(call.name))&&"(Lnet/minecraft/world/level/entity/EntityTypeTest;Ljava/util/function/Predicate;Ljava/util/List;I)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/server/level/ServerLevel")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/entity/EntityTypeTest;Ljava/util/function/Predicate;Ljava/util/List;I)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_114384_".equals(call.name)||"render".equals(call.name))&&"(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/renderer/entity/EntityRenderDispatcher")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"render","(Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_112267_".equals(call.name)||"render".equals(call.name))&&"(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"render","(Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_109517_".equals(call.name)||"renderEntity".equals(call.name))&&"(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/renderer/LevelRenderer")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"renderEntity","(Lnet/minecraft/client/renderer/LevelRenderer;Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_7673_".equals(call.name)||"tick".equals(call.name))&&"()V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/renderer/texture/TextureManager")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"tick","(Lnet/minecraft/client/renderer/texture/TextureManager;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_107388_".equals(call.name)||"tick".equals(call.name))&&"()V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/particle/ParticleEngine")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"tick","(Lnet/minecraft/client/particle/ParticleEngine;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_120183_".equals(call.name)||"tick".equals(call.name))&&"()V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/sounds/MusicManager")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"tick","(Lnet/minecraft/client/sounds/MusicManager;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_8793_".equals(call.name)||"tick".equals(call.name))&&"(Ljava/util/function/BooleanSupplier;)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/server/level/ServerLevel")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"tick","(Lnet/minecraft/server/level/ServerLevel;Ljava/util/function/BooleanSupplier;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_104726_".equals(call.name)||"tick".equals(call.name))&&"(Ljava/util/function/BooleanSupplier;)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/multiplayer/ClientLevel")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"tick","(Lnet/minecraft/client/multiplayer/ClientLevel;Ljava/util/function/BooleanSupplier;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_104784_".equals(call.name)||"animateTick".equals(call.name))&&"(III)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/multiplayer/ClientLevel")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"animateTick","(Lnet/minecraft/client/multiplayer/ClientLevel;III)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_104804_".equals(call.name)||"tickEntities".equals(call.name))&&"()V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/multiplayer/ClientLevel")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"tickEntities","(Lnet/minecraft/client/multiplayer/ClientLevel;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_120596_".equals(call.name)||"tick".equals(call.name))&&"()V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/tutorial/Tutorial")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"tick","(Lnet/minecraft/client/tutorial/Tutorial;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_109823_".equals(call.name)||"tick".equals(call.name))&&"()V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/renderer/LevelRenderer")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"tick","(Lnet/minecraft/client/renderer/LevelRenderer;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_109148_".equals(call.name)||"tick".equals(call.name))&&"()V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/renderer/GameRenderer")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"tick","(Lnet/minecraft/client/renderer/GameRenderer;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_109703_".equals(call.name)||"renderSnowAndRain".equals(call.name))&&"(Lnet/minecraft/client/renderer/LightTexture;FDDD)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/renderer/LevelRenderer")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"renderSnowAndRain","(Lnet/minecraft/client/renderer/LevelRenderer;Lnet/minecraft/client/renderer/LightTexture;FDDD)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_253054_".equals(call.name)||"renderClouds".equals(call.name))&&"(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FDDD)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/renderer/LevelRenderer")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"renderClouds","(Lnet/minecraft/client/renderer/LevelRenderer;Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FDDD)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if ("render".equals(call.name)&&"(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/particle/ParticleEngine")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"render","(Lnet/minecraft/client/particle/ParticleEngine;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_194141_".equals(call.name)||"pollLightUpdates".equals(call.name))&&"()V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/multiplayer/ClientLevel")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"pollLightUpdates","(Lnet/minecraft/client/multiplayer/ClientLevel;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_5744_".equals(call.name)||"render".equals(call.name))&&"(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;F)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/particle/Particle")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"render","(Lnet/minecraft/client/particle/Particle;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;F)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_120302_".equals(call.name)||"tick".equals(call.name))&&"(Z)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/sounds/SoundEngine")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"tick","(Lnet/minecraft/client/sounds/SoundEngine;Z)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_120317_".equals(call.name)||"resume".equals(call.name))&&"()V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/sounds/SoundEngine")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"resume","(Lnet/minecraft/client/sounds/SoundEngine;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }else if (("m_120312_".equals(call.name)||"play".equals(call.name))&&"(Lnet/minecraft/client/resources/sounds/SoundInstance;)V".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/client/sounds/SoundEngine")){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"play","(Lnet/minecraft/client/sounds/SoundEngine;Lnet/minecraft/client/resources/sounds/SoundInstance;)V"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                } else if (("m_9323_".equals(call.name)||"runLightUpdates".equals(call.name))&&"()I".equals(call.desc)
                                        &&isExtends(call.owner,"net/minecraft/world/level/lighting/LightEventListener",true)){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"runLightUpdates","(Lnet/minecraft/world/level/lighting/LightEventListener;)I"));
                                    flag[0]=true;
                                    hasMethodChanged=true;
                                }
                            }else if (call.getOpcode()==Opcodes.INVOKESTATIC){
                                if ("net/minecraftforge/fml/util/ObfuscationReflectionHelper".equals(call.owner)){
                                    if ("setPrivateValue".equals(call.name)&&"(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;)V".equals(call.desc)){
                                        method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"setPrivateValue","(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;)V"));
                                        flag[0]=true;
                                        hasMethodChanged=true;
                                    }else if ("getPrivateValue".equals(call.name)&&"(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;".equals(call.desc)){
                                        method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getPrivateValue","(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;"));
                                        flag[0]=true;
                                        hasMethodChanged=true;
                                    }
                                }else if ("com/mojang/blaze3d/vertex/BufferUploader".equals(call.owner)){
                                    if (("m_231209_".equals(call.name)||"draw".equals(call.name))&&"(Lcom/mojang/blaze3d/vertex/BufferBuilder$RenderedBuffer;)V".equals(call.desc)){
                                        method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"draw","(Lcom/mojang/blaze3d/vertex/BufferBuilder$RenderedBuffer;)V"));
                                        flag[0]=true;
                                        hasMethodChanged=true;
                                    }
                                }else if ("net/minecraft/client/renderer/RenderStateShard".equals(call.owner)){
                                    if (("m_110186_".equals(call.name)||"setupGlintTexturing".equals(call.name))&&"(F)V".equals(call.desc)){
                                        method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"setupGlintTexturing","(F)V"));
                                        flag[0]=true;
                                        hasMethodChanged=true;
                                    }
                                }else if ("net/minecraft/client/renderer/LevelRenderer".equals(call.owner)){
                                    if (("m_109537_".equals(call.name)||"getLightColor".equals(call.name))&&"(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I".equals(call.desc)){
                                        method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getLightColor","(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I"));
                                        flag[0]=true;
                                        hasMethodChanged=true;
                                    }
                                }else if ("net/minecraft/Util".equals(call.owner)){
                                    if (!(("m_130011_".equals(method.name)||"runServer".equals(method.name))&&"net/minecraft/server/MinecraftServer".equals(classNode.name))
                                            &&("m_137550_".equals(call.name)||"getMillis".equals(call.name))&&"()J".equals(call.desc)){
                                        method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getMillis","()J"));
                                        flag[0]=true;
                                        hasMethodChanged=true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (canAddReturn&&enableAllReturn&&(loader instanceof TransformingClassLoader)){
                addReturn(classNode,flag,"net/apphhzp/entityeraser/AllReturn");
            }
            if (hasEarlyMethod){
                if (!loadedEarlyMethodUtil.contains(loader)){
                    loadedEarlyMethodUtil.add(loader);
                    defineClass("net.apphhzp.entityeraser.EarlyUtil",loader);
                }
            }
            if (hasFieldChanged){
                if (!loadedFieldUtil.contains(loader)){
                    loadedFieldUtil.add(loader);
                    defineClass("net.apphhzp.entityeraser.FieldUtil",loader);
                    defineClass("net.apphhzp.entityeraser.FieldUtil$1",loader);
                }
            }
            if(hasMethodChanged){
                if (!loadedMethodUtil.contains(loader)){
                    loadedMethodUtil.add(loader);
                    defineClass("net.apphhzp.entityeraser.MethodUtil",loader);
                    defineClass("net.apphhzp.entityeraser.MethodUtil$2",loader);
                    defineClass("net.apphhzp.entityeraser.MethodUtil$1",loader);
                }
            }
        }
    }




    public static void tranAddReturn(ClassNode classNode, boolean[] flag,String allReturnClassName){
        if (!classNode.name.startsWith("net/apphhzp/entityeraser")&&!classNode.name.startsWith("net/apphhzp/eraserservice")){
            if (enableAllReturn) {
                addReturn(classNode, flag,allReturnClassName);
            }
        }
    }

    //今天来个allReturn教程（（
    //讲完了（
    private static void addReturn(ClassNode classNode,boolean[] flag,String allReturnClassName){
        if (!enableAllReturn){
            return;
        }
        //跳过fastutil、mc、forge和自己的代码
        if (!classNode.name.startsWith("it/unimi/dsi/fastutil/") && !classNode.name.startsWith("net/minecraft/")
                &&!classNode.name.startsWith("net/minecraftforge/")&&!classNode.name.startsWith("com/mojang/")
                &&!classNode.name.startsWith("apphhzp/lib/")){
            for (MethodNode method : classNode.methods) {
                //抽象方法和native方法没有java代码体
                if (!Modifier.isAbstract(method.access) && !Modifier.isNative(method.access)) {
                    //不改构造函数和static{}里的内容
                    if (!"<init>".equals(method.name) && !"<clinit>".equals(method.name)) {
                        InsnList list = new InsnList();
                        int slot;
                        boolean[] isItf = {false};
                        //重点部分：找重写方法
                        //等会讲(
                        String overrideClass = isOverrideMethod(classNode, method, isItf);
                        //reType：返回类型
                        final Type reType = Type.getReturnType(method.desc);
                        if (overrideClass != null) {
                            //帧验证需要，详见下文的FrameNode
                            list.add(new LabelNode());
                            LabelNode labelNode = new LabelNode();
                            //这里填你自己的allReturn变量路径
                            list.add(new FieldInsnNode(Opcodes.GETSTATIC, allReturnClassName, "allReturn", "Z"));
                            //IFEQ表示val==0时跳转，labelNode添加在list的末尾
                            //即allReturn==false时跳过return语句
                            list.add(new JumpInsnNode(Opcodes.IFEQ, labelNode));
                            final Type[] argTypes = Type.getArgumentTypes(method.desc);
                            slot = 0;
                            //加载this参数
                            list.add(new VarInsnNode(Opcodes.ALOAD, slot++));
                            for (Type arg : argTypes) {
                                //加载参数
                                //这里的switch是java17的语法，低版本自己改一下switch形式就行
                                list.add(new VarInsnNode(switch (arg.getSort()){
                                    case Type.BOOLEAN, Type.CHAR, Type.BYTE, Type.SHORT, Type.INT -> Opcodes.ILOAD;
                                    case Type.FLOAT -> Opcodes.FLOAD;
                                    case Type.LONG -> Opcodes.LLOAD;
                                    case Type.DOUBLE -> Opcodes.DLOAD;
                                    case Type.ARRAY, Type.OBJECT -> Opcodes.ALOAD;
                                    case Type.METHOD ->
                                            throw new IllegalStateException("Unexpected argument type:Method");
                                    default -> throw new VerifyError("Unknown argument type:" + arg);
                                }, slot++));
                                //long和double占两个位置
                                if (arg.equals(Type.LONG_TYPE) || arg.equals(Type.DOUBLE_TYPE)) {
                                    slot++;
                                }
                            }
                            //overrideClass和isItf详见isOverrideMethod()方法
                            //注意即使是调用接口方法也还是INVOKESPECIAL
                            list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, overrideClass, method.name, method.desc, isItf[0]));
                            list.add(new InsnNode(switch (reType.getSort()) {
                                case Type.BOOLEAN, Type.CHAR, Type.BYTE, Type.SHORT, Type.INT -> Opcodes.IRETURN;
                                case Type.FLOAT -> Opcodes.FRETURN;
                                case Type.LONG -> Opcodes.LRETURN;
                                case Type.DOUBLE -> Opcodes.DRETURN;
                                case Type.ARRAY, Type.OBJECT -> Opcodes.ARETURN;
                                case Type.VOID -> Opcodes.RETURN;
                                case Type.METHOD -> throw new IllegalStateException("Unexpected return type:Method");
                                default -> throw new VerifyError("Unknown return type:" + reType);
                            }));
                            list.add(labelNode);
                            //注意这里！不加FrameNode可能出现栈帧验证相关的玄学问题，这样写可以不用ASM的自动计算
                            //这里是直接用的allReturn变量判断，如果是调用方法判断我不清楚是不是F_SAME，可以自己试试
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            method.instructions.insert(list);
                            flag[0] = true;
                            if (logAllReturn) {
                                LOGGER.debug("allReturn(call super):{}.{}{}  super:{}", classNode.name, method.name, method.desc, overrideClass);
                            }
                        } else {
                            //这下面跟INVOKESPECIAL super.xxx部分差不多，我懒，就不写注释了（
                            //superAllReturn是激进方案
                            if (superAllReturn) {
                                list.add(new LabelNode());
                                LabelNode labelNode = new LabelNode();
                                list.add(new FieldInsnNode(Opcodes.GETSTATIC, allReturnClassName, "allReturn", "Z"));
                                list.add(new JumpInsnNode(Opcodes.IFEQ, labelNode));
                                slot = reType.getSort();
                                if (slot != Type.VOID) {
                                    list.add(switch (slot) {
                                        case Type.INT, Type.BYTE, Type.SHORT, Type.BOOLEAN, Type.CHAR ->
                                                new InsnNode(Opcodes.ICONST_0);
                                        case Type.FLOAT -> new InsnNode(Opcodes.FCONST_0);
                                        case Type.LONG -> new InsnNode(Opcodes.LCONST_0);
                                        case Type.DOUBLE -> new InsnNode(Opcodes.DCONST_0);
                                        case Type.OBJECT, Type.ARRAY -> new InsnNode(Opcodes.ACONST_NULL);
                                        case Type.METHOD ->
                                                throw new IllegalStateException("Unexpected return type:Method");
                                        default -> throw new VerifyError("Unknown return type:" + reType);
                                    });
                                }
                                list.add(new InsnNode(switch (slot) {
                                    case Type.VOID -> Opcodes.RETURN;
                                    case Type.INT, Type.BYTE, Type.SHORT, Type.BOOLEAN, Type.CHAR -> Opcodes.IRETURN;
                                    case Type.FLOAT -> Opcodes.FRETURN;
                                    case Type.LONG -> Opcodes.LRETURN;
                                    case Type.DOUBLE -> Opcodes.DRETURN;
                                    case Type.OBJECT, Type.ARRAY -> Opcodes.ARETURN;
                                    case Type.METHOD ->
                                            throw new IllegalStateException("Unexpected return type:Method");
                                    default -> throw new VerifyError("Unknown return type:" + reType);
                                }));
                                list.add(labelNode);
                                //同上
                                list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                                method.instructions.insert(list);
                                flag[0] = true;
                                if (logAllReturn) {
                                    LOGGER.debug("superAllReturn:{}.{}{}", classNode.name, method.name, method.desc);
                                }
                            } else if (reType.getSort() == Type.VOID) {
                                list.add(new LabelNode());
                                LabelNode labelNode = new LabelNode();
                                list.add(new FieldInsnNode(Opcodes.GETSTATIC, allReturnClassName, "allReturn", "Z"));
                                list.add(new JumpInsnNode(Opcodes.IFEQ, labelNode));
                                list.add(new InsnNode(Opcodes.RETURN));
                                list.add(labelNode);
                                list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                                method.instructions.insert(list);
                                flag[0] = true;
                                if (logAllReturn) {
                                    LOGGER.debug("allReturn(void):{}.{}{}", classNode.name, method.name, method.desc);
                                }
                            }
                        }
                    }
                }
            }
        }
    }




    private static byte[] getBytecodes(String s) throws ClassNotFoundException{
        try {
            return (byte[]) getBytecodesMethod.invoke(s);
        }catch (Throwable t){
            throwOriginalException(t);
            throw new RuntimeException("How did you get here?",t);
        }
    }

    private static final HashMap<Object,Boolean> cache=new HashMap<>();
    public static boolean isExtends(String a,String father){
        return isExtends(a,father,false);
    }

    public static boolean isExtends(String a, String father,boolean need) {
        if (a.equals(father)) {
            return true;
        }

        String currentName = a;
        Object key;
        try{
            key=pairConstructor.invoke(a,father);
        }catch (Throwable t){
            throwOriginalException(t);
            throw new RuntimeException("How did you get here?",t);
        }
        Boolean val;
        if (cache.get(key)!=null){
            return cache.get(key);
        }
        try {

            while (!currentName.equals("java/lang/Object")) {
                byte[] classData=getBytecodes(currentName);
                currentName = ClassHelperSpecial.getSuperName(classData);
                if (cache.get(pairConstructor.invoke(currentName,father))!=null){
                    return cache.get(pairConstructor.invoke(currentName,father));
                }
                if (currentName.equals(father)) {
                    cache.put(key,true);
                    return true;
                }
                if (need){
                    for (String s:ClassHelperSpecial.getInterfaceNames(classData)){
                        if (s.equals(father)){
                            cache.put(key,true);
                            return true;
                        }
                        val=cache.get(pairConstructor.invoke(s,father));
                        if (val!=null){
                            if (val){
                                return true;
                            }
                        }
                        if (isExtends(s,father,true)){
                            cache.put(key,true);
                            return true;
                        }
                    }
                }
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable ex) {
            throwOriginalException(ex);
            throw new RuntimeException("How did you get here?",ex);
        }
        cache.put(key,false);
        return false;
    }

    /**
     * @param current 方法所在的类
     * @param method 要寻找的方法
     * @param pItf 调用结束后即为MethodInsnNode构造函数中isInterface的值，使用数组来起到引用传递的效果
     * @return MethodInsnNode构造函数中owner的值，返回{@code null}表示未找到或无法调用父类方法。
     * */
    @Nullable
    private static String isOverrideMethod(ClassNode current,final MethodNode method,boolean[] pItf) {
        //静态方法显然无法继承
        if (!Modifier.isStatic(method.access)) {
            //原始classNode
            ClassNode realCurrent = current;
            try {
                //true表示无法替换为父类方法调用
                boolean cannotCallSuper=false;
                while(!"java/lang/Object".equals(current.name)) {
                    ClassNode cn = getClassNodeOf(current.superName);
                    final boolean isSamePackage=samePackage(realCurrent, cn);
                    for (MethodNode mn:cn.methods){
                        if (mn.name.equals(method.name) && mn.desc.equals(method.desc)) {
                            if (!Modifier.isStatic(mn.access) //非静态
                                    && !Modifier.isAbstract(mn.access)//已实现
                                    &&
                                    (Modifier.isPublic(mn.access) || Modifier.isProtected(mn.access)//继承的方法应为public或protected
                                    || !Modifier.isPrivate(mn.access) && isSamePackage)
                                    //或者是default(即非private)且同包
                                ) {
                                //坑点1：INVOKESPECIAL参数只能填直接继承的父类，否则会报错不是封闭类
                                return realCurrent.superName;
                            }
                            //坑点2：不能越过父类调用祖父类方法
                            if (Modifier.isAbstract(mn.access)) {//如果是抽象方法，我们无法调用它，也无法调用祖父类方法
                                //这里有个特殊的，详见下方的示例（IDEA特性）
                                cannotCallSuper=true;
                                break;
                            }
                        }
                    }
                    if (cannotCallSuper){
                        //被父类的抽象方法所重写，再在父类的接口或祖父类里查找也没用了（尝试调用会报错不是封闭类），直接返回null
                        return null;
                    }
                    String re=isOverrideItfMethod(current/*注意这里填current而不是cn！cn是current的父类*/,method);
                    if(re!=null){
                        //只有super方法在 realCurrent**直接实现**的接口中 时，MethodInsnNode的构造参数中的isItf才为true
                        pItf[0]= (current==realCurrent);
                        //isIft为true时，INVOKESPECIAL参数填接口名，
                        //否则说明方法在realCurrent的父类的接口中，INVOKESPECIAL参数填父类名（见坑点1）。
                        return pItf[0]?re:realCurrent.superName;
                    }
                    current = cn;
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ex) {
                throwOriginalException(ex);
                throw new RuntimeException("How did you get here?",ex);
            }
        }
        return null;
    }
    public interface A{
        default void f(){
            int aa;
        }
        default void f2(){
            int b;
        }
        private void sfsa(){}
    }
    public interface  B extends A{
        default void f(){
            A.super.f();
        }
        default void bbb(){

        }
        default void sfsa(){
            A.super.sfsa();
        }
    }
    public interface  D extends A{
        default void f(){
            long e;
        }
        default void f2(){
            float a;
        }
    }
    public interface G{
        default void example(){}
    }
    public static abstract class E implements B,D{
        @Override
        public void f() {
            D.super.f();
            B.super.bbb();
        }
        private void test(){

        }
        void eee(){

        }
    }

    public static abstract class F extends E implements G{
        @Override
        public void f() {
            super.bbb();
        }
        protected abstract void test();
        public abstract void example();
    }
    public static abstract class C extends F{
        @Override
        public void f() {
            //因为都是内部类，可以调用E的private方法
            //这里在IDEA里ctrl+右键会显示为调用E.test()，看似可行（IDEA的bug）
            //实际编译时会报错
            //所以找super时不需要特殊处理
            super.test();
        }
        @Override
        void eee() {
        }
    }
    private static ClassNode getClassNodeOf(String name) throws ClassNotFoundException{
        ClassReader classReader = new ClassReader(getBytecodes(name));
        ClassNode classNode = new ClassNode();
        //跳过Code部分以加速
        classReader.accept(classNode,SKIP_CODE | SKIP_DEBUG | SKIP_FRAMES);
        return classNode;
    }
    @Nullable
    private static String isOverrideItfMethod(final ClassNode current, MethodNode method){
        if (!Modifier.isStatic(method.access)) {
            try {
                List<String> interfaces = current.interfaces;
                //true表示无法替换为父接口方法调用
                boolean cannotCallSuper;
                for (String itf : interfaces) {
                    //icn:当前遍历到的接口
                    ClassNode icn = getClassNodeOf(itf);

                    //cantCallSuper对于每个接口都是独立的
                    cannotCallSuper=false;

                    for (MethodNode mn : icn.methods) {
                        if (mn.name.equals(method.name) && mn.desc.equals(method.desc)) {
                            if (!Modifier.isStatic(mn.access)
                                    &&!Modifier.isAbstract(mn.access)//必须已实现，即默认方法（接口里的抽象方法自带abstract修饰符）
                                    &&!Modifier.isPrivate(mn.access)//默认方法的权限符应只为public或private，非private即为public
                            ){
                                //同坑点1。INVOKESPECIAL参数只能填直接继承的接口
                                return itf;
                            }
                            if (Modifier.isAbstract(mn.access)) {
                                //同坑点2。不能越过父接口调用祖父接口方法
                                //所以无需再在这个接口的父接口中继续寻找
                                cannotCallSuper = true;
                                break;
                            }
                        }
                    }
                    if (!cannotCallSuper){
                        //递归查找
                        if (isOverrideItfMethod(icn, method)!=null){
                            //同坑点1。INVOKESPECIAL参数只能填直接继承的接口
                            return itf;
                        }
                    }
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ex) {
                throwOriginalException(ex);
                throw new RuntimeException("How did you get here?",ex);
            }
        }
        return null;
    }



    private static boolean genericParadigmMatch(String dest,String src,int... targets){
        return genericParadigmMatch(dest,src,targets,null);
    }
    private static boolean genericParadigmMatch(String dest,String src,int[] targets,boolean[] need){
        if (dest.equals(src)){
            return true;
        }
        if (!Type.getReturnType(dest).equals(Type.getReturnType(src))){
            return false;
        }
        Type[] destTypes=Type.getArgumentTypes(dest),srcTypes=Type.getArgumentTypes(src);
        if (destTypes.length!=srcTypes.length){
            return false;
        }
        int cnt=0;
        for (int i:targets){
            if (!destTypes[i].equals(srcTypes[i])){
                if (destTypes[i].getSort()!=Type.OBJECT){
                    return false;
                }
                if (!isExtends(destTypes[i].getClassName().replace('.','/'),srcTypes[i].getClassName().replace('.','/'), need != null && need[cnt++])){
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean samePackage(ClassNode c1, ClassNode c2) {
        return c1.name.substring(0, c1.name.lastIndexOf('/')).equals(c2.name.substring(0, c2.name.lastIndexOf('/')));
    }

    private static final class Pair{
        public final long x,y;
        public Pair(String a,String father){
            x= (long) getHash1(a)<<32L|getHash1(father);
            y= (long) getHash2(a)<<32L|getHash2(father);
        }
        @Override
        public boolean equals(Object obj) {
            if (obj==this){
                return true;
            }
            if (obj instanceof Pair pair) {
                return pair.x == this.x && pair.y == this.y;
            }
            return false;
        }
        @Override
        public int hashCode() {
            return Long.hashCode((long)Long.hashCode(x)<<32L |Long.hashCode(y));
        }
        private static int getHash1(String s){
            long re=0;
            for (int i=0;i<s.length();i++){
                re=re*151L+s.charAt(i);
                re%=1000000007L;
            }
            return (int) re;
        }

        private static int getHash2(String s){
            long re=0;
            for (int i=0;i<s.length();i++){
                re=re*137L+s.charAt(i);
                re%=1000000009L;
            }
            return (int) re;
        }
    }
}
