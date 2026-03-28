package win.trystage.felix.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

public final class AntiBot {
    private AntiBot() {}
    public static boolean isInTabList(String rawName) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.getNetworkHandler() == null) return false;
        String rawLower = rawName == null ? "" : rawName.toLowerCase();
        for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
            String profileName = entry.getProfile() != null && entry.getProfile().getName() != null ? entry.getProfile().getName() : "";
            if (!profileName.isEmpty() && profileName.equalsIgnoreCase(rawName)) {
                return true;
            }
            net.minecraft.text.Text disp = entry.getDisplayName();
            if (disp != null) {
                String plain = disp.getString().toLowerCase();
                if (plain.contains(rawLower)) {
                    return true;
                }
            }
        }
        return false;
    }
}