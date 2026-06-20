package me.madeq.client.notify;

public enum NotificationType {
	SUCCESS("Success"),
	INFO("Info"),
	WARNING("Warning"),
	ERROR("Error");

	private final String label;

	NotificationType(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}
}
