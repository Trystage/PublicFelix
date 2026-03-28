package win.trystage.felix.client.ui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class Notifications {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final int PADDING_X = 6;
    private static final int PADDING_Y = 6;
    private static final int GAP_Y = 4;
    private static final int ICON_WIDTH = 4;
    private static final int ICON_HEIGHT = 20;
    private static final long IN_MS_DEFAULT = 100;
    private static final long STAY_MS_DEFAULT = 2800;
    private static final long OUT_MS_DEFAULT = 100;
    private static final boolean RENDER_BORDER = false;
    private static final float BORDER_WIDTH = 2f;

    private static final List<Entry> entries = new ArrayList<>();

    private Notifications() {}

    public static void register() {
        HudRenderCallback.EVENT.register(Notifications::render);
    }

    public enum Type {
        SUCCESS(new Color(76, 175, 80)),
        INFO(new Color(33, 150, 243)),
        WARNING(new Color(255, 193, 7)),
        ERROR(new Color(244, 67, 54));
        public final int rgb;
        Type(Color c) { this.rgb = c.getRGB(); }
    }

    public enum Phase { IN, STAY, OUT, END }
    public enum HorizontalFadeMode { IN_ONLY, OUT_ONLY, BOTH}

    private static class Entry {
        String key;
        String title;
        String desc;
        Type type;
        long startMs;
        long inMs = IN_MS_DEFAULT;
        long stayMs = STAY_MS_DEFAULT;
        long outMs = OUT_MS_DEFAULT;
        HorizontalFadeMode hFade = HorizontalFadeMode.BOTH;
        Phase phase;

        float width;
        float height;
        float targetX;
        float currentX;
        float targetY;
        float currentY;

        Entry(String key, String title, String desc, Type type) {
            this.key = key;
            this.title = title;
            this.desc = desc;
            this.type = type;
            this.phase = Phase.IN;
            this.startMs = System.currentTimeMillis();
        }
    }

    public static void notify(String key, String title, String desc, Type type) {
        notify(key, title, desc, type, STAY_MS_DEFAULT, HorizontalFadeMode.BOTH);
    }

    public static void notify(String key, String title, String desc, Type type, long stayMs, HorizontalFadeMode hFade) {
        for (Entry e : entries) {
            if (e.key.equals(key) && (e.phase == Phase.IN || e.phase == Phase.STAY)) {
                e.title = title;
                e.desc = desc;
                e.type = type;
                e.stayMs = Math.max(500, stayMs);
                e.hFade = hFade;
                e.phase = Phase.IN;
                e.startMs = System.currentTimeMillis();
                return;
            }
        }
        Entry e = new Entry(key, title, desc, type);
        e.stayMs = Math.max(500, stayMs);
        e.hFade = hFade;
        entries.add(0, e);
    }

    private static void render(DrawContext ctx, float tickDelta) {
        if (mc == null || mc.getWindow() == null || mc.textRenderer == null) return;
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        float yCursor = Math.round(sh * 0.7f);
        for (Entry e : entries) {
            int titleW = mc.textRenderer.getWidth(Text.literal(e.title));
            int descW = e.desc == null || e.desc.isEmpty() ? 0 : mc.textRenderer.getWidth(Text.literal(e.desc));
            int textW = Math.max(titleW, descW);
            int contentW = ICON_WIDTH + 6 + textW;
            e.width = PADDING_X + contentW + PADDING_X;
            e.height = PADDING_Y + ICON_HEIGHT + PADDING_Y;

            e.targetX = e.width;
            if (e.currentX == 0) {
                e.currentX = 0;
            }

            e.targetY = yCursor;
            if (e.currentY == 0) e.currentY = e.targetY;

            yCursor += e.height + GAP_Y;
        }

        long now = System.currentTimeMillis();
        final float smoothK = 12f;
        final float dtSec = 0.05f;
        final float alpha = 1f - (float)Math.pow(2.0, -smoothK * dtSec);
        Iterator<Entry> it = entries.iterator();
        while (it.hasNext()) {
            Entry e = it.next();
            long elapsed = now - e.startMs;

            switch (e.phase) {
                case IN -> {
                    if (elapsed >= e.inMs) { e.phase = Phase.STAY; e.startMs = now; }
                }
                case STAY -> {
                    if (elapsed >= e.stayMs) { e.phase = Phase.OUT; e.startMs = now; }
                }
                case OUT -> {
                    if (elapsed >= e.outMs) { e.phase = Phase.END; }
                }
                case END -> {}
            }

            if (e.phase == Phase.END) { it.remove(); continue; }

            boolean inAllowed = (e.hFade == HorizontalFadeMode.IN_ONLY || e.hFade == HorizontalFadeMode.BOTH);
            boolean outAllowed = (e.hFade == HorizontalFadeMode.OUT_ONLY || e.hFade == HorizontalFadeMode.BOTH);
            float targetX;
            if (e.phase == Phase.OUT) {
                targetX = outAllowed ? 0f : e.targetX;
            } else if (e.phase == Phase.IN) {
                targetX = inAllowed ? e.targetX : 0f;
            } else {
                targetX = e.targetX;
            }
            e.currentX += (targetX - e.currentX) * alpha;
            e.currentY += (e.targetY - e.currentY) * alpha;

            int right = sw - 8;
            int top = (int) e.currentY;
            int bottom = (int) (top + e.height);
            int left = (int) (right - e.currentX);

            int bg = new Color(0, 0, 0, 128).getRGB();
            ctx.fill(left, top, right, bottom, bg);
            if (RENDER_BORDER) {
                int border = new Color(255, 255, 255, 64).getRGB();
                ctx.fill(left, top, right, top + (int)BORDER_WIDTH, border);
                ctx.fill(left, bottom - (int)BORDER_WIDTH, right, bottom, border);
                ctx.fill(left, top, left + (int)BORDER_WIDTH, bottom, border);
                ctx.fill(right - (int)BORDER_WIDTH, top, right, bottom, border);
            }

            int iconLeft = left + PADDING_X;
            int iconTop = top + PADDING_Y;
            ctx.fill(iconLeft, iconTop, iconLeft + ICON_WIDTH, iconTop + ICON_HEIGHT, e.type.rgb);

            int textLeft = iconLeft + ICON_WIDTH + 6;
            int textTop = top + PADDING_Y;
            ctx.drawTextWithShadow(mc.textRenderer, e.title, textLeft, textTop, 0xFFFFFFFF);
            if (e.desc != null && !e.desc.isEmpty()) {
                ctx.drawTextWithShadow(mc.textRenderer, e.desc, textLeft, textTop + mc.textRenderer.fontHeight + 2, 0xFFCCCCCC);
            }

            if (e.phase == Phase.END && Math.abs(e.currentX) < 1f) {
                it.remove();
            }
        }
    }
}