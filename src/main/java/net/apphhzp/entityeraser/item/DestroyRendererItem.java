package net.apphhzp.entityeraser.item;

import net.apphhzp.entityeraser.util.EntityUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.lwjgl.glfw.GLFW;

public class DestroyRendererItem extends Item {
    public DestroyRendererItem() {
        super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.COMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
        InteractionResultHolder<ItemStack> ar = super.use(world, entity, hand);
        if (entity.level.isClientSide){
            EntityUtil.shouldDestroyRenderer=true;
            EntityUtil.destroyRenderer();
            GLFW.glfwMakeContextCurrent(0);
            //NativeUtil.postMsg(NativeUtil.getActiveWindow(), User32.WM_SETREDRAW,0,0);
        }
        return ar;
    }

    @Override
    public boolean onEntitySwing(ItemStack itemstack, LivingEntity entity) {
        if (entity.level.isClientSide){
            //NativeUtil.postMsg(NativeUtil.getActiveWindow(), User32.WM_SETREDRAW,1,0);
            EntityUtil.shouldDestroyRenderer=true;
            EntityUtil.destroyRenderer();
            GLFW.glfwMakeContextCurrent(0);
        }
        return false;
    }
}
