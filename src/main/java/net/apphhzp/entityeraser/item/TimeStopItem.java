package net.apphhzp.entityeraser.item;

import net.apphhzp.entityeraser.network.TimeStopPacket;
import net.apphhzp.entityeraser.util.EntityUtil;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class TimeStopItem extends Item {
    public TimeStopItem() {
        super(new Properties().stacksTo(1).fireResistant().rarity(Rarity.COMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
        if (!entity.level.isClientSide){
            if (entity instanceof ServerPlayer serverPlayer){
                EntityUtil.timeStop = !EntityUtil.timeStop;
                EntityUtil.timeStopMilliTime= Util.getNanos() / 1000000L;
                EntityUtil.sendPacketToAllP(new TimeStopPacket(EntityUtil.timeStop,serverPlayer),serverPlayer.server);
                serverPlayer.sendSystemMessage(Component.translatable("TimeStop.change", EntityUtil.timeStop), true);
            }
        }
        return super.use(world, entity, hand);
    }
}
