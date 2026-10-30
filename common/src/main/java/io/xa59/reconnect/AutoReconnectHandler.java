package io.xa59.reconnect;

import com.mojang.realmsclient.dto.RealmsServer;
import io.xa59.reconnect.utils.ClientCompatibility;
import io.xa59.reconnect.utils.RetrySchedule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

public final class AutoReconnectHandler {
    private static ServerData server;
    private static RealmsServer realm;
    private static RetrySchedule schedule;
    private static boolean startingRetry;

    public static void rememberServer(ServerData target) {
        if (!startingRetry) {
            server = target;
            realm = null;
            schedule = null;
        }
    }

    public static void rememberRealm(RealmsServer target) {
        if (!startingRetry) {
            server = null;
            realm = target;
            schedule = null;
        }
    }

    public static RetrySchedule disconnected() {
        if (server == null && realm == null) {
            return null;
        }
        if (schedule == null) {
            schedule = new RetrySchedule();
        }
        schedule.disconnected(System.nanoTime());
        return schedule;
    }

    public static void reconnect(Minecraft client, Screen parent, RetrySchedule current, boolean automatic) {
        if (current == null || schedule != current || !client.allowsMultiplayer() || !current.beginAttempt(automatic)) {
            return;
        }
        startingRetry = true;
        try {
            if (realm != null) {
                ClientCompatibility.playRealm(realm, parent);
            } else {
                ClientCompatibility.connect(client, parent, ServerAddress.parseString(server.ip), server);
            }
        } finally {
            startingRetry = false;
        }
    }

    public static void leaveDisconnected(RetrySchedule current) {
        if (schedule == current && current != null && !current.isConnecting()) {
            cancel();
        }
    }

    public static void connected(Minecraft client) {
        if (client.isLocalServer()) {
            cancel();
            return;
        }
        ServerData currentServer = client.getCurrentServer();
        if (currentServer != null && !ClientCompatibility.isRealm(client, currentServer)) {
            server = currentServer;
            realm = null;
        }
        schedule = null;
    }

    public static void cancel() {
        server = null;
        realm = null;
        schedule = null;
    }
}
