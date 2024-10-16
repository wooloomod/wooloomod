package com.wooloo.main;

import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public final class Options {
	private final String json = "{\"antiblindness\":\"true\",\"wooloo\":\"true\"}";
	private final String filepath = "WoolooMod/optionswooloomod.json";
	private static Options instance;
	private boolean changed = false;
	public JSONObject options;
	private Options() {
		options = readOptions();
	}
	public static Options getInstance() {
		if(instance == null) {
			instance = new Options();
		}
		return instance;
	}
	public JSONObject getData() {
		return options;
	}
	public String getFilePath() {
		return filepath;
	}
	public boolean getState() {
		if(changed) {
			return true;
		}
		return false;
	}
	public boolean setJsonValue(String key, String value) {
		if(options.has(key)) {
			options.put(key, value);
			changed = true;
			return true;
		}
		return false;
	}
	public JSONObject readOptions() {
		File f1 = new File("WoolooMod");
		File f2 = new File(filepath);
		try {
			if(f1.mkdir() || f2.createNewFile()) {
				FileWriter fw = new FileWriter(filepath);
				fw.write(json);
				fw.close();
				return new JSONObject(json);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
		try {
			FileReader fr = new FileReader(filepath);
			int i;
			while((i = fr.read()) != -1) {
				sb.append((char) i);
			}
			fr.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return new JSONObject(sb.toString());
	}
}
