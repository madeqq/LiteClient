package me.madeq.client.alt;

import java.util.UUID;

public class AltProfile {
	private String name;
	private String uuid;
	private AltType type;
	private long lastUsed;
	private String authData;

	private AltProfile() {
	}

	public AltProfile(String name, UUID uuid, AltType type) {
		this.name = name;
		this.uuid = uuid.toString();
		this.type = type;
		this.lastUsed = System.currentTimeMillis();
	}

	public static AltProfile microsoft(String name, UUID uuid, String authData) {
		AltProfile profile = new AltProfile(name, uuid, AltType.MICROSOFT);
		profile.authData = authData;
		return profile;
	}

	public String getName() {
		return name;
	}

	public UUID getUuid() {
		return UUID.fromString(uuid);
	}

	public AltType getType() {
		return type;
	}

	public long getLastUsed() {
		return lastUsed;
	}

	public String getAuthData() {
		return authData;
	}

	public void setAuthData(String authData) {
		this.authData = authData;
	}

	public void updateIdentity(String name, UUID uuid) {
		this.name = name;
		this.uuid = uuid.toString();
	}

	public void markUsed() {
		lastUsed = System.currentTimeMillis();
	}

	public enum AltType {
		OFFLINE,
		MICROSOFT
	}
}
