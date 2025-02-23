package net.apphhzp.entityeraser.item;

import net.apphhzp.entityeraser.network.ProtectInventoryPacket;
import net.apphhzp.entityeraser.util.ColourfulFont;
import net.apphhzp.entityeraser.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.function.Consumer;

public class EntityProtectorItem extends Item {
    public EntityProtectorItem() {
        super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.COMMON));
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(itemstack, world, list, flag);
        list.add(Component.translatable("item.entityeraser.entity_protector.tooltip1"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
        InteractionResultHolder<ItemStack> ar = super.use(world, entity, hand);
        EntityUtil.defense(entity);
        if (entity.isShiftKeyDown() && entity.level.isClientSide) {
            if (entity instanceof LocalPlayer localPlayer){
                EntityUtil.disableGUI = !EntityUtil.disableGUI;
                localPlayer.sendSystemMessage(Component.translatable("DisableGUIMode.change", EntityUtil.disableGUI));
            }
        }
        return ar;
    }

    @Override
    public boolean onEntitySwing(ItemStack itemstack, LivingEntity entity) {
        if (entity instanceof Player player) {
            EntityUtil.defense(player);
            if (player.isShiftKeyDown() && !player.level.isClientSide) {
                if (player instanceof ServerPlayer serverPlayer){
                    EntityUtil.protectInventory = !EntityUtil.protectInventory;
                    EntityUtil.sendPacketToP(serverPlayer, new ProtectInventoryPacket(EntityUtil.protectInventory));
                    serverPlayer.sendSystemMessage(Component.translatable("ProtectInventoryMode.change", EntityUtil.protectInventory), true);
                }
            }
        }
        return false;
    }

    @Override
    public void inventoryTick(ItemStack p_41404_, Level p_41405_, Entity p_41406_, int p_41407_, boolean p_41408_) {
        if (p_41406_ instanceof Player player) {
            EntityUtil.defense(player);
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public Font getFont(ItemStack stack, FontContext context) {
                FontManager manager= Minecraft.getInstance().fontManager;
                return new ColourfulFont((p_284586_) -> manager.fontSets.getOrDefault(manager.getActualId(p_284586_), manager.missingFontSet), false);
            }
        });
    }
}
