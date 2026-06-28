package me.madeq.client.command.suggestion;

import me.madeq.client.LiteClient;
import me.madeq.client.command.Command;
import me.madeq.client.command.CommandArgument;
import me.madeq.client.command.CommandManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class CommandSuggestionManager {

    private static final int ROW_HEIGHT  = 12;
    private static final int PADDING_X   = 4;
    private static final int PADDING_Y   = 3;
    private static final int MAX_VISIBLE = 8;

    private static final int COLOR_BG          = 0xCC101010;
    private static final int COLOR_HIGHLIGHT   = 0xCC1A4A7A;
    private static final int COLOR_BORDER      = 0xFF2A6AB0;
    private static final int COLOR_LABEL       = 0xFFFFFFFF;
    private static final int COLOR_DESCRIPTION = 0xFFAAAAAA;
    private static final int COLOR_ARG_HINT    = 0xFF55AAFF;
    private static final int COLOR_SEPARATOR   = 0xFF333333;

    private final List<CommandSuggestion> suggestions = new ArrayList<>();
    private int selectedIndex = 0;
    private String lastInput  = "";

    public void updateIfChanged(String currentInput) {
        if (!currentInput.equals(lastInput)) {
            lastInput = currentInput;
            recompute(currentInput);
        }
    }

    public void render(GuiGraphics graphics, EditBox input, int screenWidth, int screenHeight) {
        if (suggestions.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        int fontHeight = mc.font.lineHeight;

        int visibleCount = Math.min(suggestions.size(), MAX_VISIBLE);
        int scrollOffset = computeScrollOffset(visibleCount);
        int popupWidth   = computePopupWidth(mc, visibleCount, scrollOffset);
        int popupHeight  = visibleCount * ROW_HEIGHT + PADDING_Y * 2;
        int inputY       = input.getY();
        int popupX       = input.getX();
        int popupY       = inputY - popupHeight - 2;

        if (popupY < 2) popupY = inputY + input.getHeight() + 2;

        graphics.fill(popupX - 1, popupY - 1, popupX + popupWidth + 1, popupY + popupHeight + 1, COLOR_BORDER);
        graphics.fill(popupX, popupY, popupX + popupWidth, popupY + popupHeight, COLOR_BG);

        for (int i = 0; i < visibleCount; i++) {
            int globalIdx = scrollOffset + i;
            CommandSuggestion suggestion = suggestions.get(globalIdx);

            int rowY = popupY + PADDING_Y + i * ROW_HEIGHT;

            if (globalIdx == selectedIndex) {
                graphics.fill(popupX, rowY - 1, popupX + popupWidth, rowY + ROW_HEIGHT - 1, COLOR_HIGHLIGHT);
            }

            if (i < visibleCount - 1) {
                graphics.fill(popupX + 2, rowY + ROW_HEIGHT - 2, popupX + popupWidth - 2, rowY + ROW_HEIGHT - 1, COLOR_SEPARATOR);
            }

            int textY   = rowY + (ROW_HEIGHT - fontHeight) / 2;
            int cursorX = popupX + PADDING_X;

            graphics.drawString(mc.font, suggestion.label(), cursorX, textY, COLOR_LABEL, false);
            cursorX += mc.font.width(suggestion.label()) + 4;

            if (!suggestion.description().isEmpty()) {
                String desc = "- " + suggestion.description();
                graphics.drawString(mc.font, desc, cursorX, textY, COLOR_DESCRIPTION, false);
                cursorX += mc.font.width(desc) + 6;
            }

            if (globalIdx == selectedIndex) {
                String argHint = buildArgHint(suggestion.completion());
                if (!argHint.isEmpty()) {
                    graphics.drawString(mc.font, argHint, cursorX, textY, COLOR_ARG_HINT, false);
                }
            }
        }

        if (suggestions.size() > MAX_VISIBLE) {
            String indicator = (scrollOffset + 1) + "-" + (scrollOffset + visibleCount) + "/" + suggestions.size();
            int indX = popupX + popupWidth - mc.font.width(indicator) - PADDING_X;
            int indY = popupY + popupHeight - ROW_HEIGHT / 2 - mc.font.lineHeight / 2;
            graphics.drawString(mc.font, indicator, indX, indY, COLOR_DESCRIPTION, false);
        }
    }

    public boolean handleKeyPress(int keyCode, EditBox input) {
        if (suggestions.isEmpty()) return false;

        return switch (keyCode) {
            case GLFW.GLFW_KEY_UP -> {
                selectedIndex = (selectedIndex - 1 + suggestions.size()) % suggestions.size();
                yield true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                selectedIndex = (selectedIndex + 1) % suggestions.size();
                yield true;
            }
            case GLFW.GLFW_KEY_TAB -> {
                applySuggestion(input, suggestions.get(selectedIndex));
                yield true;
            }
            case GLFW.GLFW_KEY_ESCAPE -> {
                suggestions.clear();
                yield true;
            }
            default -> false;
        };
    }

    public boolean handleMouseClick(double mouseX, double mouseY, EditBox input, int screenWidth, int screenHeight) {
        if (suggestions.isEmpty()) return false;

        Minecraft mc = Minecraft.getInstance();
        int visibleCount = Math.min(suggestions.size(), MAX_VISIBLE);
        int scrollOffset = computeScrollOffset(visibleCount);
        int popupWidth   = computePopupWidth(mc, visibleCount, scrollOffset);
        int popupHeight  = visibleCount * ROW_HEIGHT + PADDING_Y * 2;
        int popupX       = input.getX();
        int inputY       = input.getY();
        int popupY       = inputY - popupHeight - 2;

        if (popupY < 2) popupY = inputY + input.getHeight() + 2;

        if (mouseX < popupX || mouseX > popupX + popupWidth) return false;
        if (mouseY < popupY || mouseY > popupY + popupHeight) return false;

        int relY       = (int) mouseY - popupY - PADDING_Y;
        int clickedRow = relY / ROW_HEIGHT;
        int globalIdx  = scrollOffset + clickedRow;

        if (globalIdx >= 0 && globalIdx < suggestions.size()) {
            selectedIndex = globalIdx;
            applySuggestion(input, suggestions.get(globalIdx));
            return true;
        }
        return false;
    }

    private void recompute(String input) {
        suggestions.clear();
        selectedIndex = 0;

        CommandManager cm = LiteClient.getCommandManager();
        String prefix = cm.getPrefix();

        if (!input.startsWith(prefix)) return;

        String afterPrefix      = input.substring(prefix.length());
        String[] parts          = afterPrefix.split(" ", -1);

        if (parts.length == 0) return;

        String typedCommandName = parts[0].toLowerCase();

        if (parts.length == 1) {
            for (Command cmd : cm.getCommands()) {
                if (!cmd.isVisibleInHelp()) continue;
                if (cmd.getName().toLowerCase().startsWith(typedCommandName)) {
                    suggestions.add(new CommandSuggestion(
                            prefix + cmd.getName(),
                            prefix + cmd.getName(),
                            cmd.getDescription()
                    ));
                }
            }
        } else {
            Command cmd = findCommand(cm, typedCommandName);
            if (cmd == null) return;

            int argIndex = parts.length - 2;
            if (argIndex < 0 || argIndex >= cmd.getArguments().size()) return;

            CommandArgument arg = cmd.getArguments().get(argIndex);
            if (!arg.hasOptions()) return;

            String typedArg      = parts[parts.length - 1].toLowerCase();
            String baseCompletion = prefix + typedCommandName + " "
                    + String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length - 1));
            if (parts.length > 2) baseCompletion += " ";

            for (String option : arg.options()) {
                if (option.toLowerCase().startsWith(typedArg)) {
                    suggestions.add(new CommandSuggestion(
                            baseCompletion + option,
                            option,
                            arg.name() + " option"
                    ));
                }
            }
        }
    }

    private void applySuggestion(EditBox input, CommandSuggestion suggestion) {
        input.setValue(suggestion.completion() + " ");
        input.moveCursorToEnd(false);
        recompute(input.getValue());
        lastInput = input.getValue();
    }

    private Command findCommand(CommandManager cm, String name) {
        return cm.getCommands().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private String buildArgHint(String completion) {
        CommandManager cm = LiteClient.getCommandManager();
        String prefix = cm.getPrefix();

        String afterPrefix = completion.startsWith(prefix)
                ? completion.substring(prefix.length()).trim()
                : completion.trim();
        String commandName = afterPrefix.split(" ")[0];

        Command cmd = findCommand(cm, commandName);
        if (cmd == null || cmd.getArguments().isEmpty()) return "";

        StringBuilder hint = new StringBuilder("  ");
        for (CommandArgument arg : cmd.getArguments()) {
            hint.append("<").append(arg.name()).append(":").append(arg.type().getDisplayName()).append("> ");
        }
        return hint.toString().stripTrailing();
    }

    private int computeScrollOffset(int visibleCount) {
        int offset = 0;
        if (selectedIndex >= visibleCount) {
            offset = selectedIndex - visibleCount + 1;
        }
        return offset;
    }

    private int computePopupWidth(Minecraft mc, int visibleCount, int scrollOffset) {
        int maxWidth = 120;
        for (int i = 0; i < visibleCount; i++) {
            CommandSuggestion s = suggestions.get(scrollOffset + i);
            int w = PADDING_X * 2
                    + mc.font.width(s.label())
                    + (s.description().isEmpty() ? 0 : 4 + mc.font.width("- " + s.description()));
            if (w > maxWidth) maxWidth = w;
        }
        return maxWidth;
    }
}
