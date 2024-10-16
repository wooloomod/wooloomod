package com.wooloo.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ParticleMixin {
	@Inject(method = "onParticle(Lnet/minecraft/network/packet/s2c/play/ParticleS2CPacket;)V", at = @At("RETURN"))
	public void afterOnParticle(ParticleS2CPacket packet, CallbackInfo ci) {
		if(packet.getParameters().getType() == ParticleTypes.FIREWORK) {
			MinecraftClient client = MinecraftClient.getInstance();
			if(client.player != null) {
				if(client.player.getX() > 10000 && client.player.getX() < 11000 && client.player.getZ() > 3500 && client.player.getZ() < 4500) {
					client.player.sendMessage(Text.literal("Coords: " + (int) packet.getX() + " " + (int) packet.getY() + " " + (int) packet.getZ()));
				}
			}
		}
	}
}
