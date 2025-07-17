package net.apphhzp.eraserservice.classloader;

import apphhzp.lib.ClassHelperSpecial;
import apphhzp.lib.CoremodHelper;
import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.ModuleLayerHandler;
import cpw.mods.modlauncher.TransformStore;
import cpw.mods.modlauncher.TransformingClassLoader;
import cpw.mods.modlauncher.api.ITransformerActivity;
import net.apphhzp.eraserservice.EntityEraserTransformerSpecial;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static apphhzp.lib.ClassHelperSpecial.lookup;
import static apphhzp.lib.ClassHelperSpecial.throwOriginalException;

public class EntityEraserClassLoader extends TransformingClassLoader {
    private static final Logger LOGGER= LogManager.getLogger(EntityEraserClassLoader.class);
    private static final Class<?> transformerClass;
    private static final MethodHandle transformerMethod;
    static {
        transformerClass= ((Class<?>[]) ClassHelperSpecial.getClassData(EntityEraserClassLoader.class))[0];
        try {
            transformerMethod= lookup.findStatic(transformerClass,"tran", MethodType.methodType(void.class, EntityEraserTransformerSpecial.Phase.class, ClassNode.class, ClassLoader.class, boolean[].class, boolean.class));
        } catch (Throwable e) {
            throwOriginalException(e);
            throw new RuntimeException("How did you get here?",e);
        }
    }
    public EntityEraserClassLoader(TransformStore transformStore, LaunchPluginHandler pluginHandler, ModuleLayerHandler moduleLayerHandler) {
        super(transformStore, pluginHandler, moduleLayerHandler);
    }

    @Override
    protected byte[] maybeTransformClassBytes(byte[] bytes,String name,String context) {
        if (context==null||context.equals(ITransformerActivity.CLASSLOADING_REASON)){
            boolean[] flag={false};
            ClassNode node= CoremodHelper.bytes2ClassNote(bytes,name);
            try {
                transformerMethod.invoke(EntityEraserTransformerSpecial.Phase.BEFORE_COREMOD,node,(Object) this,flag,false);
            } catch (Throwable e) {
                throwOriginalException(e);
                throw new RuntimeException("How did you get here?",e);
            }
            if (flag[0]){
                bytes=CoremodHelper.classNote2bytes(node,false);
            }
        }
        bytes= super.maybeTransformClassBytes(bytes,name,context);
        if (context==null||context.equals(ITransformerActivity.CLASSLOADING_REASON)){
            boolean[] flag={false};
            ClassNode node=CoremodHelper.bytes2ClassNote(bytes,name);
            try {
                transformerMethod.invoke(EntityEraserTransformerSpecial.Phase.COREMOD,node,(Object)this,flag,true);
            } catch (Throwable e) {
                throwOriginalException(e);
                throw new RuntimeException("How did you get here?",e);
            }
            if (flag[0]){
                bytes=CoremodHelper.classNote2bytes(node,false);
            }
        }
        return bytes;
    }
}
