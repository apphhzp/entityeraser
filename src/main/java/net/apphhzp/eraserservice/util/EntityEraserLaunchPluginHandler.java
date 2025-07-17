package net.apphhzp.eraserservice.util;

import apphhzp.lib.ClassHelperSpecial;
import apphhzp.lib.ClassOption;
import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.ModuleLayerHandler;
import cpw.mods.modlauncher.TransformerAuditTrail;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import net.minecraftforge.accesstransformer.service.AccessTransformerService;
import net.minecraftforge.eventbus.service.ModLauncherService;
import net.minecraftforge.fml.common.asm.CapabilityTokenSubclass;
import net.minecraftforge.fml.common.asm.ObjectHolderDefinalize;
import net.minecraftforge.fml.common.asm.RuntimeEnumExtender;
import net.minecraftforge.fml.loading.RuntimeDistCleaner;
import net.minecraftforge.fml.loading.log4j.SLF4JFixerLaunchPluginService;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;

import static apphhzp.lib.ClassHelperSpecial.*;

public class EntityEraserLaunchPluginHandler extends LaunchPluginHandler {
    private static final ILaunchPluginService plugin1;
    private static final ILaunchPluginService plugin2;
    private static final boolean fuckCoremod;
    private static final MethodHandle predicate1Constructor;
    private static final Field pluginsField;
    static {
        Object[] data= ClassHelperSpecial.getClassData(EntityEraserLaunchPluginHandler.class);
        plugin1= (ILaunchPluginService) data[0];
        plugin2= (ILaunchPluginService) data[1];
        Class<?> klass= (Class<?>) data[2];
        try {
            fuckCoremod= (boolean) lookup.findStaticVarHandle(klass,"fuckCoremod",boolean.class).get();
            predicate1Constructor=lookup.findConstructor(defineHiddenClassWithClassData("net.apphhzp.eraserservice.util.EntityEraserLaunchPluginHandler$Predicate1",new ILaunchPluginService[]{plugin1,plugin2},EntityEraserLaunchPluginHandler.class,true,null, ClassOption.STRONG,ClassOption.NESTMATE).lookupClass(), MethodType.methodType(void.class));
            pluginsField=LaunchPluginHandler.class.getDeclaredField("plugins");
        } catch (Throwable e) {
            throwOriginalException(e);
            throw new RuntimeException("How did you get here?",e);
        }
    }
    public EntityEraserLaunchPluginHandler(ModuleLayerHandler layerHandler) {
        super(layerHandler);
    }

    public static LaunchPluginHandler copyFrom(LaunchPluginHandler old){
        try {
            EntityEraserLaunchPluginHandler re= (EntityEraserLaunchPluginHandler) unsafe.allocateInstance(EntityEraserLaunchPluginHandler.class);
            forceSetField(re,pluginsField, forceGetField(pluginsField,old));
            return re;
        } catch (Throwable e) {
            throwOriginalException(e);
            throw new RuntimeException(e);
        }
    }


    public static void forceSetField(Object o, Field field, Object x) {
        String str="To the author of Pig2mod: Don't hard code the desc of my methods.";
        str.length();
        if (Modifier.isStatic(field.getModifiers())) {
            if (field.getType() == Integer.TYPE) {
                unsafe.putIntVolatile(staticFieldBase(field), staticFieldOffset(field), (Integer)x);
            } else if (field.getType() == Long.TYPE) {
                unsafe.putLongVolatile(staticFieldBase(field), staticFieldOffset(field), (Long)x);
            } else if (field.getType() == Short.TYPE) {
                unsafe.putShortVolatile(staticFieldBase(field), staticFieldOffset(field), (Short)x);
            } else if (field.getType() == Byte.TYPE) {
                unsafe.putByteVolatile(staticFieldBase(field), staticFieldOffset(field), (Byte)x);
            } else if (field.getType() == Float.TYPE) {
                unsafe.putFloatVolatile(staticFieldBase(field), staticFieldOffset(field), (Float)x);
            } else if (field.getType() == Double.TYPE) {
                unsafe.putDoubleVolatile(staticFieldBase(field), staticFieldOffset(field), (Double)x);
            } else if (field.getType() == Character.TYPE) {
                unsafe.putCharVolatile(staticFieldBase(field), staticFieldOffset(field), (Character)x);
            } else if (field.getType() == Boolean.TYPE) {
                unsafe.putBoolean(staticFieldBase(field), staticFieldOffset(field), (Boolean)x);
            } else {
                unsafe.putObjectVolatile(staticFieldBase(field), staticFieldOffset(field), x);
            }
        } else if (field.getType() == Integer.TYPE) {
            unsafe.putIntVolatile(o, objectFieldOffset(field), (Integer)x);
        } else if (field.getType() == Long.TYPE) {
            unsafe.putLongVolatile(o, objectFieldOffset(field), (Long)x);
        } else if (field.getType() == Short.TYPE) {
            unsafe.putShortVolatile(o, objectFieldOffset(field), (Short)x);
        } else if (field.getType() == Byte.TYPE) {
            unsafe.putByteVolatile(o, objectFieldOffset(field), (Byte)x);
        } else if (field.getType() == Float.TYPE) {
            unsafe.putFloatVolatile(o, objectFieldOffset(field), (Float)x);
        } else if (field.getType() == Double.TYPE) {
            unsafe.putDoubleVolatile(o, objectFieldOffset(field), (Double)x);
        } else if (field.getType() == Character.TYPE) {
            unsafe.putCharVolatile(o, objectFieldOffset(field), (Character)x);
        } else if (field.getType() == Boolean.TYPE) {
            unsafe.putBooleanVolatile(o, objectFieldOffset(field), (Boolean)x);
        } else {
            unsafe.putObjectVolatile(o, objectFieldOffset(field), x);
        }

    }

    @Override
    public Optional<ILaunchPluginService> get(String name) {
        if (name.equals(plugin1.name())){
            return Optional.of(plugin1);
        }else if (name.equals(plugin2.name())){
            return Optional.of(plugin2);
        }
        return super.get(name);
    }

    @Override
    public EnumMap<ILaunchPluginService.Phase, List<ILaunchPluginService>> computeLaunchPluginTransformerSet(Type className, boolean isEmpty, String reason, TransformerAuditTrail auditTrail) {
        EnumMap<ILaunchPluginService.Phase, List<ILaunchPluginService>> re=super.computeLaunchPluginTransformerSet(className, isEmpty, reason, auditTrail);
        if (!re.containsKey(ILaunchPluginService.Phase.BEFORE)){
            re.put(ILaunchPluginService.Phase.BEFORE, new ArrayList<>());
        }
        if (!re.containsKey(ILaunchPluginService.Phase.AFTER)){
            re.put(ILaunchPluginService.Phase.AFTER, new ArrayList<>());
        }
        for (Map.Entry<ILaunchPluginService.Phase, List<ILaunchPluginService>> entry:re.entrySet()){
            if (entry.getKey()== ILaunchPluginService.Phase.AFTER){
                if (!entry.getValue().contains(plugin1)){
                    entry.getValue().add(plugin1);
                }else if (!entry.getValue().contains(plugin2)){
                    entry.getValue().add(plugin2);
                }
            }else {
                if (!entry.getValue().contains(plugin1)){
                    entry.getValue().add(plugin1);
                }
            }
            if (fuckCoremod){
                try {
                    //noinspection unchecked
                    entry.getValue().removeIf((Predicate<ILaunchPluginService>) predicate1Constructor.invoke());
                } catch (Throwable e) {
                    throwOriginalException(e);
                    throw new RuntimeException("How did you get here?",e);
                }
            }

        }
        //NativeUtil.createMsgBox(Arrays.toString((re.entrySet().toArray())),reason,0);
        return re;
    }

    private static class Predicate1 implements Predicate<ILaunchPluginService> {
        private static final ILaunchPluginService plugin1;
        private static final ILaunchPluginService plugin2;
        static {
            ILaunchPluginService[] data= ClassHelperSpecial.getClassData(Predicate1.class);
            plugin1= data[0];
            plugin2= data[1];
        }
        @Override
        public boolean test(ILaunchPluginService lps) {
            return lps.getClass() != plugin1.getClass() && lps.getClass() != plugin2.getClass()
                    && lps.getClass() != AccessTransformerService.class && lps.getClass() != CapabilityTokenSubclass.class
                    && lps.getClass() != ModLauncherService.class && lps.getClass() != ObjectHolderDefinalize.class
                    && lps.getClass() != RuntimeDistCleaner.class && lps.getClass() != RuntimeEnumExtender.class
                    && lps.getClass() != SLF4JFixerLaunchPluginService.class;
        }
    }
}
