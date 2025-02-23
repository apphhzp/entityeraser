package net.apphhzp.entityeraser.shitmountain;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.systems.RenderSystem;

public class RenderCall2 implements RenderCall {
    private final float f1,f2,f3,f4;
    public RenderCall2(float f1, float f2, float f3, float f4) {
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.f4 = f4;
    }
    @Override
    public void execute() {
        RenderSystem.shaderColor[0] = f1;
        RenderSystem.shaderColor[1] = f2;
        RenderSystem.shaderColor[2] = f3;
        RenderSystem.shaderColor[3] = f4;
    }
}