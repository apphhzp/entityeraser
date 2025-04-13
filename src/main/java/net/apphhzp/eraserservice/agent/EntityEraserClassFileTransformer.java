package net.apphhzp.eraserservice.agent;

import apphhzp.lib.ClassHelper;
import apphhzp.lib.CoremodHelper;
import cpw.mods.cl.ModuleClassLoader;
import cpw.mods.modlauncher.TransformingClassLoader;
import net.apphhzp.eraserservice.EntityEraserTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
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

    public EntityEraserClassFileTransformer() {}

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (className != null && classfileBuffer != null&&!className.startsWith("net/apphhzp/eraserservice")&&!className.startsWith("net/apphhzp/entityeraser")) {
            ClassNode classNode = CoremodHelper.bytes2ClassNote(classfileBuffer, className);
            boolean[] flag = {false};
            boolean flg = false;
            if (loader instanceof ModuleClassLoader){
                if (loader instanceof TransformingClassLoader)
                    EntityEraserTransformer.tran(EntityEraserTransformer.Phase.AGENT,classNode,flag,false);
                if(fuckCoremod&&EntityEraserTransformer.isExtends(classNode.name,"java/lang/instrument/ClassFileTransformer",true)){
                    for (MethodNode methodNode:classNode.methods){
                        if (methodNode.name.equals("transform")){
                            if ("(Ljava/lang/ClassLoader;Ljava/lang/String;Ljava/lang/Class;Ljava/security/ProtectionDomain;[B)[B".equals(methodNode.desc)
                                    ||"(Ljava/lang/Module;Ljava/lang/ClassLoader;Ljava/lang/String;Ljava/lang/Class;Ljava/security/ProtectionDomain;[B)[B".equals(methodNode.desc)){
                                methodNode.instructions.clear();
                                methodNode.instructions.add(new InsnNode(Opcodes.ACONST_NULL));
                                methodNode.instructions.add(new InsnNode(Opcodes.ARETURN));
                                methodNode.maxStack=1;
                                flag[0] = true;
                            }
                        }
                    }
                }
                if (fuckASM){
                    if (!classNode.name.startsWith("net/minecraftforge/fml/loading") &&!classNode.name.startsWith("org/objectweb/asm/")
                            &&(EntityEraserTransformer.isExtends(classNode.name,"org/objectweb/asm/MethodVisitor")||EntityEraserTransformer.isExtends(classNode.name,"org/objectweb/asm/ClassVisitor"))){
                        //boolean fff=false;

                        for (MethodNode methodNode:classNode.methods){
                            if (methodNode.name.startsWith("visit")&&methodNode.desc.endsWith(")V")){
                                methodNode.instructions.clear();
                                methodNode.instructions.add(new InsnNode(Opcodes.RETURN));
                                methodNode.maxStack=1;
                                flag[0] = true;
                                //fff=true;
                            }
                        }
//                        if (fff){
//                            NativeUtil.createMsgBox(className,"found!",0);
//                        }
                    }
                }
            }
//           if (!className.startsWith("net/minecraft/")&&!className.startsWith("net/minecraftforge/")&&!className.startsWith("java/lang/")&&!className.startsWith("org/lwjgl/")
//                    &&!className.startsWith("org/objectweb/asm/")&&!className.startsWith("org/slf4j/")&&!className.startsWith("org/apache/logging/")
//                    &&!className.startsWith("io/netty/")&&!className.startsWith("org/openjdk/nashorn/")&&loader!=null){
//
//            }
            if (flag[0] || flg) {
                return CoremodHelper.classNote2bytes(classNode, false);
            }
        }
        //LOGGER.debug("get():"+className);
        return null;
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
//    private static final Class<?> implClass;
//    private static final Class<?> managerClass;
//    private static final VarHandle retransfomableTransformerManagerGetter;
//    private static final VarHandle transformerManagerGetter;
//    private static final MethodHandle getUnnamedModule;
//    private static final MethodHandle transformMethod;
//    //private static final MethodHandle getFieldAccessor;
//    static {
//        try{
//            implClass=Class.forName("sun.instrument.InstrumentationImpl");
//            managerClass=Class.forName("sun.instrument.TransformerManager");
//            Class<?> bootLoader=Class.forName("jdk.internal.loader.BootLoader");
//            getUnnamedModule=ClassHelper.lookup.findStatic(bootLoader,"getUnnamedModule", MethodType.methodType(Module.class));
//            retransfomableTransformerManagerGetter=ClassHelper.lookup.findVarHandle(implClass,"mRetransfomableTransformerManager", managerClass);
//            transformerManagerGetter=ClassHelper.lookup.findVarHandle(implClass,"mTransformerManager", managerClass);
//            transformMethod=ClassHelper.lookup.findVirtual(managerClass,"transform",MethodType.methodType(byte[].class, Module.class,ClassLoader.class,String.class,Class.class,ProtectionDomain.class,byte[].class));
//            //getFieldAccessor=ClassHelper.lookup.findVirtual(Field.class, "getFieldAccessor", MethodType.methodType(FieldAccessor.class, Object.class));
//        }catch (Throwable t){
//            throw new RuntimeException(t);
//        }
//    }
}
