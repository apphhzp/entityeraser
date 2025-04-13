package net.apphhzp.eraserservice.classloader;

import apphhzp.lib.CoremodHelper;
import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.ModuleLayerHandler;
import cpw.mods.modlauncher.TransformStore;
import cpw.mods.modlauncher.TransformingClassLoader;
import cpw.mods.modlauncher.api.ITransformerActivity;
import net.apphhzp.eraserservice.EntityEraserTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;

public class EntityEraserClassLoader extends TransformingClassLoader {
    private static final Logger LOGGER= LogManager.getLogger(EntityEraserClassLoader.class);
    public EntityEraserClassLoader(TransformStore transformStore, LaunchPluginHandler pluginHandler, ModuleLayerHandler moduleLayerHandler) {
        super(transformStore, pluginHandler, moduleLayerHandler);
    }

    @Override
    protected byte[] maybeTransformClassBytes(byte[] bytes,String name,String context) {
        if (context==null||context.equals(ITransformerActivity.CLASSLOADING_REASON)){
            boolean[] flag={false};
            ClassNode node= CoremodHelper.bytes2ClassNote(bytes,name);
            EntityEraserTransformer.tran(EntityEraserTransformer.Phase.BEFORE_COREMOD,node,flag);
            if (flag[0]){
                bytes=CoremodHelper.classNote2bytes(node,false);
            }
        }
        bytes= super.maybeTransformClassBytes(bytes,name,context);
        if (context==null||context.equals(ITransformerActivity.CLASSLOADING_REASON)){
            boolean[] flag={false};
            ClassNode node=CoremodHelper.bytes2ClassNote(bytes,name);
            EntityEraserTransformer.tran(EntityEraserTransformer.Phase.COREMOD,node,flag,true);
            if (flag[0]){
                bytes=CoremodHelper.classNote2bytes(node,false);
            }
        }
        return bytes;
    }
}
