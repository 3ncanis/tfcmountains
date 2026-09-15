package com.encanis.tfcmountains.mixin;

import com.encanis.tfcmountains.TFCMountainsConfig;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.dries007.tfc.world.region.AddMountainsAndBarrierIslands;
import net.dries007.tfc.world.region.Region;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.BitSet;

@Mixin(value = AddMountainsAndBarrierIslands.class, remap = false)
public abstract class AddMountainsAndBarrierIslandsMixin
{
    @Invoker("placeRange")
    abstract IntSet tfcmountains$callPlaceRange(Region region, RandomSource random, int originIndex);

    @Redirect(
            method = "apply",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/dries007/tfc/world/region/AddMountainsAndBarrierIslands;placeRange(Lnet/dries007/tfc/world/region/Region;Lnet/minecraft/util/RandomSource;I)Lit/unimi/dsi/fastutil/ints/IntSet;",
                    remap = false
            ),
            remap = false
    )
    private IntSet tfcmountains$placeRange(AddMountainsAndBarrierIslands self, Region region, RandomSource random, int originIndex)
    {
        if (!TFCMountainsConfig.EXTENDED_MOUNTAIN_RANGES.get())
        {
            return tfcmountains$callPlaceRange(region, random, originIndex);
        }

        final BitSet explored = new BitSet(region.size());
        final IntArrayFIFOQueue queue = new IntArrayFIFOQueue();
        final IntSet range = new IntOpenHashSet();

        queue.enqueue(originIndex);
        explored.set(originIndex);
        range.add(originIndex);

        final int originBaseLandHeight = Math.max(1, region.atIndex(originIndex).baseLandHeight);
        final double lengthMultiplier = TFCMountainsConfig.EXTENDED_MOUNTAIN_RANGES_MULTIPLIER.get();
        final int maxSize = (int) Math.round((70 + random.nextInt(40)) * lengthMultiplier);
        final int heightTolerance = Math.max(1, (int) Math.round(lengthMultiplier));

        while (!queue.isEmpty())
        {
            final int last = queue.dequeueInt();
            final Region.Point lastPoint = region.atIndex(last);
            if (range.size() > maxSize)
            {
                break;
            }

            for (int dx = -1; dx <= 1; dx++)
            {
                for (int dz = -1; dz <= 1; dz++)
                {
                    final @Nullable Region.Point point = region.atOffset(last, dx, dz);

                    if (point != null &&
                            point.land() &&
                            point.baseLandHeight >= originBaseLandHeight - heightTolerance &&
                            point.baseLandHeight <= originBaseLandHeight + heightTolerance &&
                            (point.baseLandHeight > 2 || point.distanceToOcean < 3) &&
                            !explored.get(point.index))
                    {
                        if (lastPoint.baseLandHeight != point.baseLandHeight)
                        {
                            queue.enqueue(point.index);
                        }
                        else
                        {
                            queue.enqueueFirst(point.index);
                        }
                        range.add(point.index);
                        explored.set(point.index);
                    }
                }
            }
        }

        return range;
    }
}