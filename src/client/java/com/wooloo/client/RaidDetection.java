package com.wooloo.client;

public final class RaidDetection {
	private static RaidDetection instance;
	private boolean raidStatus;
	private int[] renderStatus = {0, 0, 0, 0};
	private int currentChallenge = 0;
	private RaidDetection() {
		raidStatus = false;
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
}
