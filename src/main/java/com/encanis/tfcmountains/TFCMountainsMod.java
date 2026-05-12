package com.encanis.tfcmountains;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod("tfcmountains")
public class TFCMountainsMod {
    public TFCMountainsMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, TFCMountainsConfig.SPEC);
    }
}