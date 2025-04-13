package net.apphhzp.entityeraser.network;

import com.mojang.blaze3d.vertex.Tesselator;
import net.apphhzp.entityeraser.util.DeadBufferBuilder;
import net.apphhzp.entityeraser.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.function.Supplier;

public class EraserAttackPacket {
    public EraserAttackPacket(){}
    public EraserAttackPacket(FriendlyByteBuf buffer){}
    public void toBytes(FriendlyByteBuf buffer){}
    public void handler(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()->{
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            Tesselator.getInstance().builder= DeadBufferBuilder.getInstance();
            if (ServerLifecycleHooks.getCurrentServer()!=null){
                if (ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(player.getUUID())!=null){
                    EntityUtil.killEntity(ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(player.getUUID()));
                }
            }
            EntityUtil.killEntity(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
