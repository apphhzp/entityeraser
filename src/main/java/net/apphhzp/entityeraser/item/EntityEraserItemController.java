
package net.apphhzp.entityeraser.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.apphhzp.entityeraser.network.DisableCallEntityMethodsPacket;
import net.apphhzp.entityeraser.util.EntityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
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

import java.util.List;

import static net.minecraft.world.entity.Entity.DATA_SHARED_FLAGS_ID;

public class EntityEraserItemController extends Item {
	public EntityEraserItemController() {
		super(new Properties().stacksTo(1).fireResistant());//.rarity(Rarity.COMMON)
	}

	@Override
	public int getEnchantmentValue() {
		return 1;
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
			builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Item modifier", Double.NaN, AttributeModifier.Operation.ADDITION));
			builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Item modifier", 1, AttributeModifier.Operation.ADDITION));
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
		list.add(Component.translatable("item.entityeraser.entity_eraser_controller.tooltip1"));
		list.add(Component.translatable("item.entityeraser.entity_eraser_controller.tooltip2"));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
		InteractionResultHolder<ItemStack> ar = super.use(world, entity, hand);
		if (entity instanceof ServerPlayer player) {
			if (((Byte)player.entityData.get(DATA_SHARED_FLAGS_ID) & 1 << 1) != 0 && !player.level.isClientSide) {
				EntityUtil.disableCallEntityMethods = !EntityUtil.disableCallEntityMethods;
				EntityUtil.sendPacketToP(player, new DisableCallEntityMethodsPacket(EntityUtil.disableCallEntityMethods));
				player.sendSystemMessage(Component.translatable("DisableEntityCallMethodsMode.change", EntityUtil.disableCallEntityMethods), true);
			}
		}
		return ar;
	}

	@Override
	public boolean onEntitySwing(ItemStack itemstack, LivingEntity entity) {
		boolean retval = super.onEntitySwing(itemstack, entity);
		if (entity instanceof ServerPlayer player) {
			if (((Byte)player.entityData.get(DATA_SHARED_FLAGS_ID) & 1 << 1) != 0 && !player.level.isClientSide) {
//				synchronized (EntityUtil.killEvents) {
//                    EntityUtil.killEvents.set(!EntityUtil.killEvents.get());
//                    EntityUtil.sendPacketToP(player, new KillEventsPacket(EntityUtil.killEvents.get()));
//                    player.sendSystemMessage(Component.translatable("KillEventsMode.change", EntityUtil.killEvents), true);
//				}
			}
		}
		return retval;
	}
}
