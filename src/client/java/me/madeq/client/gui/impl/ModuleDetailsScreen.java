package me.madeq.client.gui.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import me.madeq.client.LiteClient;
import me.madeq.client.command.ArgumentType;
import me.madeq.client.command.Command;
import me.madeq.client.command.CommandArgument;
import me.madeq.client.module.Module;
import me.madeq.client.gui.GuiModuleExecutor;
import me.madeq.client.gui.component.ConfigCheckbox;
import me.madeq.client.gui.component.ConfigComponent;
import me.madeq.client.gui.component.ConfigListSelect;
import me.madeq.client.gui.component.ConfigSliderField;
import me.madeq.client.gui.component.ConfigTextBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModuleDetailsScreen extends Screen {
	private static final int PANEL_WIDTH = 270;
	private static final int PANEL_BOTTOM_PADDING = 46;
	private static final int EMPTY_ARGUMENTS_HEIGHT = 18;
	private static final int BUTTON_WIDTH = 72;
	private static final int BUTTON_HEIGHT = 18;
	private static final int FIELD_HEIGHT = 18;
	private static final int FIELD_GAP = 24;
	private static final int HEADER_HEIGHT = 22;
	private static final int CONTENT_PADDING = 14;
	private static final int INFO_LINE_HEIGHT = 11;
	private static final int ARGUMENT_TOP_GAP = 10;

	private final String type;
	private final String name;
	private final String description;
	private final String usage;
	private final List<CommandArgument> arguments;
	private final String configKey;
	private final List<ArgumentField> argumentFields = new ArrayList<>();

	private ModuleDetailsScreen(String type, String name, String description, String usage, List<CommandArgument> arguments, String configKey) {
		super(Component.literal("Module Details"));
		this.type = type;
		this.name = name;
		this.description = description;
		this.usage = usage;
		this.arguments = arguments;
		this.configKey = configKey;
	}

	public static ModuleDetailsScreen forCommand(Command command) {
		return new ModuleDetailsScreen(
				"Command",
				command.getName(),
				command.getDescription(),
				command.getUsage(LiteClient.getCommandManager().getPrefix()),
				command.getArguments(),
				GuiModuleExecutor.getCommandConfigKey(command)
		);
	}

	public static ModuleDetailsScreen forModules(Module module) {
		return new ModuleDetailsScreen(
				module.getModuleType().getDisplayName(),
				module.getName(),
				module.getDescription(),
				module.getUsage(LiteClient.getCommandManager().getPrefix()),
				module.getArguments(),
				GuiModuleExecutor.getModuleConfigKey(module)
		);
	}

	@Override
	protected void init() {
		int panelX = (width - PANEL_WIDTH) / 2;
		int panelY = (height - getPanelHeight()) / 2;
		Map<String, String> savedValues = deserializeConfig(LiteClient.getClickGuiManager().getConfig(configKey));

		argumentFields.clear();
		clearWidgets();

		for (int index = 0; index < arguments.size(); index++) {
			CommandArgument argument = arguments.get(index);
			int fieldY = getArgumentStartY(panelY) + index * FIELD_GAP;
			ConfigComponent input = createInput(argument, panelX + 94, fieldY, PANEL_WIDTH - 108);
			input.setValue(savedValues.getOrDefault(argument.name(), argument.defaultValue()));
			argumentFields.add(new ArgumentField(argument, input));
		}

		if (!argumentFields.isEmpty()) {
			argumentFields.getFirst().input().setFocused(true);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.renderTransparentBackground(graphics);

		int panelX = (width - PANEL_WIDTH) / 2;
		int panelHeight = getPanelHeight();
		int panelY = (height - panelHeight) / 2;

		graphics.fill(panelX - 1, panelY - 1, panelX + PANEL_WIDTH + 1, panelY + panelHeight + 1, new Color(5, 6, 8, 210).getRGB());
		graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + panelHeight, new Color(16, 18, 24, 238).getRGB());
		graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + HEADER_HEIGHT, new Color(25, 92, 210).getRGB());
		graphics.fill(panelX, panelY + HEADER_HEIGHT - 1, panelX + PANEL_WIDTH, panelY + HEADER_HEIGHT, new Color(102, 169, 255).getRGB());

		graphics.drawString(font, type + ": " + name, panelX + 10, panelY + 7, new Color(250, 252, 255).getRGB(), true);

		List<String> infoLines = getInfoLines();
		for (int index = 0; index < infoLines.size(); index++) {
			graphics.drawString(font, infoLines.get(index), panelX + CONTENT_PADDING, getInfoStartY(panelY) + index * INFO_LINE_HEIGHT, new Color(220, 226, 235).getRGB(), true);
		}

		for (ArgumentField field : argumentFields) {
			int fieldY = field.input().getY();
			String label = field.argument().name() + ":";
			graphics.drawString(font, label, panelX + 14, fieldY + 5, new Color(220, 226, 235).getRGB(), true);
		}

		if (argumentFields.isEmpty()) {
			graphics.drawString(font, "No configurable arguments", panelX + CONTENT_PADDING, getArgumentStartY(panelY), new Color(150, 160, 174).getRGB(), true);
		}

		for (ArgumentField field : argumentFields) {
			if (!isOpenListSelect(field.input())) {
				field.input().render(graphics, font, mouseX, mouseY, partialTick);
			}
		}

		int buttonY = panelY + panelHeight - 32;
		renderButton(graphics, panelX + 14, buttonY, "Save", mouseX, mouseY);
		renderButton(graphics, panelX + 98, buttonY, "Clear", mouseX, mouseY);
		renderButton(graphics, panelX + 182, buttonY, "Execute", mouseX, mouseY);

		for (ArgumentField field : argumentFields) {
			if (isOpenListSelect(field.input())) {
				graphics.pose().pushPose();
				graphics.pose().translate(0.0F, 0.0F, 300.0F);
				field.input().render(graphics, font, mouseX, mouseY, partialTick);
				graphics.pose().popPose();
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			int panelX = (width - PANEL_WIDTH) / 2;
			int panelY = (height - getPanelHeight()) / 2;
			int buttonY = panelY + getPanelHeight() - 32;

			for (ArgumentField field : argumentFields) {
				if (field.input().mouseClicked(mouseX, mouseY, button)) {
					clearFocusExcept(field.input());
					return true;
				}
			}

			clearFocusExcept(null);

			if (isInside(mouseX, mouseY, panelX + 14, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
				saveConfig();
				return true;
			}

			if (isInside(mouseX, mouseY, panelX + 98, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
				clearConfig();
				return true;
			}

			if (isInside(mouseX, mouseY, panelX + 182, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
				execute();
				return true;
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (button == 0) {
			for (ArgumentField field : argumentFields) {
				if (field.input().mouseDragged(mouseX, mouseY, button)) {
					return true;
				}
			}
		}

		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0) {
			for (ArgumentField field : argumentFields) {
				field.input().mouseReleased();
			}
		}

		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == 256) {
			minecraft.setScreen(new ClickGuiScreen());
			return true;
		}

		for (ArgumentField field : argumentFields) {
			if (field.input().keyPressed(keyCode, scanCode, modifiers)) {
				return true;
			}
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		for (ArgumentField field : argumentFields) {
			if (field.input().charTyped(codePoint, modifiers)) {
				return true;
			}
		}

		return super.charTyped(codePoint, modifiers);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void saveConfig() {
		LiteClient.getClickGuiManager().saveConfig(configKey, serializeConfig());
	}

	private void clearConfig() {
		for (ArgumentField field : argumentFields) {
			field.input().setValue("");
		}

		LiteClient.getClickGuiManager().clearConfig(configKey);
	}

	private void execute() {
		saveConfig();
		String config = buildArgumentLine();

		if ("Command".equals(type)) {
			String commandLine = LiteClient.getCommandManager().getPrefix() + name + (config.isBlank() ? "" : " " + config);
			LiteClient.getCommandManager().handleChatMessage(commandLine);
			return;
		}

		LiteClient.getModuleManager().executeModule(name, splitArguments(config));
	}

	private String buildArgumentLine() {
		List<String> values = new ArrayList<>();

		for (ArgumentField field : argumentFields) {
			values.add(formatArgumentValue(field.input().getValue()));
		}

		return String.join(" ", values).trim();
	}

	private String formatArgumentValue(String value) {
		String trimmed = value.trim();

		if (trimmed.contains(" ")) {
			return "\"" + trimmed.replace("\"", "") + "\"";
		}

		return trimmed;
	}

	private String serializeConfig() {
		List<String> entries = new ArrayList<>();

		for (ArgumentField field : argumentFields) {
			entries.add(field.argument().name() + "=" + field.input().getValue().replace(";", "\\;"));
		}

		return String.join(";", entries);
	}

	private Map<String, String> deserializeConfig(String config) {
		Map<String, String> values = new LinkedHashMap<>();

		if (config.isBlank()) {
			return values;
		}

		for (String entry : config.split(";", -1)) {
			int separator = entry.indexOf('=');

			if (separator <= 0) {
				continue;
			}

			String key = entry.substring(0, separator);
			String value = entry.substring(separator + 1).replace("\\;", ";");
			values.put(key, value);
		}

		return values;
	}

	private List<String> splitArguments(String input) {
		if (input.isBlank()) {
			return List.of();
		}

		List<String> parts = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean insideQuotes = false;

		for (int index = 0; index < input.length(); index++) {
			char character = input.charAt(index);

			if (character == '"') {
				insideQuotes = !insideQuotes;
				continue;
			}

			if (Character.isWhitespace(character) && !insideQuotes) {
				addPart(parts, current);
				continue;
			}

			current.append(character);
		}

		addPart(parts, current);
		return parts;
	}

	private void addPart(List<String> parts, StringBuilder current) {
		if (!current.isEmpty()) {
			parts.add(current.toString());
			current.setLength(0);
		}
	}

	private String getArgumentText() {
		if (arguments.isEmpty()) {
			return "none";
		}

		List<String> names = new ArrayList<>();
		for (CommandArgument argument : arguments) {
			names.add(argument.name() + ":" + argument.type().getDisplayName());
		}

		return String.join(", ", names);
	}

	private void renderButton(GuiGraphics graphics, int x, int y, String label, int mouseX, int mouseY) {
		boolean hovered = isInside(mouseX, mouseY, x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
		graphics.fill(x, y, x + BUTTON_WIDTH, y + BUTTON_HEIGHT, (hovered ? new Color(38, 121, 255) : new Color(25, 92, 210)).getRGB());
		graphics.fill(x, y + BUTTON_HEIGHT - 1, x + BUTTON_WIDTH, y + BUTTON_HEIGHT, new Color(102, 169, 255).getRGB());
		graphics.drawString(font, label, x + 8, y + 5, new Color(250, 252, 255).getRGB(), true);
	}

	private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}

	private ConfigComponent createInput(CommandArgument argument, int x, int y, int width) {
		if (argument.type() == ArgumentType.LIST) {
			return new ConfigListSelect(x, y, width, FIELD_HEIGHT, argument.options());
		}

		if (argument.type() == ArgumentType.BOOLEAN) {
			return new ConfigCheckbox(x, y, width, FIELD_HEIGHT);
		}

		if (argument.hasRange()) {
			return new ConfigSliderField(x, y, width, FIELD_HEIGHT, argument.min(), argument.max(), argument.type());
		}

		return new ConfigTextBox(x, y, width, FIELD_HEIGHT, getInputMode(argument));
	}

	private ConfigTextBox.InputMode getInputMode(CommandArgument argument) {
		return switch (argument.type()) {
			case INT -> ConfigTextBox.InputMode.INT;
			case DOUBLE -> ConfigTextBox.InputMode.DOUBLE;
			default -> ConfigTextBox.InputMode.TEXT;
		};
	}

	private boolean isOpenListSelect(ConfigComponent input) {
		return input instanceof ConfigListSelect select && select.isOpen();
	}

	private void clearFocusExcept(ConfigComponent focusedInput) {
		for (ArgumentField field : argumentFields) {
			if (field.input() != focusedInput) {
				field.input().setFocused(false);
			}
		}
	}

	private int getPanelHeight() {
		int contentHeight = arguments.isEmpty() ? EMPTY_ARGUMENTS_HEIGHT : arguments.size() * FIELD_GAP;
		return HEADER_HEIGHT + getInfoHeight() + ARGUMENT_TOP_GAP + contentHeight + PANEL_BOTTOM_PADDING;
	}

	private int getInfoStartY(int panelY) {
		return panelY + HEADER_HEIGHT + 10;
	}

	private int getArgumentStartY(int panelY) {
		return panelY + HEADER_HEIGHT + getInfoHeight() + ARGUMENT_TOP_GAP;
	}

	private int getInfoHeight() {
		List<String> infoLines = getInfoLines();
		return infoLines.isEmpty() ? 0 : infoLines.size() * INFO_LINE_HEIGHT + 4;
	}

	private List<String> getInfoLines() {
		List<String> lines = new ArrayList<>();

		if (!description.isBlank()) {
			lines.addAll(wrapLine("Description: " + description, PANEL_WIDTH - CONTENT_PADDING * 2));
		}

		return lines;
	}

	private List<String> wrapLine(String value, int maxWidth) {
		List<String> lines = new ArrayList<>();
		StringBuilder current = new StringBuilder();

		for (String word : value.split(" ")) {
			String next = current.isEmpty() ? word : current + " " + word;

			if (!current.isEmpty() && font.width(next) > maxWidth) {
				lines.add(current.toString());
				current.setLength(0);
				current.append(word);
				continue;
			}

			current.setLength(0);
			current.append(next);
		}

		if (!current.isEmpty()) {
			lines.add(current.toString());
		}

		return lines;
	}

	private record ArgumentField(CommandArgument argument, ConfigComponent input) {
	}
}
