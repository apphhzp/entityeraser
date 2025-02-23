package net.apphhzp.entityeraser.util;

import apphhzp.lib.ClassHelper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.apphhzp.entityeraser.EntityeraserMod;
import net.apphhzp.entityeraser.event.Events;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.BusBuilderImpl;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.lang.reflect.Field;

public class EntityEraserEventBus extends EventBus {
    static {
        oldBus= MinecraftForge.EVENT_BUS;
        EntityEraserEventBus bus=new EntityEraserEventBus((BusBuilderImpl) BusBuilder.builder());
        if (MinecraftForge.EVENT_BUS instanceof EventBus){
            try {
                Field field=EventBus.class.getDeclaredField("busID");
                field.setAccessible(true);
                int id=field.getInt(MinecraftForge.EVENT_BUS);
                field.setInt(bus,id);
                INSTANCE=bus;
            }catch (Throwable t){
                throw new RuntimeException("SB",t);
            }
        }else {
            INSTANCE=bus;
        }
    }
    public static final EntityEraserEventBus INSTANCE;
    public static final IEventBus oldBus;
    public EntityEraserEventBus(BusBuilderImpl busBuilder) {
        super(busBuilder);
        try{
            if (oldBus instanceof EventBus){
                Field field=EventBus.class.getDeclaredField("listeners");
                ClassHelper.forceSetField(this,field,ClassHelper.forceGetField(field,oldBus));
            }
        }catch (Throwable throwable){
            throw new RuntimeException("SB",throwable);
        }
    }

    @Override
    public void unregister(Object object) {
        if (object instanceof Class<?> klass){
            if (klass.getName().equals("net.apphhzp.entityeraser.event.Events")||klass.getName().equals("net.apphhzp.entityeraser.EntityeraserMod")){
                return;
            }
        }
        super.unregister(object);
    }

    @Override
    public boolean post(Event event) {
        return this.post(event, (IEventListener::invoke));
    }
    private static final Object2LongMap<Class<?>> st_time = new Object2LongOpenHashMap<>();
    private static final Object2IntMap<Class<?>> cnt = new Object2IntOpenHashMap<>();
    @Override
    public boolean post(Event event, IEventBusInvokeDispatcher wrapper) {

//        {
//            Class<?> klass=event.getClass();
//            cnt.put(klass,cnt.getOrDefault(klass,0) + 1);
//            if (!st_time.containsKey(klass)){
//                st_time.put(klass,System.nanoTime());
//            }
//            if (cnt.getInt(klass)/Math.max((System.nanoTime()-st_time.getLong(klass))/1000000000.0,1.0)<10){
//                System.err.println(klass.getName());
//            }
//        }

        if (!EntityUtil.killEvents.get()) {
            return super.post(event, wrapper);
        }
        if (event instanceof RenderTooltipEvent.Color color){
            Events.onRenderTooltipColor(color);
        }else if (event instanceof TickEvent.PlayerTickEvent playerTickEvent){
            Events.onPlayerTick(playerTickEvent);
        }else if (event instanceof TickEvent.RenderTickEvent renderTickEvent){
            Events.afterLevelRender(renderTickEvent);
        }else if (event instanceof LivingEvent.LivingTickEvent livingTickEvent){
            Events.onLivingEntityTick(livingTickEvent);
        }else if (event instanceof EntityJoinLevelEvent joinLevelEvent){
            Events.onEntityJoinLevel(joinLevelEvent);
        }else if (event instanceof TickEvent.LevelTickEvent levelTickEvent){
            Events.onLevelTick(levelTickEvent);
        }else if (event instanceof RenderGuiEvent.Post renderGuiEvent){
            Events.afterGuiRender(renderGuiEvent);
        }else if (event instanceof LivingDamageEvent damageEvent){
            Events.onEntityDamage(damageEvent);
        }else if (event instanceof LivingDeathEvent deathEvent){
            Events.onEntityDie(deathEvent);
        }else if (event instanceof LivingHurtEvent hurtEvent){
            Events.onEntityHurt(hurtEvent);
        }else if (event instanceof LivingAttackEvent attackEvent){
            Events.onEntityAttack(attackEvent);
        }else if (event instanceof RenderGuiEvent.Pre renderGuiEvent){
            Events.beforeGuiRender(renderGuiEvent);
        }else if (event instanceof TickEvent.ServerTickEvent serverTickEvent){
            EntityeraserMod.tick(serverTickEvent);
        }else if (FMLEnvironment.dist.isClient()){
            doClientEventHandle(event);
        }
        return event.isCanceled();
    }
    @OnlyIn(Dist.CLIENT)
    private static void doClientEventHandle(Event event){
        if(event instanceof ScreenEvent.Render.Pre render){
            Events.beforeScreenRender(render);
        }else if (event instanceof ScreenEvent.Init.Pre pre){
            Events.beforeScreenInit(pre);
        }else if (event instanceof ScreenEvent.Opening opening){
            Events.beforeScreenOpen(opening);
        }
    }
    @Override
    public void shutdown() {}
    @Override
    public void start() {}
}
