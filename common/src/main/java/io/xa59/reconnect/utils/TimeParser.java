package io.xa59.reconnect.utils;

public class TimeParser {

    public static int parseTime(String input) {
        input = input.toLowerCase().trim();

        try {
            if (input.endsWith("s")) { // Seconds
                return Integer.parseInt(input.replace("s", ""));
            }

            if (input.endsWith("m")) { // Minutes
                return Integer.parseInt(input.replace("m", "")) * 60;
            }

            if (input.endsWith("t")) { // Ticks
                return Integer.parseInt(input.replace("t", "")) / 20;
            }

            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
