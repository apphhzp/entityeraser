package net.apphhzp.entityeraser.util;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class ColourfulFont extends Font {
    public ColourfulFont(Function<ResourceLocation, FontSet> p_243253_, boolean p_243245_) {
        super(p_243253_, p_243245_);
    }
    @Override
    public int drawInBatch(String p_272751_, float p_272661_, float p_273129_, int p_273272_, boolean p_273209_, Matrix4f p_272940_, MultiBufferSource p_273017_, DisplayMode p_272608_, int p_273365_, int p_272755_) {
        return this.drawInBatch(p_272751_, p_272661_, p_273129_, p_273272_, p_273209_, p_272940_, p_273017_, p_272608_, p_273365_, p_272755_, this.isBidirectional());
    }
    @Override
    public int drawInBatch(String p_272780_, float p_272811_, float p_272610_, int p_273422_, boolean p_273016_, Matrix4f p_273443_, MultiBufferSource p_273387_, DisplayMode p_273551_, int p_272706_, int p_273114_, boolean p_273022_) {
        return this.drawInternal(p_272780_, p_272811_, p_272610_, p_273422_, p_273016_, p_273443_, p_273387_, p_273551_, p_272706_, p_273114_, p_273022_);
    }
    @Override
    public int drawInBatch(Component p_273032_, float p_273249_, float p_273594_, int p_273714_, boolean p_273050_, Matrix4f p_272974_, MultiBufferSource p_273695_, DisplayMode p_272782_, int p_272603_, int p_273632_) {
        return this.drawInBatch(p_273032_.getVisualOrderText(), p_273249_, p_273594_, p_273714_, p_273050_, p_272974_, p_273695_, p_272782_, p_272603_, p_273632_);
    }
    @Override
    public int drawInBatch(FormattedCharSequence p_273262_, float p_273006_, float p_273254_, int p_273375_, boolean p_273674_, Matrix4f p_273525_, MultiBufferSource p_272624_, DisplayMode p_273418_, int p_273330_, int p_272981_) {
        return this.drawInternal(p_273262_, p_273006_, p_273254_, p_273375_, p_273674_, p_273525_, p_272624_, p_273418_, p_273330_, p_272981_);
    }

    @Override
    public int drawInternal(String s, float x, float y, int color, boolean p_272778_, Matrix4f p_272662_, MultiBufferSource p_273012_, DisplayMode p_273381_, int p_272855_, int p_272745_, boolean p_272785_) {
        if (p_272785_) {
            s = this.bidirectionalShaping(s);
        }
        for (int i=0;i<s.length();i++){
            float yOff=(float)(Math.cos(Util.getMillis()/200D+i/4D));
            color = adjustColor(EntityUtil.getColor(255,-Math.PI/3*2+i/16D,1500D));
            Matrix4f matrix4f = new Matrix4f(p_272662_);
            if (p_272778_) {
                this.renderText(String.valueOf(s.charAt(i)), x, y+yOff, color, true, p_272662_, p_273012_, p_273381_, p_272855_, p_272745_);
                matrix4f.translate(SHADOW_OFFSET);
            }
            x+= this.renderText(String.valueOf(s.charAt(i)), x, y+yOff, color, false, matrix4f, p_273012_, p_273381_, p_272855_, p_272745_);
        }
        return (int)x + (p_272778_ ? 1 : 0);
    }

    @Override
    public int drawInternal(FormattedCharSequence formattedCharSequence, float x, float y, int color, boolean p_273531_, Matrix4f p_273265_, MultiBufferSource p_273560_, DisplayMode p_273342_, int p_273373_, int p_273266_) {
        StringBuilder stringBuilder = new StringBuilder();
        formattedCharSequence.accept((index, style, codePoint) -> {
            stringBuilder.appendCodePoint(codePoint);
            return true;
        });
        String text = ChatFormatting.stripFormatting(stringBuilder.toString());
        if (text != null) {
            for(int i=0;i<text.length();i++) {
                float yOff=(float)(Math.cos(Util.getMillis()/200D+i/4D));
                color = adjustColor(EntityUtil.getColor(255,-Math.PI/3*2+i/16D,1500D));
                Matrix4f matrix4f = new Matrix4f(p_273265_);
                if (p_273531_) {
                    this.renderText(String.valueOf(text.charAt(i)), x, y+yOff, color, true, p_273265_, p_273560_, p_273342_, p_273373_, p_273266_);
                    matrix4f.translate(SHADOW_OFFSET);
                }
                x = this.renderText(String.valueOf(text.charAt(i)), x, y+yOff, color, false, matrix4f, p_273560_, p_273342_, p_273373_, p_273266_);
            }
        }
        return (int)x + (p_273531_ ? 1 : 0);
    }

    public float renderText(String p_273765_, float p_273532_, float p_272783_, int p_273217_, boolean p_273583_, Matrix4f p_272734_, MultiBufferSource p_272595_, DisplayMode p_273610_, int p_273727_, int p_273199_) {
        StringRenderOutput font$stringrenderoutput = new StringRenderOutput(p_272595_, p_273532_, p_272783_, p_273217_, p_273583_, p_272734_, p_273610_, p_273199_);
        StringDecomposer.iterateFormatted(p_273765_, Style.EMPTY, font$stringrenderoutput);
        return font$stringrenderoutput.finish(p_273727_, p_273532_);
    }

    public static int adjustColor(int p_92720_) {
        return (p_92720_ & -67108864) == 0 ? p_92720_ | -16777216 : p_92720_;
    }
}
