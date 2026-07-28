package com.realhealth.client;

import net.fabricmc.api.ClientModInitializer;

/**
 * Entrypoint de cliente. Se ejecuta solo en tu juego, nunca en el servidor.
 */
public class RealHealthClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HealthRenderer.register();
    }
}
