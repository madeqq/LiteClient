package me.madeq.client.gui.menu;

public enum ConnectionStage {
	CONNECTING("Connecting to server"),
	HANDSHAKE("Handshake"),
	AUTHENTICATING("Authenticating"),
	ENCRYPTING("Encrypting session"),
	LOGGING_IN("Logging in"),
	JOINING_WORLD("Joining world");

	private final String label;

	ConnectionStage(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}
}
