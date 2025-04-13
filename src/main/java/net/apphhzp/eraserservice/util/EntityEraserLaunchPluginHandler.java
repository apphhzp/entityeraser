package net.apphhzp.eraserservice.util;

import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.ModuleLayerHandler;
import cpw.mods.modlauncher.TransformerAuditTrail;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import net.apphhzp.eraserservice.EntityEraserService;
import net.apphhzp.eraserservice.agent.EntityEraserClassFileTransformer;
import net.apphhzp.eraserservice.coremod.AllReturnPlugin;
import net.apphhzp.eraserservice.coremod.EntityEraserPlugin;
import net.minecraftforge.accesstransformer.service.AccessTransformerService;
import net.minecraftforge.eventbus.service.ModLauncherService;
import net.minecraftforge.fml.common.asm.CapabilityTokenSubclass;
import net.minecraftforge.fml.common.asm.ObjectHolderDefinalize;
import net.minecraftforge.fml.common.asm.RuntimeEnumExtender;
import net.minecraftforge.fml.loading.RuntimeDistCleaner;
import net.minecraftforge.fml.loading.log4j.SLF4JFixerLaunchPluginService;
import org.objectweb.asm.Type;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class EntityEraserLaunchPluginHandler extends LaunchPluginHandler {
    public EntityEraserLaunchPluginHandler(ModuleLayerHandler layerHandler) {
        super(layerHandler);
    }

    @Override
    public EnumMap<ILaunchPluginService.Phase, List<ILaunchPluginService>> computeLaunchPluginTransformerSet(Type className, boolean isEmpty, String reason, TransformerAuditTrail auditTrail) {
        EnumMap<ILaunchPluginService.Phase, List<ILaunchPluginService>> re=super.computeLaunchPluginTransformerSet(className, isEmpty, reason, auditTrail);

        for (Map.Entry<ILaunchPluginService.Phase, List<ILaunchPluginService>> entry:re.entrySet()){
            if (entry.getKey()== ILaunchPluginService.Phase.AFTER){
                if (!entry.getValue().contains(EntityEraserService.plugin1)){
                    entry.getValue().add(EntityEraserService.plugin1);
                }else if (!entry.getValue().contains(EntityEraserService.plugin2)){
                    entry.getValue().add(EntityEraserService.plugin2);
                }
            }else {
                if (!entry.getValue().contains(EntityEraserService.plugin1)){
                    entry.getValue().add(EntityEraserService.plugin1);
                }
            }
            if (EntityEraserClassFileTransformer.fuckCoremod){
                entry.getValue().removeIf((lps)-> lps.getClass()!= EntityEraserPlugin.class&&lps.getClass()!= AllReturnPlugin.class
                        &&lps.getClass()!= AccessTransformerService.class&&lps.getClass()!= CapabilityTokenSubclass.class
                        &&lps.getClass()!= ModLauncherService.class&&lps.getClass()!= ObjectHolderDefinalize.class
                        &&lps.getClass()!= RuntimeDistCleaner.class&&lps.getClass()!= RuntimeEnumExtender.class
                        &&lps.getClass()!= SLF4JFixerLaunchPluginService.class);
            }
        }

        return re;
    }
}
