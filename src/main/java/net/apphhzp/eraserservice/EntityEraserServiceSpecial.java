package net.apphhzp.eraserservice;

import apphhzp.lib.ClassHelperSpecial;
import apphhzp.lib.ClassOption;
import apphhzp.lib.api.callbacks.CallObjectMethodCallBack;
import apphhzp.lib.api.callbacks.SetEventCallbacksCallback;
import cpw.mods.bootstraplauncher.BootstrapLauncher;
import cpw.mods.modlauncher.ArgumentHandler;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.*;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import net.minecraftforge.accesstransformer.service.AccessTransformerService;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.service.ModLauncherService;
import net.minecraftforge.fml.common.asm.CapabilityTokenSubclass;
import net.minecraftforge.fml.common.asm.ObjectHolderDefinalize;
import net.minecraftforge.fml.common.asm.RuntimeEnumExtender;
import net.minecraftforge.fml.loading.ImmediateWindowHandler;
import net.minecraftforge.fml.loading.RuntimeDistCleaner;
import net.minecraftforge.fml.loading.log4j.SLF4JFixerLaunchPluginService;
import net.minecraftforge.fml.loading.targets.CommonClientLaunchHandler;
import net.minecraftforge.fml.loading.targets.CommonLaunchHandler;
import net.minecraftforge.fml.loading.targets.CommonServerLaunchHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.function.Predicate;

import static apphhzp.lib.ClassHelperSpecial.*;

public class EntityEraserServiceSpecial implements ITransformationService {
    private static final Logger LOGGER;
    static {
        LOGGER = LogManager.getLogger(EntityEraserServiceSpecial.class);
        //testReboot();
        try {
            Class.forName("net.apphhzp.eraserservice.agent.EarlyMethodsForAgent",true,EntityEraserServiceSpecial.class.getClassLoader());
            ClassHelperSpecial.defineHiddenClass("net.apphhzp.eraserservice.HiddenService", EntityEraserServiceSpecial.class,true,EntityEraserServiceSpecial.class.getProtectionDomain(),ClassOption.STRONG,ClassOption.NESTMATE);
        }catch (Throwable t){
            LOGGER.fatal("coremod loaded error", t);
            LOGGER.fatal("call System.exit");
            System.exit(114514);
            throw new RuntimeException(t);
        }
    }

    private static void testReboot(){
        try {
            Field field= Launcher.class.getDeclaredField("argumentHandler"),f2=ArgumentHandler.class.getDeclaredField("args");
            field.setAccessible(true);
            f2.setAccessible(true);
            String[] argumentHandler= (String[])f2.get(field.get(Launcher.INSTANCE));
            Class<?> NLClass=Class.forName("jdk.internal.loader.NativeLibraries");
            Set<String> set= (Set<String>) lookup.findStaticVarHandle(NLClass,"loadedLibraryNames",Set.class).get();
            set.clear();
            BootstrapLauncher.main(argumentHandler);
        }catch (Throwable t){
            throwOriginalException(t);
        }
    }

    public static class EntityEraserHashMap extends HashMap<String, ILaunchPluginService> {
        private static final ILaunchPluginService plugin1, plugin2;
        private static final boolean fuckCoremod= getBoolFromResource("/enableBadCoremod.txt", EntityEraserHashMap.class);
        private static final MethodHandle predicate1Constructor;
        static {
            try {
                ILaunchPluginService[] data=ClassHelperSpecial.getClassData(EntityEraserHashMap.class);
                plugin1=data[0];
                plugin2=data[1];
                predicate1Constructor=lookup.findConstructor(defineHiddenClassWithClassData("net.apphhzp.eraserservice.EntityEraserServiceSpecial$EntityEraserHashMap$Predicate1",data),MethodType.methodType(void.class));
            } catch (Throwable e) {
                throwOriginalException(e);
                throw new RuntimeException("How did you get here?",e);
            }
        }
        private static Class<?> defineHiddenClassWithClassData(String name,Object data){
            return ClassHelperSpecial.defineHiddenClassWithClassData(name,data,EntityEraserHashMap.class, true, null, ClassOption.STRONG,ClassOption.NESTMATE).lookupClass();
        }
        public EntityEraserHashMap(Map<? extends String, ? extends ILaunchPluginService> m) {
            super(m==null?new HashMap<>():m);
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
            if (fuckCoremod){
                try {
                    //noinspection unchecked
                    re.removeIf((Predicate<ILaunchPluginService>) predicate1Constructor.invoke());
                } catch (Throwable e) {
                    throwOriginalException(e);
                    throw new RuntimeException("How did you get here?",e);
                }
            }
            return re;
        }

        private static class Predicate1 implements Predicate<ILaunchPluginService> {
            private static final ILaunchPluginService plugin1, plugin2;
            static {
                ILaunchPluginService[] data=ClassHelperSpecial.getClassData(Predicate1.class);
                plugin1=data[0];
                plugin2=data[1];
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

    public static class EmptySet extends HashSet<String> {
        private static final Iterator<String> emptyIterator;
        static {
            try {
                //noinspection unchecked
                emptyIterator= (Iterator<String>) lookup.findConstructor(defineHiddenClass("net.apphhzp.eraserservice.EntityEraserServiceSpecial$EmptySet$StringIterator"),MethodType.methodType(void.class)).invoke();
            } catch (Throwable e) {
                throwOriginalException(e);
                throw new RuntimeException("How did you get here?",e);
            }
        }
        private static Class<?> defineHiddenClass(String name){
            return ClassHelperSpecial.defineHiddenClass(name,EmptySet.class, true, null, ClassOption.STRONG,ClassOption.NESTMATE).lookupClass();
        }

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
            return emptyIterator;
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

    private static class EntityEraserLaunchHandler extends CommonLaunchHandler {
        private final CommonLaunchHandler old;
        private static final MethodHandle makeServiceMethod;
        private static final MethodHandle runTargetMethod;
        private static final MethodHandle dataServiceMethod;
        private static final MethodHandle serverServiceMethod;
        private static final MethodHandle clientServiceMethod;
        private static final MethodHandle preLaunchMethod;
        private static final MethodHandle runnableConstructor;
        static {
            try {
                makeServiceMethod=lookup.findVirtual(CommonLaunchHandler.class,"makeService", MethodType.methodType(ServiceRunner.class, String[].class, ModuleLayer.class));
                runTargetMethod=lookup.findVirtual(CommonLaunchHandler.class,"runTarget", MethodType.methodType(void.class, String.class, String[].class, ModuleLayer.class));
                dataServiceMethod=lookup.findVirtual(CommonLaunchHandler.class,"dataService", MethodType.methodType(void.class, String[].class, ModuleLayer.class));
                serverServiceMethod=lookup.findVirtual(CommonLaunchHandler.class,"serverService", MethodType.methodType(void.class, String[].class, ModuleLayer.class));
                clientServiceMethod=lookup.findVirtual(CommonLaunchHandler.class,"clientService", MethodType.methodType(void.class, String[].class, ModuleLayer.class));
                preLaunchMethod=lookup.findVirtual(CommonLaunchHandler.class,"preLaunch", MethodType.methodType(String[].class, String[].class, ModuleLayer.class));
                runnableConstructor=lookup.findConstructor(defineHiddenClass("net.apphhzp.eraserservice.EntityEraserServiceSpecial$EntityEraserServiceRunnable"),MethodType.methodType(void.class,CommonLaunchHandler.class,String[].class,ModuleLayer.class));
            }catch (Throwable t){
                EntityEraserServiceSpecial.LOGGER.fatal(t);
                System.exit(114514);
                throw new RuntimeException(t);
            }
        }
        private static Class<?> defineHiddenClass(String name){
            return ClassHelperSpecial.defineHiddenClass(name,EntityEraserLaunchHandler.class, true, null, ClassOption.STRONG,ClassOption.NESTMATE).lookupClass();
        }
        public EntityEraserLaunchHandler(CommonLaunchHandler commonLaunchHandler) {
            super();
            this.old=commonLaunchHandler;
        }
        @Override
        public Dist getDist() {
            return old.getDist();
        }

        @Override
        public String getNaming() {
            return old.getNaming();
        }

        @Override
        public LocatedPaths getMinecraftPaths() {
            return old.getMinecraftPaths();
        }

        @Override
        protected ServiceRunner makeService(String[] strings, ModuleLayer moduleLayer) {
            try {
                return (ServiceRunner) makeServiceMethod.invoke(old,strings,moduleLayer);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String name() {
            return old.name();
        }

        @Override
        public NamedPath[] getPaths() {
            return old.getPaths();
        }

        @Override
        protected void runTarget(String target, String[] arguments, ModuleLayer layer) throws Throwable {
            runTargetMethod.invoke(old,target, arguments, layer);
        }

        @Override
        protected void dataService(String[] arguments, ModuleLayer layer) throws Throwable {
            dataServiceMethod.invoke(old,arguments, layer);
        }

        @Override
        protected void serverService(String[] arguments, ModuleLayer layer) throws Throwable {
            serverServiceMethod.invoke(old,arguments, layer);
        }

        @Override
        public ServiceRunner launchService(String[] arguments, ModuleLayer gameLayer) {
            try {
                return (ServiceRunner) runnableConstructor.invoke(old,arguments,gameLayer);
            } catch (Throwable e) {
                throwOriginalException(e);
                throw new RuntimeException("How did you get here?",e);
            }
        }

        @Override
        protected void clientService(String[] arguments, ModuleLayer layer) throws Throwable {
            clientServiceMethod.invoke(old,arguments,layer);
        }

        @Override
        public void configureTransformationClassLoader(ITransformingClassLoaderBuilder builder) {
            old.configureTransformationClassLoader(builder);
        }

        @Override
        protected String[] preLaunch(String[] arguments, ModuleLayer layer) {
            try {
                return (String[]) preLaunchMethod.invoke(old,arguments, layer);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean isData() {
            return old.isData();
        }

        @Override
        public boolean isProduction() {
            return old.isProduction();
        }
    }

    public static void debug(){
        System.err.toString();
    }

    private static final class EntityEraserServiceRunnable implements ServiceRunner {
        private final CommonLaunchHandler launchHandler;
        private final String[] arguments;
        private final ModuleLayer gameLayer;
        private EntityEraserServiceRunnable(CommonLaunchHandler launchHandler, String[] arguments,
                                            ModuleLayer gameLayer) {
            this.launchHandler = launchHandler;
            this.arguments = arguments;
            this.gameLayer = gameLayer;
        }
        private void runTarget(final String target, final String[] arguments, final ModuleLayer layer) throws Throwable {
            Class.forName(target,true,Thread.currentThread().getContextClassLoader()).getMethod("main", String[].class).invoke(null, (Object)arguments);
        }
        @Override
        public void run() throws Throwable {
            try {
                if (launchHandler instanceof CommonClientLaunchHandler clientLaunchHandler){
                    ImmediateWindowHandler.acceptGameLayer(gameLayer);
                    runTarget("net.apphhzp.entityeraser.EntityEraserMain",arguments,gameLayer);
                }else if (launchHandler instanceof CommonServerLaunchHandler serverLaunchHandler){
                    launchHandler.launchService(arguments, gameLayer).run();
                }else {
                    launchHandler.launchService(arguments, gameLayer).run();
                }
            }catch (Throwable t){
                LOGGER.fatal(t);
                throw t;
            }
        }
    }

    private static class EntityEraserCallBack1 implements CallObjectMethodCallBack {
        private final Instrumentation inst;
        private static final boolean fuckAllAgents= getBoolFromResource("/disableAllJavaAgents.txt", EntityEraserCallBack1.class);

        public EntityEraserCallBack1(Instrumentation inst) {
            this.inst = inst;
        }

        @Override
        public Object pre(Object obj, Class<?> klass, String name, String desc,Object... args) {
            if (fuckAllAgents&&obj instanceof Instrumentation){
                return NULL;
            }
            if (obj!=inst&&obj instanceof Instrumentation&&name.equals("transform")&&args.length==7){
                String className= (String) args[2];
                if (className.startsWith("apphhzp/lib/")||className.startsWith("net/apphhzp/entityeraser/")
                        ||className.startsWith("net/apphhzp/eraserservice/")){
                    //NativeUtil.createMsgBox(obj.toString(),"",0);
                    return NULL;
                }
            }
            return null;
        }

        @Override
        public Object post(Object o, Object o1, Class<?> aClass, String s, String s1,Object... args) {
            return null;
        }
    }
    private static class EntityEraserCallBack2 implements SetEventCallbacksCallback {

        public EntityEraserCallBack2(){
        }

        @Override
        public int callback(EventCallbacks callbacks) {
            callbacks.setCallbackFunc(EventCallbacks.ClassFileLoadHook,0);
            return SetEventCallbacksCallback.super.callback(callbacks);
        }
    }



}
