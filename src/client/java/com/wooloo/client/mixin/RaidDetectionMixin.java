package com.wooloo.client.mixin;

import com.wooloo.client.RaidDetection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(InGameHud.class)
public class RaidDetectionMixin {
	@Unique
	private static final RaidDetection raidStatusInstance = RaidDetection.getInstance();
	@Inject(method="renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at=@At("HEAD"))
	private void onRenderScoreboard(DrawContext drawContext, ScoreboardObjective objective, CallbackInfo ci) {
		if(objective != null) {
			Scoreboard scoreboard = MinecraftClient.getInstance().world.getScoreboard();
			Collection<ScoreboardEntry> entries = scoreboard.getScoreboardEntries(objective);
			long raidCount = entries.stream()
					.filter(entry -> entry.name().getString().contains("Raid"))
					.count();
			raidStatusInstance.setRaidStatus(raidCount > 0);
			if(raidCount > 0) {
				AtomicInteger challenge = new AtomicInteger();
				Pattern pattern = Pattern.compile("Challenges: (\\d+)/(\\d+)");
				entries.stream()
						.filter(entry -> entry.name().getString().contains("Challenges:"))
						.forEach(entry -> {
							Matcher matcher = pattern.matcher(Formatting.strip(entry.name().getString()));
							if(matcher.find()) {
								challenge.set(Integer.parseInt(matcher.group(1)));
							}
						});
				raidStatusInstance.setCurrentChallenge(challenge.get());
			}
			if(!raidStatusInstance.getRaidStatus()) {
				int[] x = {0, 0, 0, 0};
				raidStatusInstance.setRenderStatus(x);
			}
		}
	}
}
