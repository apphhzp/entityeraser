package net.apphhzp.eraserservice;

import apphhzp.lib.ClassHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;

import static net.apphhzp.eraserservice.classloader.BytecodesGetter.getBytecodes;
import static org.objectweb.asm.ClassReader.*;

public final class EntityEraserTransformer {
    private static final Logger LOGGER= LogManager.getLogger(EntityEraserTransformer.class);
    private static final String FIELD_OWNER="net/apphhzp/entityeraser/FieldUtil";
    private static final String METHOD_OWNER="net/apphhzp/entityeraser/MethodUtil";
    public static boolean setEventBus=true;
    public static boolean enableAllReturn=false;
    public static boolean superAllReturn=false;
    public static boolean logAllReturn=false;
    public static void tran(ClassNode classNode, boolean[] flag){
        tran(classNode, flag, false);
    }
    public static void tran(ClassNode classNode, boolean[] flag, boolean canAddReturn){
        if (!classNode.name.startsWith("net/apphhzp/entityeraser")&&!classNode.name.startsWith("net/apphhzp/eraserservice")){
            for (MethodNode method:classNode.methods){
                for (AbstractInsnNode insn:method.instructions){
                    if (insn instanceof FieldInsnNode call) {
                        if (insn.getOpcode() == Opcodes.GETFIELD) {
                            if (("f_91080_".equals(call.name)||"screen".equals(call.name))&&"Lnet/minecraft/client/gui/screens/Screen;".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/client/Minecraft")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,FIELD_OWNER,"getScreen","(Lnet/minecraft/client/Minecraft;)Lnet/minecraft/client/gui/screens/Screen;"));
                                flag[0]=true;
                            }else if (("f_91520_".equals(call.name)||"mouseGrabbed".equals(call.name))&&"Z".equals(call.desc)
                                    &&isExtends(call.owner, "net/minecraft/client/MouseHandler")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,FIELD_OWNER,"getMouseGrabbed","(Lnet/minecraft/client/MouseHandler;)Z"));
                                flag[0]=true;
                            }else if (("f_156807_".equals(call.name)||"byId".equals(call.name))&&"Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;".equals(call.desc)
                                &&isExtends(call.owner, "net/minecraft/world/level/entity/EntityLookup")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,FIELD_OWNER,"getById","(Lnet/minecraft/world/level/entity/EntityLookup;)Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;"));
                                flag[0]=true;
                            }else if (("f_156808_".equals(call.name)||"byUuid".equals(call.name))&&"Ljava/util/Map;".equals(call.desc)
                                    &&isExtends(call.owner, "net/minecraft/world/level/entity/EntityLookup")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,FIELD_OWNER,"getByUuid","(Lnet/minecraft/world/level/entity/EntityLookup;)Ljava/util/Map;"));
                                flag[0]=true;
                            }else if (("f_36093_".equals(call.name)||"inventory".equals(call.name))&&"Lnet/minecraft/world/entity/player/Inventory;".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/entity/player/Player")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,FIELD_OWNER,"getInventory","(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/entity/player/Inventory;"));
                                flag[0]=true;
                            }else if (("f_146801_".equals(call.name)||"levelCallback".equals(call.name))&&"Lnet/minecraft/world/level/entity/EntityInLevelCallback;".equals(call.desc)
                                    &&isExtends(call.owner, "net/minecraft/world/entity/Entity")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,FIELD_OWNER,"getLevelCallBack","(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/level/entity/EntityInLevelCallback;"));
                                flag[0]=true;
                            }
                        }else if (insn.getOpcode()==Opcodes.GETSTATIC){
                            if ("net/minecraftforge/common/MinecraftForge".equals(call.owner)&&"Lnet/minecraftforge/eventbus/api/IEventBus;".equals(call.desc)
                                    &&"EVENT_BUS".equals(call.name)&&setEventBus) {
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,FIELD_OWNER,"getEventBus","()Lnet/minecraftforge/eventbus/api/IEventBus;"));
                                flag[0]=true;
                            }else if ("net/minecraftforge/client/ForgeHooksClient".equals(call.owner)&&"Ljava/util/Stack;".equals(call.desc)
                                    &&"guiLayers".equals(call.name)){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,FIELD_OWNER,"getGuiLayers","()Ljava/util/Stack;"));
                                flag[0]=true;
                            }
                        }
                    }else if (insn instanceof MethodInsnNode call){
                        if (insn.getOpcode()==Opcodes.INVOKEVIRTUAL||insn.getOpcode()==Opcodes.INVOKEINTERFACE){
                            if (("m_21223_".equals(call.name) || "getHealth".equals(call.name))&&"()F".equals(call.desc)&&isExtends(call.owner,"net/minecraft/world/entity/LivingEntity")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getHealth","(Lnet/minecraft/world/entity/LivingEntity;)F"));
                                flag[0]=true;
                            }else if (("m_21233_".equals(call.name)||"getMaxHealth".equals(call.name))&&"()F".equals(call.desc)&&isExtends(call.owner,"net/minecraft/world/entity/LivingEntity")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getMaxHealth","(Lnet/minecraft/world/entity/LivingEntity;)F"));
                                flag[0]=true;
                            }else if (("m_41619_".equals(call.name)||"isEmpty".equals(call.name))&&"()Z".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/item/ItemStack")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"isEmptyStack","(Lnet/minecraft/world/item/ItemStack;)Z"));
                                flag[0]=true;
                            }else if (("m_6084_".equals(call.name)||"isAlive".equals(call.name))&&"()Z".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/entity/Entity")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"isAlive","(Lnet/minecraft/world/entity/Entity;)Z"));
                                flag[0]=true;
                            }else if (("m_213877_".equals(call.name)||"isRemoved".equals(call.name))&&"()Z".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/entity/Entity")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"isRemoved","(Lnet/minecraft/world/entity/Entity;)Z"));
                                flag[0]=true;
                            }else if ("isAddedToWorld".equals(call.name)&&"()Z".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/entity/Entity")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"isAddedToWorld","(Lnet/minecraft/world/entity/Entity;)Z"));
                                flag[0]=true;
                            }else if (("m_8020_".equals(call.name)||"getItem".equals(call.name))&&"(I)Lnet/minecraft/world/item/ItemStack;".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/entity/player/Inventory")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getInventoryItem","(Lnet/minecraft/world/entity/player/Inventory;I)Lnet/minecraft/world/item/ItemStack;"));
                                flag[0]=true;
                            }else if (("m_21224_".equals(call.name)||"isDeadOrDying".equals(call.name))&&"()Z".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/entity/LivingEntity")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"isDeadOrDying","(Lnet/minecraft/world/entity/LivingEntity;)Z"));
                                flag[0]=true;
                            }else if (("m_109093_".equals(call.name) ||"render".equals(call.name))&&"(FJZ)V".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/client/renderer/GameRenderer")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"renderGameRenderer","(Lnet/minecraft/client/renderer/GameRenderer;FJZ)V"));
                                flag[0]=true;
                            }else if (("m_91152_".equals(call.name)||"setScreen".equals(call.name))&&"(Lnet/minecraft/client/gui/screens/Screen;)V".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/client/Minecraft")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"setScreen","(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/screens/Screen;)V"));
                                flag[0]=true;
                            }else if (("m_280421_".equals(call.name)||"render".equals(call.name))&&"(Lnet/minecraft/client/gui/GuiGraphics;F)V".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/client/gui/Gui")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"guiRender","(Lnet/minecraft/client/gui/Gui;Lnet/minecraft/client/gui/GuiGraphics;F)V"));
                                flag[0]=true;
                            }else if (("m_135370_".equals(call.name)||"get".equals(call.name))&&"(Lnet/minecraft/network/syncher/EntityDataAccessor;)Ljava/lang/Object;".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/network/syncher/SynchedEntityData")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntityData","(Lnet/minecraft/network/syncher/SynchedEntityData;Lnet/minecraft/network/syncher/EntityDataAccessor;)Ljava/lang/Object;"));
                                flag[0]=true;
                            }else if (("m_156811_".equals(call.name)||"getAllEntities".equals(call.name))&&"()Ljava/lang/Iterable;".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/level/entity/EntityLookup")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getAllEntities0","(Lnet/minecraft/world/level/entity/EntityLookup;)Ljava/lang/Iterable;"));
                                flag[0]=true;
                            }else if ("post".equals(call.name) &&"(Lnet/minecraftforge/eventbus/api/Event;)Z".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraftforge/eventbus/api/IEventBus",true)){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"postEvent1","(Lnet/minecraftforge/eventbus/api/IEventBus;Lnet/minecraftforge/eventbus/api/Event;)Z"));
                                flag[0]=true;
                            }else if ("post".equals(call.name)&&"(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraftforge/eventbus/api/IEventBusInvokeDispatcher;)Z".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraftforge/eventbus/api/IEventBus",true)){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"postEvent2","(Lnet/minecraftforge/eventbus/api/IEventBus;Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraftforge/eventbus/api/IEventBusInvokeDispatcher;)Z"));
                                flag[0]=true;
                            }else if (("m_285795_".equals(call.name)||"fill".equals(call.name))&&"(Lnet/minecraft/client/renderer/RenderType;IIIIII)V".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/client/gui/GuiGraphics")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"fill","(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/renderer/RenderType;IIIIII)V"));
                                flag[0]=true;
                            }else if (("m_280584_".equals(call.name)||"fillGradient".equals(call.name))&&"(Lcom/mojang/blaze3d/vertex/VertexConsumer;IIIIIII)V".equals(call.name)
                                    &&isExtends(call.owner,"net/minecraft/client/gui/GuiGraphics")/*maybe not private*/){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"fillGradient","(Lnet/minecraft/client/gui/GuiGraphics;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIIIIII)V"));
                                flag[0]=true;
                            }else if(("m_280444_".equals(call.name)||"innerBlit".equals(call.name))&&"(Lnet/minecraft/resources/ResourceLocation;IIIIIFFFF)V".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/client/gui/GuiGraphics")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"innerBlit","(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;IIIIIFFFF)V"));
                                flag[0]=true;
                            }else if (("m_280479_".equals(call.name)||"innerBlit".equals(call.name))&&"(Lnet/minecraft/resources/ResourceLocation;IIIIIFFFFFFFF)V".equals(call.name)
                                    &&isExtends(call.owner,"net/minecraft/client/gui/GuiGraphics")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"innerBlit","(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;IIIIIFFFFFFFF)V"));
                                flag[0]=true;
                            }else if (("m_6667_".equals(call.name)||"die".equals(call.name))&&"(Lnet/minecraft/world/damagesource/DamageSource;)V".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/entity/LivingEntity")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"die","(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;)V"));
                                flag[0]=true;
                            }else if (("m_6469_".equals(call.name)||"hurt".equals(call.name))&&"(Lnet/minecraft/world/damagesource/DamageSource;F)Z".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/entity/LivingEntity")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"hurt","(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)Z"));
                                flag[0]=true;
                            }else if (("m_88315_".equals(call.name)||"render".equals(call.name))&&"(Lnet/minecraft/client/gui/GuiGraphics;IIF)V".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/client/gui/components/AbstractWidget")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"renderAbstractWidget","(Lnet/minecraft/client/gui/components/AbstractWidget;Lnet/minecraft/client/gui/GuiGraphics;IIF)V"));
                                flag[0]=true;
                            }else if (("m_87963_".equals(call.name)||"renderWidget".equals(call.name))&&"(Lnet/minecraft/client/gui/GuiGraphics;IIF)V".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/client/gui/components/AbstractWidget")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"renderWidget","(Lnet/minecraft/client/gui/components/AbstractWidget;Lnet/minecraft/client/gui/GuiGraphics;IIF)V"));
                                flag[0]=true;
                            }else if ("drawString".equals(call.name)&&"(Lnet/minecraft/client/gui/Font;Ljava/lang/String;FFIZ)I".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/client/gui/GuiGraphics")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"drawString","(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;Ljava/lang/String;FFIZ)I"));
                                flag[0]=true;
                            }else if ("drawString".equals(call.name)&&"(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;FFIZ)I".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/client/gui/GuiGraphics")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"drawString","(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;FFIZ)I"));
                                flag[0]=true;
                            }else if (("m_142646_".equals(call.name)||"getEntities".equals(call.name))&&"()Lnet/minecraft/world/level/entity/LevelEntityGetter;".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/level/Level")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/level/entity/LevelEntityGetter;"));
                                flag[0]=true;
                            }else if (("m_6249_".equals(call.name)||"getEntities".equals(call.name))&&"(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/level/EntityGetter",true)){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/world/level/EntityGetter;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"));
                                flag[0]=true;
                            }else if (("m_142425_".equals(call.name)||"getEntities".equals(call.name))&&"(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/level/EntityGetter",true)){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/world/level/EntityGetter;Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"));
                                flag[0]=true;
                            }else if (("m_261153_".equals(call.name)||"getEntities".equals(call.name))&&"(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;Ljava/util/List;)V".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/level/Level")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;Ljava/util/List;)V"));
                                flag[0]=true;
                            }else if (("m_260826_".equals(call.name)||"getEntities".equals(call.name))&&"(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;Ljava/util/List;I)V".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/level/Level")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;Ljava/util/List;I)V"));
                                flag[0]=true;
                            }else if (("m_36071_".equals(call.name)||"dropAll".equals(call.name))&&"()V".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/entity/player/Inventory")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"dropAll","(Lnet/minecraft/world/entity/player/Inventory;)V"));
                                flag[0]=true;
                            }else if (("m_9942_".equals(call.name)||"disconnect".equals(call.name))&&"(Lnet/minecraft/network/chat/Component;)V".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/server/network/ServerGamePacketListenerImpl")){
                                //NativeUtil.createMsgBox(classNode.name+"."+method.name,"found",NativeUtil.MB_OK);
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"disconnect","(Lnet/minecraft/server/network/ServerGamePacketListenerImpl;Lnet/minecraft/network/chat/Component;)V"));
                                flag[0]=true;
                            }else if (("m_156822_".equals(call.name)||"remove".equals(call.name))&&"(Lnet/minecraft/world/level/entity/EntityAccess;)V".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/level/entity/EntityLookup")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"remove","(Lnet/minecraft/world/level/entity/EntityLookup;Lnet/minecraft/world/level/entity/EntityAccess;)V"));
                                flag[0]=true;
                            }else if (("m_91400_".equals(call.name)||"allowsMultiplayer".equals(call.name))&&"()Z".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/client/Minecraft")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"allowsMultiplayer","(Lnet/minecraft/client/Minecraft;)Z"));
                                flag[0]=true;
                            }else if (("m_239210_".equals(call.name)||"multiplayerBan".equals(call.name))&&"()Lcom/mojang/authlib/minecraft/BanDetails;".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/client/Minecraft")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"multiplayerBan","(Lnet/minecraft/client/Minecraft;)Lcom/mojang/authlib/minecraft/BanDetails;"));
                                flag[0]=true;
                            }else if (("m_168022_".equals(call.name)||"getChatStatus".equals(call.name))&&"()Lnet/minecraft/client/Minecraft$ChatStatus;".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/client/Minecraft")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getChatStatus","(Lnet/minecraft/client/Minecraft;)Lnet/minecraft/client/Minecraft$ChatStatus;"));
                                flag[0]=true;
                            }else if ("addEntityWithoutEvent".equals(call.name)&&"(Lnet/minecraft/world/level/entity/EntityAccess;Z)Z".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/level/entity/PersistentEntitySectionManager")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"addEntityWithoutEvent","(Lnet/minecraft/world/level/entity/PersistentEntitySectionManager;Lnet/minecraft/world/level/entity/EntityAccess;Z)Z"));
                                flag[0]=true;
                            }else if (("m_157653_".equals(call.name)||"addEntity".equals(call.name))&&"(Lnet/minecraft/world/level/entity/EntityAccess;)V".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/level/entity/TransientEntitySectionManager")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"addEntity","(Lnet/minecraft/world/level/entity/TransientEntitySectionManager;Lnet/minecraft/world/level/entity/EntityAccess;)V"));
                                flag[0]=true;
                            }else if (("m_156814_".equals(call.name)||"add".equals(call.name))&&"(Lnet/minecraft/world/level/entity/EntityAccess;)V".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/level/entity/EntityLookup")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"add","(Lnet/minecraft/world/level/entity/EntityLookup;Lnet/minecraft/world/level/entity/EntityAccess;)V"));
                                flag[0]=true;
                            }else if (("m_6286_".equals(call.name)||"addEntity".equals(call.name))&&"(Lnet/minecraft/world/entity/Entity;)V".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/level/chunk/ChunkAccess")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"addEntity","(Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/entity/Entity;)V"));
                                flag[0]=true;
                            }else if (("m_7967_".equals(call.name)||"addFreshEntity".equals(call.name))&&"(Lnet/minecraft/world/entity/Entity;)Z".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/level/LevelWriter",true)){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"addFreshEntity","(Lnet/minecraft/world/level/LevelWriter;Lnet/minecraft/world/entity/Entity;)Z"));
                                flag[0]=true;
                            }else if ("onAddedToWorld".equals(call.name)&&"()V".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraftforge/common/extensions/IForgeEntity",true)){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"onAddedToWorld","(Lnet/minecraftforge/common/extensions/IForgeEntity;)V"));
                                flag[0]=true;
                            }else if (("m_156908_".equals(call.name)||"add".equals(call.name))&&"(Lnet/minecraft/world/entity/Entity;)V".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/level/entity/EntityTickList")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"add","(Lnet/minecraft/world/level/entity/EntityTickList;Lnet/minecraft/world/entity/Entity;)V"));
                                flag[0]=true;
                            }else if (("m_156912_".equals(call.name)||"remove".equals(call.desc))&&"(Lnet/minecraft/world/entity/Entity;)V".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/level/entity/EntityTickList")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"remove","(Lnet/minecraft/world/level/entity/EntityTickList;Lnet/minecraft/world/entity/Entity;)V"));
                                flag[0]=true;
                            }else if (("m_156792_".equals(call.name)||"getEntities".equals(call.name))&&"()Ljava/util/stream/Stream;".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/level/entity/ChunkEntities")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/world/level/entity/ChunkEntities;)Ljava/util/stream/Stream;"));
                                flag[0]=true;
                            }else if (("m_260822_".equals(call.name)||"getEntities".equals(call.name))&&"(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/util/AbortableIterationConsumer;)V".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/level/entity/EntityLookup")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/world/level/entity/EntityLookup;Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/util/AbortableIterationConsumer;)V"));
                                flag[0]=true;
                            }else if (("m_156845_".equals(call.name)||"getEntities".equals(call.name))&&"()Ljava/util/stream/Stream;".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/level/entity/EntitySection")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/world/level/entity/EntitySection;)Ljava/util/stream/Stream;"));
                                flag[0]=true;
                            }else if (("m_188348_".equals(call.name)||"getEntities".equals(call.name))&&"(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/util/AbortableIterationConsumer;)Lnet/minecraft/util/AbortableIterationConsumer$Continuation;".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/level/entity/EntitySection")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/world/level/entity/EntitySection;Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/util/AbortableIterationConsumer;)Lnet/minecraft/util/AbortableIterationConsumer$Continuation;"));
                                flag[0]=true;
                            }else if (("m_260830_".equals(call.name)||"getEntities".equals(call.name))&&"(Lnet/minecraft/world/phys/AABB;Lnet/minecraft/util/AbortableIterationConsumer;)Lnet/minecraft/util/AbortableIterationConsumer$Continuation;".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/level/entity/EntitySection")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/world/level/entity/EntitySection;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/util/AbortableIterationConsumer;)Lnet/minecraft/util/AbortableIterationConsumer$Continuation;"));
                                flag[0]=true;
                            }else if (("m_261111_".equals(call.name)||"getEntities".equals(call.name))&&"(Lnet/minecraft/world/phys/AABB;Lnet/minecraft/util/AbortableIterationConsumer;)V".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/level/entity/EntitySectionStorage")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/world/level/entity/EntitySectionStorage;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/util/AbortableIterationConsumer;)V"));
                                flag[0]=true;
                            }else if (("m_261191_".equals(call.name)||"getEntities".equals(call.name))&&"(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/util/AbortableIterationConsumer;)V".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/level/entity/EntitySectionStorage")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntities","(Lnet/minecraft/world/level/entity/EntitySectionStorage;Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/util/AbortableIterationConsumer;)V"));
                                flag[0]=true;
                            }else if (("m_83971_".equals(call.name)||"_blitToScreen".equals(call.name))&&"(IIZ)V".equals(call.desc)
                                    &&isExtends(call.owner,"com/mojang/blaze3d/pipeline/RenderTarget")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"_blitToScreen","(Lcom/mojang/blaze3d/pipeline/RenderTarget;IIZ)V"));
                                flag[0]=true;
                            }else if (("m_85435_".equals(call.name)||"updateDisplay".equals(call.name))&&"()V".equals(call.desc)
                                    &&isExtends(call.owner,"com/mojang/blaze3d/platform/Window")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"updateDisplay","(Lcom/mojang/blaze3d/platform/Window;)V"));
                                flag[0]=true;
                            }else if(("m_157567_".equals(call.name)||"getEntityGetter".equals(call.name))&&"()Lnet/minecraft/world/level/entity/LevelEntityGetter;".equals(call.desc)
                                    &&isExtends(call.owner,"net/minecraft/world/level/entity/PersistentEntitySectionManager")){
                                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getEntityGetter","(Lnet/minecraft/world/level/entity/PersistentEntitySectionManager;)Lnet/minecraft/world/level/entity/LevelEntityGetter;"));
                                flag[0]=true;
                            }
                        }else if (call.getOpcode()==Opcodes.INVOKESTATIC){
                            if ("net/minecraftforge/fml/util/ObfuscationReflectionHelper".equals(call.owner)){
                                if ("setPrivateValue".equals(call.name)&&"(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;)V".equals(call.desc)){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"setPrivateValue","(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;)V"));
                                    flag[0]=true;
                                }else if ("getPrivateValue".equals(call.name)&&"(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;".equals(call.desc)){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"getPrivateValue","(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;"));
                                    flag[0]=true;
                                }
                            }else if ("com/mojang/blaze3d/vertex/BufferUploader".equals(call.owner)){
                                if (("m_231209_".equals(call.name)||"draw".equals(call.name))&&"(Lcom/mojang/blaze3d/vertex/BufferBuilder$RenderedBuffer;)V".equals(call.desc)){
                                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,METHOD_OWNER,"draw","(Lcom/mojang/blaze3d/vertex/BufferBuilder$RenderedBuffer;)V"));
                                    flag[0]=true;
                                }
                            }
                        }
                    }
                }
            }
            if (canAddReturn&&enableAllReturn){
                addReturn(classNode,flag,"net/apphhzp/entityeraser/AllReturn");
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

    private static void addReturn(ClassNode classNode,boolean[] flag,String allReturnClassName){
        if (!classNode.name.startsWith("it/unimi/dsi/fastutil/") && !classNode.name.startsWith("net/minecraft/")
                &&!classNode.name.startsWith("net/minecraftforge/")&&!classNode.name.startsWith("com/mojang/")
                &&!classNode.name.startsWith("apphhzp/lib/")){
            for (MethodNode method : classNode.methods) {
                if (!Modifier.isAbstract(method.access) && !Modifier.isNative(method.access)) {
                    if (!"<init>".equals(method.name) && !"<clinit>".equals(method.name)) {
                        InsnList list = new InsnList();
                        int slot;
                        boolean[] isItf = {false};
                        String overrideClass = isOverrideMethod(classNode, method, isItf);
                        final Type reType = Type.getReturnType(method.desc);
                        if (overrideClass != null) {
                            list.add(new LabelNode());
                            LabelNode labelNode = new LabelNode();
                            list.add(new FieldInsnNode(Opcodes.GETSTATIC, allReturnClassName, "allReturn", "Z"));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, labelNode));
                            final Type[] argTypes = Type.getArgumentTypes(method.desc);
                            slot = 0;
                            list.add(new VarInsnNode(Opcodes.ALOAD, slot++));
                            for (Type arg : argTypes) {
                                list.add(new VarInsnNode(switch (arg.getSort()) {
                                    case Type.BOOLEAN, Type.CHAR, Type.BYTE, Type.SHORT, Type.INT -> Opcodes.ILOAD;
                                    case Type.FLOAT -> Opcodes.FLOAD;
                                    case Type.LONG -> Opcodes.LLOAD;
                                    case Type.DOUBLE -> Opcodes.DLOAD;
                                    case Type.ARRAY, Type.OBJECT -> Opcodes.ALOAD;
                                    case Type.METHOD ->
                                            throw new IllegalStateException("Unexpected argument type:Method");
                                    default -> throw new VerifyError("Unknown argument type:" + arg);
                                }, slot++));
                                if (arg.equals(Type.LONG_TYPE) || arg.equals(Type.DOUBLE_TYPE)) {
                                    slot++;
                                }
                            }
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
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            method.instructions.insert(list);
                            flag[0] = true;
                            if (logAllReturn) {
                                LOGGER.debug("allReturn(call super):{}.{}{}  super:{}", classNode.name, method.name, method.desc, overrideClass);
                            }
                        } else {
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
    private static final HashMap<Pair,Boolean> cache=new HashMap<>();
    public static boolean isExtends(String a,String father){
        return isExtends(a,father,false);
    }

    public static boolean isExtends(String a, String father,boolean need) {
        if (a.equals(father)) {
            return true;
        }
        Pair key=new Pair(a,father);
        Boolean val;
        if (cache.get(key)!=null){
            return cache.get(key);
        }
        String currentName = a;
        try {
            while (!currentName.equals("java/lang/Object")) {
                byte[] classData=getBytecodes(currentName);
                currentName = ClassHelper.getSuperName(classData);
                if (cache.get(new Pair(currentName,father))!=null){
                    return cache.get(new Pair(currentName,father));
                }
                if (currentName.equals(father)) {
                    cache.put(key,true);
                    return true;
                }
                if (need){
                    for (String s:ClassHelper.getInterfaceNames(classData)){
                        if (s.equals(father)){
                            cache.put(key,true);
                            return true;
                        }
                        val=cache.get(new Pair(s,father));
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
            throw new RuntimeException(ex);
        }
        cache.put(key,false);
        return false;
    }

    private static final int SKIP_ALL=SKIP_CODE | SKIP_DEBUG | SKIP_FRAMES;
    @Nullable
    private static String isOverrideMethod(ClassNode current, MethodNode method,boolean[] pItf) {
        if (!Modifier.isStatic(method.access)) {
            ClassNode realCurrent = current;
            try {
                boolean cantCallSuper;
                while(!"java/lang/Object".equals(current.name)) {
                    byte[] classData = getBytecodes(current.superName);
                    ClassReader classReader = new ClassReader(classData);
                    ClassNode cn = new ClassNode();
                    classReader.accept(cn, SKIP_ALL);
                    cantCallSuper=false;
                    for (MethodNode mn:cn.methods){
                        if (mn.name.equals(method.name) && mn.desc.equals(method.desc)) {
                            if (!Modifier.isStatic(mn.access) && !Modifier.isAbstract(mn.access)
                                    && (Modifier.isPublic(mn.access) || Modifier.isProtected(mn.access)
                                    || !Modifier.isPrivate(mn.access) && samePackage(realCurrent, cn))) {
                                return realCurrent.superName;
                            }
                            if (Modifier.isAbstract(mn.access)) {
                                cantCallSuper=true;
                                break;
                            }
                        }
                    }
                    boolean[] cantCallItf={false};
                    String re=isOverrideItfMethod(current,method);
                    if(re!=null){
                        pItf[0]=current.name.equals(realCurrent.name);
                        return pItf[0]?re:realCurrent.superName;
                    }
                    if (cantCallItf[0]&&cantCallSuper){
                        return null;
                    }
                    current = cn;
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }
        return null;
    }
    @Nullable
    private static String isOverrideItfMethod(ClassNode current, MethodNode method){
        if (!Modifier.isStatic(method.access)) {
            try {
                List<String> interfaces = current.interfaces;
                boolean flag=false;
                for (String itf : interfaces) {
                    ClassNode icn = getClassNodeOf(itf);
                    for (MethodNode mn : icn.methods) {
                        if (mn.name.equals(method.name) && mn.desc.equals(method.desc)) {
                            if (!Modifier.isStatic(mn.access) && !Modifier.isAbstract(mn.access)
                                    &&!Modifier.isPrivate(mn.access)) {
                                return itf;
                            }
                            if (Modifier.isAbstract(mn.access)) {
                                flag = true;
                                break;
                            }
                        }
                    }
                    if (!flag) {
                        String re = isOverrideItfMethod(icn, method);
                        if (re != null) {
                            return itf;
                        }
                    }
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }
        return null;
    }

    private static ClassNode getClassNodeOf(String name) throws ClassNotFoundException{
        ClassReader classReader = new ClassReader(getBytecodes(name));
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode,SKIP_ALL);
        return classNode;
    }
//    private interface E{
//        void fhd();
//    }
//    private interface ee{
//        void fio();
//    }
//    private interface E2 extends E,ee{
//
//    }
//
//    private static class EE implements E2{
//        @Override
//        public void fhd(){
//
//        }
//
//        @Override
//        public void fio() {
//
//        }
//    }

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
