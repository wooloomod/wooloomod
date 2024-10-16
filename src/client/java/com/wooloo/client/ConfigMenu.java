package com.wooloo.client;

import com.wooloo.main.Options;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.json.JSONObject;

@Environment(EnvType.CLIENT)
public class ConfigMenu extends Screen {
	protected ConfigMenu() {
		super(Text.literal("[WOOLOOMOD] Config"));
	}
	public ButtonWidget button1;
	public TextFieldWidget button2;
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
		//button2 = new TextFieldWidget((Text.literal("API Key: ")) button -> {});
		addDrawableChild(button1);
	}
}
