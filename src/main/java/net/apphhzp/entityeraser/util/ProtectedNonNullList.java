package net.apphhzp.entityeraser.util;

import net.apphhzp.entityeraser.item.EntityEraserItem;
import net.apphhzp.entityeraser.item.EntityProtectorItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import static net.minecraft.world.item.ItemStack.EMPTY;

public class ProtectedNonNullList extends NonNullList<ItemStack> {
    public Inventory inventory;
    public ProtectedNonNullList(NonNullList<ItemStack> old,Inventory i) {
        super(old.list instanceof DisableRemoveList?old.list:new DisableRemoveList(old.list),old.defaultValue);
        inventory=i;
    }

    @Override
    public ItemStack set(int p_122795_, ItemStack p_122796_) {
        if (EntityUtil.protectInventory){
            Item item=this.list.get(p_122795_).getItem();
            if(item instanceof EntityEraserItem||item instanceof EntityProtectorItem){
                return p_122796_;
            }
        }
        ItemStack re=super.set(p_122795_, p_122796_);
        EntityUtil.checkNonNullList(this,inventory);
        return re;
    }

    @Override
    public ItemStack remove(int p_122793_) {
        if (EntityUtil.protectInventory){
            if (this.list.get(p_122793_).getItem() instanceof EntityEraserItem||this.list.get(p_122793_).getItem() instanceof EntityProtectorItem){
                return EMPTY;
            }
        }
        ItemStack re=super.remove(p_122793_);
        EntityUtil.checkNonNullList(this,inventory);
        return re;
    }

    @Override
    public int size() {
        return this.list.size();
    }

    @Override
    public void clear() {
        if (!EntityUtil.protectInventory) {
            super.clear();
        }
        EntityUtil.checkNonNullList(this,inventory);
    }

    @Override
    public boolean remove(Object o) {
        if (EntityUtil.protectInventory) {
            if (o instanceof ItemStack stack) {
                if (!(stack.getItem() instanceof EntityProtectorItem)&&!(stack.getItem() instanceof EntityEraserItem)) {
                    return super.remove(o);
                }
            }
            return false;
        }else {
            return super.remove(o);
        }
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        if (EntityUtil.protectInventory) {
            c = c.stream().filter((object) -> {
                if (object instanceof ItemStack stack){
                    return !(stack.getItem() instanceof EntityEraserItem) && !(stack.getItem() instanceof EntityProtectorItem);
                }
                return true;
            }).toList();
        }
        return super.removeAll(c);
    }

    public final static class DisableRemoveList extends ArrayList<ItemStack>{
        public DisableRemoveList() {
            super();
        }

        public DisableRemoveList(@NotNull Collection<? extends ItemStack> c) {
            super(c);
        }

        @Override
        public ItemStack remove(int index) {
            ItemStack stack=this.get(index);
            if (EntityUtil.protectInventory){
                if (stack.getItem() instanceof EntityProtectorItem||stack.getItem() instanceof EntityEraserItem){
                    return EMPTY;
                }
            }
            return super.remove(index);
        }

        @Override
        public boolean remove(Object o) {
            if (EntityUtil.protectInventory) {
                if (o instanceof ItemStack stack) {
                    if (!(stack.getItem() instanceof EntityProtectorItem)&&!(stack.getItem() instanceof EntityEraserItem)) {
                        return super.remove(o);
                    }
                }
                return false;
            }else {
                return super.remove(o);
            }
        }

        @Override
        public void clear() {
            if (!EntityUtil.protectInventory) {
                super.clear();
            }
        }

        @Override
        protected void removeRange(int fromIndex, int toIndex) {
            if (!EntityUtil.protectInventory) {
                super.removeRange(fromIndex, toIndex);
            }
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            if (EntityUtil.protectInventory) {
                c = c.stream().filter((object) -> {
                    if (object instanceof ItemStack stack){
                        return !(stack.getItem() instanceof EntityEraserItem) && !(stack.getItem() instanceof EntityProtectorItem);
                    }
                    return true;
                }).toList();
            }
            return super.removeAll(c);
        }

        @Override
        public boolean removeIf(Predicate<? super ItemStack> filter) {
            return super.removeIf(stack -> {
                if (EntityUtil.protectInventory){
                    return !(stack.getItem() instanceof EntityEraserItem) && !(stack.getItem() instanceof EntityProtectorItem)&&filter.test(stack);
                }else {
                    return filter.test(stack);
                }
            });
        }
    }
}
