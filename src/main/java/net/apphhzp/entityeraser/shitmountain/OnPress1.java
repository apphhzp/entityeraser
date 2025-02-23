package net.apphhzp.entityeraser.shitmountain;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;

public class OnPress1 implements Button.OnPress {
    private final Screen screen;

    public OnPress1(Screen screen) {
        this.screen = screen;
    }

    @Override
    public void onPress(Button button) {
        screen.minecraft.getReportingContext().draftReportHandled(screen.minecraft, screen, new EmptyRunnable(), true);
    }
}
