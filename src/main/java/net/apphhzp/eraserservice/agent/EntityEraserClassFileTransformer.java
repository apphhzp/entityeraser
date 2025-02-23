package net.apphhzp.eraserservice.agent;

import apphhzp.lib.ClassHelper;
import apphhzp.lib.CoremodHelper;
import cpw.mods.cl.ModuleClassLoader;
import net.apphhzp.eraserservice.EntityEraserTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

public class EntityEraserClassFileTransformer implements ClassFileTransformer {
    private static final Logger LOGGER = LogManager.getLogger(EntityEraserClassFileTransformer.class);
    private static final String OWNER="net/apphhzp/eraserservice/agent/EntityEraserClassFileTransformer";
    private static final HashSet<ClassLoader> checkedCls=new HashSet<>();
    private static final byte[] allReturnClassData;
    public static boolean fuckASM=false;
    public static boolean fuckCoremod=false;
    static {
        try {
            InputStream is = EntityEraserClassFileTransformer.class.getResourceAsStream("/net/apphhzp/eraserservice/AllReturn.class");
            allReturnClassData = new byte[is.available()];
            is.read(allReturnClassData);
            is.close();
        }catch (Throwable t){
            throw new RuntimeException(t);
        }
    }

    public EntityEraserClassFileTransformer() {
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (className != null && classfileBuffer != null&&!className.startsWith("net/apphhzp/eraserservice")&&!className.startsWith("net/apphhzp/entityeraser")) {
            ClassNode classNode = CoremodHelper.bytes2ClassNote(classfileBuffer, className);
            boolean[] flag = {false};
            boolean flg = false;
            if (!"java/lang/instrument/ClassFileTransformer".equals(className) && EntityEraserTransformer.isExtends(className, "java/lang/instrument/ClassFileTransformer", true)) {
                for (MethodNode method : classNode.methods) {
                    if (!Modifier.isNative(method.access) && !Modifier.isAbstract(method.access) && !Modifier.isStatic(method.access)) {
                        if ("transform".equals(method.name) && "(Ljava/lang/ClassLoader;Ljava/lang/String;Ljava/lang/Class;Ljava/security/ProtectionDomain;[B)[B".equals(method.desc)) {
                            method.instructions.clear();
                            method.localVariables.clear();
                            method.instructions.add(new InsnNode(Opcodes.ACONST_NULL));
                            method.instructions.add(new InsnNode(Opcodes.ARETURN));
                            method.visitMaxs(1, 6);
                            LOGGER.debug("fuck agent transformer:{}",className);
                            flg = true;
                        } else if ("transform".equals(method.name) && "(Ljava/lang/Module;Ljava/lang/ClassLoader;Ljava/lang/String;Ljava/lang/Class;Ljava/security/ProtectionDomain;[B)[B".equals(method.desc)) {
                            method.instructions.clear();
                            method.localVariables.clear();
                            method.instructions.add(new InsnNode(Opcodes.ACONST_NULL));
                            method.instructions.add(new InsnNode(Opcodes.ARETURN));
                            method.visitMaxs(1, 7);
                            LOGGER.debug("fuck agent transformer:{}",className);
                            flg = true;
                        }
                    }
                }
            } else if (!className.startsWith("net/minecraft/")&&!className.startsWith("net/minecraftforge/")&&!className.startsWith("java/lang/")&&!className.startsWith("org/lwjgl/")
                    &&!className.startsWith("org/objectweb/asm/")&&!className.startsWith("org/slf4j/")&&!className.startsWith("org/apache/logging/")
                    &&!className.startsWith("io/netty/")&&!className.startsWith("org/openjdk/nashorn/")&&loader!=null){
                if (loader instanceof ModuleClassLoader){

                    for (MethodNode method:classNode.methods){
                        for (AbstractInsnNode insn:method.instructions){
                            if (insn instanceof MethodInsnNode call){
                                if (fuckASM){
                                    if (call.owner.equals("org/objectweb/asm/tree/InsnList")&&call.desc.endsWith(")V")||
                                            call.desc.endsWith(")V")&&call.name.startsWith("visit")&&EntityEraserTransformer.isExtends(call.owner,"org/objectweb/asm/MethodVisitor")){
                                        method.instructions.remove(call);
                                        LOGGER.debug("agent:fuck ASM at {}.{}", className, method.name);
                                        flg=true;
                                    }
                                }
                            }
                        }
                    }
                    if (fuckCoremod){
                        if (EntityEraserTransformer.isExtends(classNode.name,"cpw/mods/modlauncher/api/ITransformationService",true)){
                            for (MethodNode method:classNode.methods){
                                if ("transformers".equals(method.name)&&"()Ljava/util/List;".equals(method.desc)){
                                    method.instructions.clear();;
                                    method.localVariables.clear();
                                    method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"java/util/Collections","emptyList","()Ljava/util/List;"));
                                    method.instructions.add(new InsnNode(Opcodes.ARETURN));
                                    method.visitMaxs(1, 1);
                                }
                            }
                        }else if (EntityEraserTransformer.isExtends(classNode.name,"cpw/mods/modlauncher/serviceapi/ILaunchPluginService",true)){
                            for (MethodNode method:classNode.methods){
                                if ("processClass".equals(method.name)){
                                    if ("(Lcpw/mods/modlauncher/serviceapi/ILaunchPluginService$Phase;Lorg/objectweb/asm/tree/ClassNode;Lorg/objectweb/asm/Type;)Z".equals(method.desc)||"(Lcpw/mods/modlauncher/serviceapi/ILaunchPluginService$Phase;Lorg/objectweb/asm/tree/ClassNode;Lorg/objectweb/asm/Type;Ljava/lang/String;)Z".equals(method.desc)){
                                        method.instructions.clear();;
                                        method.localVariables.clear();
                                        method.instructions.add(new InsnNode(Opcodes.ICONST_0));
                                        method.instructions.add(new InsnNode(Opcodes.IRETURN));
                                        method.visitMaxs(4, 5);
                                    }
                                }else if ("processClassWithFlags".equals(method.name)&&"(Lcpw/mods/modlauncher/serviceapi/ILaunchPluginService$Phase;Lorg/objectweb/asm/tree/ClassNode;Lorg/objectweb/asm/Type;Ljava/lang/String;)I".equals(method.desc)){
                                    method.instructions.clear();
                                    method.localVariables.clear();
                                    method.instructions.add(new InsnNode(Opcodes.ICONST_0));
                                    method.instructions.add(new InsnNode(Opcodes.IRETURN));
                                    method.visitMaxs(5, 5);
                                }
                            }
                        }
                    }
                }

//                EntityEraserTransformer.tranAddReturn(classNode,flag,"net/apphhzp/eraserservice/AllReturn");
//                if (!checkedCls.contains(loader)) {
//                    try {//class org.spongepowered.asm.mixin.transformer.DefaultExtensions (in module org.spongepowered.mixin) cannot access class net.apphhzp.eraserservice.AllReturn (in unnamed module @0x7f977fba) because module org.spongepowered.mixin does not read unnamed module @0x7f977fba
//                        loader.loadClass("net.apphhzp.eraserservice.AllReturn");
//                    }catch (ClassNotFoundException e){
//                        ClassHelper.defineClass("net.apphhzp.eraserservice.AllReturn",allReturnClassData,loader);
//                    }
//                    checkedCls.add(loader);
//                }
            }
            if (flag[0] || flg) {
                return CoremodHelper.classNote2bytes(classNode, false);
            }
        }
        //LOGGER.debug("get():"+className);
        return classfileBuffer;
    }

    private Module entityeraserModule = null;
    private final Set<Module> added = new HashSet<>();

    @Override
    public byte[] transform(Module module, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (entityeraserModule == null && className != null && className.startsWith("net/apphhzp/entityeraser")) {
            entityeraserModule = module;
        }
        if (added.add(module)) {
            for (String s : module.getPackages()) {
                ClassHelper.addExportImpl(module, s);
                module.addReads(entityeraserModule);
            }
        }
        return this.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
    }
    private static final Class<?> implClass;
    private static final Class<?> managerClass;
    private static final VarHandle retransfomableTransformerManagerGetter;
    private static final VarHandle transformerManagerGetter;
    private static final MethodHandle getUnnamedModule;
    private static final MethodHandle transformMethod;
    //private static final MethodHandle getFieldAccessor;
    static {
        try{
            implClass=Class.forName("sun.instrument.InstrumentationImpl");
            managerClass=Class.forName("sun.instrument.TransformerManager");
            Class<?> bootLoader=Class.forName("jdk.internal.loader.BootLoader");
            getUnnamedModule=ClassHelper.lookup.findStatic(bootLoader,"getUnnamedModule", MethodType.methodType(Module.class));
            retransfomableTransformerManagerGetter=ClassHelper.lookup.findVarHandle(implClass,"mRetransfomableTransformerManager", managerClass);
            transformerManagerGetter=ClassHelper.lookup.findVarHandle(implClass,"mTransformerManager", managerClass);
            transformMethod=ClassHelper.lookup.findVirtual(managerClass,"transform",MethodType.methodType(byte[].class, Module.class,ClassLoader.class,String.class,Class.class,ProtectionDomain.class,byte[].class));
            //getFieldAccessor=ClassHelper.lookup.findVirtual(Field.class, "getFieldAccessor", MethodType.methodType(FieldAccessor.class, Object.class));
        }catch (Throwable t){
            throw new RuntimeException(t);
        }
    }

    public static byte[] transform(Object impl, Module module, ClassLoader loader, String classname, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer, boolean isRetransformer) {
        Object mgr = (isRetransformer ?
                        retransfomableTransformerManagerGetter.get(impl):
                        transformerManagerGetter.get(impl));
        if (module == null) {
            if (classBeingRedefined != null) {
                module = classBeingRedefined.getModule();
            } else {
                try {
                    module = (loader == null) ? (Module) getUnnamedModule.invoke() :loader.getUnnamedModule();
                }catch (Throwable t){
                    throw new RuntimeException();
                }
            }
        }
        if(!classname.startsWith("net/apphhzp/eraserservice")&&!classname.startsWith("net/apphhzp/entityeraser")){
            if (mgr == null){
                return null; // no manager, no transform
            } else {
                try{
                    return (byte[]) transformMethod.invoke(mgr,module,loader,classname,classBeingRedefined,protectionDomain,classfileBuffer);
                }catch (Throwable t){
                    throw new RuntimeException();
                }
            }
        }
        return null;
    }

//    public static FieldAccessor getFieldAccessor(Field field, Object obj) {
//        Class<?> clazz = field.getDeclaringClass();
//        FieldAccessor accessor = null;
//        try {
//            accessor= (FieldAccessor) getFieldAccessor.invoke(field,obj);
//        } catch (Throwable e) {
//            throw new RuntimeException(e);
//        }
//        if(clazz.getName().startsWith("net.apphhzp.entityeraser")) {
//            FieldAccessor finalAccessor = accessor;
//            return new FieldAccessor() {
//                @Override
//                public Object get(Object obj) throws IllegalArgumentException {
//                    return finalAccessor.get(obj);
//                }
//
//                @Override
//                public boolean getBoolean(Object obj) throws IllegalArgumentException {
//                    return finalAccessor.getBoolean(obj);
//                }
//
//                @Override
//                public byte getByte(Object obj) throws IllegalArgumentException {
//                    return finalAccessor.getByte(obj);
//                }
//
//                @Override
//                public char getChar(Object obj) throws IllegalArgumentException {
//                    return finalAccessor.getChar(obj);
//                }
//
//                @Override
//                public short getShort(Object obj) throws IllegalArgumentException {
//                    return finalAccessor.getShort(obj);
//                }
//
//                @Override
//                public int getInt(Object obj) throws IllegalArgumentException {
//                    return finalAccessor.getInt(obj);
//                }
//
//                @Override
//                public long getLong(Object obj) throws IllegalArgumentException {
//                    return finalAccessor.getLong(obj);
//                }
//
//                @Override
//                public float getFloat(Object obj) throws IllegalArgumentException {
//                    return finalAccessor.getFloat(obj);
//                }
//
//                @Override
//                public double getDouble(Object obj) throws IllegalArgumentException {
//                    return finalAccessor.getDouble(obj);
//                }
//
//                @Override
//                public void set(Object obj, Object value) {
//                }
//
//                @Override
//                public void setBoolean(Object obj, boolean z) {
//                }
//
//                @Override
//                public void setByte(Object obj, byte b) {
//                }
//
//                @Override
//                public void setChar(Object obj, char c) {
//                }
//
//                @Override
//                public void setShort(Object obj, short s) {
//                }
//
//                @Override
//                public void setInt(Object obj, int i) {
//                }
//
//                @Override
//                public void setLong(Object obj, long l) {
//                }
//
//                @Override
//                public void setFloat(Object obj, float f) {
//                }
//
//                @Override
//                public void setDouble(Object obj, double d) {
//                }
//            };
//        }
//        return accessor;
//    }
}
