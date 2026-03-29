package io.xa59.reconnect.utils;

public class ArgumentUtils {

    private static String postCommand;
    private static int delaySeconds;

    public static String cleanupCommandInput(String input) {
        if (input == null) return null;

        input = input.trim();

        // Remove surrounding quotes
        if (input.startsWith("\"") && input.endsWith("\"") && input.length() >= 2) {
            input = input.substring(1, input.length() - 1);
        }

        // Remove leading slash
        if (input.startsWith("/")) {
            input = input.substring(1);
        }

        return input;
    }

    // Setter for postCommand
    public static void setPostCommand(String cmd) { postCommand = cmd; }

    // Getter for postCommand
    public static String getPostCommand() { return postCommand; }

    // Setter for delaySeconds
    public static void setDelay(String input) { delaySeconds = TimeParser.parseTime(input); }

    // Getter for delaySeconds
    public static int getDelaySeconds() { return delaySeconds; }

    // Clears shared state
    public static void clear() {
        postCommand = null;
        delaySeconds = 0;
    }

}
