package win.trystage.felix.client.ui.features;

import net.minecraft.client.MinecraftClient;

import java.util.List;

public interface MenuFeature {
    List<String> options();
    void execute(int optionIndex, String targetPlayerName, MinecraftClient client);
}