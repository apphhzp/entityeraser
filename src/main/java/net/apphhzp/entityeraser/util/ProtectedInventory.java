package net.apphhzp.entityeraser.util;

import net.apphhzp.entityeraser.init.EntityeraserModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;
import java.util.function.Predicate;

import static net.minecraft.world.item.ItemStack.EMPTY;

public class ProtectedInventory extends Inventory {
    public ProtectedInventory(Player p_35983_) {
        super(p_35983_);
    }

    @Override
    public ItemStack removeItem(int p_35993_, int p_35994_) {
        NonNullList<ItemStack> list = null;

        NonNullList<ItemStack> nonnulllist;
        for(Iterator<NonNullList<ItemStack>> var4 = this.compartments.iterator(); var4.hasNext(); p_35993_ -= nonnulllist.size()) {
            nonnulllist = var4.next();
            if (p_35993_ < nonnulllist.list.size()) {
                list = nonnulllist;
                break;
            }
        }
        if (list==null){
            return EMPTY;
        }else{
            ItemStack itemstack=list.list.get(p_35993_);
            if (EntityUtil.protectInventory&&(itemstack.getItem()== EntityeraserModItems.ENTITY_PROTECTOR.get()||itemstack.getItem()==EntityeraserModItems.ENTITY_ERASER.get())) {
                return EMPTY;
            }else if (!itemstack.isEmpty()){
                ItemStack re= ContainerHelper.removeItem(list, p_35993_, p_35994_);
                EntityUtil.checkInventory(this);
                return re;
            }
        }
        return EMPTY;
    }

    @Override
    public void removeItem(ItemStack itemstack) {
        if (EntityUtil.protectInventory){
            if (itemstack.getItem()!= EntityeraserModItems.ENTITY_PROTECTOR.get()&&itemstack.getItem()!=EntityeraserModItems.ENTITY_ERASER.get()) {
                super.removeItem(itemstack);
            }
        }else {
            super.removeItem(itemstack);
        }
        EntityUtil.checkInventory(this);
    }

    @Override
    public ItemStack removeItemNoUpdate(int p_36029_) {
        EntityUtil.checkInventory(this);
        NonNullList<ItemStack> nonnulllist = null;
        NonNullList<ItemStack> nonnulllist1;
        for(Iterator<NonNullList<ItemStack>> var3 = this.compartments.iterator(); var3.hasNext(); p_36029_ -= nonnulllist1.size()) {
            nonnulllist1 = var3.next();
            if (p_36029_ < nonnulllist1.list.size()) {
                nonnulllist = nonnulllist1;
                break;
            }
        }
        if (nonnulllist != null && !nonnulllist.list.get(p_36029_).isEmpty()) {
            ItemStack itemstack = nonnulllist.list.get(p_36029_);
            if (EntityUtil.protectInventory&&(itemstack.getItem()== EntityeraserModItems.ENTITY_PROTECTOR.get()||itemstack.getItem()==EntityeraserModItems.ENTITY_ERASER.get())){
                return EMPTY;
            }
            nonnulllist.list.set(p_36029_, EMPTY);
            EntityUtil.checkInventory(this);
            return itemstack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void replaceWith(Inventory p_36007_) {
        if (!EntityUtil.protectInventory){
            super.replaceWith(p_36007_);
        }
        EntityUtil.checkInventory(this);
    }

    @Override
    public ItemStack removeFromSelected(boolean p_182404_) {
        ItemStack itemstack = this.getSelected();
        if (EntityUtil.protectInventory &&(itemstack.getItem()== EntityeraserModItems.ENTITY_PROTECTOR.get()
                ||itemstack.getItem()==EntityeraserModItems.ENTITY_ERASER.get())){
            return EMPTY;
        }
        ItemStack re=itemstack.isEmpty() ? EMPTY : this.removeItem(this.selected, p_182404_ ? itemstack.getCount() : 1);
        EntityUtil.checkInventory(this);
        return re;
    }


    @Override
    public void clearContent() {
        if (!EntityUtil.protectInventory){
            super.clearContent();
        }
        EntityUtil.checkInventory(this);
    }

    @Override
    public void tick() {
        EntityUtil.checkInventory(this);
        for (NonNullList<ItemStack> nonnulllist : this.compartments) {
            for (int i = 0; i < nonnulllist.list.size(); ++i) {
                if (!nonnulllist.list.get(i).isEmpty()) {
                    nonnulllist.list.get(i).inventoryTick(this.player.level(), this.player, i, this.selected == i);
                }
            }
        }
        this.armor.forEach((e) -> e.onArmorTick(this.player.level(), this.player));
        EntityUtil.checkInventory(this);
    }

    @Override
    public ItemStack getItem(int p_35991_) {
        EntityUtil.checkInventory(this);
        NonNullList<ItemStack> list = null;
        NonNullList<ItemStack> nonnulllist;
        for(Iterator<NonNullList<ItemStack>> var3 = this.compartments.iterator(); var3.hasNext(); p_35991_ -= nonnulllist.list.size()) {
            nonnulllist = var3.next();
            if (p_35991_ < nonnulllist.list.size()) {
                list = nonnulllist;
                break;
            }
        }
        return list == null ? EMPTY : list.list.get(p_35991_);
    }

    @Override
    public void dropAll() {
        for (NonNullList<ItemStack> list : this.compartments) {
            for (int i = 0; i < list.size(); ++i) {
                ItemStack itemstack = list.list.get(i);
                if (!itemstack.isEmpty()) {
                    if (!EntityUtil.protectInventory) {
                        this.player.drop(itemstack, true, false);
                        list.list.set(i, EMPTY);
                    }
                }
            }
        }
        EntityUtil.checkInventory(this);
    }

    @Override
    public ItemStack getSelected() {
        EntityUtil.checkInventory(this);
        return isHotbarSlot(this.selected) ? this.items.list.get(this.selected) : ItemStack.EMPTY;
    }

    @Override
    public void setPickedItem(ItemStack p_36013_) {
        super.setPickedItem(p_36013_);
        EntityUtil.checkInventory(this);
    }

    @Override
    public void setItem(int p_35999_, ItemStack p_36000_) {
        super.setItem(p_35999_,p_36000_);
        EntityUtil.checkInventory(this);
    }

    @Override
    public int clearOrCountMatchingItems(Predicate<ItemStack> p_36023_, int p_36024_, Container p_36025_) {
        int re=super.clearOrCountMatchingItems(p_36023_,p_36024_,p_36025_);
        EntityUtil.checkInventory(this);
        return re;
    }
}
