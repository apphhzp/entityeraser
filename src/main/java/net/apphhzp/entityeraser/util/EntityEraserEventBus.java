package net.apphhzp.entityeraser.util;

import apphhzp.lib.ClassHelperSpecial;
import net.apphhzp.entityeraser.EntityeraserMod;
import net.apphhzp.entityeraser.event.Events;
import net.apphhzp.entityeraser.init.EntityeraserModTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.BusBuilderImpl;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.IEventListenerFactory;
import net.minecraftforge.eventbus.ModLauncherFactory;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.WeakHashMap;

import static apphhzp.lib.ClassHelperSpecial.lookup;

public class EntityEraserEventBus extends EventBus {
    private static final WeakHashMap<EventBus,EntityEraserEventBus> cache=new WeakHashMap<>();
    private static final VarHandle exceptionHandlerVar;
    private static final VarHandle trackPhasesVar;
    private static final VarHandle shutdownVar;
    private static final VarHandle baseTypeVar;
    private static final VarHandle checkTypesOnDispatchVar;
    private static final VarHandle factoryVar;
    static {
        try {
            exceptionHandlerVar= lookup.findVarHandle(EventBus.class,"exceptionHandler", IEventExceptionHandler.class);
            trackPhasesVar=lookup.findVarHandle(EventBus.class,"trackPhases",boolean.class);
            shutdownVar=lookup.findVarHandle(EventBus.class,"shutdown",boolean.class);
            baseTypeVar=lookup.findVarHandle(EventBus.class,"baseType",Class.class);
            checkTypesOnDispatchVar=lookup.findVarHandle(EventBus.class,"checkTypesOnDispatch",boolean.class);
            factoryVar=lookup.findVarHandle(EventBus.class,"factory", IEventListenerFactory.class);
        }catch (Throwable t){
            throw new RuntimeException(t);
        }
    }

    public static EventBus getOrCreate(EventBus oldBus) {
        if (oldBus instanceof EntityEraserEventBus re) {
            return re;
        }
        return cache.computeIfAbsent(oldBus,x-> new EntityEraserEventBus(copyFrom(oldBus),oldBus));
    }

    private static BusBuilderImpl copyFrom(EventBus bus){
        BusBuilderImpl builder=new BusBuilderImpl();
        if (!exceptionHandlerVar.get(bus).equals(bus)){
            builder.setExceptionHandler((IEventExceptionHandler) exceptionHandlerVar.get(bus));
        }
        builder.setTrackPhases((Boolean) trackPhasesVar.get(bus));
        if (((Class<?>)baseTypeVar.get(bus)).isInterface()){
            builder.markerType((Class<?>) baseTypeVar.get(bus));
        }
        if ((Boolean)checkTypesOnDispatchVar.get(bus)){
            builder.checkTypesOnDispatch();
        }
        if (factoryVar.get(bus) instanceof ModLauncherFactory){
            builder.useModLauncher();
        }
        return builder;
    }
    private final IEventBus oldIns;
    public EntityEraserEventBus(BusBuilderImpl busBuilder,EventBus oldBus) {
        super(busBuilder);
        oldIns=oldBus;
        try{
            if (oldBus instanceof EventBus){
                Field field=EventBus.class.getDeclaredField("busID");
                field.setAccessible(true);
                ClassHelperSpecial.unsafe.putInt(this,ClassHelperSpecial.unsafe.objectFieldOffset(field),field.getInt(oldBus));
                field=EventBus.class.getDeclaredField("listeners");
                ClassHelperSpecial.forceSetField(this,field,ClassHelperSpecial.forceGetField(field,oldBus));
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

    @Override
    public boolean post(Event event, IEventBusInvokeDispatcher wrapper) {
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
        }else if (event instanceof BuildCreativeModeTabContentsEvent buildCreativeModeTabContentsEvent){
            EntityeraserModTabs.buildTabContentsVanilla(buildCreativeModeTabContentsEvent);
        }
        if (FMLEnvironment.dist.isClient()){
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
