package net.apphhzp.eraserservice.coremod;

import cpw.mods.modlauncher.api.ITransformerActivity;
import cpw.mods.modlauncher.api.NamedPath;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import net.apphhzp.eraserservice.EntityEraserTransformer;
import net.apphhzp.eraserservice.classloader.BytecodesGetter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.EnumSet;

public class EntityEraserPlugin implements ILaunchPluginService {
    private static final Logger LOGGER= LogManager.getLogger();


    @Override
    public String name() {
        return "EntityEraser";
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        return EnumSet.of(Phase.AFTER);
    }

    @Override
    public boolean processClass(Phase phase, ClassNode classNode, Type classType, String reason) {
        if (phase==Phase.AFTER&&reason.equals(ITransformerActivity.CLASSLOADING_REASON)){
            boolean[] flag ={false,false};
            EntityEraserTransformer.tran(classNode,flag);
            return flag[0];
        }
        return false;
    }

    @Override
    public void initializeLaunch(ITransformerLoader transformerLoader, NamedPath[] specialPaths) {
        BytecodesGetter.byteCodeLoader=transformerLoader;
    }
}
