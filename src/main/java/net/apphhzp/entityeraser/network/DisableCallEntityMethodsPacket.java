package net.apphhzp.entityeraser.network;

import net.apphhzp.entityeraser.util.EntityUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DisableCallEntityMethodsPacket {
    private final boolean disableCallEntityMethods;
    public DisableCallEntityMethodsPacket(boolean disableSpawn){
        this.disableCallEntityMethods=disableSpawn;
    }
    public DisableCallEntityMethodsPacket(FriendlyByteBuf buffer){
        this.disableCallEntityMethods=buffer.readBoolean();
    }
    public void toBytes(FriendlyByteBuf buffer){
        buffer.writeBoolean(this.disableCallEntityMethods);
    }
    public void handler(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()-> EntityUtil.disableCallEntityMethods=this.disableCallEntityMethods);
        ctx.get().setPacketHandled(true);
    }
}
