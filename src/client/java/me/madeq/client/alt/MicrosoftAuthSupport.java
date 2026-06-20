package me.madeq.client.alt;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import me.madeq.client.LiteClient;
import net.lenni0451.commons.httpclient.HttpClient;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.step.java.StepMCProfile;
import net.raphimc.minecraftauth.step.java.session.StepFullJavaSession;
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode;

public final class MicrosoftAuthSupport {
	private static final HttpClient HTTP_CLIENT;
	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
		Thread thread = new Thread(runnable, "LiteClient-MicrosoftAuth");
		thread.setDaemon(true);
		return thread;
	});

	static {
		MinecraftAuth.USER_AGENT = "LiteClient/" + LiteClient.VERSION;
		HTTP_CLIENT = MinecraftAuth.createHttpClient();
	}

	private MicrosoftAuthSupport() {
	}

	public static CompletableFuture<AltProfile> loginWithDeviceCode(Consumer<String> statusUpdater) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				StepFullJavaSession.FullJavaSession session = MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.getFromInput(
						HTTP_CLIENT,
						new StepMsaDeviceCode.MsaDeviceCodeCallback(deviceCode -> {
							statusUpdater.accept("Open: " + deviceCode.getDirectVerificationUri());
							Minecraft.getInstance().execute(() -> Util.getPlatform().openUri(deviceCode.getDirectVerificationUri()));
						})
				);
				return createProfile(session);
			} catch (Exception exception) {
				throw new IllegalStateException(exception.getMessage() == null ? "Microsoft login failed" : exception.getMessage(), exception);
			}
		}, EXECUTOR);
	}

	public static User createSessionUser(AltProfile profile) {
		try {
			StepFullJavaSession.FullJavaSession session = refreshSession(loadSession(profile));
			StepMCProfile.MCProfile minecraftProfile = session.getMcProfile();
			profile.updateIdentity(minecraftProfile.getName(), minecraftProfile.getId());
			profile.setAuthData(MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.toJson(session).toString());
			return new User(
					minecraftProfile.getName(),
					minecraftProfile.getId(),
					minecraftProfile.getMcToken().getAccessToken(),
					Optional.empty(),
					Optional.empty(),
					User.Type.MSA
			);
		} catch (Exception exception) {
			throw new IllegalStateException(exception.getMessage() == null ? "Microsoft login failed" : exception.getMessage(), exception);
		}
	}

	public static void attachTokenPersistence(AltProfile profile, Runnable onSave) {
	}

	private static AltProfile createProfile(StepFullJavaSession.FullJavaSession session) {
		StepMCProfile.MCProfile minecraftProfile = session.getMcProfile();
		return AltProfile.microsoft(
				minecraftProfile.getName(),
				minecraftProfile.getId(),
				MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.toJson(session).toString()
		);
	}

	private static StepFullJavaSession.FullJavaSession loadSession(AltProfile profile) {
		if (profile.getAuthData() == null || profile.getAuthData().isBlank()) {
			throw new IllegalStateException("Missing Microsoft auth data");
		}

		JsonObject authJson = JsonParser.parseString(profile.getAuthData()).getAsJsonObject();
		return MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.fromJson(authJson);
	}

	private static StepFullJavaSession.FullJavaSession refreshSession(StepFullJavaSession.FullJavaSession session) throws Exception {
		if (session.getMcProfile().isExpiredOrOutdated()) {
			return MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.refresh(HTTP_CLIENT, session);
		}

		return session;
	}
}
