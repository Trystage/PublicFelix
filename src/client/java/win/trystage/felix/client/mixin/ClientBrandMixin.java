package win.trystage.felix.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.ClientBrandRetriever;

@Mixin(ClientBrandRetriever.class)
public class ClientBrandMixin {
    @Unique
    private static final String CUSTOM_CLIENT_BRAND = "FelixClient";
    @Inject(
            method = "getClientModName",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void modifyClientBrand(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(CUSTOM_CLIENT_BRAND);
    }
}