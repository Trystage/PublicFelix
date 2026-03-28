package win.trystage.felix.FelixClient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screen.TitleScreen;
import win.trystage.felix.client.Onlines;
import win.trystage.felix.client.core.RawSound;
import win.trystage.felix.client.input.KeyBindings;
import win.trystage.felix.client.ui.Notifications;
import win.trystage.felix.client.uis.UIS;
import win.trystage.felix.client.event.EventManager;

public class FelixClient implements ClientModInitializer {
    private static boolean startupPlayed = false;
    public static final EventManager BUS = EventManager.INSTANCE;
    public record ClientInputEvent(FelixClient.ClientInputEvent.Action action,
                                   net.minecraft.client.MinecraftClient client, int keyCode, double scrollDelta,
                                   long timestampMillis,
                                   boolean hudShowing) implements EventManager.Event {
            public enum Action {SELECT_KEY, RESET_KEY, NAVIGATION_TICK, SCROLL, MIDDLE_CLICK}
    }
    @Override
    public void onInitializeClient() {
        Onlines.register();
        KeyBindings.register();
        UIS.register();
        Notifications.register();
        BUS.register(new UIS.InputSubscriber());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!startupPlayed && client.currentScreen instanceof TitleScreen) {
                RawSound.playFelix("startup.wav", 1.0f);
                startupPlayed = true;
            }
        });
    }
}