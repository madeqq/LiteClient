package me.madeq.client.alt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;

public class AltManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("freeclient").resolve("alts.json");
	private final AtomicBoolean microsoftLoginInProgress = new AtomicBoolean();
	private AltFile altFile = new AltFile();
	private String status = "Select an account to login";

	public void initialize() {
		load();
	}

	public List<AltProfile> getProfiles() {
		return List.copyOf(altFile.profiles);
	}

	public String getStatus() {
		return status;
	}

	public boolean isMicrosoftLoginInProgress() {
		return microsoftLoginInProgress.get();
	}

	public Optional<AltProfile> getSelectedProfile() {
		return altFile.profiles.stream()
				.filter(profile -> profile.getUuid().toString().equals(altFile.selectedUuid))
				.findFirst();
	}

	public void addOfflineProfile(String name) {
		String normalized = normalizeName(name);
		if (normalized.isBlank()) {
			status = "Enter a nickname first";
			return;
		}

		AltProfile profile = new AltProfile(normalized, createOfflineUuid(normalized), AltProfile.AltType.OFFLINE);
		altFile.profiles.removeIf(existing -> existing.getName().equalsIgnoreCase(normalized));
		altFile.profiles.add(profile);
		altFile.selectedUuid = profile.getUuid().toString();
		save();
		status = "Saved offline profile: " + normalized;
	}

	public boolean login(AltProfile profile) {
		try {
			User user = profile.getType() == AltProfile.AltType.MICROSOFT
					? MicrosoftAuthSupport.createSessionUser(profile)
					: new User(profile.getName(), profile.getUuid(), "0", Optional.empty(), Optional.empty(), User.Type.LEGACY);

			if (!replaceMinecraftUser(user)) {
				status = "Could not switch session";
				return false;
			}

			profile.markUsed();
			altFile.selectedUuid = profile.getUuid().toString();
			save();
			status = "Logged in as " + profile.getName();
			return true;
		} catch (RuntimeException exception) {
			status = exception.getMessage() == null ? "Login failed" : exception.getMessage();
			return false;
		}
	}

	public void remove(AltProfile profile) {
		altFile.profiles.removeIf(existing -> existing.getUuid().equals(profile.getUuid()));
		if (profile.getUuid().toString().equals(altFile.selectedUuid)) {
			altFile.selectedUuid = "";
		}

		save();
		status = "Removed " + profile.getName();
	}

	public void startMicrosoftLogin(Runnable onComplete) {
		if (!microsoftLoginInProgress.compareAndSet(false, true)) {
			status = "Microsoft login already in progress";
			return;
		}

		status = "Starting Microsoft login...";
		MicrosoftAuthSupport.loginWithDeviceCode(this::setStatus)
				.whenComplete((profile, error) -> Minecraft.getInstance().execute(() -> {
					microsoftLoginInProgress.set(false);
					if (error != null) {
						Throwable cause = error.getCause() != null ? error.getCause() : error;
						status = cause.getMessage() != null ? cause.getMessage() : "Microsoft login failed";
						return;
					}

					upsertMicrosoftProfile(profile);
					MicrosoftAuthSupport.attachTokenPersistence(profile, this::save);
					altFile.selectedUuid = profile.getUuid().toString();
					save();
					status = "Saved Microsoft profile: " + profile.getName();
					if (onComplete != null) {
						onComplete.run();
					}
				}));
	}

	private void upsertMicrosoftProfile(AltProfile profile) {
		altFile.profiles.removeIf(existing -> existing.getUuid().equals(profile.getUuid())
				|| existing.getName().equalsIgnoreCase(profile.getName()));
		altFile.profiles.add(profile);
	}

	private void setStatus(String value) {
		Minecraft.getInstance().execute(() -> status = value);
	}

	private boolean replaceMinecraftUser(User user) {
		Minecraft minecraft = Minecraft.getInstance();

		for (String fieldName : List.of("user", "field_1726", "session", "f_90998_", "Z")) {
			try {
				Field field = Minecraft.class.getDeclaredField(fieldName);
				field.setAccessible(true);
				field.set(minecraft, user);
				return true;
			} catch (NoSuchFieldException | IllegalAccessException ignored) {
			}
		}

		return false;
	}

	private void load() {
		if (!Files.exists(configPath)) {
			altFile = new AltFile();
			return;
		}

		try (Reader reader = Files.newBufferedReader(configPath)) {
			AltFile loadedFile = GSON.fromJson(reader, AltFile.class);
			altFile = loadedFile == null ? new AltFile() : loadedFile;

			if (altFile.profiles == null) {
				altFile.profiles = new ArrayList<>();
			}
		} catch (IOException | JsonSyntaxException exception) {
			altFile = new AltFile();
			status = "Failed to load alt config";
		}
	}

	private void save() {
		try {
			Files.createDirectories(configPath.getParent());

			try (Writer writer = Files.newBufferedWriter(configPath)) {
				GSON.toJson(altFile, writer);
			}
		} catch (IOException exception) {
			status = "Failed to save alt config";
		}
	}

	private UUID createOfflineUuid(String name) {
		return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
	}

	private String normalizeName(String name) {
		String trimmed = name == null ? "" : name.trim();
		if (trimmed.length() > 16) {
			return trimmed.substring(0, 16);
		}

		return trimmed;
	}

	private static class AltFile {
		private List<AltProfile> profiles = new ArrayList<>();
		private String selectedUuid = "";
	}
}
