package com.encanis.tfcmountains.mixin;

import com.encanis.tfcmountains.TFCMountainsConfig;
import net.dries007.tfc.world.region.Region;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Region.Point.class, remap = false)
public class RegionPointDiscreteBiomeAltitudeMixin
{
    @Inject(method = "discreteBiomeAltitude", at = @At("HEAD"), cancellable = true)
    private void tfcmountains$useConfigWidth(CallbackInfoReturnable<Integer> cir)
    {
        final int width = TFCMountainsConfig.FOOTHILL_WIDTH.get();
        if (width == 4)
            return;

        cir.setReturnValue(Math.floorDiv(((Region.Point) (Object) this).biomeAltitude, width));
    }
}