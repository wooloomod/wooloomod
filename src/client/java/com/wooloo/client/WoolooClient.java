package com.wooloo.client;

import com.wooloo.main.Options;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class WoolooClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("wooloo");
	public static final Options optionsInstance = Options.getInstance();
	private List<UUID> uuidList;
	private boolean durabilityWarning = true;
	private int tickCounter = 0;
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
		ClientTickEvents.END_WORLD_TICK.register(world -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if(client != null) {
				tickCounter++;
				if(tickCounter >= 300) {
					tickCounter = 0;
					durabilityWarning = !durabilityWarning;
				}
				if(durabilityWarning) {
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
									if(matcher.find()) {
										float curDurability = Integer.parseInt(matcher.group(1));
										float maxDurability = Integer.parseInt(matcher.group(2));
										if(curDurability/maxDurability <= .5) {
											client.player.sendMessage(Text.literal(itemStack.getName().getString() + " is low durability (" + (int) curDurability + "/" + (int) maxDurability + ")")
													.withColor(Formatting.RED.getColorValue()));
										}
									}
								}
							);
				}
			}
		});
	}
	private boolean ifInTooltip(ItemStack itemStack, String searchString) {
		List<Text> tooltip = itemStack.getTooltip(Item.TooltipContext.DEFAULT, MinecraftClient.getInstance().player, TooltipType.BASIC);
		return tooltip.stream()
				.anyMatch(text -> text.getString().toLowerCase().contains(searchString.toLowerCase()));
	}
}