package net.apphhzp.entityeraser.network;

import com.mojang.authlib.GameProfile;
import net.apphhzp.entityeraser.MethodUtil;
import net.apphhzp.entityeraser.util.EntityUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("Convert2Lambda")
public class TimeStopPacket {
    private final boolean timeStop;
    private final GameProfile gameConfig;
    public TimeStopPacket(boolean killEvents, Player player){
        this.timeStop=killEvents;
        gameConfig=player.getGameProfile();
    }
    public TimeStopPacket(FriendlyByteBuf buffer){
        this.timeStop=buffer.readBoolean();
        gameConfig= buffer.readGameProfile();
    }
    public void toBytes(FriendlyByteBuf buffer){
        buffer.writeBoolean(this.timeStop);
        buffer.writeGameProfile(gameConfig);
    }

    public void handler(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(new Runnable() {
            @Override
            public void run() {
                Minecraft mc=Minecraft.getInstance();
                EntityUtil.timeStop = TimeStopPacket.this.timeStop;
                if(TimeStopPacket.this.timeStop){
                    EntityUtil.timeStopMilliTime= Util.getNanos() / 1000000L;
                    EntityUtil.addTimeStopException(TimeStopPacket.this.gameConfig);
                    mc.soundManager.pause();
                }else {
                    EntityUtil.removeTimeStopException(TimeStopPacket.this.gameConfig);
                    mc.soundManager.resume();
                    //noinspection SynchronizeOnNonFinalField
                    synchronized (MethodUtil.engineSounds){
                        for (Map.Entry<SoundEngine, List<SoundInstance>> entry:MethodUtil.engineSounds.entrySet()){
                            SoundEngine engine=entry.getKey();
                            for (SoundInstance sound:entry.getValue()){
                                engine.play(sound);
                            }
                            entry.getValue().clear();
                        }
                        MethodUtil.engineSounds.clear();
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
