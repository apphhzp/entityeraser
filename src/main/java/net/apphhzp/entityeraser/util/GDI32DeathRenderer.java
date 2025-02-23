package net.apphhzp.entityeraser.util;

import com.sun.jna.Library;

public interface GDI32DeathRenderer extends Library {

    void preRender();
    void render(long hWnd,String s);
    void doRender();
}
