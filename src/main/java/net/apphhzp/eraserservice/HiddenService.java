package net.apphhzp.eraserservice;

import apphhzp.lib.ClassHelperSpecial;
import apphhzp.lib.ClassOption;
import apphhzp.lib.CoremodHelper;
import apphhzp.lib.api.ApphhzpInst;
import apphhzp.lib.api.JVMInterfaceFunctions;
import apphhzp.lib.api.callbacks.Callback;
import apphhzp.lib.helfy.JVM;
import apphhzp.lib.hotspot.prims.JvmtiExport;
import apphhzp.lib.natives.NativeUtil;
import cpw.mods.modlauncher.*;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import net.minecraftforge.fml.loading.targets.CommonLaunchHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static apphhzp.lib.ClassHelperSpecial.*;

public class HiddenService {
    private static final Logger LOGGER;
    private static final ILaunchPluginService plugin1, plugin2;
    private static final Set<String> EMPTY_INSTANCE;
    private static final boolean fuckCoremod;
    private static final boolean fuckAllAgents;
    static {
        LOGGER = LogManager.getLogger(HiddenService.class);
        try {
            Class.forName("apphhzp.lib.ClassHelperSpecial");
        } catch (ClassNotFoundException t) {
            LOGGER.fatal("Missing apphhzpLIB!");
            LOGGER.fatal(t);
            System.exit(114514);
        }
        try {
            if (ClassHelperSpecial.version()<8){
                LOGGER.fatal("Wrong apphhzpLIB version! Need 1.0.8 or higher!");
                System.exit(114514);
            }
        }catch (NoSuchMethodError error){
            LOGGER.fatal("Wrong apphhzpLIB version! Need 1.0.8 or higher!");
            LOGGER.fatal(error);
            System.exit(114514);
        }
        try {
            defineClass("net.apphhzp.eraserservice.EntityEraserTransformerSpecial$Phase");
            Class<?> bytecodesGetter=defineHiddenClass("net.apphhzp.eraserservice.classloader.BytecodesGetter");
            lookup.findStaticVarHandle(bytecodesGetter,"useOriginalBytecodes",boolean.class).set(getBoolFromResource("/useOriginalBytecodes.txt", HiddenService.class));
            Class<?> transformerClass=defineHiddenClassWithClassData("net.apphhzp.eraserservice.EntityEraserTransformerSpecial",
                    new Class[]{bytecodesGetter,
                            defineHiddenClass("net.apphhzp.eraserservice.EntityEraserTransformerSpecial$Pair")});
            lookup.findStaticVarHandle(transformerClass,"fuckCoremod",boolean.class).set(fuckCoremod=getBoolFromResource("/enableBadCoremod.txt", HiddenService.class));
            lookup.findStaticVarHandle(transformerClass,"setEventBus",boolean.class).set(getBoolFromResource("/setEventBus.txt", HiddenService.class));
            lookup.findStaticVarHandle(transformerClass,"enableAllReturn",boolean.class).set(getBoolFromResource("/enableAllReturn.txt", HiddenService.class));
            lookup.findStaticVarHandle(transformerClass,"superAllReturn",boolean.class).set(getBoolFromResource("/superAllReturn.txt", HiddenService.class));
            lookup.findStaticVarHandle(transformerClass,"logAllReturn",boolean.class).set(getBoolFromResource("/enableAllReturnLog.txt", HiddenService.class));
            lookup.findStaticVarHandle(transformerClass,"restoreVanillaMethods",boolean.class).set(getBoolFromResource("/restoreVanillaMethods.txt", HiddenService.class));
            lookup.findStaticVarHandle(transformerClass,"hideFromStackTrace",boolean.class).set(getBoolFromResource("/hideFromStackTrace.txt", HiddenService.class));
            lookup.findStaticVarHandle(transformerClass,"abductReflection",boolean.class).set(getBoolFromResource("/abductReflection.txt", HiddenService.class));


            Class<?> plugin1Class=defineHiddenClassWithClassData("net.apphhzp.eraserservice.coremod.EntityEraserPlugin",new Class[]{bytecodesGetter,transformerClass}),
                    plugin2Class=defineHiddenClassWithClassData("net.apphhzp.eraserservice.coremod.AllReturnPlugin",new Class[]{transformerClass});
            plugin1 = (ILaunchPluginService) plugin1Class.getDeclaredConstructor().newInstance();
            plugin2 = (ILaunchPluginService) plugin2Class.getDeclaredConstructor().newInstance();

            Class<?> transformerCLClass=defineHiddenClassWithClassData("net.apphhzp.eraserservice.classloader.EntityEraserClassLoader",new Class[]{transformerClass});
            Class<?> launchPluginHandlerClass=defineHiddenClassWithClassData("net.apphhzp.eraserservice.util.EntityEraserLaunchPluginHandler",new Object[]{plugin1,plugin2,transformerClass});
            Class<?> hashMapClass=defineHiddenClassWithClassData("net.apphhzp.eraserservice.EntityEraserServiceSpecial$EntityEraserHashMap",new ILaunchPluginService[]{plugin1,plugin2});
            MethodHandle hashMapConstructor=lookup.findConstructor(hashMapClass, MethodType.methodType(void.class, Map.class));
            //noinspection unchecked
            EMPTY_INSTANCE = (Set<String>) defineHiddenClass("net.apphhzp.eraserservice.EntityEraserServiceSpecial$EmptySet").getDeclaredConstructor().newInstance();

            Class<?> hiddenAgentClass=defineHiddenClassWithClassData("net.apphhzp.eraserservice.agent.EntityEraserClassFileTransformerSpecial",new Class[]{transformerClass,EntityEraserTransformerSpecial.Phase.class});
            lookup.findStaticVarHandle(hiddenAgentClass,"reloadBytecodes",boolean.class).set(getBoolFromResource("/reloadBytecodes.txt", HiddenService.class));
            fuckAllAgents=getBoolFromResource("/disableAllJavaAgents.txt", HiddenService.class);
            ApphhzpInst inst=null;
            if (getBoolFromResource("/useAgent.txt", HiddenService.class)&&isWindows){
                inst= NativeUtil.createApphhzpInstImpl();
                inst.addTransformer((ClassFileTransformer) hiddenAgentClass.getDeclaredConstructor().newInstance(), true);
                if (inst.isRetransformClassesSupported()){
                    for (Class<?> klass:inst.getAllLoadedClasses()){
                        if (inst.isModifiableClass(klass)){
                            if (klass.isAssignableFrom(ILaunchPluginService.class)||klass.isAssignableFrom(ClassFileTransformer.class)){
                                inst.retransformClasses(klass);
                            }
                        }
                    }
                }
            }
            if (fuckAllAgents){
                if (isWindows){
                    NativeUtil.setJNIFunction(JVMInterfaceFunctions.CallObjectMethod, (Callback) lookup.findConstructor(defineHiddenClass("net.apphhzp.eraserservice.EntityEraserServiceSpecial$EntityEraserCallBack1"),MethodType.methodType(void.class,Instrumentation.class)).invoke(inst));
                }
                if (isWindows){
                    NativeUtil.setJNIFunction(JVMInterfaceFunctions.SetEventCallbacks,(Callback) lookup.findConstructor(defineHiddenClass("net.apphhzp.eraserservice.EntityEraserServiceSpecial$EntityEraserCallBack2"),MethodType.methodType(void.class)).invoke());
                }
                if (isHotspotJVM){
                    createHiddenThread((Runnable) lookup.findConstructor(defineHiddenClass("net.apphhzp.eraserservice.HiddenService$FuckAgentRunnable"),MethodType.methodType(void.class)).invoke(),"DisableJavaAgents");
                }
            }

            Field field = Launcher.class.getDeclaredField("launchPlugins"), lphField, field3;
            field.setAccessible(true);
            lphField = field;
            field3 = TransformStore.class.getDeclaredField("classNeedsTransforming");
            {
                LaunchPluginHandler pluginHandler = (LaunchPluginHandler) field.get(Launcher.INSTANCE);
                field = LaunchPluginHandler.class.getDeclaredField("plugins");
                field.setAccessible(true);
                @SuppressWarnings("unchecked")
                Map<String, ILaunchPluginService> map = (Map<String, ILaunchPluginService>) field.get(pluginHandler);
                map.put(plugin1.name(), plugin1);
                map.put(plugin2.name(), plugin2);
            }
            {
                AtomicInteger cnt = new AtomicInteger();
                final boolean enableSetClassLoader = getBoolFromResource("/enableSetClassLoader.txt", HiddenService.class);
                final boolean enableSetLaunchHandler=getBoolFromResource("/enableSetLaunchHandler.txt", HiddenService.class);
                final Field classTransformerField = TransformingClassLoader.class.getDeclaredField("classTransformer");
                classTransformerField.setAccessible(true);
                final Field lphField2= ClassTransformer.class.getDeclaredField("pluginHandler");
                lphField2.setAccessible(true);
                VarHandle xformsHandlerGetter = lookup.findVarHandle(Launcher.class, "transformationServicesHandler", Class.forName("cpw.mods.modlauncher.TransformationServicesHandler")),
                        transformStoreGetter = lookup.findVarHandle(Class.forName("cpw.mods.modlauncher.TransformationServicesHandler"), "transformStore", TransformStore.class);
                MethodHandle lphConstructor=lookup.findStatic(launchPluginHandlerClass,"copyFrom",MethodType.methodType(LaunchPluginHandler.class,LaunchPluginHandler.class));
                final boolean[] modifiedLauncher = {false};
                final MethodHandle launchHandlerConstructor=lookup.findConstructor(defineHiddenClass("net.apphhzp.eraserservice.EntityEraserServiceSpecial$EntityEraserLaunchHandler"),MethodType.methodType(void.class, CommonLaunchHandler.class));
                createHiddenThread((Runnable) lookup.findConstructor(defineHiddenClass("net.apphhzp.eraserservice.HiddenService$HiddenRunnable"),
                        MethodType.methodType(void.class, boolean.class, Class.class, Field.class, Field.class, Class.class, MethodHandle.class,
                                Field.class, Class.class, MethodHandle.class, Field.class, VarHandle.class, VarHandle.class, Field.class,
                                boolean.class, boolean[].class, MethodHandle.class, AtomicInteger.class,ILaunchPluginService.class,ILaunchPluginService.class,
                                Set.class,boolean.class,boolean.class))
                        .invoke(enableSetClassLoader, transformerCLClass, classTransformerField, lphField2, launchPluginHandlerClass,
                                lphConstructor, field, hashMapClass, hashMapConstructor, lphField, xformsHandlerGetter,
                                transformStoreGetter, field3, enableSetLaunchHandler, modifiedLauncher, launchHandlerConstructor,
                                cnt,plugin1,plugin2,EMPTY_INSTANCE,fuckCoremod,fuckAllAgents), "EntityEraserServiceSpecial");
            }
            if (fuckCoremod && isWindows) {
                Class<?> emptyAgentClass=defineHiddenClass("net.apphhzp.eraserservice.EntityEraserServiceSpecial$EmptyClassFileTransformer");
                Field mTransformer = Class.forName("sun.instrument.TransformerManager$TransformerInfo").getDeclaredField("mTransformer");
                for (Object obj : NativeUtil.getInstancesOfClass(Class.forName("sun.instrument.TransformerManager$TransformerInfo"))) {
                    Object agent=forceGetField(mTransformer,obj);
                    if (agent!=null&&agent.getClass()!= hiddenAgentClass&&agent.getClass()!=emptyAgentClass) {
                        LOGGER.debug("Remove JavaAgent Transformer: {}",agent);
                        forceSetField(obj, mTransformer, emptyAgentClass.getDeclaredConstructor().newInstance());
                    }
                }
            }
            CoremodHelper.coexist(HiddenService.class);
        } catch (Throwable throwable) {
            LOGGER.fatal("coremod loaded error", throwable);
            LOGGER.fatal("call System.exit");
            System.exit(114514);
            throw new RuntimeException(throwable);
        }
    }

//    private static void createHiddenThread(Runnable task, String name) {
//        Thread thread = new Thread((ThreadGroup)null, task, name);
//        thread.start();
//    }

    private static Class<?> defineHiddenClass(String name){
        return ClassHelperSpecial.defineHiddenClass(name,HiddenService.class, true, null, ClassOption.STRONG,ClassOption.NESTMATE).lookupClass();
    }

    private static Class<?> defineHiddenClassWithClassData(String name,Object data){
        return ClassHelperSpecial.defineHiddenClassWithClassData(name,data,HiddenService.class, true, null,ClassOption.STRONG,ClassOption.NESTMATE).lookupClass();
    }


    private static void defineClass(String name) throws Throwable {
        if (findLoadedClass(HiddenService.class.getClassLoader(), name) == null) {
            InputStream is = HiddenService.class.getResourceAsStream("/" + name.replace('.', '/') + ".class");
            byte[] dat = new byte[is.available()];
            is.read(dat);
            is.close();
            ClassHelperSpecial.defineClass(name, dat, HiddenService.class.getClassLoader());
        }
    }

    private static class HiddenRunnable implements Runnable {
        private final boolean enableSetClassLoader;
        private final Class<?> transformerCLClass;
        private final Field classTransformerField;
        private final Field lphField2;
        private final Class<?> launchPluginHandlerClass;
        private final MethodHandle lphConstructor;
        private final Field pluginsField;
        private final Class<?> hashMapClass;
        private final MethodHandle hashMapConstructor;
        private final Field lphField;
        private final VarHandle xformsHandlerGetter;
        private final VarHandle transformStoreGetter;
        private final Field field3;
        private final boolean enableSetLaunchHandler;
        private final boolean[] modifiedLauncher;
        private final MethodHandle launchHandlerConstructor;
        private final AtomicInteger cnt;
        private static final Logger LOGGER;
        private final ILaunchPluginService plugin1, plugin2;
        private final Set<String> EMPTY_INSTANCE;
        private final boolean fuckCoremod;
        private final boolean fuckAllAgents;
        static {
            LOGGER = LogManager.getLogger(HiddenRunnable.class);
        }

        public HiddenRunnable(boolean enableSetClassLoader, Class<?> transformerCLClass, Field classTransformerField, Field lphField2,
                              Class<?> launchPluginHandlerClass, MethodHandle lphConstructor, Field pluginsField, Class<?> hashMapClass,
                              MethodHandle hashMapConstructor, Field lphField, VarHandle xformsHandlerGetter, VarHandle transformStoreGetter,
                              Field field3, boolean enableSetLaunchHandler, boolean[] modifiedLauncher, MethodHandle launchHandlerConstructor,
                              AtomicInteger cnt,ILaunchPluginService p1,ILaunchPluginService p2,Set<String> emptyInstance,boolean fC,boolean fAA) {
            this.enableSetClassLoader = enableSetClassLoader;
            this.transformerCLClass = transformerCLClass;
            this.classTransformerField = classTransformerField;
            this.lphField2 = lphField2;
            this.launchPluginHandlerClass = launchPluginHandlerClass;
            this.lphConstructor = lphConstructor;
            this.pluginsField = pluginsField;
            this.hashMapClass = hashMapClass;
            this.hashMapConstructor = hashMapConstructor;
            this.lphField = lphField;
            this.xformsHandlerGetter = xformsHandlerGetter;
            this.transformStoreGetter = transformStoreGetter;
            this.field3 = field3;
            this.enableSetLaunchHandler = enableSetLaunchHandler;
            this.modifiedLauncher = modifiedLauncher;
            this.launchHandlerConstructor = launchHandlerConstructor;
            this.cnt = cnt;
            this.plugin1=p1;
            this.plugin2=p2;
            this.EMPTY_INSTANCE = emptyInstance;
            this.fuckCoremod=fC;
            this.fuckAllAgents=fAA;
        }

        @Override
        public void run() {
            for (; ; ) {
                try {
                    {
                        Object obj = unsafe.getObjectVolatile(Launcher.INSTANCE, unsafe.objectFieldOffset(Launcher.class.getDeclaredField("classLoader")));
                        if (enableSetClassLoader && isHotspotJVM && obj != null && obj.getClass() == TransformingClassLoader.class) {
                            setClassPointer(obj, transformerCLClass);
                        }
                        if (!(obj instanceof TransformingClassLoader) && Thread.currentThread().getContextClassLoader() instanceof TransformingClassLoader cl) {
                            obj = cl;
                        }
                        if (obj instanceof TransformingClassLoader cl) {
                            ClassTransformer transformer = (ClassTransformer) classTransformerField.get(cl);
                            LaunchPluginHandler lph = (LaunchPluginHandler) lphField2.get(transformer);
                            if (lph != null && isHotspotJVM && lph.getClass() == LaunchPluginHandler.class) {
                                setClassPointer(lph, launchPluginHandlerClass);
                            } else if (lph != null && lph.getClass() != launchPluginHandlerClass) {
                                forceSetField(transformer, lphField2, lphConstructor.invoke(lph));
                            }
                            @SuppressWarnings("unchecked")
                            Map<String, ILaunchPluginService> map2 = (Map<String, ILaunchPluginService>) pluginsField.get(lph);
                            if (map2 != null && map2.getClass() == HashMap.class && isHotspotJVM) {
                                setClassPointer(map2, hashMapClass);
                            } else if (map2 == null || map2.getClass() != hashMapClass) {
                                forceSetField(lph, pluginsField, hashMapConstructor.invoke(map2));
                            }
                        }
                    }
                    {
                        LaunchPluginHandler lph = (LaunchPluginHandler) lphField.get(Launcher.INSTANCE);
                        if (lph != null && isHotspotJVM && lph.getClass() == LaunchPluginHandler.class) {
                            setClassPointer(lph, launchPluginHandlerClass);
                        } else if (lph != null && lph.getClass() != launchPluginHandlerClass) {
                            forceSetField(Launcher.INSTANCE, lphField, lphConstructor.invoke(lph));
                        }
                        @SuppressWarnings("unchecked")
                        Map<String, ILaunchPluginService> map2 = (Map<String, ILaunchPluginService>) pluginsField.get(lph);
                        if (map2 != null && map2.getClass() == HashMap.class && isHotspotJVM) {
                            setClassPointer(map2, hashMapClass);
                        } else if (map2 == null || map2.getClass() != hashMapClass) {
                            forceSetField(lph, pluginsField, hashMapConstructor.invoke(map2));
                        }
                    }
                    if (fuckCoremod) {
                        Object o = xformsHandlerGetter.get(Launcher.INSTANCE);
                        o = transformStoreGetter.get(o);
                        forceSetField(o, field3, EMPTY_INSTANCE);
                    }
                    if (enableSetLaunchHandler && !modifiedLauncher[0]) {
                        Field f1, f2, f3, f4;
                        f1 = Launcher.class.getDeclaredField("launchService");
                        Class<?> launchServiceHandlerClass = Class.forName("cpw.mods.modlauncher.LaunchServiceHandler"),
                                launchServiceHandlerDecoratorClass = Class.forName("cpw.mods.modlauncher.LaunchServiceHandlerDecorator");
                        f1.setAccessible(true);
                        Object launchServiceHandler = f1.get(Launcher.INSTANCE);
                        f2 = launchServiceHandlerClass.getDeclaredField("launchHandlerLookup");
                        f2.setAccessible(true);
                        Map<String, Object> map = (Map<String, Object>) f2.get(launchServiceHandler);
                        f3 = Launcher.class.getDeclaredField("argumentHandler");
                        f3.setAccessible(true);
                        ArgumentHandler argumentHandler = (ArgumentHandler) f3.get(Launcher.INSTANCE);
                        MethodHandle getLaunchTarget = lookup.findVirtual(ArgumentHandler.class, "getLaunchTarget", MethodType.methodType(String.class));
                        String target = null;
                        try {
                            target = (String) getLaunchTarget.invoke(argumentHandler);
                        } catch (NullPointerException ignored) {
                        }
                        if (target != null) {
                            Object launchServiceHandlerDecorator = map.get(target);
                            f4 = launchServiceHandlerDecoratorClass.getDeclaredField("service");
                            f4.setAccessible(true);
                            Object launchHandlerService = f4.get(launchServiceHandlerDecorator);
                            if (launchHandlerService instanceof CommonLaunchHandler commonLaunchHandler) {
                                forceSetField(launchServiceHandlerDecorator, f4, launchHandlerConstructor.invoke(commonLaunchHandler));
                            } else {
                                LOGGER.fatal("Wrong class of launchHandlerService: {}", launchHandlerService.getClass());
                                System.exit(114514);
                            }
                            modifiedLauncher[0] = true;
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                    LOGGER.fatal(t);
                    if (cnt.incrementAndGet() > 3) {
                        LOGGER.fatal("Too many exceptions were thrown in coremod thread!");
                        System.exit(114514);
                        break;
                    }
                }
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
        public static <T> T forceGetField(Field field, Object obj) {
            String str="To the author of Pig2mod: Don't hard code the desc of my methods.";
            str.length();
            if (Modifier.isStatic(field.getModifiers())) {
                return (T) unsafe.getObjectVolatile(staticFieldBase(field), staticFieldOffset(field));
            }else {
                return (T) unsafe.getObjectVolatile(obj, objectFieldOffset(field));
            }
        }

        public static void setClassPointer(Object object, Class<?> targetClass) {
            String str="To the author of Pig2mod: Don't hard code the desc of my methods.";
            str.length();
            if (object == null) {
                throw new NullPointerException("object==null");
            } else if (targetClass == null) {
                throw new NullPointerException("targetClass==null");
            } else {
                try {
                    lookup.ensureInitialized(targetClass);
                    if (JVM.usingCompressedClassPointers) {
                        int klass_ptr = unsafe.getIntVolatile(unsafe.allocateInstance(targetClass), (long)unsafe.addressSize());
                        unsafe.putIntVolatile(object, (long)unsafe.addressSize(), klass_ptr);
                    } else {
                        long klass_ptr = unsafe.getLongVolatile(unsafe.allocateInstance(targetClass), (long)unsafe.addressSize());
                        unsafe.putLongVolatile(object, (long)unsafe.addressSize(), klass_ptr);
                    }

                } catch (IllegalAccessException | InstantiationException var4) {
                    ReflectiveOperationException ex = var4;
                    throwOriginalException(ex);
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    private static class FuckAgentRunnable implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    unsafe.putByte(JvmtiExport.SHOULD_POST_CLASS_FILE_LOAD_HOOK_ADDRESS, (byte) 0);
                    TimeUnit.NANOSECONDS.sleep(1);
                } catch (Throwable ignored) {
                }
            }
        }
    }
}
