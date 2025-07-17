package net.apphhzp.eraserservice.agent;

import java.lang.instrument.*;
import java.security.ProtectionDomain;
import java.util.Arrays;

import static apphhzp.lib.ClassHelperSpecial.getBoolFromResource;

public class EarlyMethodsForAgent {
    private static final boolean fuckIt= getBoolFromResource("/enableBadCoremod.txt",EarlyMethodsForAgent.class)||getBoolFromResource("/disableAllJavaAgents.txt",EarlyMethodsForAgent.class);
    public static void redefineClasses(Instrumentation inst, ClassDefinition... definitions)
            throws  ClassNotFoundException, UnmodifiableClassException {
        //NativeUtil.createMsgBox(Arrays.toString(definitions),"redefineClasses",0);
        if (fuckIt){
            return;
        }
        definitions= Arrays.stream(definitions).filter((obj)->{
            String name= obj.getDefinitionClass().getName();
            return !name.startsWith("apphhzp.lib.")&&!name.startsWith("net.apphhzp.eraserservice.")
                    &&!name.startsWith("net.apphhzp.entityeraser.");
        }).toArray(ClassDefinition[]::new);
        inst.redefineClasses(definitions);
    }

    public static void addTransformer(Instrumentation inst, ClassFileTransformer transformer, boolean canRetransform){
        //NativeUtil.createMsgBox(transformer.getClass().getName(),"addTransformer!!!",0);
        if (fuckIt){
            return;
        }
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                if (className.startsWith("apphhzp/lib/")||className.startsWith("net/apphhzp/eraserservice/")
                        ||className.startsWith("net/apphhzp/entityeraser/")){
                    return null;
                }
                return transformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
            }

            @Override
            public byte[] transform(Module module, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                if (className.startsWith("apphhzp/lib/")||className.startsWith("net/apphhzp/eraserservice/")
                        ||className.startsWith("net/apphhzp/entityeraser/")){
                    return null;
                }
                return transformer.transform(module, loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
            }
        }, canRetransform);
    }

    public static void addTransformer(Instrumentation inst,ClassFileTransformer transformer){
        addTransformer(inst,transformer,false);
    }

    public static void retransformClasses(Instrumentation inst,Class<?>... classes) throws UnmodifiableClassException{
        if (fuckIt){
            return;
        }
        classes= Arrays.stream(classes).filter((obj)->{
            String name= obj.getName();
            return !name.startsWith("apphhzp.lib.")&&!name.startsWith("net.apphhzp.eraserservice.")
                    &&!name.startsWith("net.apphhzp.entityeraser.");
        }).toArray(Class<?>[]::new);
        inst.retransformClasses(classes);
    }
}
