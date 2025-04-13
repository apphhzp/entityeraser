package net.apphhzp.eraserservice.coremod;

import cpw.mods.modlauncher.api.ITransformerActivity;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import net.apphhzp.eraserservice.EntityEraserTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.EnumSet;

public class AllReturnPlugin implements ILaunchPluginService {
    private static final Logger LOGGER= LogManager.getLogger();
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
        return reason.equals(ITransformerActivity.CLASSLOADING_REASON)?EnumSet.of(Phase.AFTER):EnumSet.noneOf(Phase.class);
    }

    @Override
    public boolean processClass(Phase phase, ClassNode classNode, Type classType, String reason) {
        if (phase==Phase.AFTER&&reason.equals(ITransformerActivity.CLASSLOADING_REASON)){
            boolean[] flag={false};
            EntityEraserTransformer.tranAddReturn(classNode,flag,"net/apphhzp/entityeraser/AllReturn");
            //new Throwable().printStackTrace();
            return flag[0];
        }
        return false;
    }
}