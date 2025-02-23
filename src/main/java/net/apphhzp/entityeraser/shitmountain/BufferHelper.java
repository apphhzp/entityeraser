package net.apphhzp.entityeraser.shitmountain;

import apphhzp.lib.ClassHelper;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

@SuppressWarnings({"UnusedReturnValue","unused"})
@OnlyIn(Dist.CLIENT)
public final class BufferHelper {
    public static void ensureVertexCapacity(BufferBuilder builder) {
        ensureCapacity(builder,builder.format.getVertexSize());
    }

    public static void ensureCapacity(BufferBuilder builder,int p_85723_) {
        if (builder.nextElementByte + p_85723_ > builder.buffer.capacity()) {
            int i = builder.buffer.capacity();
            int j = i + roundUp(p_85723_);
            BufferBuilder.LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", i, j);
            ByteBuffer bytebuffer = MemoryTracker.resize(builder.buffer, j);
            bytebuffer.rewind();
            builder.buffer = bytebuffer;
        }
    }

    public static int roundUp(int p_85726_) {
        int i = 2097152;
        if (p_85726_ == 0) {
            return i;
        } else {
            if (p_85726_ < 0) {
                i *= -1;
            }
            int j = p_85726_ % i;
            return j == 0 ? p_85726_ : p_85726_ + i - j;
        }
    }

    public static void setQuadSorting(BufferBuilder builder,VertexSorting p_277454_) {
        if (builder.mode == VertexFormat.Mode.QUADS) {
            builder.sorting = p_277454_;
            if (builder.sortingPoints == null) {
                builder.sortingPoints = makeQuadSortingPoints(builder);
            }

        }
    }

    public static BufferBuilder.SortState getSortState(BufferBuilder builder) {
        return new BufferBuilder.SortState(builder.mode, builder.vertices, builder.sortingPoints, builder.sorting);
    }

    public static void restoreSortState(BufferBuilder builder,BufferBuilder.SortState p_166776_) {
        builder.buffer.rewind();
        builder.mode = p_166776_.mode;
        builder.vertices = p_166776_.vertices;
        builder.nextElementByte = builder.renderedBufferPointer;
        builder.sortingPoints = p_166776_.sortingPoints;
        builder.sorting = p_166776_.sorting;
        builder.indexOnly = true;
    }

    public static void begin(BufferBuilder builder,VertexFormat.Mode p_166780_, VertexFormat p_166781_) {
        if (builder.building) {
            throw new IllegalStateException("Already building!");
        } else {
            builder.building = true;
            builder.mode = p_166780_;
            switchFormat(builder,p_166781_);
            builder.currentElement = p_166781_.getElements().get(0);
            builder.elementIndex = 0;
            builder.buffer.rewind();
        }
    }

    public static void switchFormat(BufferBuilder builder,VertexFormat p_85705_) {
        if (builder.format != p_85705_) {
            builder.format = p_85705_;
            boolean flag = p_85705_ == DefaultVertexFormat.NEW_ENTITY;
            boolean flag1 = p_85705_ == DefaultVertexFormat.BLOCK;
            builder.fastFormat = flag || flag1;
            builder.fullFormat = flag;
        }
    }

    public static IntConsumer intConsumer(BufferBuilder builder,int p_231159_, VertexFormat.IndexType p_231160_) {
        MutableInt mutableint = new MutableInt(p_231159_);
        IntConsumer intconsumer;
        switch (p_231160_) {
            case SHORT:
                intconsumer = (p_231167_) -> {
                    builder.buffer.putShort(mutableint.getAndAdd(2), (short)p_231167_);
                };
                break;
            case INT:
                intconsumer = (p_231163_) -> {
                    builder.buffer.putInt(mutableint.getAndAdd(4), p_231163_);
                };
                break;
            default:
                throw new IncompatibleClassChangeError();
        }

        return intconsumer;
    }

    public static Vector3f[] makeQuadSortingPoints(BufferBuilder builder) {
        FloatBuffer floatbuffer = builder.buffer.asFloatBuffer();
        int i = builder.renderedBufferPointer / 4;
        int j = builder.format.getIntegerSize();
        int k = j * builder.mode.primitiveStride;
        int l = builder.vertices / builder.mode.primitiveStride;
        Vector3f[] avector3f = new Vector3f[l];
        for(int i1 = 0; i1 < l; ++i1) {
            float f = floatbuffer.get(i + i1 * k);
            float f1 = floatbuffer.get(i + i1 * k + 1);
            float f2 = floatbuffer.get(i + i1 * k + 2);
            float f3 = floatbuffer.get(i + i1 * k + j * 2);
            float f4 = floatbuffer.get(i + i1 * k + j * 2 + 1);
            float f5 = floatbuffer.get(i + i1 * k + j * 2 + 2);
            float f6 = (f + f3) / 2.0F;
            float f7 = (f1 + f4) / 2.0F;
            float f8 = (f2 + f5) / 2.0F;
            avector3f[i1] = new Vector3f(f6, f7, f8);
        }

        return avector3f;
    }

    public static void putSortedQuadIndices(BufferBuilder builder,VertexFormat.IndexType p_166787_) {
        if (builder.sortingPoints != null && builder.sorting != null) {
            int[] aint = builder.sorting.sort(builder.sortingPoints);
            IntConsumer intconsumer = intConsumer(builder,builder.nextElementByte, p_166787_);
            for(int i : aint) {
                intconsumer.accept(i * builder.mode.primitiveStride);
                intconsumer.accept(i * builder.mode.primitiveStride + 1);
                intconsumer.accept(i * builder.mode.primitiveStride + 2);
                intconsumer.accept(i * builder.mode.primitiveStride + 2);
                intconsumer.accept(i * builder.mode.primitiveStride + 3);
                intconsumer.accept(i * builder.mode.primitiveStride);
            }
        } else {
            throw new IllegalStateException("Sorting state uninitialized");
        }
    }

    public static boolean isCurrentBatchEmpty(BufferBuilder builder) {
        return builder.vertices == 0;
    }

    @Nullable
    public static BufferBuilder.RenderedBuffer endOrDiscardIfEmpty(BufferBuilder builder) {
        ensureDrawing(builder);
        if (isCurrentBatchEmpty(builder)) {
            reset(builder);
            return null;
        } else {
            BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = storeRenderedBuffer(builder);
            reset(builder);
            return bufferbuilder$renderedbuffer;
        }
    }

    public static BufferBuilder.RenderedBuffer end(BufferBuilder builder) {
        ensureDrawing(builder);
        BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = storeRenderedBuffer(builder);
        reset(builder);
        return bufferbuilder$renderedbuffer;
    }

    public static void ensureDrawing(BufferBuilder builder) {
        if (!builder.building) {
            throw new IllegalStateException("Not building!");
        }
    }

    public static BufferBuilder.RenderedBuffer storeRenderedBuffer(BufferBuilder builder) {
        int i = builder.mode.indexCount(builder.vertices);
        int j = !builder.indexOnly ? builder.vertices * builder.format.getVertexSize() : 0;
        VertexFormat.IndexType vertexformat$indextype = VertexFormat.IndexType.least(i);
        boolean flag;
        int k;
        if (builder.sortingPoints != null) {
            int l = Mth.roundToward(i * vertexformat$indextype.bytes, 4);
            ensureCapacity(builder,l);
            putSortedQuadIndices(builder,vertexformat$indextype);
            flag = false;
            builder.nextElementByte += l;
            k = j + l;
        } else {
            flag = true;
            k = j;
        }

        int i1 = builder.renderedBufferPointer;
        builder.renderedBufferPointer += k;
        ++builder.renderedBufferCount;
        BufferBuilder.DrawState bufferbuilder$drawstate = new BufferBuilder.DrawState(builder.format, builder.vertices, i, builder.mode, vertexformat$indextype, builder.indexOnly, flag);
        return builder.new RenderedBuffer(i1, bufferbuilder$drawstate);
    }

    public static void reset(BufferBuilder builder) {
        builder.building = false;
        builder.vertices = 0;
        builder.currentElement = null;
        builder.elementIndex = 0;
        builder.sortingPoints = null;
        builder.sorting = null;
        builder.indexOnly = false;
    }

    public static void putByte(BufferBuilder builder,int p_85686_, byte p_85687_) {
        builder.buffer.put(builder.nextElementByte + p_85686_, p_85687_);
    }

    public static void putShort(BufferBuilder builder,int p_85700_, short p_85701_) {
        builder.buffer.putShort(builder.nextElementByte + p_85700_, p_85701_);
    }

    public static void putFloat(BufferBuilder builder,int p_85689_, float p_85690_) {
        builder.buffer.putFloat(builder.nextElementByte + p_85689_, p_85690_);
    }

    public static void endVertex(BufferBuilder builder) {
        if (builder.elementIndex != 0) {
            throw new IllegalStateException("Not filled all elements of the vertex");
        } else {
            ++builder.vertices;
            ensureVertexCapacity(builder);
            if (builder.mode == VertexFormat.Mode.LINES || builder.mode == VertexFormat.Mode.LINE_STRIP) {
                int i = builder.format.getVertexSize();
                builder.buffer.put(builder.nextElementByte, builder.buffer, builder.nextElementByte - i, i);
                builder.nextElementByte += i;
                ++builder.vertices;
                ensureVertexCapacity(builder);
            }

        }
    }

    public static void nextElement(BufferBuilder builder) {
        ImmutableList<VertexFormatElement> immutablelist = builder.format.getElements();
        builder.elementIndex = (builder.elementIndex + 1) % immutablelist.size();
        builder.nextElementByte += builder.currentElement.getByteSize();
        VertexFormatElement vertexformatelement = immutablelist.get(builder.elementIndex);
        builder.currentElement = vertexformatelement;
        if (vertexformatelement.getUsage() == VertexFormatElement.Usage.PADDING) {
            nextElement(builder);
        }

        if (builder.defaultColorSet && builder.currentElement.getUsage() == VertexFormatElement.Usage.COLOR) {
            colorBufferVertexConsumer(builder,builder.defaultR, builder.defaultG, builder.defaultB, builder.defaultA);
        }

    }

    public static BufferBuilder color(BufferBuilder builder,int p_85692_, int p_85693_, int p_85694_, int p_85695_) {
        if (builder.defaultColorSet) {
            throw new IllegalStateException();
        } else {
            return colorBufferVertexConsumer(builder,p_85692_, p_85693_, p_85694_, p_85695_);
        }
    }

    public static void vertex(BufferBuilder builder,float p_85671_, float p_85672_, float p_85673_, float p_85674_, float p_85675_, float p_85676_, float p_85677_, float p_85678_, float p_85679_, int p_85680_, int p_85681_, float p_85682_, float p_85683_, float p_85684_) {
        if (builder.defaultColorSet) {
            throw new IllegalStateException();
        } else if (builder.fastFormat) {
            putFloat(builder,0, p_85671_);
            putFloat(builder,4, p_85672_);
            putFloat(builder,8, p_85673_);
            putByte(builder,12, (byte)((int)(p_85674_ * 255.0F)));
            putByte(builder,13, (byte)((int)(p_85675_ * 255.0F)));
            putByte(builder,14, (byte)((int)(p_85676_ * 255.0F)));
            putByte(builder,15, (byte)((int)(p_85677_ * 255.0F)));
            putFloat(builder,16, p_85678_);
            putFloat(builder,20, p_85679_);
            int i;
            if (builder.fullFormat) {
                putShort(builder,24, (short)(p_85680_ & '\uffff'));
                putShort(builder,26, (short)(p_85680_ >> 16 & '\uffff'));
                i = 28;
            } else {
                i = 24;
            }

            putShort(builder, i, (short)(p_85681_ & '\uffff'));
            putShort(builder,i + 2, (short)(p_85681_ >> 16 & '\uffff'));
            putByte(builder,i + 4, normalIntValue(p_85682_));
            putByte(builder,i + 5, normalIntValue(p_85683_));
            putByte(builder,i + 6, normalIntValue(p_85684_));
            builder.nextElementByte += i + 8;
            endVertex(builder);
        } else {
            superVertex(builder,p_85671_, p_85672_, p_85673_, p_85674_, p_85675_, p_85676_, p_85677_, p_85678_, p_85679_, p_85680_, p_85681_, p_85682_, p_85683_, p_85684_);
        }
    }

    public static void releaseRenderedBuffer(BufferBuilder builder) {
        if (builder.renderedBufferCount > 0 && --builder.renderedBufferCount == 0) {
            clear(builder);
        }

    }

    public static void clear(BufferBuilder builder) {
        if (builder.renderedBufferCount > 0) {
            BufferBuilder.LOGGER.warn("Clearing BufferBuilder with unused batches");
        }

        discard(builder);
    }

    public static void discard(BufferBuilder builder) {
        builder.renderedBufferCount = 0;
        builder.renderedBufferPointer = 0;
        builder.nextElementByte = 0;
    }

    public static VertexFormatElement currentElement(BufferBuilder builder) {
        if (builder.currentElement == null) {
            throw new IllegalStateException("BufferBuilder not started");
        } else {
            return builder.currentElement;
        }
    }

    public static boolean building(BufferBuilder builder) {
        return builder.building;
    }

    static ByteBuffer bufferSlice(BufferBuilder builder,int p_231170_, int p_231171_) {
        return MemoryUtil.memSlice(builder.buffer, p_231170_, p_231171_ - p_231170_);
    }

    public static void putBulkData(BufferBuilder builder,ByteBuffer buffer) {
        ensureCapacity(builder,buffer.limit() + builder.format.getVertexSize());
        builder.buffer.position(builder.nextElementByte);
        builder.buffer.put(buffer);
        builder.buffer.position(0);
        builder.vertices += buffer.limit() / builder.format.getVertexSize();
        builder.nextElementByte += buffer.limit();
    }

    public static ByteBuffer vertexBuffer(BufferBuilder.RenderedBuffer buffer) {
        int i = buffer.pointer + buffer.drawState.vertexBufferStart();
        int j = buffer.pointer + buffer.drawState.vertexBufferEnd();
        return bufferSlice(ClassHelper.getOuterInstance(buffer, BufferBuilder.class),i, j);
    }

    public static ByteBuffer indexBuffer(BufferBuilder.RenderedBuffer buffer) {
        int i = buffer.pointer + buffer.drawState.indexBufferStart();
        int j = buffer.pointer + buffer.drawState.indexBufferEnd();
        return bufferSlice(ClassHelper.getOuterInstance(buffer, BufferBuilder.class),i, j);
    }


    public static boolean isEmpty(BufferBuilder.RenderedBuffer buffer) {
        return buffer.drawState.vertexCount == 0;
    }

    public static void release(BufferBuilder.RenderedBuffer buffer) {
        if (buffer.released) {
            throw new IllegalStateException("Buffer has already been released!");
        } else {
            releaseRenderedBuffer(ClassHelper.getOuterInstance(buffer, BufferBuilder.class));
            buffer.released = true;
        }
    }
    
    //================super=======================
    //================super=======================
    //================super=======================
    //================super=======================
    //================super=======================
    //================super=======================
    //================super=======================
    //================super=======================

    public static void superVertex(BufferBuilder builder,float p_85955_, float p_85956_, float p_85957_, float p_85958_, float p_85959_, float p_85960_, float p_85961_, float p_85962_, float p_85963_, int p_85964_, int p_85965_, float p_85966_, float p_85967_, float p_85968_) {
        vertexBufferVertexConsumer(builder,(double)p_85955_, (double)p_85956_, (double)p_85957_);
        colorVertexConsumer(builder,p_85958_, p_85959_, p_85960_, p_85961_);
        uvBufferVertexConsumer(builder,p_85962_, p_85963_);
        overlayCoordsVertexConsumer(builder,p_85964_);
        uv2VertexConsumer(builder,p_85965_);
        normalBufferVertexConsumer(builder,p_85966_, p_85967_, p_85968_);
        endVertex(builder);
    }

    public static BufferBuilder colorVertexConsumer(BufferBuilder builder, float p_85951_, float p_85952_, float p_85953_, float p_85954_) {
        return color(builder,(int)(p_85951_ * 255.0F), (int)(p_85952_ * 255.0F), (int)(p_85953_ * 255.0F), (int)(p_85954_ * 255.0F));
    }

    public static BufferBuilder vertexBufferVertexConsumer(BufferBuilder builder, double p_85771_, double p_85772_, double p_85773_) {
        if (currentElement(builder).getUsage() != VertexFormatElement.Usage.POSITION) {
            return builder;
        } else if (currentElement(builder).getType() == VertexFormatElement.Type.FLOAT && currentElement(builder).getCount() == 3) {
            putFloat(builder,0, (float)p_85771_);
            putFloat(builder,4, (float)p_85772_);
            putFloat(builder,8, (float)p_85773_);
            nextElement(builder);
            return builder;
        } else {
            throw new IllegalStateException();
        }
    }
    
    public static BufferBuilder uvBufferVertexConsumer(BufferBuilder builder, float p_85777_, float p_85778_) {
        VertexFormatElement vertexformatelement = currentElement(builder);
        if (vertexformatelement.getUsage() == VertexFormatElement.Usage.UV && vertexformatelement.getIndex() == 0) {
            if (vertexformatelement.getType() == VertexFormatElement.Type.FLOAT && vertexformatelement.getCount() == 2) {
                putFloat(builder,0, p_85777_);
                putFloat(builder,4, p_85778_);
                nextElement(builder);
                return builder;
            } else {
                throw new IllegalStateException();
            }
        } else {
            return builder;
        }
    }

    public static BufferBuilder uv2VertexConsumer(BufferBuilder builder, int p_85970_) {
        return uv2BufferVertexConsumer(builder,p_85970_ & '\uffff', p_85970_ >> 16 & '\uffff');
    }

    public static BufferBuilder uv2BufferVertexConsumer(BufferBuilder builder,int p_85802_, int p_85803_) {
        return uvShortBufferVertexConsumer(builder,(short)p_85802_, (short)p_85803_, 2);
    }

    public static BufferBuilder overlayCoordsVertexConsumer(BufferBuilder builder, int p_86009_) {
        return overlayCoordsBufferVertexConsumer(builder,p_86009_ & '\uffff', p_86009_ >> 16 & '\uffff');
    }

    public static BufferBuilder overlayCoordsBufferVertexConsumer(BufferBuilder builder, int p_85784_, int p_85785_) {
        return uvShortBufferVertexConsumer(builder,(short)p_85784_, (short)p_85785_, 1);
    }

    public static BufferBuilder uvShortBufferVertexConsumer(BufferBuilder builder, short p_85794_, short p_85795_, int p_85796_) {
        VertexFormatElement vertexformatelement = currentElement(builder);
        if (vertexformatelement.getUsage() == VertexFormatElement.Usage.UV && vertexformatelement.getIndex() == p_85796_) {
            if (vertexformatelement.getType() == VertexFormatElement.Type.SHORT && vertexformatelement.getCount() == 2) {
                putShort(builder,0, p_85794_);
                putShort(builder,2, p_85795_);
                nextElement(builder);
                return builder;
            } else {
                throw new IllegalStateException();
            }
        } else {
            return builder;
        }
    }

    public static BufferBuilder normalBufferVertexConsumer(BufferBuilder builder, float p_85798_, float p_85799_, float p_85800_) {
        VertexFormatElement vertexformatelement = currentElement(builder);
        if (vertexformatelement.getUsage() != VertexFormatElement.Usage.NORMAL) {
            return builder;
        } else if (vertexformatelement.getType() == VertexFormatElement.Type.BYTE && vertexformatelement.getCount() == 3) {
            putByte(builder,0, normalIntValue(p_85798_));
            putByte(builder,1, normalIntValue(p_85799_));
            putByte(builder,2, normalIntValue(p_85800_));
            nextElement(builder);
            return builder;
        } else {
            throw new IllegalStateException();
        }
    }

    public static byte normalIntValue(float p_85775_) {
        return (byte)((int)(Mth.clamp(p_85775_, -1.0F, 1.0F) * 127.0F) & 255);
    }

    public static BufferBuilder colorBufferVertexConsumer(BufferBuilder builder,int p_85787_, int p_85788_, int p_85789_, int p_85790_) {
        VertexFormatElement vertexformatelement = currentElement(builder);
        if (vertexformatelement.getUsage() != VertexFormatElement.Usage.COLOR) {
            return builder;
        } else if (vertexformatelement.getType() == VertexFormatElement.Type.UBYTE && vertexformatelement.getCount() == 4) {
            putByte(builder,0, (byte)p_85787_);
            putByte(builder,1, (byte)p_85788_);
            putByte(builder,2, (byte)p_85789_);
            putByte(builder,3, (byte)p_85790_);
            nextElement(builder);
            return builder;
        } else {
            throw new IllegalStateException();
        }
    }

    public static BufferBuilder vertexVertexConsumer(BufferBuilder builder,Matrix4f p_254075_, float p_254519_, float p_253869_, float p_253980_) {
        Vector4f vector4f = p_254075_.transform(new Vector4f(p_254519_, p_253869_, p_253980_, 1.0F));
        return vertexBufferVertexConsumer(builder,(double)vector4f.x(), (double)vector4f.y(), (double)vector4f.z());
    }
}
