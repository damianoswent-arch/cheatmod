package pl.cheat;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4fStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class CheatClient implements ClientModInitializer {
    public static final List<Module> MODULES = new ArrayList<>();
    public static final Module TRACERS = new Module("Tracers");

    private static KeyBinding guiKey;

    @Override
    public void onInitializeClient() {
        MODULES.add(TRACERS);

        guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cheatmod.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_U, "category.cheatmod"));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (guiKey.wasPressed()) {
                if (mc.currentScreen == null) mc.setScreen(new ModuleScreen());
            }
        });

        WorldRenderEvents.LAST.register(CheatClient::renderTracers);
    }

    private static void renderTracers(WorldRenderContext ctx) {
        if (!TRACERS.enabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        Camera camera = ctx.camera();
        Vec3d camPos = camera.getPos();
        float td = ctx.tickCounter().getTickDelta(false);

        // punkt startowy tuz przed kamera (srodek ekranu)
        Vec3d look = Vec3d.fromPolar(camera.getPitch(), camera.getYaw()).normalize();
        float sx = (float) look.x, sy = (float) look.y, sz = (float) look.z;

        // od 1.21.2 rotacja kamery siedzi w positionMatrix, wpinamy ja w modelview
        Matrix4fStack mv = RenderSystem.getModelViewStack();
        mv.pushMatrix();
        mv.mul(ctx.positionMatrix());

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(2.0f);

        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player) continue;

            Vec3d pos = p.getLerpedPos(td);
            float x = (float) (pos.x - camPos.x);
            float y = (float) (pos.y - camPos.y + p.getHeight() / 2.0);
            float z = (float) (pos.z - camPos.z);

            // kolor zalezny od dystansu: blisko = czerwony, daleko = zielony
            float t = MathHelper.clamp(mc.player.distanceTo(p) / 60f, 0f, 1f);
            float r = 1f - t, g = t, b = 0.1f;

            buf.vertex(sx, sy, sz).color(r, g, b, 1f);
            buf.vertex(x, y, z).color(r, g, b, 1f);
        }

        BufferRenderer.drawWithGlobalProgram(buf.end());

        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        mv.popMatrix();
    }
}
