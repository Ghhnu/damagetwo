package com.realhealth.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Dibuja "❤ <vida actual en corazones>" encima del nombre de cada LivingEntity
 * visible (jugadores y mobs), tanto los tuyos como los de otros.
 *
 * No usa mixins ni toca clases internas del juego: solo lee entity.getHealth(),
 * que el servidor ya envía a tu cliente de forma automática (es el mismo dato
 * que usa el juego para la animación de "daño"). Por eso no hace falta ser
 * admin ni instalar nada en el servidor: todo pasa en tu propio cliente.
 */
public final class HealthRenderer {

    // Distancia máxima (en bloques) a la que se muestra el texto, para no
    // gastar rendimiento dibujando texto minúsculo muy lejos.
    private static final double MAX_DISTANCE_SQ = 48.0 * 48.0;

    private HealthRenderer() {
    }

    public static void register() {
        LevelRenderEvents.AFTER_ENTITIES.register(HealthRenderer::render);
    }

    private static void render(LevelRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        Camera camera = context.camera();
        Vec3 camPos = camera.getPosition();
        PoseStack poseStack = context.poseStack();
        MultiBufferSource bufferSource = context.bufferSource();
        Font font = mc.font;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }
            // No mostramos nuestra propia vida encima de nuestra cabeza:
            // ya la ves en la barra de corazones normal.
            if (living == mc.player) {
                continue;
            }
            if (!living.isAlive() || living.isInvisible()) {
                continue;
            }
            if (living.distanceToSqr(camPos.x, camPos.y, camPos.z) > MAX_DISTANCE_SQ) {
                continue;
            }

            drawHealth(living, camera, camPos, poseStack, bufferSource, font);
        }
    }

    private static void drawHealth(LivingEntity living, Camera camera, Vec3 camPos,
                                    PoseStack poseStack, MultiBufferSource bufferSource, Font font) {
        float health = Math.max(living.getHealth(), 0F);
        float maxHealth = Math.max(living.getMaxHealth(), 1F);
        float hearts = health / 2F;

        String text = "\u2665 " + formatHearts(hearts);

        float percent = health / maxHealth;
        int color;
        if (percent > 0.5F) {
            color = 0x55FF55; // verde
        } else if (percent > 0.25F) {
            color = 0xFFFF55; // amarillo
        } else {
            color = 0xFF5555; // rojo
        }

        // Posición justo por encima de donde el juego dibuja normalmente el
        // nombre (altura de la entidad + un pequeño margen extra).
        double x = living.getX() - camPos.x;
        double y = living.getY() - camPos.y + living.getBbHeight() + 0.75D;
        double z = living.getZ() - camPos.z;

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(camera.rotation()); // orienta el texto hacia la cámara
        poseStack.scale(-0.025F, -0.025F, 0.025F);

        Matrix4f matrix = poseStack.last().pose();
        float backgroundOpacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int background = (int) (backgroundOpacity * 255.0F) << 24;
        float halfWidth = -font.width(text) / 2F;

        font.drawInBatch(text, halfWidth, 0F, color, false, matrix, bufferSource,
                Font.DisplayMode.NORMAL, background, LightTexture.FULL_BRIGHT);

        poseStack.popPose();
    }

    private static String formatHearts(float hearts) {
        int rounded = Math.round(hearts);
        if (Math.abs(hearts - rounded) < 0.05F) {
            return String.valueOf(rounded);
        }
        return String.format("%.1f", hearts);
    }
}
