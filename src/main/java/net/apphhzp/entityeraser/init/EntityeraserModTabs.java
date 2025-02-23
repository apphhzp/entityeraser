
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.apphhzp.entityeraser.init;

import apphhzp.lib.ClassHelper;
import net.apphhzp.entityeraser.EntityeraserMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityeraserModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EntityeraserMod.MODID);

	@SubscribeEvent
	public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
		if (tabData.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			tabData.accept(EntityeraserModItems.ENTITY_ERASER.get());
			tabData.accept(EntityeraserModItems.ENTITY_ERASER_CONTROLLER.get());
			tabData.accept(EntityeraserModItems.KILL_SELF.get());
			tabData.accept(EntityeraserModItems.ENTITY_PROTECTOR.get());
			tabData.accept(EntityeraserModItems.DESTROY_RENDERER.get());
			if (EntityeraserMod.debug){
				tabData.accept(EntityeraserModItems.TEST_ITEM.get());
			}
			tabData.accept(EntityeraserModItems.ALL_RETURN_ITEM.get());
			if (ClassHelper.isWindows){
				tabData.accept(EntityeraserModItems.GDI_KILL_SELF_ITEM.get());
			}
		}
	}
}
