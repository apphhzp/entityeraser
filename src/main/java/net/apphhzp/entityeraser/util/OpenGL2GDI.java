package net.apphhzp.entityeraser.util;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.PointerByReference;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11C;

import java.nio.ByteBuffer;

import static com.sun.jna.platform.win32.WinGDI.DIB_RGB_COLORS;
import static org.lwjgl.opengl.GL11C.GL_RGB;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE;

public class OpenGL2GDI {
    private static byte[] pixels;
    private static WinDef.HDC pic;
    public static void updPixels(){
        Minecraft mc=Minecraft.getInstance();
        int width=mc.window.getWidth(),height=mc.window.getHeight();
        int bytesPerPixel = 3;
        int totalBytes = width * height * bytesPerPixel;

        // 创建ByteBuffer存储像素数据
        ByteBuffer buffer = BufferUtils.createByteBuffer(totalBytes);

        // 设置像素存储模式
        GL11C.glPixelStorei(GL11C.GL_PACK_ALIGNMENT, 1); // 禁用字节对齐

        // 使用glReadPixels读取像素数据
        GL11C.glReadPixels(0, 0, width, height, GL_RGB, GL_UNSIGNED_BYTE, buffer);

        // 将ByteBuffer转换为byte[]
        byte[] pixelData = new byte[totalBytes];
        buffer.get(pixelData);

        // 翻转图像（因为OpenGL的原点在左下角）
        byte[] flippedData = flipImageVertically(pixelData, width, height, bytesPerPixel);
        if (pic!=null){
            GDI32.INSTANCE.DeleteDC(pic);
        }
        pic=CreateHDCFromRGB(width,height,flippedData);
    }

    private static byte[] flipImageVertically(byte[] data, int width, int height, int bytesPerPixel) {
        byte[] flipped = new byte[data.length];
        int rowSize = width * bytesPerPixel;

        for (int y = 0; y < height; y++) {
            // 原始数据中第y行的位置
            int srcRow = y * rowSize;
            // 翻转后数据中第y行的位置（从底部开始）
            int destRow = (height - 1 - y) * rowSize;
            // 复制整行
            System.arraycopy(data, srcRow, flipped, destRow, rowSize);
        }

        return flipped;
    }

    private static WinDef.HDC CreateHDCFromRGB(int width, int height,byte[] pixelData) {
        // 获取屏幕DC用于创建兼容DC
        WinDef.HWND hwnd=new WinDef.HWND(new Pointer(Minecraft.getInstance().window.window));
        WinDef.HDC hScreenDC = new WinDef.HDC(new Pointer(org.lwjgl.system.windows.User32.GetDC(Minecraft.getInstance().window.window)));
        if (hScreenDC==null){
            System.err.println("Err0");
            return null;
        }

        // 创建内存DC
        WinDef.HDC hMemDC = GDI32.INSTANCE.CreateCompatibleDC(hScreenDC);
        if (hMemDC==null) {
            System.err.println("Err1");
            User32.INSTANCE.ReleaseDC(hwnd, hScreenDC);
            return null;
        }

        // 设置BITMAPINFO结构（24位BGR格式）
        WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
        bmi.bmiHeader.biSize = bmi.size();
        bmi.bmiHeader.biWidth = width;
        bmi.bmiHeader.biHeight = -height;  // 负值表示从上到下存储
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 24;     // 24位色（每像素3字节）
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;

        // 创建DIB段并获取像素缓冲区
        PointerByReference pDIBBits=new PointerByReference();
        WinDef.HBITMAP hBitmap =GDI32.INSTANCE.CreateDIBSection(hMemDC, bmi, DIB_RGB_COLORS, pDIBBits, Pointer.NULL, 0);

        if (hBitmap==null || Pointer.nativeValue(pDIBBits.getValue())==0L){
            System.err.println("Err2");
            GDI32.INSTANCE.DeleteDC(hMemDC);
            User32.INSTANCE.ReleaseDC(hwnd, hScreenDC);
            return null;
        }

        // 将RGB数据转换为BGR格式并复制到DIB
        int srcStride = width * 3;   // 源数据行长度
        int dstStride = (width * 3 + 3) & ~3; // 目标行长度（4字节对齐）
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //BYTE* src = pixelData + y * srcStride + x * 3;
                //BYTE* dst = pDIBBits + y * dstStride + x * 3;

                // RGB -> BGR 转换
                pDIBBits.getValue().setByte((long) y * dstStride + x * 3L,pixelData[y * srcStride + x * 3+2]);  // B
                pDIBBits.getValue().setByte((long) y * dstStride + x * 3L+1,pixelData[y * srcStride + x * 3+1]);
                pDIBBits.getValue().setByte((long) y * dstStride + x * 3L+2,pixelData[y * srcStride + x * 3]);
            }
        }

        // 将位图选入内存DC
        GDI32.INSTANCE.SelectObject(hMemDC, hBitmap);
        User32.INSTANCE.ReleaseDC(hwnd, hScreenDC);
        System.err.println("ret:"+Pointer.nativeValue(hMemDC.getPointer()));
        return hMemDC;
    }

    public static void render(){
        if (pic==null){
            return;
        }
        Minecraft mc=Minecraft.getInstance();
        int width=mc.window.getWidth(),height=mc.window.getHeight();
        WinDef.HWND hwnd=new WinDef.HWND(new Pointer(Minecraft.getInstance().window.window));
        WinDef.HDC hScreenDC = new WinDef.HDC(new Pointer(org.lwjgl.system.windows.User32.GetDC(Minecraft.getInstance().window.window)));
        if (hScreenDC==null){
            System.exit(114514);
        }
        if(!GDI32.INSTANCE.BitBlt(hScreenDC,0,0,width,height,pic,0,0,GDI32.SRCCOPY)){
            System.err.println(Kernel32.INSTANCE.GetLastError());
        }
        User32.INSTANCE.ReleaseDC(hwnd, hScreenDC);
    }
}
