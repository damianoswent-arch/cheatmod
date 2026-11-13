package pl.cheat;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ModuleScreen extends Screen {
    public ModuleScreen() { super(Text.literal("CheatMod")); }

    @Override
    protected void init() {
        int w = 120, h = 20;
        int x = width / 2 - w / 2;
        int y = height / 2 - (CheatClient.MODULES.size() * (h + 4)) / 2 + 12;

        for (Module m : CheatClient.MODULES) {
            addDrawableChild(ButtonWidget.builder(label(m), btn -> {
                m.toggle();
                btn.setMessage(label(m));
            }).dimensions(x, y, w, h).build());
            y += h + 4;
        }
    }

    private Text label(Module m) {
        return Text.literal(m.name + ": " + (m.enabled ? "\u00a7aON" : "\u00a7cOFF"));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int top = height / 2 - (CheatClient.MODULES.size() * 24) / 2 - 12;
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, "CheatMod", width / 2, top, 0xFFFFFF);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_U) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }
}
