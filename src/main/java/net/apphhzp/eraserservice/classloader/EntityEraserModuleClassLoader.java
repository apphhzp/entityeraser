package net.apphhzp.eraserservice.classloader;

import apphhzp.lib.CoremodHelper;
import apphhzp.lib.natives.NativeUtil;
import cpw.mods.cl.ModuleClassLoader;
import cpw.mods.modlauncher.api.ITransformerActivity;
import org.objectweb.asm.tree.ClassNode;

import java.lang.module.Configuration;
import java.util.List;

public class EntityEraserModuleClassLoader extends ModuleClassLoader {
    public EntityEraserModuleClassLoader(String name, Configuration configuration, List<ModuleLayer> parentLayers) {
        super(name, configuration, parentLayers);
    }

    @Override
    protected byte[] maybeTransformClassBytes(byte[] bytes,String name,String context) {
//        NativeUtil.createMsgBox(name,"classloader",NativeUtil.MB_OK);
        if (context==null||context.equals(ITransformerActivity.CLASSLOADING_REASON)){
            boolean[] flag={false};
            ClassNode node= CoremodHelper.bytes2ClassNote(bytes,name);
//            if (name.equals("org.spongepowered.asm.launch.MixinTransformationServiceAbstract")){
//                for (MethodNode methodNode : node.methods) {
//                    if (methodNode.name.equals("initialize")){
//                        methodNode.instructions.clear();
//                        methodNode.instructions.add(new InsnNode(Opcodes.RETURN));
//                        flag[0]=true;
//                    }
//                }
//            }else if ("org.spongepowered.asm.launch.MixinLaunchPluginLegacy".equals(name)){
//                for (MethodNode methodNode : node.methods) {
//                    if (methodNode.name.equals("initializeLaunch")){
//                        methodNode.instructions.clear();
//                        methodNode.instructions.add(new InsnNode(Opcodes.RETURN));
//                        flag[0]=true;
//                    }
//                }
//            }
            if (flag[0]){
                NativeUtil.createMsgBox(name,"yes",NativeUtil.MB_OK);
                bytes=CoremodHelper.classNote2bytes(node,true);
            }
        }
        return bytes;
    }
}
