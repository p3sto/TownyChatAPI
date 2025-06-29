package io.github.p3sto.chatbridge.utils;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {

	public static Duration parseDuration(String inp) {
		// Pattern: one or more digits (\\d+), followed by one or more letters (\\D+ or [a-zA-Z]+)
		Pattern pattern = Pattern.compile("^(\\d+)([a-zA-Z]+)$");
		Matcher matcher = pattern.matcher(inp);
		Duration duration = null;

		if (matcher.matches()) {
			long amount = Long.parseLong(matcher.group(1));
			String unit = matcher.group(2);

			duration = switch (unit) {
				case "d" -> Duration.ofDays(amount);
				case "h" -> Duration.ofHours(amount);
				case "m" -> Duration.ofMinutes(amount);
				case "s" -> Duration.ofSeconds(amount);
				case "ms" -> Duration.ofMillis(amount);
				case "us" -> Duration.ofNanos(amount * 1000);
				case "ns" -> Duration.ofNanos(amount);
				default -> null;
			};
		}

		return duration;
	}
}
