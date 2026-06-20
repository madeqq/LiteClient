package me.madeq.client.command;

import java.util.*;

import me.madeq.client.LiteClient;
import me.madeq.client.chat.ChatHelper;
import me.madeq.client.command.impl.*;

public class CommandManager {
    private String prefix = "!";
    private final Map<String, Command> commands = new LinkedHashMap<>();
    private boolean defaultsRegistered;

    public void registerDefaults() {
        if (defaultsRegistered) {
            return;
        }

        registerCommands(
                new HelpCommand(),
                new ModuleCommand(),
                new AuthorCommand(),
                new PrefixCommand(),
                new HideEntityCommand(),
                new FakeGmCommand(),
                new HideNickCommand(),
                new PluginListCommand(),
                new PlayerListCommand(),
                new TestCommand()
        );

        defaultsRegistered = true;
    }

    public void registerCommands(Command... commands) {
        Arrays.stream(commands).forEach(
                command -> this.commands.put(command.getName().toLowerCase(Locale.ROOT), command)
        );
    }

    public List<Command> getCommands() {
        return List.copyOf(commands.values());
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            sendClientMessage("Prefix cannot be empty");
            return;
        }

        applyPrefix(prefix);
        LiteClient.getConfigManager().saveCurrentServer();
    }

    public void applyPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            this.prefix = "!";
            return;
        }

        this.prefix = prefix.trim();
    }

    public boolean handleChatMessage(String message) {
        registerDefaults();

        if (!message.startsWith(prefix)) {
            return false;
        }

        execute(message.substring(prefix.length()).trim());
        return true;
    }

    private void execute(String input) {
        if (input.isBlank()) {
            sendClientMessage("Type " + prefix + "help to list client commands");
            return;
        }

        List<String> parts = splitArguments(input);
        String commandName = parts.getFirst().toLowerCase(Locale.ROOT);
        Command command = commands.get(commandName);

        if (command == null) {
            sendClientMessage("Unknown command: " + prefix + commandName + ". Type " + prefix + "help");
            return;
        }

        List<String> rawArguments = parts.subList(1, parts.size());
        int expectedArguments = command.getArguments().size();
        boolean hasValidArgumentCount = command.acceptsAdditionalArguments()
                ? rawArguments.size() >= expectedArguments
                : rawArguments.size() == expectedArguments;

        if (!hasValidArgumentCount) {
            sendClientMessage("Usage: " + command.getUsage(prefix));
            return;
        }

        try {
            command.executeCommand(new CommandContext(this, command, parseArguments(command, rawArguments), rawArguments));
        } catch (CommandParseException exception) {
            sendClientMessage(exception.getMessage());
            sendClientMessage("Usage: " + command.getUsage(prefix));
        } catch (RuntimeException exception) {
            sendClientMessage("An error occurred while executing the command: " + exception.getMessage());
        }
    }

    private Map<String, Object> parseArguments(Command command, List<String> rawArguments) throws CommandParseException {
        Map<String, Object> parsedArguments = new LinkedHashMap<>();
        List<CommandArgument> definitions = command.getArguments();

        for (int index = 0; index < definitions.size(); index++) {
            CommandArgument definition = definitions.get(index);
            Object value = definition.type().parse(rawArguments.get(index));
            validateRange(definition, value);
            validateOptions(definition, value);
            parsedArguments.put(definition.name(), value);
        }

        return parsedArguments;
    }

    private void validateRange(CommandArgument definition, Object value) throws CommandParseException {
        if (!definition.hasRange() || !(value instanceof Number number)) {
            return;
        }

        double doubleValue = number.doubleValue();
        if (doubleValue < definition.min() || doubleValue > definition.max()) {
            throw new CommandParseException("'" + definition.name() + "' must be between " + definition.min() + " and " + definition.max());
        }
    }

    private void validateOptions(CommandArgument definition, Object value) throws CommandParseException {
        if (!definition.hasOptions() || !(value instanceof String stringValue)) {
            return;
        }

        if (!definition.options().contains(stringValue)) {
            throw new CommandParseException("'" + definition.name() + "' must be one of: " + String.join(", ", definition.options()));
        }
    }

    private List<String> splitArguments(String input) {
        List<String> arguments = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;

        for (int index = 0; index < input.length(); index++) {
            char character = input.charAt(index);

            if (character == '"') {
                insideQuotes = !insideQuotes;
                continue;
            }

            if (Character.isWhitespace(character) && !insideQuotes) {
                addPart(arguments, current);
                continue;
            }

            current.append(character);
        }

        addPart(arguments, current);
        return arguments;
    }

    private void addPart(List<String> arguments, StringBuilder current) {
        if (!current.isEmpty()) {
            arguments.add(current.toString());
            current.setLength(0);
        }
    }

    private void sendClientMessage(String message) {
        ChatHelper.send(message);
    }
}
