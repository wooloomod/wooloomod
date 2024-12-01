package com.wooloo.client.mixin;

import com.wooloo.client.RaidDetection;
import com.wooloo.main.Options;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ParticleMixin {
	@Unique
	private static final Options optionsInstance = Options.getInstance();
	@Unique
	private static final RaidDetection raidStatusInstance = RaidDetection.getInstance();
	@Inject(method = "onParticle(Lnet/minecraft/network/packet/s2c/play/ParticleS2CPacket;)V", at = @At("RETURN"))
	public void afterOnParticle(ParticleS2CPacket packet, CallbackInfo ci) {
		if(optionsInstance.getData().get("autowings").toString().equals("true")) {
			if (raidStatusInstance.getRaidStatus()) {
				if (packet.getParameters().getType() == ParticleTypes.WITCH) {
					MinecraftClient client = MinecraftClient.getInstance();
					if (client.player != null) {
						if (packet.getX() > 10000 && packet.getX() < 11000 && packet.getZ() > 3500 && packet.getZ() < 4500) {
							int[] renderStatus = {1, (int)packet.getX(), (int)packet.getY(), (int)packet.getZ()};
							raidStatusInstance.setRenderStatus(renderStatus);
						}
					}
				}
			}
		}
	}
}