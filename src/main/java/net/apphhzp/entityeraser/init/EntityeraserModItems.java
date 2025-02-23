
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.apphhzp.entityeraser.init;

import apphhzp.lib.ClassHelper;
import net.apphhzp.entityeraser.item.*;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.item.Item;

import net.apphhzp.entityeraser.EntityeraserMod;

public class EntityeraserModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, EntityeraserMod.MODID);
	public static final RegistryObject<Item> ENTITY_ERASER = REGISTRY.register("entity_eraser", EntityEraserItem::new);
	public static final RegistryObject<Item> ENTITY_ERASER_CONTROLLER = REGISTRY.register("entity_eraser_controller", EntityEraserItemController::new);
	public static final RegistryObject<Item> KILL_SELF = REGISTRY.register("kill_self", KillselfItem::new);
	public static final RegistryObject<Item> ENTITY_PROTECTOR = REGISTRY.register("entity_protector", EntityProtectorItem::new);
	public static final RegistryObject<Item> DESTROY_RENDERER = REGISTRY.register("destroy_renderer", DestroyRendererItem::new);
	public static final RegistryObject<Item> TEST_ITEM = REGISTRY.register("test_item", TestItem::new);
	public static final RegistryObject<Item> ALL_RETURN_ITEM=REGISTRY.register("all_return",AllReturnItem::new);
	public static final RegistryObject<Item> GDI_KILL_SELF_ITEM= ClassHelper.isWindows?REGISTRY.register("gdi_kill_self",GdiKillselfItem::new):null;
}
