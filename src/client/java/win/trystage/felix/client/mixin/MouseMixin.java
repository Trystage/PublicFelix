package win.trystage.felix.client.mixin;

import net.minecraft.client.Mouse;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.trystage.felix.client.uis.UIS;
import win.trystage.felix.client.util.ScrollWheelHelper;
import win.trystage.felix.FelixClient.FelixClient;

@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void felix$captureMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        ScrollWheelHelper.updateScroll(vertical);
        FelixClient.BUS.call(new FelixClient.ClientInputEvent(
                FelixClient.ClientInputEvent.Action.SCROLL,
                MinecraftClient.getInstance(),
                0,
                vertical,
                System.currentTimeMillis(),
                UIS.isHudShowing()
        ));
        if (ScrollWheelHelper.isUIShowing()) {
            ci.cancel();
        }
    }
    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void felix$captureMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (button == 2 && action == 1) {
            ScrollWheelHelper.updateMiddleClick();
            FelixClient.BUS.call(new FelixClient.ClientInputEvent(
                    FelixClient.ClientInputEvent.Action.MIDDLE_CLICK,
                    MinecraftClient.getInstance(),
                    0,
                    0d,
                    System.currentTimeMillis(),
                    UIS.isHudShowing()
            ));
            if (ScrollWheelHelper.isUIShowing()) {
                ci.cancel();
            }
        }
    }
}