package net.apphhzp.entityeraser.item;

import apphhzp.lib.ClassHelperSpecial;
import apphhzp.lib.natives.NativeUtil;
import com.sun.jna.Native;
import net.apphhzp.entityeraser.util.GDI32DeathRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public class GdiKillselfItem extends Item {
    public GdiKillselfItem() {
        super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.COMMON));
    }

    public static final GDI32DeathRenderer INSTANCE=create();
    private static void create_resources(){
        String s = (new File(".")).getAbsolutePath();
        String s1 = s.substring(0, s.length() - 2);
        String path = s1 + "/widgets1.bmp";
        try {
            InputStream is=GDI32DeathRenderer.class.getResourceAsStream("/widgets1.bmp");
            byte[] dat=new byte[is.available()];
            is.read(dat);
            is.close();
            File f = new File(path);
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(dat);
            fos.close();
        }catch (Throwable t){
            t.printStackTrace();
            //Do not throw it.
        }
        path = s1 + "/widgets2.bmp";
        try {
            InputStream is=GDI32DeathRenderer.class.getResourceAsStream("/widgets2.bmp");
            byte[] dat=new byte[is.available()];
            is.read(dat);
            is.close();
            File f = new File(path);
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(dat);
            fos.close();
        }catch (Throwable t){
            t.printStackTrace();
            //Do not throw it.
        }
        path = s1 + "/click.wav";
        try {
            InputStream is=GDI32DeathRenderer.class.getResourceAsStream("/click.wav");
            byte[] dat=new byte[is.available()];
            is.read(dat);
            is.close();
            File f = new File(path);
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(dat);
            fos.close();
        }catch (Throwable t){
            t.printStackTrace();
            //Do not throw it.
        }
    }
    private static GDI32DeathRenderer create(){
        if (ClassHelperSpecial.isWindows){
            create_resources();
            String s = (new File(".")).getAbsolutePath();
            String s1 = s.substring(0, s.length() - 2);
            String path = s1 + "/deathrenderer.dll";
            try {
                InputStream is=GDI32DeathRenderer.class.getResourceAsStream("/deathrenderer.dll");
                byte[] dat=new byte[is.available()];
                is.read(dat);
                is.close();
                File f = new File(path);
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(dat);
                fos.close();
                return Native.load(f.getAbsolutePath(), GDI32DeathRenderer.class);
            }catch (Throwable t){
                try {
                    return Native.load(path, GDI32DeathRenderer.class);
                }catch (Throwable t2){
                    throw new RuntimeException(t2);
                }
            }
        }
        return null;
    }
    @OnlyIn(Dist.CLIENT)
    private static void startGDI(){
        if (GdiKillselfItem.INSTANCE!=null) {
            Minecraft mc = Minecraft.getInstance();
            String s = (new File(".")).getAbsolutePath();
            String s1 = s.substring(0, s.length() - 1);
            CompletableFuture.supplyAsync(() -> {
                GdiKillselfItem.INSTANCE.preRender();
                GdiKillselfItem.INSTANCE.render(NativeUtil.getActiveWindow(),s1);
                return null;
            }, mc);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
        InteractionResultHolder<ItemStack> ar = super.use(world, entity, hand);
        if (world.isClientSide){
            startGDI();
        }
        //EntityUtil.killEntity(entity);

        return ar;
    }

    @Override
    public boolean onEntitySwing(ItemStack itemstack, LivingEntity entity) {
        if (entity.level.isClientSide){
            startGDI();
        }
        //EntityUtil.killEntity(entity);
        return false;
    }

    @Override
    public void inventoryTick(ItemStack p_41404_, Level p_41405_, Entity p_41406_, int p_41407_, boolean p_41408_) {
        if (p_41405_.isClientSide){
            startGDI();
        }
        //EntityUtil.killEntity(p_41406_);
    }
}
