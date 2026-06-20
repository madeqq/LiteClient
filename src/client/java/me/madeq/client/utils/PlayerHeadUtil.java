package me.madeq.client.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilEnvironment;
import java.net.Proxy;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import me.madeq.client.alt.AltProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;

public final class PlayerHeadUtil {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "LiteClient-PlayerHead");
        thread.setDaemon(true);
        return thread;
    });
    private static final Map<String, PlayerSkin> CACHE = new ConcurrentHashMap<>();
    private static final Set<String> LOADING = ConcurrentHashMap.newKeySet();
    private static volatile GameProfileRepository profileRepository;

    private PlayerHeadUtil() {
    }

    public static void draw(GuiGraphics graphics, Minecraft minecraft, AltProfile profile, int x, int y, int size) {
        PlayerFaceRenderer.draw(graphics, resolveSkin(minecraft, profile), x, y, size);
    }

    public static void invalidate(AltProfile profile) {
        CACHE.remove(cacheKey(profile.getName()));
        LOADING.remove(cacheKey(profile.getName()));
    }

    private static PlayerSkin resolveSkin(Minecraft minecraft, AltProfile profile) {
        String cacheKey = cacheKey(profile.getName());
        PlayerSkin cached = CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        GameProfile fallbackProfile = new GameProfile(profile.getUuid(), profile.getName());
        requestSkin(minecraft, profile.getName());
        return DefaultPlayerSkin.get(fallbackProfile);
    }

    private static void requestSkin(Minecraft minecraft, String profileName) {
        String cacheKey = cacheKey(profileName);
        if (!LOADING.add(cacheKey)) {
            return;
        }

        resolveGameProfile(minecraft, profileName)
                .thenCompose(profile -> profile
                        .map(value -> minecraft.getSkinManager().getOrLoad(value))
                        .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty())))
                .whenComplete((skin, error) -> {
                    LOADING.remove(cacheKey);
                    skin.ifPresent(value -> CACHE.put(cacheKey, value));
                });
    }

    private static CompletableFuture<Optional<GameProfile>> resolveGameProfile(Minecraft minecraft, String profileName) {
        return CompletableFuture.supplyAsync(() -> lookupProfileByName(profileName)
                .map(profile -> enrichProfile(minecraft.getMinecraftSessionService(), profile))
                .orElse(Optional.empty()), EXECUTOR);
    }

    private static Optional<GameProfile> enrichProfile(MinecraftSessionService sessionService, GameProfile profile) {
        ProfileResult result = sessionService.fetchProfile(profile.getId(), false);
        if (result != null && result.profile() != null) {
            return Optional.of(result.profile());
        }

        return Optional.of(profile);
    }

    private static Optional<GameProfile> lookupProfileByName(String profileName) {
        CompletableFuture<Optional<GameProfile>> future = new CompletableFuture<>();
        getProfileRepository().findProfilesByNames(new String[]{profileName}, new ProfileLookupCallback() {
            @Override
            public void onProfileLookupSucceeded(GameProfile profile) {
                future.complete(Optional.of(profile));
            }

            @Override
            public void onProfileLookupFailed(String name, Exception exception) {
                future.complete(Optional.empty());
            }
        });

        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (ExecutionException | TimeoutException | RuntimeException exception) {
            future.cancel(true);
            return Optional.empty();
        }
    }

    private static GameProfileRepository getProfileRepository() {
        if (profileRepository == null) {
            profileRepository = new YggdrasilAuthenticationService(
                    Proxy.NO_PROXY,
                    YggdrasilEnvironment.PROD.getEnvironment()
            ).createProfileRepository();
        }

        return profileRepository;
    }

    private static String cacheKey(String profileName) {
        return profileName.toLowerCase(Locale.ROOT);
    }
}
