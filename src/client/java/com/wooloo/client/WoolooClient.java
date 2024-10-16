package com.wooloo.client;

import com.wooloo.main.Options;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WoolooClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("wooloo");
	public static final Options optionsInstance = Options.getInstance();
	private List<UUID> uuidList;
	@Override
	public void onInitializeClient() {
		this.uuidList = new ArrayList<>();
		HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			ClientPlayerEntity player = client.player;
			JSONObject options = optionsInstance.getData();
			if(options.get("antiblindness").toString().equals("true")) {
				if (player != null && player.hasStatusEffect(StatusEffects.BLINDNESS)) {
					player.removeStatusEffectInternal(StatusEffects.BLINDNESS);
				}
			}
		});
		ClientLifecycleEvents.CLIENT_STOPPING.register((matrixStack) -> {
			if(optionsInstance.getState()) {
				JSONObject options = optionsInstance.getData();
				LOGGER.info("[WOOLOOMOD] Saving config...");
				try {
					FileWriter fw = new FileWriter(optionsInstance.getFilePath());
					fw.write(options.toString());
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				LOGGER.info("[WOOLOOMOD] Config saved.");
			}
		});
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("wooloomod")
				.executes(context -> {
							MinecraftClient client = context.getSource().getClient();
							client.send(() -> client.setScreen(new ConfigMenu()));
							return 1;
						}
				)));
		/*ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("peek")
				.then(ClientCommandManager.argument("player", StringArgumentType.string())
				.executes(context -> {
							String playerName = StringArgumentType.getString(context, "player");
							MinecraftClient client = context.getSource().getClient();
							PlayerEntity playerObj = client.world.getPlayers().stream()
									.filter(player -> player.getName().getString().equalsIgnoreCase(playerName))
									.findFirst()
									.orElse(null);
							if(playerObj == null) {
								context.getSource().sendFeedback(Text.literal("Peek failed, " + playerName + " is not in your world."));
								return 1;
							}
							context.getSource().sendFeedback(Text.literal("Peeking " + playerName));
							client.send(() -> client.setScreen(new InventoryScreen(playerObj)));
							playerObj.getInventory().armor.stream()
									.filter(itemStack -> !itemStack.isEmpty())
									.forEach(itemStack -> {
										context.getSource().sendFeedback(Text.literal(itemStack.));
									});
							return 1;
						}
				))));*/
	}
}