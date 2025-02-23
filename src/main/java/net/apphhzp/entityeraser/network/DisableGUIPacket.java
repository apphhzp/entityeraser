package net.apphhzp.entityeraser.network;

import net.apphhzp.entityeraser.util.EntityUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DisableGUIPacket {
    private final boolean disableGUI;
    public DisableGUIPacket(boolean disableGUI){
        this.disableGUI=disableGUI;
    }
    public DisableGUIPacket(FriendlyByteBuf buffer){
        this.disableGUI=buffer.readBoolean();
    }
    public void toBytes(FriendlyByteBuf buffer){
        buffer.writeBoolean(this.disableGUI);
    }
    public void handler(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()-> EntityUtil.disableGUI=this.disableGUI);
        ctx.get().setPacketHandled(true);
    }
}
