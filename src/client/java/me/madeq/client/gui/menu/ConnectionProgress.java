package me.madeq.client.gui.menu;

public final class ConnectionProgress {
	public enum StageState {
		PENDING,
		ACTIVE,
		COMPLETED,
		SKIPPED
	}

	private static final ConnectionProgress INSTANCE = new ConnectionProgress();

	private final StageState[] stageStates = new StageState[ConnectionStage.values().length];
	private String serverName = "";
	private String serverAddress = "";
	private boolean active;
	private boolean encryptingReached;

	private ConnectionProgress() {
		reset();
	}

	public static ConnectionProgress get() {
		return INSTANCE;
	}

	public void begin(String serverName, String serverAddress) {
		reset();
		this.serverName = serverName == null ? "" : serverName;
		this.serverAddress = serverAddress == null ? "" : serverAddress;
		this.active = true;
		advanceTo(ConnectionStage.CONNECTING);
	}

	public void advanceTo(ConnectionStage stage) {
		if (!active) {
			return;
		}

		if (stage == ConnectionStage.ENCRYPTING) {
			encryptingReached = true;
		}

		int target = stage.ordinal();
		for (int index = 0; index < stageStates.length; index++) {
			if (index < target) {
				if (stageStates[index] == StageState.ACTIVE || stageStates[index] == StageState.PENDING) {
					stageStates[index] = StageState.COMPLETED;
				}
			} else if (index == target) {
				stageStates[index] = StageState.ACTIVE;
			}
		}

		maybeSkipEncrypting(target);
	}

	public void complete() {
		if (!active) {
			return;
		}

		for (int index = 0; index < stageStates.length; index++) {
			if (stageStates[index] == StageState.ACTIVE || stageStates[index] == StageState.PENDING) {
				stageStates[index] = StageState.COMPLETED;
			}
		}

		maybeSkipEncrypting(ConnectionStage.JOINING_WORLD.ordinal());
		active = false;
	}

	public void reset() {
		active = false;
		encryptingReached = false;
		serverName = "";
		serverAddress = "";

		for (int index = 0; index < stageStates.length; index++) {
			stageStates[index] = StageState.PENDING;
		}
	}

	public boolean isActive() {
		return active;
	}

	public String getServerName() {
		return serverName;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public StageState getState(ConnectionStage stage) {
		return stageStates[stage.ordinal()];
	}

	private void maybeSkipEncrypting(int targetIndex) {
		if (encryptingReached) {
			return;
		}

		if (targetIndex > ConnectionStage.ENCRYPTING.ordinal()
				&& stageStates[ConnectionStage.ENCRYPTING.ordinal()] == StageState.PENDING) {
			stageStates[ConnectionStage.ENCRYPTING.ordinal()] = StageState.SKIPPED;
		}
	}
}
