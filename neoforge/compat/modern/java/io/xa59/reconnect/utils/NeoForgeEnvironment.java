package io.xa59.reconnect.utils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public final class NeoForgeEnvironment {
    public static boolean isClient() {
        return FMLEnvironment.getDist() == Dist.CLIENT;
    }
}
