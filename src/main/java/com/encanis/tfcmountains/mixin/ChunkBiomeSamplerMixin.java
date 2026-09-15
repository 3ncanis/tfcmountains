package com.encanis.tfcmountains.mixin;

import com.encanis.tfcmountains.world.noise.MountainBlendKernel;
import net.dries007.tfc.world.noise.Kernel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.dries007.tfc.world.ChunkBiomeSampler;

@Mixin(value = ChunkBiomeSampler.class, remap = false)
public abstract class ChunkBiomeSamplerMixin
{
    @Redirect(
            method = "sampleBiomes",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/dries007/tfc/world/ChunkBiomeSampler;KERNEL_9x9:Lnet/dries007/tfc/world/noise/Kernel;",
                    remap = false
            ),
            remap = false
    )
    private static Kernel tfcmountains$widerKernel()
    {
        return MountainBlendKernel.KERNEL;
    }
}