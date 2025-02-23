package net.apphhzp.entityeraser.shitmountain;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.vertex.BufferBuilder;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public  class RenderCall1 implements RenderCall {
    private static final MethodHandle _drawWithShaderMethod;
    static {
        try {
            _drawWithShaderMethod=EntityEraserRenderers.deathRenderer.findStatic(EntityEraserRenderers.deathRenderer.lookupClass(),"_drawWithShader", MethodType.methodType(void.class,BufferBuilder.RenderedBuffer.class));
        }catch (Throwable t){
            throw new RuntimeException(t);
        }
    }
    private final BufferBuilder.RenderedBuffer data;

    public RenderCall1(BufferBuilder.RenderedBuffer data) {
        this.data = data;
    }

    @Override
    public void execute() {
        try {
            _drawWithShaderMethod.invoke(data);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        //_drawWithShader(data);
    }
}