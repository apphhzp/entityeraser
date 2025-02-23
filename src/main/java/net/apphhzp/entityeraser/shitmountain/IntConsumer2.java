package net.apphhzp.entityeraser.shitmountain;

import com.mojang.blaze3d.vertex.BufferBuilder;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import org.apache.commons.lang3.mutable.MutableInt;

public  class IntConsumer2 implements IntConsumer {
    private final BufferBuilder builder;
    private final MutableInt mutableint;

    public IntConsumer2(BufferBuilder builder, MutableInt mutableint) {
        this.builder = builder;
        this.mutableint = mutableint;
    }

    @Override
    public void accept(int p_231163_) {
        builder.buffer.putInt(mutableint.getAndAdd(4), p_231163_);
    }
}
