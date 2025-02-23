package net.apphhzp.entityeraser.network;

import net.apphhzp.entityeraser.util.EntityUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DisableSpawnPacket {
    private final boolean disableSpawn;
    public DisableSpawnPacket(boolean disableSpawn){
        this.disableSpawn=disableSpawn;
    }
    public DisableSpawnPacket(FriendlyByteBuf buffer){
        this.disableSpawn=buffer.readBoolean();
    }
    public void toBytes(FriendlyByteBuf buffer){
        buffer.writeBoolean(this.disableSpawn);
    }
    public void handler(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()-> EntityUtil.disableSpawn=this.disableSpawn);
        ctx.get().setPacketHandled(true);
    }
}
