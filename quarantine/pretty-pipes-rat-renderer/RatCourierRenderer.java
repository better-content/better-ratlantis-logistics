package com.bettercontent.betterratlantislogistics.client;

import com.github.alexthe666.rats.registry.RatsEntityRegistry;
import com.github.alexthe666.rats.server.entity.rat.Rat;
import com.github.alexthe666.rats.server.misc.RatVariant;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.ellpeck.prettypipes.network.PipeItem;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.Map;
import java.util.WeakHashMap;

/** Client-only visual substitution. Cached rats are never added to a Level. */
public final class RatCourierRenderer {
    private static final Map<PipeItem, Rat> CACHE = new WeakHashMap<>();

    private RatCourierRenderer() {}

    public static void render(PipeItem packet, PipeBlockEntity pipe, PoseStack pose, float partialTicks, int light, MultiBufferSource buffers) {
        if (pipe.getLevel() == null || packet.stack.isEmpty()) return;
        Rat rat = CACHE.computeIfAbsent(packet, key -> {
            Rat created = RatsEntityRegistry.RAT.get().create(pipe.getLevel());
            if (created != null) {
                long seed = 31L * System.identityHashCode(packet) + packet.stack.getItem().hashCode() + packet.stack.getDamageValue();
                created.setColorVariant(RatVariant.getRandomVariant(RandomSource.create(seed), false));
                created.setNoAi(true);
                created.setSilent(true);
            }
            return created;
        });
        if (rat == null) return;
        rat.setItemSlot(EquipmentSlot.MAINHAND, packet.stack.copy());

        float x = Mth.lerp(partialTicks, packet.lastX, packet.x);
        float y = Mth.lerp(partialTicks, packet.lastY, packet.y);
        float z = Mth.lerp(partialTicks, packet.lastZ, packet.z);
        float dx = packet.x - packet.lastX;
        float dy = packet.y - packet.lastY;
        float dz = packet.z - packet.lastZ;
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;

        pose.pushPose();
        pose.translate(x, y - 0.18F, z);
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        if (Math.abs(dy) > Math.max(Math.abs(dx), Math.abs(dz))) {
            pose.mulPose(Axis.XP.rotationDegrees(dy > 0 ? -90.0F : 90.0F));
        }
        pose.scale(0.42F, 0.42F, 0.42F);
        rat.setYRot(yaw);
        rat.yRotO = yaw;
        Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(rat).render(rat, yaw, partialTicks, pose, buffers, light);
        pose.popPose();
    }
}
