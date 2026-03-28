package win.trystage.felix.client.input;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import win.trystage.felix.FelixClient.FelixClient;
import win.trystage.felix.client.uis.UIS;

public final class KeyBindings {
    private static KeyBinding selectKey;
    private static KeyBinding resetKey;
    private KeyBindings() {}
    public static void register() {
        selectKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "选取玩家",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "Felix"
        ));
        resetKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "重置目标",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "Felix"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (MinecraftClient.getInstance().isInSingleplayer()) {
                return;
            }
            while (selectKey.wasPressed()) {
                FelixClient.BUS.call(new FelixClient.ClientInputEvent(
                        FelixClient.ClientInputEvent.Action.SELECT_KEY,
                        client,
                        GLFW.GLFW_KEY_R,
                        0d,
                        System.currentTimeMillis(),
                        UIS.isHudShowing()
                ));
            }
            while (resetKey.wasPressed()) {
                FelixClient.BUS.call(new FelixClient.ClientInputEvent(
                        FelixClient.ClientInputEvent.Action.RESET_KEY,
                        client,
                        GLFW.GLFW_KEY_H,
                        0d,
                        System.currentTimeMillis(),
                        UIS.isHudShowing()
                ));
            }
            FelixClient.BUS.call(new FelixClient.ClientInputEvent(
                    FelixClient.ClientInputEvent.Action.NAVIGATION_TICK,
                    client,
                    0,
                    0d,
                    System.currentTimeMillis(),
                    UIS.isHudShowing()
            ));
        });
    }
}