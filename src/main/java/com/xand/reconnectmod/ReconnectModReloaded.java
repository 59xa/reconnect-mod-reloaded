package com.xand.reconnectmod;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReconnectModReloaded implements ModInitializer {
	public static final String MOD_ID = "reconnectmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";

	@Override
	public void onInitialize() {
		LOGGER.info(ANSI_GREEN + "Reconnect Mod Reloaded " + ANSI_YELLOW + "SUCCESSFULLY INITIALISED!");
	}
}