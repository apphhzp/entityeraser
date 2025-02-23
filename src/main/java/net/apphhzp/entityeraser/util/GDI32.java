package net.apphhzp.entityeraser.util;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.W32APIOptions;

public interface GDI32 extends Library {
    GDI32 INSTANCE= Native.load("gdi32", GDI32.class, W32APIOptions.DEFAULT_OPTIONS);
    boolean SwapBuffers(WinDef.HDC hdc);
}
