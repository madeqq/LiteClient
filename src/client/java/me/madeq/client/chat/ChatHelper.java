package me.madeq.client.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public final class ChatHelper {
	private static final String CLIENT_PREFIX = "<dark_gray>[<gradient:#4facfe:#00f2fe>LiteClient</gradient><dark_gray>]</dark_gray> <reset>";
	private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
	private static final PlainTextComponentSerializer PLAIN_TEXT = PlainTextComponentSerializer.plainText();

	private ChatHelper() {
	}

	public static void send(String message) {
		sendRaw(CLIENT_PREFIX + message);
	}

	public static void sendRaw(String message) {
		Minecraft.getInstance().gui.getChat().addMessage(format(message));
	}

	public static MutableComponent format(String message) {
		try {
			return convert(MINI_MESSAGE.deserialize(message));
		} catch (RuntimeException exception) {
			return Component.literal(message);
		}
	}

	private static MutableComponent convert(net.kyori.adventure.text.Component component) {
		MutableComponent minecraftComponent = Component.literal(plainContent(component));
		minecraftComponent.setStyle(convertStyle(component.style()));

		for (net.kyori.adventure.text.Component child : component.children()) {
			minecraftComponent.append(convert(child));
		}

		return minecraftComponent;
	}

	private static String plainContent(net.kyori.adventure.text.Component component) {
		net.kyori.adventure.text.Component withoutChildren = component.children(List.of());
		return PLAIN_TEXT.serialize(withoutChildren);
	}

	private static Style convertStyle(net.kyori.adventure.text.format.Style style) {
		Style minecraftStyle = Style.EMPTY;

		if (style.color() != null) {
			minecraftStyle = minecraftStyle.withColor(TextColor.fromRgb(
					Objects.requireNonNull(style.color()).value())
			);
		}

		for (TextDecoration decoration : activeDecorations(style)) {
			minecraftStyle = applyDecoration(minecraftStyle, decoration);
		}

		if (style.insertion() != null) {
			minecraftStyle = minecraftStyle.withInsertion(style.insertion());
		}

		return minecraftStyle;
	}

	private static List<TextDecoration> activeDecorations(net.kyori.adventure.text.format.Style style) {
		List<TextDecoration> decorations = new ArrayList<>();

		for (TextDecoration decoration : TextDecoration.values()) {
			if (style.decoration(decoration) == TextDecoration.State.TRUE) {
				decorations.add(decoration);
			}
		}

		return decorations;
	}

	private static Style applyDecoration(Style style, TextDecoration decoration) {
		return switch (decoration) {
			case BOLD -> style.withBold(true);
			case ITALIC -> style.withItalic(true);
			case UNDERLINED -> style.withUnderlined(true);
			case STRIKETHROUGH -> style.withStrikethrough(true);
			case OBFUSCATED -> style.withObfuscated(true);
		};
	}

}
