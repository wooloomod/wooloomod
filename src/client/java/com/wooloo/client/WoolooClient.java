package com.wooloo.client;

import com.wooloo.main.Options;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class WoolooClient implements ClientModInitializer {
	public static final String currentVersion = FabricLoader.getInstance().getModContainer("wooloo")
			.map(container -> container.getMetadata().getVersion().getFriendlyString())
			.orElse("1.0.0");
	public static final Logger LOGGER = LoggerFactory.getLogger("wooloo");
	public static final Options optionsInstance = Options.getInstance();
	public static final RaidDetection raidStatusInstance = RaidDetection.getInstance();
	private boolean durabilityWarning = true;
	private int tickCounter = 0;
	@Override
	public void onInitializeClient() {
		ClientLifecycleEvents.CLIENT_STARTED.register((matrixStack) -> {
			JSONObject options = optionsInstance.getData();
			if(!currentVersion.equals(options.get("version"))) {
				optionsInstance.updateVersion(currentVersion);
			}
		});
		HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			ClientPlayerEntity player = client.player;
			int[] renderStatus = raidStatusInstance.getRenderStatus();
			double dist = Double.MAX_VALUE;
			String colorText = "";
			if(renderStatus[0] == 1 && raidStatusInstance.getRaidStatus() && raidStatusInstance.getCurrentChallenge() == 1) {
				float x = 10;
				float y = client.getWindow().getScaledHeight() - 10 - client.textRenderer.fontHeight;
				int color = 0x808080;
				Map<int[], String> wingsCoords = raidStatusInstance.getWingsCoords();
				double minDistance = Double.MAX_VALUE;
				for(Map.Entry<int[], String> entry : wingsCoords.entrySet()) {
					int[] keyArray = entry.getKey();
					String value = entry.getValue();
					dist = distance(keyArray, new int[]{renderStatus[1], renderStatus[3]});
					if(dist <= minDistance) {
						minDistance = dist;
						colorText = value;
					}
				}
				if(dist <= 10) {
					renderText(client, colorText + ": " + renderStatus[1] + " " + renderStatus[2] + " " + renderStatus[3], x, y, color);
				}
				else {
					renderText(client, "Wings: " + renderStatus[1] + " " + renderStatus[2] + " " + renderStatus[3], x, y, color);
				}
			}
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
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("woolootest")
						.executes(context -> {
							MinecraftClient client = context.getSource().getClient();
							int[] r = raidStatusInstance.getRenderStatus();
							boolean rStatus = raidStatusInstance.getRaidStatus();
							client.player.sendMessage(Text.literal("[WOOLOOMOD] renderStatus: " + r[0] + " x: " + r[1] + " y: " + r[2] + " z: " + r[3] + "\n" + "raidStatus: " + rStatus));
							return 1;
						}
				)));
		ClientTickEvents.END_WORLD_TICK.register(world -> {
			JSONObject options = optionsInstance.getData();
			if(options.get("durabilitywarning").toString().equals("true")) {
				MinecraftClient client = MinecraftClient.getInstance();
				if (client != null) {
					tickCounter++;
					if (tickCounter >= 300) {
						tickCounter = 0;
						durabilityWarning = !durabilityWarning;
					}
					if (durabilityWarning) {
						durabilityWarning = !durabilityWarning;
						DefaultedList<ItemStack> mainInv = client.player.getInventory().main;
						DefaultedList<ItemStack> armorInv = client.player.getInventory().armor;
						Pattern pattern = Pattern.compile("(\\d+)/(\\d+) Durability");
						Stream<ItemStack> invStream = Stream.concat(mainInv.stream(), armorInv.stream());
						invStream.filter(itemStack -> !itemStack.isEmpty())
								.filter(itemStack -> ifInTooltip(itemStack, "Crafted") && ifInTooltip(itemStack, "Durability"))
								.forEach(itemStack -> {
											List<Text> tooltip = itemStack.getTooltip(Item.TooltipContext.DEFAULT, MinecraftClient.getInstance().player, TooltipType.BASIC);
											String lastLine = tooltip.getLast().getString();
											Matcher matcher = pattern.matcher(lastLine);
											if (matcher.find()) {
												float curDurability = Integer.parseInt(matcher.group(1));
												float maxDurability = Integer.parseInt(matcher.group(2));
												if (curDurability / maxDurability <= .5) {
													client.player.sendMessage(Text.literal(itemStack.getName().getString() + " is low durability (" + (int) curDurability + "/" + (int) maxDurability + ")")
															.withColor(Formatting.RED.getColorValue()));
												}
											}
										}
								);
					}
				}
			}
			});
		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			if(message.getString().contains("Raid Failed") || message.getString().contains("Raid Completed")) {
				raidStatusInstance.setRaidStatus(false);
				raidStatusInstance.setCurrentChallenge(1);
			}
		});
	}
	private boolean ifInTooltip(ItemStack itemStack, String searchString) {
		List<Text> tooltip = itemStack.getTooltip(Item.TooltipContext.DEFAULT, MinecraftClient.getInstance().player, TooltipType.BASIC);
		return tooltip.stream()
				.anyMatch(text -> text.getString().toLowerCase().contains(searchString.toLowerCase()));
	}
	private void renderText(MinecraftClient client, String message, float x, float y, int color) {
		TextRenderer textRenderer = client.textRenderer;
		MatrixStack matrixStack = new MatrixStack();
		textRenderer.draw(Text.literal(message), x, y, color, false, matrixStack.peek().getPositionMatrix(), client.getBufferBuilders().getEntityVertexConsumers(), TextRenderer.TextLayerType.NORMAL, 0, 15728880);
	}
	private double distance(int[] d1, int[] d2) {
		double sum = 0;
		for(int i = 0; i < d1.length; i++) {
			sum += Math.pow(d1[i] - d2[i], 2);
		}
		return Math.sqrt(sum);
	}
}