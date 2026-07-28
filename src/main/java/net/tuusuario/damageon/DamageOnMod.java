package net.tuusuario.damageon;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;

public class DamageOnMod implements ModInitializer {

    // Jugadores que tienen el modo activado
    private final Map<UUID, Boolean> enabledPlayers = new HashMap<>();
    // Cuentas atrás en curso (para no solaparlas)
    private final Map<UUID, CountdownTask> activeCountdowns = new HashMap<>();

    @Override
    public void onInitialize() {

        // --- Comando /damageon (funciona como interruptor: on/off) ---
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("damageon").executes(ctx -> {
                    ServerCommandSource source = ctx.getSource();
                    ServerPlayerEntity player = source.getPlayerOrThrow();
                    UUID id = player.getUuid();
                    boolean currentlyEnabled = enabledPlayers.getOrDefault(id, false);
                    enabledPlayers.put(id, !currentlyEnabled);

                    if (!currentlyEnabled) {
                        player.sendMessage(Text.literal(
                                "§c[DamageOn] ACTIVADO. Si recibes daño, el chunk se borrará en 7 segundos."), false);
                    } else {
                        player.sendMessage(Text.literal("§a[DamageOn] Desactivado."), false);
                    }
                    return 1;
                }))
        );

        // --- Detección de daño ---
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, damageSource, baseDamageTaken, damageTaken, blocked) -> {
            if (blocked) return;
            if (!(entity instanceof ServerPlayerEntity player)) return;

            UUID id = player.getUuid();
            if (!enabledPlayers.getOrDefault(id, false)) return;
            if (activeCountdowns.containsKey(id)) return; // ya hay una cuenta atrás en marcha para este jugador

            ServerWorld world = (ServerWorld) player.getWorld();
            ChunkPos chunkPos = player.getChunkPos();
            activeCountdowns.put(id, new CountdownTask(player, world, chunkPos));
        });

        // --- Tick del servidor: avanza las cuentas atrás activas ---
        ServerTickEvents.END_SERVER_TICK.register(server ->
                activeCountdowns.entrySet().removeIf(entry -> entry.getValue().tick())
        );
    }

    /**
     * Representa una cuenta atrás de 7 segundos para un jugador concreto:
     * muestra los límites del chunk con partículas y, al llegar a 0, borra el chunk.
     */
    private static class CountdownTask {
        private static final int TOTAL_TICKS = 7 * 20; // 7 segundos

        private final ServerPlayerEntity player;
        private final ServerWorld world;
        private final ChunkPos chunkPos;
        private int ticksLeft = TOTAL_TICKS;
        private int lastSecondShown = -1;

        CountdownTask(ServerPlayerEntity player, ServerWorld world, ChunkPos chunkPos) {
            this.player = player;
            this.world = world;
            this.chunkPos = chunkPos;
        }

        /** Se llama cada tick. Devuelve true cuando termina (para quitarla del mapa). */
        boolean tick() {
            int secondsLeft = (ticksLeft + 19) / 20;
            if (secondsLeft != lastSecondShown) {
                lastSecondShown = secondsLeft;
                player.sendMessage(Text.literal("§c[DamageOn] Borrando el chunk en " + secondsLeft + "..."), true);
            }

            showChunkBoundary();

            ticksLeft--;
            if (ticksLeft <= 0) {
                eraseChunk();
                player.sendMessage(Text.literal("§4[DamageOn] ¡El chunk ha sido borrado!"), false);
                return true;
            }
            return false;
        }

        /** Dibuja los límites verticales del chunk con partículas alrededor del jugador. */
        private void showChunkBoundary() {
            int minX = chunkPos.getStartX();
            int minZ = chunkPos.getStartZ();
            int maxX = chunkPos.getEndX();
            int maxZ = chunkPos.getEndZ();

            int playerY = player.getBlockY();
            int yStart = Math.max(world.getBottomY(), playerY - 8);
            int yEnd = Math.min(world.getTopY(), playerY + 12);

            for (int y = yStart; y <= yEnd; y++) {
                for (int x = minX; x <= maxX; x += 4) {
                    world.spawnParticles(ParticleTypes.FLAME, x + 0.5, y + 0.5, minZ + 0.5, 1, 0, 0, 0, 0);
                    world.spawnParticles(ParticleTypes.FLAME, x + 0.5, y + 0.5, maxZ + 0.5, 1, 0, 0, 0, 0);
                }
                for (int z = minZ; z <= maxZ; z += 4) {
                    world.spawnParticles(ParticleTypes.FLAME, minX + 0.5, y + 0.5, z + 0.5, 1, 0, 0, 0, 0);
                    world.spawnParticles(ParticleTypes.FLAME, maxX + 0.5, y + 0.5, z + 0.5, 1, 0, 0, 0, 0);
                }
            }
        }

        /** Vacía todo el chunk (todos los bloques, de abajo a arriba, se convierten en aire). */
        private void eraseChunk() {
            int minX = chunkPos.getStartX();
            int minZ = chunkPos.getStartZ();
            int bottomY = world.getBottomY();
            int topY = world.getTopY();

            BlockPos.Mutable mutable = new BlockPos.Mutable();
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = bottomY; y < topY; y++) {
                        mutable.set(minX + x, y, minZ + z);
                        world.setBlockState(mutable, Blocks.AIR.getDefaultState(), 3);
                    }
                }
            }
        }
    }
}
