package net.apphhzp.eraserservice.coremod;

import apphhzp.lib.ClassHelperSpecial;
import apphhzp.lib.CoremodHelper;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerActivity;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.EnumSet;

import static apphhzp.lib.ClassHelperSpecial.lookup;
import static apphhzp.lib.ClassHelperSpecial.throwOriginalException;

public class AllReturnPlugin implements ILaunchPluginService {
    private static final Logger LOGGER= LogManager.getLogger();
    private static final Class<?> transformerClass;
    private static final MethodHandle transformerMethod;
    static {
        transformerClass= ((Class<?>[])ClassHelperSpecial.getClassData(AllReturnPlugin.class))[0];
        try {
            transformerMethod= lookup.findStatic(transformerClass,"tranAddReturn", MethodType.methodType(void.class, ClassNode.class, boolean[].class, String.class));
        } catch (Throwable e) {
            throwOriginalException(e);
            throw new RuntimeException("How did you get here?",e);
        }
    }

    @Override
    public String name() {
        return "￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿￿";
    }
    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        return EnumSet.of(Phase.AFTER);
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty, String reason) {
        return EnumSet.of(Phase.AFTER);
    }

    @Override
    public boolean processClass(Phase phase, ClassNode classNode, Type classType, String reason) {
        if (phase==Phase.AFTER&&reason.equals(ITransformerActivity.CLASSLOADING_REASON)){
            boolean[] flag={false};
            try{
                transformerMethod.invoke(classNode,flag,"net/apphhzp/entityeraser/AllReturn");
            }catch (Throwable t){
                throwOriginalException(t);
                throw new RuntimeException("How did you get here?",t);
            }
            if (classNode.name.startsWith("net/apphhzp/entityeraser/")||classNode.name.startsWith("net/apphhzp/eraserservice/")||classNode.name.startsWith("apphhzp/lib/")){
                byte[] dat=CoremodHelper.getBytecodesFromFile(classNode.name,Thread.currentThread().getContextClassLoader(),true);
                if (dat!=null){
                    classNode.interfaces = new ArrayList<>();
                    classNode.innerClasses = new ArrayList<>();
                    classNode.fields = new ArrayList<>();
                    classNode.methods = new ArrayList<>();
                    CoremodHelper.bytes2ClassNote(dat,classNode.name).accept(classNode);
                    flag[0]=true;
                }else {
                    LOGGER.error("Failed to fix class file: {}",classNode.name);
                }
            }
            return flag[0];
        }
        return false;
    }

}