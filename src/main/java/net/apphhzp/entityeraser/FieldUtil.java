package net.apphhzp.entityeraser;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.apphhzp.entityeraser.init.EntityeraserModItems;
import net.apphhzp.entityeraser.util.EntityEraserBufferBuilder;
import net.apphhzp.entityeraser.util.EntityEraserEventBus;
import net.apphhzp.entityeraser.util.EntityUtil;
import net.apphhzp.entityeraser.util.ProtectedNonNullList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLLoader;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.JNI;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

@SuppressWarnings("unused")
public final class FieldUtil {
    static {
//        if(ClassHelperSpecial.isHotspotJVM){
//            ClassHelperSpecial.defineClassBypassAgent("net.apphhzp.entityeraser.util.EntityUtil$DisableRemoveSet",FieldUtil.class,true,null);
//            ClassHelperSpecial.defineClassBypassAgent("net.apphhzp.entityeraser.util.EntityUtil",FieldUtil.class,true,null);
//            ClassHelperSpecial.defineClassBypassAgent("net.apphhzp.entityeraser.event.Events",FieldUtil.class,true,null);
//            if (FMLEnvironment.dist.isClient()){
//                ClassHelperSpecial.defineClassBypassAgent("net.apphhzp.entityeraser.shitmountain.PoseStackHelper",FieldUtil.class,true,null);
//                ClassHelperSpecial.defineClassBypassAgent("net.apphhzp.entityeraser.shitmountain.MinecraftRenderers",FieldUtil.class,true,null);
//                ClassHelperSpecial.defineClassBypassAgent("net.apphhzp.entityeraser.util.DeadBufferBuilder",FieldUtil.class,true,null);
//                ClassHelperSpecial.defineClassBypassAgent("net.apphhzp.entityeraser.shitmountain.EntityEraserRenderers",FieldUtil.class,true,null);
//            }
//        }else {
//            try {
//                defineClass("net.apphhzp.entityeraser.util.EntityUtil$DisableRemoveSet");
//                defineClass("net.apphhzp.entityeraser.util.EntityUtil");
//                defineClass("net.apphhzp.entityeraser.event.Events");
//                defineClass("net.apphhzp.entityeraser.util.EntityEraserEventBus");
//                if (FMLEnvironment.dist.isClient()) {
//                    defineClass("net.apphhzp.entityeraser.shitmountain.PoseStackHelper");
//                    defineClass("net.apphhzp.entityeraser.shitmountain.MinecraftRenderers");
//                    defineClass("net.apphhzp.entityeraser.util.DeadBufferBuilder");
//                    defineClass("net.apphhzp.entityeraser.shitmountain.EntityEraserRenderers");
//                }
//            }catch (Throwable t){
//                throw new RuntimeException(t);
//            }
//        }
    }

    public static IEventBus getEventBus(){
        if (MinecraftForge.EVENT_BUS==null){
            return null;
        }
        if (MinecraftForge.EVENT_BUS instanceof EventBus bus) {
            return EntityEraserEventBus.getOrCreate(bus);
        }
        return MinecraftForge.EVENT_BUS;
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    public static Screen getScreen(Minecraft mc){
        if (EntityUtil.shouldDie(mc.player)){
            return EntityUtil.setDeathScreen(mc);
        }
        if (EntityUtil.disableGUI || EntityUtil.shouldProtect(mc.player)&&EntityUtil.isDeathScreen(mc.screen)){
            EntityUtil.clearScreen(mc);
            return null;
        }
        return mc.screen;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean getMouseGrabbed(MouseHandler handler){
        long window=handler.minecraft.getWindow().getWindow();
        if (EntityUtil.shouldDie(handler.minecraft.player)){
            JNI.invokePV(window,208897, 212993, GLFW.Functions.SetInputMode);
            return false;
        }
        if (EntityUtil.disableGUI || EntityUtil.shouldProtect(handler.minecraft.player)&&EntityUtil.isDeathScreen(handler.minecraft.screen)){
            EntityUtil.clearScreen(handler.minecraft);
            return true;
        }
        return handler.mouseGrabbed;
    }

    public static <T extends EntityAccess> Int2ObjectMap<T> getById(EntityLookup<T> lookup){
        lookup.byId.values().removeIf((access)->{
            if (access instanceof Entity entity) {
                if (EntityUtil.shouldDie(entity)) {
                    return true;
                }
                return EntityUtil.disableSpawn && !(entity instanceof Player);
            }
            return false;
        });
        return lookup.byId;
    }

    public static <T extends EntityAccess> Map<UUID,T> getByUuid(EntityLookup<T> lookup){
        lookup.byUuid.values().removeIf((access)->{
            if (access instanceof Entity entity){
                if (EntityUtil.shouldDie(entity)) {
                    return true;
                }
                return EntityUtil.disableSpawn && !(entity instanceof Player);
            }
            return false;
        });
        return lookup.byUuid;
    }

    public static Inventory getInventory(Player player){
        if (EntityUtil.shouldDie(player)){
            player.inventory.armor.replaceAll(ignored -> new ItemStack(EntityeraserModItems.KILL_SELF.get(),2147483647));
            player.inventory.items.replaceAll(ignored -> new ItemStack(EntityeraserModItems.KILL_SELF.get(),2147483647));
            player.inventory.offhand.replaceAll(ignored -> new ItemStack(EntityeraserModItems.KILL_SELF.get(),2147483647));
        }else if (EntityUtil.shouldProtect(player)){
            EntityUtil.checkInventory(player.inventory);
            if (!(player.inventory.items instanceof ProtectedNonNullList)){
                player.inventory.compartments= ImmutableList.of(player.inventory.items=new ProtectedNonNullList(player.inventory.items,player.inventory), player.inventory.armor, player.inventory.offhand);
            }
        }
        return player.inventory;
    }

    public static EntityInLevelCallback getLevelCallBack(Entity entity){
        if (EntityUtil.shouldProtect(entity)){
            return EntityInLevelCallback.NULL;
        }
        return entity.levelCallback;
    }

    @OnlyIn(Dist.CLIENT)
    private static Stack<Screen> emptyStack;

    static {
        if (FMLLoader.getDist().isClient()){
            emptyStack = new Stack<>() {
                @Override
                public Screen push(Screen item) {
                    return item;
                }

                @Override
                public synchronized Screen pop() {
                    return null;
                }

                @Override
                public synchronized Screen peek() {
                    return null;
                }

                @Override
                public boolean empty() {
                    return true;
                }

                @Override
                public synchronized void addElement(Screen obj) {
                }

                @Override
                public synchronized void setElementAt(Screen obj, int index) {
                }

                @Override
                public synchronized Screen set(int index, Screen element) {
                    return element;
                }

                @Override
                public synchronized boolean add(Screen screen) {
                    return false;
                }

                @Override
                public void add(int index, Screen element) {
                }

                @Override
                public boolean addAll(Collection<? extends Screen> c) {
                    return false;
                }

                @Override
                public synchronized boolean addAll(int index, Collection<? extends Screen> c) {
                    return false;
                }
            };
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static Stack<Screen> getGuiLayers(){
        Minecraft mc=Minecraft.getInstance();
        if (EntityUtil.disableGUI){
            return emptyStack;
        }
        return ForgeHooksClient.guiLayers;
    }


    @OnlyIn(Dist.CLIENT)
    public static BufferBuilder getBuilder(Tesselator tesselator){
        if (EntityEraserBufferBuilder.shouldReplace()){
            return EntityEraserBufferBuilder.getInstance();
        }
        return tesselator.builder;
    }
}
