package com.wooloo.client;

import com.wooloo.main.Options;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.json.JSONObject;

@Environment(EnvType.CLIENT)
public class ConfigMenu extends Screen {
	protected ConfigMenu() {
		super(Text.literal("[WOOLOOMOD] Config"));
	}
	public ButtonWidget button1;
	public ButtonWidget button2;
	public ButtonWidget button3;
	@Override
	protected void init() {
		Options instance = Options.getInstance();
		JSONObject options = instance.getData();
		button1 = ButtonWidget.builder(Text.literal("Anti Blindness: " + options.get("antiblindness").toString()), button -> {
			if(options.get("antiblindness").toString().equals("true")) {
				instance.setJsonValue("antiblindness", "false");
				button1.setMessage(Text.literal("Anti Blindness: false"));
			}
			else {
				instance.setJsonValue("antiblindness", "true");
				button1.setMessage(Text.literal("Anti Blindness: true"));
			}
		})
						.dimensions(width / 2 - 205, 20, 200, 20)
								.tooltip(Tooltip.of(Text.literal("Stops the blindness potion effect from rendering")))
										.build();
		addDrawableChild(button1);
		button2 = ButtonWidget.builder(Text.literal("Auto TCC Wings: " + options.get("autowings").toString()), button -> {
					if(options.get("autowings").toString().equals("true")) {
						instance.setJsonValue("autowings", "false");
						button2.setMessage(Text.literal("Auto TCC Wings: false"));
					}
					else {
						instance.setJsonValue("autowings", "true");
						button2.setMessage(Text.literal("Auto TCC Wings: true"));
					}
				})
				.dimensions(width / 2 - 205, 20 + 25, 200, 20)
				.tooltip(Tooltip.of(Text.literal("Gives you the location of the exit in TCC wings room")))
				.build();
		addDrawableChild(button2);
		button3 = ButtonWidget.builder(Text.literal("Durability Warning: " + options.get("durabilitywarning").toString()), button -> {
					if(options.get("durabilitywarning").toString().equals("true")) {
						instance.setJsonValue("durabilitywarning", "false");
						button3.setMessage(Text.literal("Durability Warning: false"));
					}
					else {
						instance.setJsonValue("durabilitywarning", "true");
						button3.setMessage(Text.literal("Durability Warning: true"));
					}
				})
				.dimensions(width / 2 - 205 + 200+10, 20, 200, 20)
				.tooltip(Tooltip.of(Text.literal("Warns you about low durability crafted items")))
				.build();
		addDrawableChild(button3);
	}
}
