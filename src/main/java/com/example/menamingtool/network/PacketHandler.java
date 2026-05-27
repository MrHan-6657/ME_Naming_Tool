package com.example.menamingtool.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class PacketHandler {

    public static final SimpleNetworkWrapper INSTANCE =
            NetworkRegistry.INSTANCE.newSimpleChannel("menamingtool");

    public static void init() {
        INSTANCE.registerMessage(
                PacketSyncTemplate.Handler.class,
                PacketSyncTemplate.class,
                0,
                Side.SERVER
        );
    }
}
