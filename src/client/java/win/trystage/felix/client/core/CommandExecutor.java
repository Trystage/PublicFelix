package win.trystage.felix.client.core;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class CommandExecutor {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    private CommandExecutor() {}
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            SCHEDULER.shutdown();
            try {
                if (!SCHEDULER.awaitTermination(2, TimeUnit.SECONDS)) {
                    SCHEDULER.shutdownNow();
                }
            } catch (InterruptedException e) {
                SCHEDULER.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }, "Felix-CommandExecutor-Shutdown"));
    }
    public static void sendServerCommand(String command) {
        if (mc.player != null) {
            if (!MinecraftClient.getInstance().isInSingleplayer()) {
                SCHEDULER.schedule(() -> mc.execute(() -> Objects.requireNonNull(mc.getNetworkHandler()).sendCommand(command)), 1, TimeUnit.SECONDS);
            }
        }
    }
    public static void sendChatMessage(String message) {
        if (mc.player != null) {
            if (!MinecraftClient.getInstance().isInSingleplayer()) {
                SCHEDULER.schedule(() -> mc.execute(() -> Objects.requireNonNull(mc.getNetworkHandler()).sendChatMessage(message)), 1, TimeUnit.SECONDS);
            }
        }
    }
    public static void openChatWithCommand(String command, MinecraftClient client) {
        if (client != null) {
            client.setScreen(new ChatScreen("/" + command));
        }
    }
}