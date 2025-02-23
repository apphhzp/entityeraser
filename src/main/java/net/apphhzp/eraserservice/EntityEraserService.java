package net.apphhzp.eraserservice;

import apphhzp.lib.ClassHelper;
import apphhzp.lib.CoremodHelper;
import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.TransformingClassLoader;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import net.apphhzp.eraserservice.agent.EntityEraserClassFileTransformer;
import net.apphhzp.eraserservice.classloader.BytecodesGetter;
import net.apphhzp.eraserservice.classloader.EntityEraserClassLoader;
import net.apphhzp.eraserservice.coremod.AllReturnPlugin;
import net.apphhzp.eraserservice.coremod.EntityEraserPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import sun.misc.Unsafe;

import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import static apphhzp.lib.ClassHelper.*;

public class EntityEraserService implements ITransformationService {
    static {
        Logger LOGGER = LogManager.getLogger(EntityEraserService.class);
        try {
            Class.forName("apphhzp.lib.ClassHelper");
        } catch (ClassNotFoundException t) {
            LOGGER.fatal("MISSING APPHHZP_LIB!");
            LOGGER.fatal(t);
            System.exit(114514);
        }
        try {
            if (isHotspotJVM){
                ClassHelper.defineClassBypassAgent("net.apphhzp.eraserservice.classloader.BytecodesGetter$Function_WithExceptions", EntityEraserService.class,true,null);
                ClassHelper.defineClassBypassAgent("net.apphhzp.eraserservice.classloader.BytecodesGetter$Consumer_WithExceptions", EntityEraserService.class,true,null);
                ClassHelper.defineClassBypassAgent("net.apphhzp.eraserservice.classloader.BytecodesGetter", EntityEraserService.class,true,null);
                ClassHelper.defineClassBypassAgent("net.apphhzp.eraserservice.EntityEraserTransformer$Pair", EntityEraserService.class,true,null);
                ClassHelper.defineClassBypassAgent("net.apphhzp.eraserservice.EntityEraserTransformer", EntityEraserService.class,true,null);
                ClassHelper.defineClassBypassAgent("net.apphhzp.eraserservice.coremod.EntityEraserPlugin",EntityEraserService.class,true,null);
                ClassHelper.defineClassBypassAgent("net.apphhzp.eraserservice.coremod.AllReturnPlugin", EntityEraserService.class,true,null);
                ClassHelper.defineClassBypassAgent("net.apphhzp.eraserservice.classloader.EntityEraserClassLoader", EntityEraserService.class,true,null);
                ClassHelper.defineClassBypassAgent("net.apphhzp.eraserservice.agent.EntityEraserClassFileTransformer",EntityEraserService.class,true,null);
            }else {
                defineClass("net.apphhzp.eraserservice.classloader.BytecodesGetter$Function_WithExceptions");
                defineClass("net.apphhzp.eraserservice.classloader.BytecodesGetter$Consumer_WithExceptions");
                defineClass("net.apphhzp.eraserservice.classloader.BytecodesGetter");
                defineClass("net.apphhzp.eraserservice.EntityEraserTransformer$Pair");
                defineClass("net.apphhzp.eraserservice.EntityEraserTransformer");
                defineClass("net.apphhzp.eraserservice.coremod.EntityEraserPlugin");
                defineClass("net.apphhzp.eraserservice.coremod.AllReturnPlugin");
                defineClass("net.apphhzp.eraserservice.classloader.EntityEraserClassLoader");
                defineClass("net.apphhzp.eraserservice.agent.EntityEraserClassFileTransformer");
            }
            if (get("/useAgent.txt")&&instImpl!=null) {
                //EntityEraserClassFileTransformer.fuckASM=get("/enableFuckASM.txt");
                EntityEraserClassFileTransformer.fuckCoremod=get("/enableBadCoremod.txt");
                instImpl.addTransformer(new EntityEraserClassFileTransformer(), true);
                if (instImpl.isRedefineClassesSupported()){
                    Class<?> implClass=Class.forName("sun.instrument.InstrumentationImpl");
                    InputStream is=implClass.getResourceAsStream("/sun/instrument/InstrumentationImpl.class");
                    byte[] dat=new byte[is.available()];
                    is.read(dat);
                    is.close();
                    ClassNode classNode= CoremodHelper.bytes2ClassNote(dat,"sun.instrument.InstrumentationImpl");
                    for (MethodNode method : classNode.methods) {
                        if ("transform".equals(method.name) && "(Ljava/lang/Module;Ljava/lang/ClassLoader;Ljava/lang/String;Ljava/lang/Class;Ljava/security/ProtectionDomain;[BZ)[B".equals(method.desc)) {
                            method.instructions.clear();
                            method.localVariables.clear();
                            method.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));
                            method.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));
                            method.instructions.add(new VarInsnNode(Opcodes.ALOAD,2));
                            method.instructions.add(new VarInsnNode(Opcodes.ALOAD,3));
                            method.instructions.add(new VarInsnNode(Opcodes.ALOAD,4));
                            method.instructions.add(new VarInsnNode(Opcodes.ALOAD,5));
                            method.instructions.add(new VarInsnNode(Opcodes.ALOAD,6));
                            method.instructions.add(new VarInsnNode(Opcodes.ILOAD,7));
                            method.visitMethodInsn(Opcodes.INVOKESTATIC, "net/apphhzp/eraserservice/agent/EntityEraserClassFileTransformer", "transform", "(Lsun/instrument/InstrumentationImpl;Ljava/lang/Module;Ljava/lang/ClassLoader;Ljava/lang/String;Ljava/lang/Class;Ljava/security/ProtectionDomain;[BZ)[B", false);
                            method.instructions.add(new InsnNode(Opcodes.ARETURN));
                            method.visitMaxs(8,8);
                        }
                    }
                    dat=CoremodHelper.classNote2bytes(classNode,true);
                    instImpl.redefineClasses(new ClassDefinition(implClass,dat));
                }else {
                    instImpl.retransformClasses(Class.forName("sun.instrument.InstrumentationImpl"));
                }
            }
            Field field = Launcher.class.getDeclaredField("launchPlugins");
            field.setAccessible(true);
            LaunchPluginHandler pluginHandler = (LaunchPluginHandler) field.get(Launcher.INSTANCE);
            field = LaunchPluginHandler.class.getDeclaredField("plugins");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, ILaunchPluginService> map = (Map<String, ILaunchPluginService>) field.get(pluginHandler);
            ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
            boolean enableSetClassLoader = get("/enableSetClassLoader.txt");
            EntityEraserTransformer.setEventBus = get("/setEventBus.txt");
            EntityEraserTransformer.enableAllReturn = get("/enableAllReturn.txt");
            EntityEraserTransformer.superAllReturn = get("/superAllReturn.txt");
            BytecodesGetter.useOriginalBytecodes = get("/useOriginalBytecodes.txt");
            EntityEraserTransformer.logAllReturn=get("/enableAllReturnLog.txt");
            unsafe.ensureClassInitialized(EntityEraserClassLoader.class);
            AtomicInteger cnt= new AtomicInteger();
            executor.execute(() -> {
                for (; ; ) {
                    try {
                        if (!(map.get("") instanceof EntityEraserPlugin)) {
                            synchronized (map) {
                                map.put("", new EntityEraserPlugin());
                            }
                        }
                        if (!(map.get("￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿") instanceof AllReturnPlugin)) {
                            synchronized (map) {
                                map.put("￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿", new AllReturnPlugin());
                            }
                        }
                        if (enableSetClassLoader) {
                            Object obj = unsafe.getObjectVolatile(Launcher.INSTANCE, unsafe.objectFieldOffset(Launcher.class.getDeclaredField("classLoader")));
                            if (obj instanceof TransformingClassLoader && !(obj instanceof EntityEraserClassLoader)) {
                                int klass_ptr = unsafe.getIntVolatile(UnsafeAccess.UNSAFE.allocateInstance(EntityEraserClassLoader.class), Unsafe.ARRAY_OBJECT_INDEX_SCALE * 2L);
                                unsafe.putIntVolatile(obj, Unsafe.ARRAY_OBJECT_INDEX_SCALE * 2L, klass_ptr);
                            }
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                        LOGGER.fatal(t);
                        if (cnt.incrementAndGet()>3){
                            LOGGER.fatal("Too many exceptions were thrown in coremod thread!");
                            System.exit(114514);
                            break;
                        }
                    }
                }
            });
//            JarFile jarFile = new JarFile(ClassHelper.getJarPath(EntityEraserService.class));
//            Enumeration<JarEntry> entries = jarFile.entries();
//            MethodHandle methodHandle = ClassHelper.lookup.findVirtual(ClassLoader.class, "findLoadedClass", MethodType.methodType(Class.class, String.class));
//
//            while(entries.hasMoreElements()) {
//                JarEntry entry = (JarEntry)entries.nextElement();
//                String name = entry.getName();
//                if (name.endsWith(".class")&&name.endsWith("net.apphhzp.eraserservice.")) {
//                    name = name.replace('/', '.').substring(0, name.length() - 6);
//                    if (methodHandle.invoke(EntityEraserService.class.getClassLoader(), name) == null) {
//                        InputStream in = jarFile.getInputStream(entry);
//                        byte[] dat = new byte[in.available()];
//                        in.read(dat);
//                        in.close();
//                        ClassHelper.defineClass(name, dat, EntityEraserService.class.getClassLoader());
//                    }
//                }
//            }
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

    private static void defineClass(String name)throws Throwable{
        if (ClassHelper.findLoadedClass(EntityEraserService.class.getClassLoader(),name)==null) {
            InputStream is=EntityEraserService.class.getResourceAsStream("/"+name.replace('.','/')+".class");
            byte[] dat=new byte[is.available()];
            is.read(dat);
            is.close();
            ClassHelper.defineClass(name,dat,EntityEraserService.class.getClassLoader());
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
    public @NotNull List<ITransformer> transformers() {
        return Collections.emptyList();
    }
}
