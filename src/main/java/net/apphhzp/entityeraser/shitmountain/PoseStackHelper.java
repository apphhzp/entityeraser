package net.apphhzp.entityeraser.shitmountain;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public final class PoseStackHelper {
    public static void translate(PoseStack stack,double p_85838_, double p_85839_, double p_85840_) {
        translate(stack,(float)p_85838_, (float)p_85839_, (float)p_85840_);
    }

    public static void translate(PoseStack stack,float p_254202_, float p_253782_, float p_254238_) {
        PoseStack.Pose posestack$pose = stack.poseStack.getLast();
        posestack$pose.pose.translate(p_254202_, p_253782_, p_254238_);
    }

    public static void scale(PoseStack stack,float p_85842_, float p_85843_, float p_85844_) {
        PoseStack.Pose posestack$pose = stack.poseStack.getLast();
        posestack$pose.pose.scale(p_85842_, p_85843_, p_85844_);
        if (p_85842_ == p_85843_ && p_85843_ == p_85844_) {
            if (p_85842_ > 0.0F) {
                return;
            }
            posestack$pose.normal.scale(-1.0F);
        }
        float f = 1.0F / p_85842_;
        float f1 = 1.0F / p_85843_;
        float f2 = 1.0F / p_85844_;
        float f3 = Mth.fastInvCubeRoot(f * f1 * f2);
        posestack$pose.normal.scale(f3 * f, f3 * f1, f3 * f2);
    }

    public static void mulPose(PoseStack stack,Quaternionf p_254385_) {
        PoseStack.Pose posestack$pose = stack.poseStack.getLast();
        posestack$pose.pose.rotate(p_254385_);
        posestack$pose.normal.rotate(p_254385_);
    }

    public static void rotateAround(PoseStack stack,Quaternionf p_272904_, float p_273581_, float p_272655_, float p_273275_) {
        PoseStack.Pose posestack$pose = stack.poseStack.getLast();
        posestack$pose.pose.rotateAround(p_272904_, p_273581_, p_272655_, p_273275_);
        posestack$pose.normal.rotate(p_272904_);
    }

    public static void pushPose(PoseStack stack) {
        PoseStack.Pose posestack$pose = stack.poseStack.getLast();
        stack.poseStack.addLast(new PoseStack.Pose(new Matrix4f(posestack$pose.pose), new Matrix3f(posestack$pose.normal)));
    }

    public static void popPose(PoseStack stack) {
        stack.poseStack.removeLast();
    }

    public static PoseStack.Pose last(PoseStack stack) {
        return stack.poseStack.getLast();
    }

    public static boolean clear(PoseStack stack) {
        return stack.poseStack.size() == 1;
    }

    public static void setIdentity(PoseStack stack) {
        PoseStack.Pose posestack$pose = stack.poseStack.getLast();
        posestack$pose.pose.identity();
        posestack$pose.normal.identity();
    }

    public static void mulPoseMatrix(PoseStack stack,Matrix4f p_254128_) {
        stack.poseStack.getLast().pose.mul(p_254128_);
    }

    public static Matrix4f pose(PoseStack.Pose pose) {
        return pose.pose;
    }

    public static Matrix3f normal(PoseStack.Pose pose) {
        return pose.normal;
    }
}
