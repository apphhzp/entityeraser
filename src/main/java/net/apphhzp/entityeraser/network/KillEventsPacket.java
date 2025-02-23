package net.apphhzp.entityeraser.network;

import net.apphhzp.entityeraser.util.EntityUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class KillEventsPacket{
    private final boolean killEvents;
    public KillEventsPacket(boolean killEvents){
        this.killEvents=killEvents;
    }
    public KillEventsPacket(FriendlyByteBuf buffer){
        this.killEvents=buffer.readBoolean();
    }
    public void toBytes(FriendlyByteBuf buffer){
        buffer.writeBoolean(this.killEvents);
    }
    public void handler(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()-> EntityUtil.killEvents.set(this.killEvents));
        ctx.get().setPacketHandled(true);
    }
}
