package net.apphhzp.eraserservice.agent;

import apphhzp.lib.ClassHelperSpecial;
import apphhzp.lib.CoremodHelper;
import cpw.mods.cl.ModuleClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.security.ProtectionDomain;

import static apphhzp.lib.ClassHelperSpecial.lookup;

public class EntityEraserClassFileTransformerSpecial implements ClassFileTransformer {

    private static final Logger LOGGER = LogManager.getLogger(EntityEraserClassFileTransformerSpecial.class);
    private static final String OWNER="net/apphhzp/eraserservice/agent/EarlyMethodsForAgent";
    private static final byte[] allReturnClassData;
    public static boolean reloadBytecodes=false;
    private static final Class<?> transformerClass;
    private static final Class<?> transformerPhaseClass;
    private static final Object agentPhase;
    private static final MethodHandle tranMethod;
    private static final boolean fuckCoremod;
    private static final boolean restoreVanillaMethods;
    private static final MethodHandle isExtendsMethod;
    static {
        try {
            Class<?>[] data= ClassHelperSpecial.getClassData(EntityEraserClassFileTransformerSpecial.class);
            transformerClass=data[0];
            transformerPhaseClass=data[1];
            agentPhase= lookup.findStaticVarHandle(transformerPhaseClass,"AGENT",transformerPhaseClass).get();
            tranMethod=lookup.findStatic(transformerClass,"tran", MethodType.methodType(void.class, transformerPhaseClass, ClassNode.class, ClassLoader.class, boolean[].class, boolean.class));
            fuckCoremod= (boolean) lookup.findStaticVarHandle(transformerClass,"fuckCoremod",boolean.class).get();
            isExtendsMethod=lookup.findStatic(transformerClass,"isExtends", MethodType.methodType(boolean.class, String.class, String.class, boolean.class));
            restoreVanillaMethods= (boolean) lookup.findStaticVarHandle(transformerClass,"restoreVanillaMethods",boolean.class).get();


            InputStream is = EntityEraserClassFileTransformerSpecial.class.getResourceAsStream("/net/apphhzp/eraserservice/AllReturn.class");
            allReturnClassData = new byte[is.available()];
            is.read(allReturnClassData);
            is.close();
        }catch (Throwable t){
            throw new RuntimeException(t);
        }
    }

    public EntityEraserClassFileTransformerSpecial() {}

    @Override
    public byte[] transform(Module module,ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        try {
            if (className != null && classfileBuffer != null) {
                if (shouldReload(className)) {
                    byte[] orgCodes=CoremodHelper.getBytecodesFromFile(className,loader,true);
                    if (orgCodes==null){
                        orgCodes=CoremodHelper.getBytecodesFromFile(className,module,true);
                    }
                    if (orgCodes!=null){
                        LOGGER.debug("fix class file: {}", className);
                        classfileBuffer=orgCodes;
                    }else {
                        LOGGER.error("Failed to fix class file: {}", className);
                    }
                }
                ClassNode classNode = CoremodHelper.bytes2ClassNote(classfileBuffer, className);
                boolean[] flag = {false};
                boolean flg = false;
                if (shouldReload(className)){
                    for (FieldNode field : classNode.fields) {
                        field.access|= Opcodes.ACC_PUBLIC;
                        field.access&= ~(Opcodes.ACC_FINAL|Opcodes.ACC_PRIVATE|Opcodes.ACC_PROTECTED);
                    }
                    for (MethodNode method : classNode.methods) {
                        method.access|= Opcodes.ACC_PUBLIC;
                        method.access&= ~(Opcodes.ACC_FINAL|Opcodes.ACC_PRIVATE|Opcodes.ACC_PROTECTED);
                    }
                    flg=true;
                }
                if (loader instanceof ModuleClassLoader){
                    tranMethod.invoke(agentPhase,classNode,loader,flag,false);
                    if (!className.startsWith("net/apphhzp/eraserservice")&&!className.startsWith("net/apphhzp/entityeraser")&&!className.startsWith("apphhzp/lib/")){
                        if(fuckCoremod){
                            if (!"java/lang/instrument/ClassFileTransformer".equals(className)&& isExtends(classNode.name,"java/lang/instrument/ClassFileTransformer",true)) {
                                for (MethodNode methodNode : classNode.methods) {
                                    if (methodNode.name.equals("transform")) {
                                        if ("(Ljava/lang/ClassLoader;Ljava/lang/String;Ljava/lang/Class;Ljava/security/ProtectionDomain;[B)[B".equals(methodNode.desc)
                                                || "(Ljava/lang/Module;Ljava/lang/ClassLoader;Ljava/lang/String;Ljava/lang/Class;Ljava/security/ProtectionDomain;[B)[B".equals(methodNode.desc)) {
                                            methodNode.instructions.clear();
                                            methodNode.instructions.add(new InsnNode(Opcodes.ACONST_NULL));
                                            methodNode.instructions.add(new InsnNode(Opcodes.ARETURN));
                                            methodNode.maxStack = 1;
                                            flag[0] = true;
                                        }
                                    }
                                }
                            }else if (!"cpw/mods/modlauncher/serviceapi/ILaunchPluginService".equals(className)&& isExtends(classNode.name,"cpw/mods/modlauncher/serviceapi/ILaunchPluginService",true)){
                                for (MethodNode methodNode:classNode.methods) {
                                    if (methodNode.name.equals("processClass")&&methodNode.desc.endsWith(")Z")||methodNode.name.equals("processClassWithFlags")&&methodNode.desc.endsWith(")I")) {
                                        methodNode.instructions.clear();
                                        methodNode.instructions.add(new InsnNode(Opcodes.ICONST_0));
                                        methodNode.instructions.add(new InsnNode(Opcodes.IRETURN));
                                        flag[0] = true;
                                    }
                                }
                            }
                        }

                        for (MethodNode method : classNode.methods) {
                            for (AbstractInsnNode insn:method.instructions){
                                if (insn instanceof MethodInsnNode call){
                                    if (call.getOpcode()==Opcodes.INVOKEVIRTUAL||call.getOpcode()==Opcodes.INVOKEINTERFACE){
                                        if ("addTransformer".equals(call.name)&&"(Ljava/lang/instrument/ClassFileTransformer;Z)V".equals(call.desc)
                                                && isExtends(call.owner,"java/lang/instrument/Instrumentation",true)){
                                            method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,OWNER,"addTransformer","(Ljava/lang/instrument/Instrumentation;Ljava/lang/instrument/ClassFileTransformer;Z)V"));
                                            flag[0]=true;
                                        }else if ("addTransformer".equals(call.name)&&"(Ljava/lang/instrument/ClassFileTransformer;)V".equals(call.desc)
                                                && isExtends(call.owner,"java/lang/instrument/Instrumentation",true)){
                                            method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,OWNER,"addTransformer","(Ljava/lang/instrument/Instrumentation;Ljava/lang/instrument/ClassFileTransformer;)V"));
                                            flag[0]=true;
                                        }else if ("redefineClasses".equals(call.name)&&"([Ljava/lang/instrument/ClassDefinition;)V".equals(call.desc)
                                                && isExtends(call.owner,"java/lang/instrument/Instrumentation",true)){
                                            method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,OWNER,"redefineClasses","(Ljava/lang/instrument/Instrumentation;[Ljava/lang/instrument/ClassDefinition;)V"));
                                            flag[0]=true;
                                        }else if ("retransformClasses".equals(call.name)&&"([Ljava/lang/Class;)V".equals(call.desc)
                                                &&isExtends(call.owner,"java/lang/instrument/Instrumentation",true)){
                                            method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,OWNER,"retransformClasses","(Ljava/lang/instrument/Instrumentation;[Ljava/lang/Class;)V"));
                                            flag[0]=true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (flag[0] || flg) {
                    return CoremodHelper.classNote2bytes(classNode, false);
                }
                if (shouldReload(className)){
                    ////NativeUtil.createMsgBox(className,"reload",0);
                    return classfileBuffer;
                }
            }
        }catch (Throwable t){
            LOGGER.throwing(t);
        }
        return null;
    }

    private static boolean shouldReload(String className) {
        return reloadBytecodes||className.startsWith("apphhzp/lib/")||className.startsWith("net/apphhzp/entityeraser/")||className.startsWith("net/apphhzp/eraserservice/")
                ||(restoreVanillaMethods&&className.equals("net/minecraft/client/gui/GuiGraphics"));
    }

    private static boolean isExtends(String a,String father)throws Throwable{
        return isExtends(a,father,false);
    }

    private static boolean isExtends(String a,String father,boolean need) throws Throwable{
        return (boolean) isExtendsMethod.invoke(a,father,need);
    }
}
