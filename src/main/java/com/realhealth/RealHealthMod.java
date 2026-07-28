package com.realhealth;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entrypoint "main" del mod. Este mod no necesita nada en el lado del servidor:
 * toda la lógica vive en el entrypoint de cliente (ver RealHealthClient).
 */
public class RealHealthMod implements ModInitializer {

    public static final String MOD_ID = "realhealth";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[RealHealth] Mod cargado (solo cliente, no requiere permisos de servidor).");
    }
}
