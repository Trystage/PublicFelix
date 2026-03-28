package win.trystage.felix.client.uis;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.RotationAxis;
import org.lwjgl.glfw.GLFW;

import win.trystage.felix.client.ui.Notifications;
import win.trystage.felix.FelixClient.FelixClient;
import win.trystage.felix.client.core.RawSound;
import win.trystage.felix.client.event.EventManager;
import win.trystage.felix.client.ui.features.MenuFeature;
import win.trystage.felix.client.ui.features.Features;
import win.trystage.felix.client.util.ScrollWheelHelper;
import win.trystage.felix.client.util.AntiBot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UIS{
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static String name;
    private static boolean showHud;
    private static int selectedMainMenuIndex = 0;
    private static int selectedSubMenuIndex = 0;
    private static boolean showSubMenu = false;
    private static long lastResetKeyPressTime = 0;
    private static long lastSelectKeyPressTime = 0;
    private static long lastDirectionKeyPressTime = 0;
    private static final long RESET_KEY_DELAY = 500;
    private static final long SELECT_KEY_DELAY = 500;
    private static final long DIRECTION_KEY_DELAY = 250;
    private static final int MENU_WIDTH = 120;
    private static final int MENU_ITEM_HEIGHT = 20;
    private static final int MENU_SPACING = 2;
    private static final String CONTROLS_HINT = "↑↓/滚轮:选择 →/中键:确认 ←:返回";
    private static final List<String> MAIN_MENU_OPTIONS = List.of(
            "传送玩家",
            "封禁玩家",
            "禁言玩家",
            "查询历史",
            "举报玩家",
            "答疑解惑",
            "提醒警告"
    );
    private static final Map<String, List<String>> SUB_MENU_OPTIONS = new HashMap<>();
    private static final Map<String, MenuFeature> FEATURE_REGISTRY = new HashMap<>();
    private static String currentFunction = "";
    private static java.util.UUID targetUuid;
    private static String hudLabel = "";
    private static long lastScrollTime = 0;
    private static final long SCROLL_DELAY = 1;
    private static long lastMiddleClickTime = 250;
    private static void initFeatures() {
        if (!FEATURE_REGISTRY.isEmpty()) return;
        registerFeature("传送玩家", Features.teleport());
        registerFeature("封禁玩家", Features.ban());
        registerFeature("禁言玩家", Features.mute());
        registerFeature("查询历史", Features.history());
        registerFeature("举报玩家", Features.report());
        registerFeature("答疑解惑", Features.qna());
        registerFeature("提醒警告", Features.warn());
    }
    private static void registerFeature(String name, MenuFeature feature) {
        FEATURE_REGISTRY.put(name, feature);
        SUB_MENU_OPTIONS.put(name, feature.options());
    }
    public static void register() {
        initFeatures();
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (showHud) {
                renderPlayerHud(drawContext, tickDelta);
            }
        });
    }

    public static boolean isHudShowing() {
        return showHud;
    }
    public static final class InputSubscriber {
        @EventManager.EventTarget
        public void onInput(FelixClient.ClientInputEvent e) {
            switch (e.action()) {
                case SELECT_KEY -> handleSelectKey(e.client());
                case RESET_KEY -> handleResetKey(e.client());
                case NAVIGATION_TICK -> processNavigationTick(e.client());
                case SCROLL, MIDDLE_CLICK -> {
                }
            }
        }
    }
    public static void handleSelectKey(MinecraftClient client) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSelectKeyPressTime < SELECT_KEY_DELAY) return;
        HitResult hitResult = client.crosshairTarget;
        boolean shouldToggleHud = false;
        if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) hitResult).getEntity();
            if (entity instanceof PlayerEntity player) {
                String playerName = player.getName().getString();
                if (playerName.indexOf('\u00A7') >= 0 || playerName.contains("§")) {
                    Notifications.notify("not_player", "这不是玩家", "选择的实体不是玩家", Notifications.Type.WARNING);
                    RawSound.playFelix("info.wav", 1.0f);
                } else {
                    if (!isInTabList(playerName)) {
                        Notifications.notify("not_player", "这不是玩家", "选择的实体不是玩家", Notifications.Type.WARNING);
                        RawSound.playFelix("info.wav", 1.0f);
                        showHud = false;
                        ScrollWheelHelper.setUIShowing(false);
                        lastSelectKeyPressTime = currentTime;
                        return;
                    }
                    Notifications.notify("selected", "目标玩家选取为", playerName, Notifications.Type.SUCCESS);
                    name = player.getName().getString();
                    targetUuid = player.getUuid();
                    hudLabel = "目标: " + name;
                    RawSound.playFelix("enable.wav", 1.0f);
                    resetMenuState();
                    showHud = false;
                    shouldToggleHud = true;
                }
            } else {
                Notifications.notify("not_player", "这不是玩家", "选择的实体不是玩家", Notifications.Type.WARNING);
                RawSound.playFelix("info.wav", 1.0f);
                showHud = false;
                ScrollWheelHelper.setUIShowing(false);
            }
        } else {
            Notifications.notify("not_found", "未找到目标", "请将十字准心对准玩家", Notifications.Type.WARNING);
            RawSound.playFelix("info.wav", 1.0f);
            showHud = false;
            ScrollWheelHelper.setUIShowing(false);
        }
        if (shouldToggleHud) {
            showHud = !showHud;
            if (showHud) {
                RawSound.playFelix("enable.wav", 1.0f);
            } else {
                RawSound.playFelix("disable.wav", 1.0f);
            }
            ScrollWheelHelper.setUIShowing(showHud);
        }
        lastSelectKeyPressTime = currentTime;
    }
    public static void handleResetKey(MinecraftClient client) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastResetKeyPressTime < RESET_KEY_DELAY) return;
        Notifications.notify("reset", "目标已重置", "已清除选择的目标", Notifications.Type.INFO);
        RawSound.playFelix("disable.wav", 1.0f);
        if (mc.player != null) {
            name = mc.player.getName().getString();
            targetUuid = null;
            hudLabel = "目标: " + name;
        }
        showHud = false;
        RawSound.playFelix("disable.wav", 1.0f);
        ScrollWheelHelper.setUIShowing(false);
        lastResetKeyPressTime = currentTime;
    }
    private static void handleMenuNavigation(MinecraftClient client) {
        if (!showHud || client.currentScreen != null) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        long window = mc.getWindow().getHandle();
        if (InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_UP)) {
            if (currentTime - lastDirectionKeyPressTime >= DIRECTION_KEY_DELAY) {
                navigateMenu(-1);
                playNavigationSound();
                lastDirectionKeyPressTime = currentTime;
            }
        }
        if (InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_DOWN)) {
            if (currentTime - lastDirectionKeyPressTime >= DIRECTION_KEY_DELAY) {
                navigateMenu(1);
                playNavigationSound();
                lastDirectionKeyPressTime = currentTime;
            }
        }
        if (InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT)) {
            if (currentTime - lastDirectionKeyPressTime >= DIRECTION_KEY_DELAY) {
                selectMenuOption(client);
                playSelectSound();
                lastDirectionKeyPressTime = currentTime;
            }
        }
        if (ScrollWheelHelper.consumeMiddleClick()) {
            if (currentTime - lastMiddleClickTime >= DIRECTION_KEY_DELAY) {
                selectMenuOption(client);
                playSelectSound();
                lastMiddleClickTime = currentTime;
            }
        }
        if (InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT)) {
            if (showSubMenu) {
                if (currentTime - lastDirectionKeyPressTime >= DIRECTION_KEY_DELAY) {
                    showSubMenu = false;
                    playNavigationSound();
                    lastDirectionKeyPressTime = currentTime;
                }
            }
        }
        double scrollDelta = ScrollWheelHelper.consumeVerticalScroll();
        if (Math.abs(scrollDelta) > 0.1 && currentTime - lastScrollTime >= SCROLL_DELAY) {
            if (scrollDelta > 0) {
                navigateMenu(-1);
            } else {
                navigateMenu(1);
            }
            playNavigationSound();
            lastScrollTime = currentTime;
        }
    }
    public static void processNavigationTick(MinecraftClient client) {
        handleMenuNavigation(client);
    }
    private static void navigateMenu(int direction) {
        if (showSubMenu) {
            List<String> subOptions = SUB_MENU_OPTIONS.get(currentFunction);
            selectedSubMenuIndex = (selectedSubMenuIndex + direction + subOptions.size()) % subOptions.size();
        } else {
            selectedMainMenuIndex = (selectedMainMenuIndex + direction + MAIN_MENU_OPTIONS.size()) % MAIN_MENU_OPTIONS.size();
        }
    }
    private static void selectMenuOption(MinecraftClient client) {
        if (showSubMenu) {
            executeMenuAction(currentFunction, selectedSubMenuIndex,client);
        } else {
            currentFunction = MAIN_MENU_OPTIONS.get(selectedMainMenuIndex);
            selectedSubMenuIndex = 0;
            showSubMenu = true;
        }
    }
    private static void executeMenuAction(String function, int optionIndex,MinecraftClient client) {
        List<String> options = SUB_MENU_OPTIONS.get(function);
        String option = options.get(optionIndex);
        if (option.equals("取消") || option.equals("返回")) {
            showSubMenu = false;
            return;
        }
        MenuFeature feature = FEATURE_REGISTRY.get(function);
        if (feature != null) {
            feature.execute(optionIndex, name, client);
        }
        showSubMenu = false;
    }
    private static void playNavigationSound() {
        mc.getSoundManager().play(PositionedSoundInstance.master(
                SoundEvents.UI_BUTTON_CLICK.value(),
                1.0f,
                1.5f
        ));
    }
    private static void playSelectSound() {
        mc.getSoundManager().play(PositionedSoundInstance.master(
                SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                1.0f,
                1.0f
        ));
    }
    private static void resetMenuState() {
        selectedMainMenuIndex = 0;
        selectedSubMenuIndex = 0;
        showSubMenu = false;
        currentFunction = "";
    }
    private static void renderPlayerHud(DrawContext context,float tickDelta) {
        EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();
        int x = 60, y = 50, size = 50;
        String text = hudLabel != null ? hudLabel : ("目标: " + name);
        context.drawCenteredTextWithShadow(
                mc.textRenderer,
                text,
                x + size, y - 4,
                0xFFFFFF
        );
        context.fill(x, y - 10, x + (size * 2), y + 10, 0x80000000);
        x += size;
        y += 10 + size;
        PlayerEntity player = null;
        if (mc.world != null) {
            if (targetUuid != null) {
                player = mc.world.getPlayerByUuid(targetUuid);
            }
            if (player == null) {
                player = mc.world.getPlayers().stream().filter((p) -> p.getName().getString().equalsIgnoreCase(name)).findFirst().orElse(null);
            }
        }
        if (player == null) {
            player = mc.player;
        }
        float rotationYaw = 0;
        if (player != null) {
            rotationYaw = player.getYaw();
        }
        context.getMatrices().push();
        context.getMatrices().translate(x, y + 20 + size, 0);
        context.getMatrices().multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        context.getMatrices().scale(size, size, size);
        renderManager.render(
                player,
                0, 0, 0,
                rotationYaw,
                tickDelta,
                context.getMatrices(),
                context.getVertexConsumers(),
                LightmapTextureManager.MAX_LIGHT_COORDINATE
        );
        context.getMatrices().pop();
        renderMenuSystem(context, 60 + size + size + 2);
    }
    private static void renderMenuSystem(DrawContext context, int x) {
        int menuWidth = MENU_WIDTH;
        int itemHeight = MENU_ITEM_HEIGHT;
        int spacing = MENU_SPACING;
        List<String> menuToRender;
        int selectedIndex;
        if (showSubMenu) {
            menuToRender = SUB_MENU_OPTIONS.get(currentFunction);
            selectedIndex = selectedSubMenuIndex;
            context.fill(x, 65 - 25, x + menuWidth, 65 - 5, 0x80000000);
            context.drawCenteredTextWithShadow(
                    mc.textRenderer,
                    currentFunction,
                    x + menuWidth / 2,
                    65 - 20,
                    0xFFFFFF
            );
        } else {
            menuToRender = MAIN_MENU_OPTIONS;
            selectedIndex = selectedMainMenuIndex;
            context.fill(x, 65 - 25, x + menuWidth, 65 - 5, 0x80000000);
            context.drawCenteredTextWithShadow(
                    mc.textRenderer,
                    "功能菜单",
                    x + menuWidth / 2,
                    65 - 20,
                    0xFFFFFF
            );
        }
        int totalHeight = menuToRender.size() * (itemHeight + spacing);
        context.fill(x, 65, x + menuWidth, 65 + totalHeight, 0x80000000);
        for (int i = 0; i < menuToRender.size(); i++) {
            int itemY = 65 + i * (itemHeight + spacing);
            if (i == selectedIndex) {
                context.fill(x + 2, itemY + 2, x + menuWidth - 2, itemY + itemHeight - 2, 0x805090FF);
            }
            String displayText = menuToRender.get(i);
            int textColor = (i == selectedIndex) ? 0xFFFFFF : 0xAAAAAA;
            context.drawTextWithShadow(
                    mc.textRenderer,
                    displayText,
                    x + 10,
                    itemY + 6,
                    textColor
            );
        }
        context.drawTextWithShadow(
                mc.textRenderer,
                CONTROLS_HINT,
                x,
                65 + totalHeight + 5,
                0xAAAAAA
        );
    }
    private static boolean isInTabList(String rawName) {
        return AntiBot.isInTabList(rawName);
    }
}