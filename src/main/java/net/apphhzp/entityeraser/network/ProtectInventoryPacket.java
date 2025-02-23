package net.apphhzp.entityeraser.network;

import net.apphhzp.entityeraser.util.EntityUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ProtectInventoryPacket {
    private final boolean protectInventory;
    public ProtectInventoryPacket(boolean protectEraser){
        this.protectInventory=protectEraser;
    }
    public ProtectInventoryPacket(FriendlyByteBuf buffer){
        this.protectInventory=buffer.readBoolean();
    }
    public void toBytes(FriendlyByteBuf buffer){
        buffer.writeBoolean(this.protectInventory);
    }
    public void handler(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()-> EntityUtil.protectInventory =this.protectInventory);
        ctx.get().setPacketHandled(true);
    }
}
