package net.apphhzp.eraserservice.coremod;

import apphhzp.lib.ClassHelperSpecial;
import cpw.mods.modlauncher.api.ITransformerActivity;
import cpw.mods.modlauncher.api.NamedPath;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import net.apphhzp.eraserservice.EntityEraserTransformerSpecial;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.EnumSet;

import static apphhzp.lib.ClassHelperSpecial.lookup;
import static apphhzp.lib.ClassHelperSpecial.throwOriginalException;

public class EntityEraserPlugin implements ILaunchPluginService {
    private static final Logger LOGGER= LogManager.getLogger();
    private static final Class<?> bytecodesGetter;
    private static final Class<?> transformerClass;
    private static final VarHandle byteCodeLoaderVar;
    private static final MethodHandle transformMethod;
    static {
        Class<?>[] data= ClassHelperSpecial.getClassData(EntityEraserPlugin.class);
        bytecodesGetter=data[0];
        transformerClass=data[1];
        try {
            byteCodeLoaderVar= lookup.findStaticVarHandle(bytecodesGetter,"byteCodeLoader", ITransformerLoader.class);
            transformMethod=lookup.findStatic(transformerClass,"tran", MethodType.methodType(void.class, EntityEraserTransformerSpecial.Phase.class, ClassNode.class, boolean[].class));
        } catch (Throwable e) {
            throwOriginalException(e);
            throw new RuntimeException("How did you get here?",e);
        }
    }

    @Override
    public String name() {
        return "EntityEraser";
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        return EnumSet.of(Phase.AFTER,Phase.BEFORE);
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty, String reason) {
        return EnumSet.of(Phase.AFTER,Phase.BEFORE);
    }

    @Override
    public boolean processClass(Phase phase, ClassNode classNode, Type classType, String reason) {
        if (reason.equals(ITransformerActivity.CLASSLOADING_REASON)){
            //NativeUtil.createMsgBox(classNode.name,"tran",0);
            boolean[] flag ={false,false};
            try {
                transformMethod.invoke(EntityEraserTransformerSpecial.Phase.COREMOD,classNode,flag);
            } catch (Throwable e) {
                throwOriginalException(e);
                throw new RuntimeException("How did you get here?",e);
            }
            return flag[0];
        }
        return false;
    }

    @Override
    public void initializeLaunch(ITransformerLoader transformerLoader, NamedPath[] specialPaths) {
        byteCodeLoaderVar.set(transformerLoader);
    }
}
