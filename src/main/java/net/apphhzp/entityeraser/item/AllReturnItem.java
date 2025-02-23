package net.apphhzp.entityeraser.item;

import net.apphhzp.entityeraser.AllReturn;
import net.apphhzp.entityeraser.network.AllReturnPacket;
import net.apphhzp.entityeraser.util.EntityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class AllReturnItem extends Item {
    public AllReturnItem() {
        super(new Properties().stacksTo(1).fireResistant().rarity(Rarity.COMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
        if (!entity.level.isClientSide){
            if (entity instanceof ServerPlayer serverPlayer){
                AllReturn.allReturn = !AllReturn.allReturn;
                EntityUtil.sendPacketToAllP(new AllReturnPacket(AllReturn.allReturn),serverPlayer.server);
                serverPlayer.sendSystemMessage(Component.translatable("AllReturnMode.change", AllReturn.allReturn), true);
            }
        }
        return super.use(world, entity, hand);
    }
}
