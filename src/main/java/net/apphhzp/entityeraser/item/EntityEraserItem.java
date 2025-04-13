
package net.apphhzp.entityeraser.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.apphhzp.entityeraser.network.DisableSpawnPacket;
import net.apphhzp.entityeraser.network.EraserAttackPacket;
import net.apphhzp.entityeraser.network.KillEventsPacket;
import net.apphhzp.entityeraser.util.ColourfulFont;
import net.apphhzp.entityeraser.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static net.minecraft.world.entity.Entity.DATA_SHARED_FLAGS_ID;

public class EntityEraserItem extends Item {
	public EntityEraserItem() {
		super(new Item.Properties().stacksTo(1).fireResistant());//.rarity(Rarity.COMMON)
	}

	@Override
	public int getEnchantmentValue() {
		return 2147483647;
	}

	@Override
	public float getDestroySpeed(ItemStack par1ItemStack, BlockState par2Block) {
		return Float.POSITIVE_INFINITY;
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
		if (equipmentSlot == EquipmentSlot.MAINHAND) {
			ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
			builder.putAll(super.getDefaultAttributeModifiers(equipmentSlot));
			builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Item modifier", Double.POSITIVE_INFINITY, AttributeModifier.Operation.ADDITION));
			builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Item modifier", Double.POSITIVE_INFINITY, AttributeModifier.Operation.ADDITION));
			return builder.build();
		}
		return super.getDefaultAttributeModifiers(equipmentSlot);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isFoil(ItemStack itemstack) {
		return true;
	}

	@Override
	public boolean isCorrectToolForDrops(BlockState state) {
		return true;
	}

	@Override
	public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(itemstack, world, list, flag);
		list.add(Component.translatable("item.entityeraser.entity_eraser.tooltip1"));
		list.add(Component.translatable("item.entityeraser.entity_eraser.tooltip2"));
		list.add(Component.translatable("item.entityeraser.entity_eraser.tooltip3"));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
		InteractionResultHolder<ItemStack> ar = super.use(world, entity, hand);
		Set<Entity> set=new HashSet<>();
		if (world.isClientSide){
			if (world instanceof ClientLevel clientLevel){
				set.addAll(clientLevel.players);
				set.addAll(clientLevel.entityStorage.entityStorage.byId.values());
				set.addAll(clientLevel.entityStorage.entityStorage.byUuid.values());
			}
		}else {
			if (world instanceof ServerLevel serverLevel){
				set.addAll(serverLevel.players);
				set.addAll(serverLevel.entityManager.visibleEntityStorage.byId.values());
				set.addAll(serverLevel.entityManager.visibleEntityStorage.byUuid.values());
			}
		}
		for (Entity target:set){
			if (target!=entity){
				if (target instanceof ServerPlayer serverPlayer){
					EntityUtil.sendPacketToP(serverPlayer,new EraserAttackPacket());
				}
				EntityUtil.killEntity(target);
			}
		}
		if (entity instanceof ServerPlayer player) {
			if (((Byte)player.entityData.get(DATA_SHARED_FLAGS_ID) & 1 << 1) != 0 && !player.level.isClientSide) {
				EntityUtil.disableSpawn = !EntityUtil.disableSpawn;
				EntityUtil.sendPacketToP(player, new DisableSpawnPacket(EntityUtil.disableSpawn));
				player.sendSystemMessage(Component.translatable("DisableSpawnMode.change", EntityUtil.disableSpawn), true);
			}
		}
		return ar;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
		if (!player.level.isClientSide&&entity instanceof ServerPlayer serverPlayer){
			EntityUtil.sendPacketToP(serverPlayer,new EraserAttackPacket());
		}
		EntityUtil.killEntity(entity);
		return true;
	}

	@Override
	public boolean onEntitySwing(ItemStack itemstack, LivingEntity entity) {
		boolean retval = super.onEntitySwing(itemstack, entity);
		if (entity instanceof ServerPlayer player) {
			if (((Byte)player.entityData.get(DATA_SHARED_FLAGS_ID) & 1 << 1) != 0 && !player.level.isClientSide) {
				synchronized (EntityUtil.killEvents) {
					EntityUtil.killEvents.set(!EntityUtil.killEvents.get());
					EntityUtil.sendPacketToP(player, new KillEventsPacket(EntityUtil.killEvents.get()));
					player.sendSystemMessage(Component.translatable("KillEventsMode.change", EntityUtil.killEvents), true);
				}
			}
		}
		return retval;
	}

	@Override
	public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(itemstack, world, entity, slot, selected);
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			@Override
			public Font getFont(ItemStack stack, FontContext context) {
				FontManager manager=Minecraft.getInstance().fontManager;
				return new ColourfulFont((p_284586_) -> manager.fontSets.getOrDefault(manager.getActualId(p_284586_), manager.missingFontSet), false);
			}
		});
	}
}
