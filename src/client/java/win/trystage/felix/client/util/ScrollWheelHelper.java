package win.trystage.felix.client.util;

public class ScrollWheelHelper {
    private static double lastVerticalScroll = 0;
    private static long lastScrollTime = 0;
    private static boolean uiShowing = false;
    private static boolean middleClicked = false;
    private static long lastMiddleClickTime = 0;
    public static void updateScroll(double vertical) {
        lastVerticalScroll = vertical;
        lastScrollTime = System.currentTimeMillis();
    }
    public static void setUIShowing(boolean showing) {
        uiShowing = showing;
    }
    public static boolean isUIShowing() {
        return uiShowing;
    }
    public static double getLastVerticalScroll() {
        if (System.currentTimeMillis() - lastScrollTime > 200) {
            lastVerticalScroll = 0;
        }
        return lastVerticalScroll;
    }
    public static double consumeVerticalScroll() {
        double scroll = getLastVerticalScroll();
        lastVerticalScroll = 0;
        return scroll;
    }
    public static void updateMiddleClick() {
        middleClicked = true;
        lastMiddleClickTime = System.currentTimeMillis();
    }
    public static boolean hasMiddleClick() {
        if (System.currentTimeMillis() - lastMiddleClickTime > 100) {
            middleClicked = false;
        }
        return middleClicked;
    }
    public static boolean consumeMiddleClick() {
        boolean clicked = hasMiddleClick();
        middleClicked = false;
        return clicked;
    }
}