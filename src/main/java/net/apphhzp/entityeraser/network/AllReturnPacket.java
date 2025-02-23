package net.apphhzp.entityeraser.network;

import net.apphhzp.entityeraser.AllReturn;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AllReturnPacket {
    private final boolean allReturn;
    public AllReturnPacket(boolean killEvents){
        this.allReturn=killEvents;
    }
    public AllReturnPacket(FriendlyByteBuf buffer){
        this.allReturn=buffer.readBoolean();
    }
    public void toBytes(FriendlyByteBuf buffer){
        buffer.writeBoolean(this.allReturn);
    }

    public void handler(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()-> AllReturn.allReturn=this.allReturn);
        ctx.get().setPacketHandled(true);
    }
}
