package net.apphhzp.eraserservice;

import apphhzp.lib.ClassHelper;
import apphhzp.lib.CoremodHelper;
import apphhzp.lib.natives.NativeUtil;
import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.TransformStore;
import cpw.mods.modlauncher.TransformingClassLoader;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import net.apphhzp.eraserservice.agent.EntityEraserClassFileTransformer;
import net.apphhzp.eraserservice.classloader.BytecodesGetter;
import net.apphhzp.eraserservice.classloader.EntityEraserClassLoader;
import net.apphhzp.eraserservice.coremod.AllReturnPlugin;
import net.apphhzp.eraserservice.coremod.EntityEraserPlugin;
import net.apphhzp.eraserservice.util.EntityEraserLaunchPluginHandler;
import net.minecraftforge.accesstransformer.service.AccessTransformerService;
import net.minecraftforge.eventbus.service.ModLauncherService;
import net.minecraftforge.fml.common.asm.CapabilityTokenSubclass;
import net.minecraftforge.fml.common.asm.ObjectHolderDefinalize;
import net.minecraftforge.fml.common.asm.RuntimeEnumExtender;
import net.minecraftforge.fml.loading.RuntimeDistCleaner;
import net.minecraftforge.fml.loading.log4j.SLF4JFixerLaunchPluginService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static apphhzp.lib.ClassHelper.*;

public class EntityEraserService implements ITransformationService {
    public static final ILaunchPluginService plugin1, plugin2;
    public static final Set<String> EMPTY_INSTANCE;

    static {
        Logger LOGGER = LogManager.getLogger(EntityEraserService.class);
        try {
            Class.forName("apphhzp.lib.ClassHelper");
        } catch (ClassNotFoundException t) {
            LOGGER.fatal("Missing apphhzpLIB!");
            LOGGER.fatal(t);
            System.exit(114514);
        }
        try {
            if (ClassHelper.version()<7){
                LOGGER.fatal("Wrong apphhzpLIB version! Need 1.0.7 or higher!");
                System.exit(114514);
            }
        }catch (NoSuchMethodError error){
            LOGGER.fatal("Wrong apphhzpLIB version! Need 1.0.7 or higher!");
            LOGGER.fatal(error);
            System.exit(114514);
        }
        try {
            if (isHotspotJVM) {
                defineClassBypassAgent("net.apphhzp.eraserservice.classloader.BytecodesGetter$Function_WithExceptions", EntityEraserService.class, true, null);
                defineClassBypassAgent("net.apphhzp.eraserservice.classloader.BytecodesGetter$Consumer_WithExceptions", EntityEraserService.class, true, null);
                defineClassBypassAgent("net.apphhzp.eraserservice.classloader.BytecodesGetter", EntityEraserService.class, true, null);
                defineClass("net.apphhzp.eraserservice.EntityEraserTransformer$Phase");
                defineClassBypassAgent("net.apphhzp.eraserservice.EntityEraserTransformer$Pair", EntityEraserService.class, true, null);
                defineClassBypassAgent("net.apphhzp.eraserservice.EntityEraserTransformer", EntityEraserService.class, true, null);
                defineClassBypassAgent("net.apphhzp.eraserservice.coremod.EntityEraserPlugin", EntityEraserService.class, true, null);
                defineClassBypassAgent("net.apphhzp.eraserservice.coremod.AllReturnPlugin", EntityEraserService.class, true, null);
                defineClassBypassAgent("net.apphhzp.eraserservice.classloader.EntityEraserClassLoader", EntityEraserService.class, true, null);
                defineClassBypassAgent("net.apphhzp.eraserservice.classloader.EntityEraserModuleClassLoader", EntityEraserService.class, true, null);
                defineClassBypassAgent("net.apphhzp.eraserservice.agent.EntityEraserClassFileTransformer", EntityEraserService.class, true, null);
                defineClassBypassAgent("net.apphhzp.eraserservice.util.EntityEraserLaunchPluginHandler", EntityEraserService.class, true, null);
                defineClassBypassAgent("net.apphhzp.eraserservice.EntityEraserService$EntityEraserHashMap", EntityEraserService.class, true, null);
                defineClassBypassAgent("net.apphhzp.eraserservice.EntityEraserService$EmptySet$StringIterator", EntityEraserService.class, true, null);
                defineClassBypassAgent("net.apphhzp.eraserservice.EntityEraserService$EmptySet", EntityEraserService.class, true, null);
                defineClassBypassAgent("net.apphhzp.eraserservice.EntityEraserService$EmptyClassFileTransformer", EntityEraserService.class, true, null);
            } else {
                defineClass("net.apphhzp.eraserservice.classloader.BytecodesGetter$Function_WithExceptions");
                defineClass("net.apphhzp.eraserservice.classloader.BytecodesGetter$Consumer_WithExceptions");
                defineClass("net.apphhzp.eraserservice.classloader.BytecodesGetter");
                defineClass("net.apphhzp.eraserservice.EntityEraserTransformer$Phase");
                defineClass("net.apphhzp.eraserservice.EntityEraserTransformer$Pair");
                defineClass("net.apphhzp.eraserservice.EntityEraserTransformer");
                defineClass("net.apphhzp.eraserservice.coremod.EntityEraserPlugin");
                defineClass("net.apphhzp.eraserservice.coremod.AllReturnPlugin");
                defineClass("net.apphhzp.eraserservice.classloader.EntityEraserClassLoader");
                defineClass("net.apphhzp.eraserservice.classloader.EntityEraserModuleClassLoader");
                defineClass("net.apphhzp.eraserservice.agent.EntityEraserClassFileTransformer");
                defineClass("net.apphhzp.eraserservice.util.EntityEraserLaunchPluginHandler");
                defineClass("net.apphhzp.eraserservice.EntityEraserService$EntityEraserHashMap");
                defineClass("net.apphhzp.eraserservice.EntityEraserService$EmptySet$StringIterator");
                defineClass("net.apphhzp.eraserservice.EntityEraserService$EmptySet");
                defineClass("net.apphhzp.eraserservice.EntityEraserService$EmptyClassFileTransformer");
            }
            plugin1 = new EntityEraserPlugin();
            plugin2 = new AllReturnPlugin();
            EMPTY_INSTANCE = new EmptySet();
            EntityEraserClassFileTransformer.fuckCoremod = get("/enableBadCoremod.txt");
            BytecodesGetter.useOriginalBytecodes = get("/useOriginalBytecodes.txt");
            EntityEraserTransformer.setEventBus = get("/setEventBus.txt");
            EntityEraserTransformer.enableAllReturn = get("/enableAllReturn.txt");
            EntityEraserTransformer.superAllReturn = get("/superAllReturn.txt");
            EntityEraserTransformer.logAllReturn = get("/enableAllReturnLog.txt");
            EntityEraserTransformer.restoreVanillaMethods = get("/restoreVanillaMethods.txt");
            EntityEraserTransformer.hideFromStackTrace = get("/hideFromStackTrace.txt");
            if (get("/useAgent.txt") && instImpl != null) {
                instImpl.addTransformer(new EntityEraserClassFileTransformer(), true);
                if (instImpl.isRedefineClassesSupported()){

                }
            }
            Field field = Launcher.class.getDeclaredField("launchPlugins"), field2, field3;
            field.setAccessible(true);
            field2 = field;
            field3 = TransformStore.class.getDeclaredField("classNeedsTransforming");
            LaunchPluginHandler pluginHandler = (LaunchPluginHandler) field.get(Launcher.INSTANCE);
            field = LaunchPluginHandler.class.getDeclaredField("plugins");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, ILaunchPluginService> map = (Map<String, ILaunchPluginService>) field.get(pluginHandler);
            boolean enableSetClassLoader = get("/enableSetClassLoader.txt");
            unsafe.ensureClassInitialized(EntityEraserClassLoader.class);
            AtomicInteger cnt = new AtomicInteger();
            map.put("", plugin1);

//            if (enableSetClassLoader) {
//                Object obj = Thread.currentThread().getContextClassLoader();
//                if (obj.getClass() == ModuleClassLoader.class) {
//                    setClassPointer(obj, EntityEraserModuleClassLoader.class);
//                }
//            }
            Field finalField = field;
            VarHandle xformsHandlerGetter = lookup.findVarHandle(Launcher.class, "transformationServicesHandler", Class.forName("cpw.mods.modlauncher.TransformationServicesHandler")),
                    transformStoreGetter = lookup.findVarHandle(Class.forName("cpw.mods.modlauncher.TransformationServicesHandler"), "transformStore", TransformStore.class);
            createHiddenThread(() -> {
                for (; ; ) {
                    try {
//                        if (!(map.get("") instanceof EntityEraserPlugin)) {
//                            synchronized (map) {
//                                map.put("", plugin1);
//                            }
//                        }
//                        if (!(map.get("￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿") instanceof AllReturnPlugin)) {
//                            synchronized (map) {
//                                map.put("￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿", plugin2);
//                            }
//                        }
                        @SuppressWarnings("unchecked")
                        Map<String, ILaunchPluginService> map2 = (Map<String, ILaunchPluginService>) finalField.get(pluginHandler);
                        if (map2.getClass() == HashMap.class && isHotspotJVM) {
                            setClassPointer(map2, EntityEraserHashMap.class);
                        } else if (!(map2 instanceof EntityEraserHashMap)) {
                            forceSetField(pluginHandler, finalField, new EntityEraserHashMap(map2));
                        }
                        if (enableSetClassLoader && isHotspotJVM) {
                            Object obj = unsafe.getObjectVolatile(Launcher.INSTANCE, unsafe.objectFieldOffset(Launcher.class.getDeclaredField("classLoader")));
                            if (obj instanceof TransformingClassLoader && !(obj instanceof EntityEraserClassLoader)) {
                                setClassPointer(obj, EntityEraserClassLoader.class);
                            }
                        }
                        if (isHotspotJVM && field2.get(Launcher.INSTANCE).getClass() == LaunchPluginHandler.class) {
                            setClassPointer(field2.get(Launcher.INSTANCE), EntityEraserLaunchPluginHandler.class);
                        }
                        if (EntityEraserClassFileTransformer.fuckCoremod) {
                            Object o = xformsHandlerGetter.get(Launcher.INSTANCE);
                            o = transformStoreGetter.get(o);
                            forceSetField(o, field3, EMPTY_INSTANCE);
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
            }, "EntityEraserService");
            if (EntityEraserClassFileTransformer.fuckCoremod && isWindows) {
                Field mTransformer = Class.forName("sun.instrument.TransformerManager$TransformerInfo").getDeclaredField("mTransformer");
                for (Object obj : NativeUtil.getInstancesOfClass(Class.forName("sun.instrument.TransformerManager$TransformerInfo"))) {
                    if (obj.getClass() != EntityEraserClassFileTransformer.class) {
                        ClassHelper.forceSetField(obj, mTransformer, new EmptyClassFileTransformer());
                    }
                }
            }
            CoremodHelper.coexist(EntityEraserService.class);
        } catch (Throwable throwable) {
            LOGGER.fatal("coremod loaded error", throwable);
            LOGGER.fatal("call System.exit");
            System.exit(114514);
            throw new RuntimeException(throwable);
        }

    }


    private static boolean get(String name) {
        try {
            InputStream is = EntityEraserService.class.getResourceAsStream(name);
            if (is == null) {
                LogManager.getLogger().error("File(" + name + ") not found");
                return false;
            }
            byte[] dat = new byte[is.available()];
            is.read(dat);
            is.close();
            char[] text = new char[dat.length];
            for (int i = 0; i < dat.length; i++) {
                text[i] = (char) dat[i];
            }
            String s = String.copyValueOf(text);
            return !s.contains("false") && s.contains("true");
        } catch (Throwable t) {
            LogManager.getLogger().catching(t);
            return false;
        }
    }

    private static void defineClass(String name) throws Throwable {
        if (findLoadedClass(EntityEraserService.class.getClassLoader(), name) == null) {
            InputStream is = EntityEraserService.class.getResourceAsStream("/" + name.replace('.', '/') + ".class");
            byte[] dat = new byte[is.available()];
            is.read(dat);
            is.close();
            ClassHelper.defineClass(name, dat, EntityEraserService.class.getClassLoader());
        }
    }

    public static class EntityEraserHashMap extends HashMap<String, ILaunchPluginService> {
        public EntityEraserHashMap(Map<? extends String, ? extends ILaunchPluginService> m) {
            super(m);
        }

        @Override
        public ILaunchPluginService get(Object key) {
            if (key.equals(plugin1.name())) {
                return plugin1;
            } else if (key.equals(plugin2.name())) {
                return plugin2;
            }
            return super.get(key);
        }

        @Override
        public Collection<ILaunchPluginService> values() {
            ArrayList<ILaunchPluginService> re = new ArrayList<>(super.values());
            re.remove(plugin1);
            re.remove(plugin2);
            re.add(0, plugin1);
            re.add(plugin2);
            if (EntityEraserClassFileTransformer.fuckCoremod) {
                re.removeIf((lps) -> lps.getClass() != EntityEraserPlugin.class && lps.getClass() != AllReturnPlugin.class
                        && lps.getClass() != AccessTransformerService.class && lps.getClass() != CapabilityTokenSubclass.class
                        && lps.getClass() != ModLauncherService.class && lps.getClass() != ObjectHolderDefinalize.class
                        && lps.getClass() != RuntimeDistCleaner.class && lps.getClass() != RuntimeEnumExtender.class
                        && lps.getClass() != SLF4JFixerLaunchPluginService.class);
            }
            return re;
        }
    }

    public static class EmptySet extends HashSet<String> {


        @Override
        public int size() {
            return 0;
        }


        @Override
        public boolean add(String string) {
            return false;
        }

        @Override
        public Iterator<String> iterator() {
            return new StringIterator();
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends String> c) {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        public static class StringIterator implements Iterator<String> {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public String next() {
                return "";
            }
        }
    }

    @Override
    public @NotNull String name() {
        return " EntityEraser";
    }

    @Override
    public void initialize(IEnvironment environment) {
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) {

    }

    @Override
    public List<Resource> completeScan(IModuleLayerManager layerManager) {

        try {
            Field field = Launcher.class.getDeclaredField("launchPlugins");
            field.setAccessible(true);
            LaunchPluginHandler pluginHandler = (LaunchPluginHandler) field.get(Launcher.INSTANCE);
            field = LaunchPluginHandler.class.getDeclaredField("plugins");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, ILaunchPluginService> map = (Map<String, ILaunchPluginService>) field.get(pluginHandler);
            map.put("", plugin1);
            map.put("￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿", plugin2);
        } catch (Throwable t) {
            t.printStackTrace();
        }


        return List.of();
    }

    @Override
    public @NotNull List<ITransformer> transformers() {
        return Collections.emptyList();
    }


    public static class EmptyClassFileTransformer implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            return null;
        }

        @Override
        public byte[] transform(Module module, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            return null;
        }
    }
}
