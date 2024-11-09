package com.wooloo.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class RaidDetection {
	private static RaidDetection instance;
	private boolean raidStatus;
	private int[] renderStatus = {0, 0, 0, 0};
	private int currentChallenge = 0;
	private Map<int[], String> wingsCoords = new HashMap<>();
	private RaidDetection() {
		raidStatus = false;
		// BLUE
		wingsCoords.put(new int[]{11100, 76, 3456}, "BLUE");
		wingsCoords.put(new int[]{11120, 76, 3500}, "BLUE");
		// RED
		wingsCoords.put(new int[]{10800, 76, 3450}, "RED");
		wingsCoords.put(new int[]{10825, 76, 3350}, "RED");
		wingsCoords.put(new int[]{10840, 76, 3515}, "RED");
		// YELLOW
		wingsCoords.put(new int[]{11025, 76, 3550}, "YELLOW");
		// GREEN
		wingsCoords.put(new int[]{10900, 76, 3416}, "GREEN");
		wingsCoords.put(new int[]{10877, 76, 3431}, "GREEN");
	}
	public static RaidDetection getInstance() {
		if(instance == null) {
			instance = new RaidDetection();
		}
		return instance;
	}
	public void setRaidStatus(boolean status) {
		raidStatus = status;
	}
	public boolean getRaidStatus() {
		return raidStatus;
	}
	public void setRenderStatus(int[] status) {
		renderStatus = status;
	}
	public int[] getRenderStatus() {
		return renderStatus;
	}
	public void setCurrentChallenge(int challenge) {
		currentChallenge = challenge;
	}
	public int getCurrentChallenge() {
		return currentChallenge;
	}
	public Map<int[], String> getWingsCoords() {
		return wingsCoords;
	}
}
