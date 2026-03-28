package win.trystage.felix.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import win.trystage.felix.client.core.RawSound;
import win.trystage.felix.client.ui.Notifications;

import java.util.Objects;

public class Onlines {
    private static long lastGlistKeyPressTime = 0;
    private static final long GLIST_KEY_DELAY = 500;
    public static void register() {
        KeyBinding glistKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "获取玩家列表",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                "Felix"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (glistKeyBinding.wasPressed()) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastGlistKeyPressTime >= GLIST_KEY_DELAY) {
                    if (!MinecraftClient.getInstance().isInSingleplayer()) {
                        Objects.requireNonNull(client.getNetworkHandler()).sendCommand("glist");
                        Notifications.notify(
                                "glist",
                                "获取玩家列表",
                                "已向服务器发送请求",
                                Notifications.Type.INFO
                        );
                        RawSound.playFelix("info.wav", 1.0f);
                    }
                    lastGlistKeyPressTime = currentTime;
                }
            }
        });
    }
}