package com.shoppr.ui.utils;

public class FormattingUtils {

	/**
	 * Formats a price value to remove trailing zeros if it's a whole number.
	 * Example: 300.0 -> "300", 25.50 -> "25.5"
	 */
	public static String formatPrice(double price) {
		if (price == (long) price) {
			return String.format("%d", (long) price);
		} else {
			return String.format("%s", price);
		}
	}

	/**
	 * Formats a currency code and an amount into a user-friendly string with the correct symbol.
	 * Example: ("USD", 300.0) -> "$300"
	 *
	 * @param currencyCode The 3-letter currency code (e.g., "USD", "EUR").
	 * @param amount The numerical amount.
	 * @return A formatted string like "$300", "€300", or "USD 300" as a fallback.
	 */
	public static String formatCurrency(String currencyCode, double amount) {
		if (currencyCode == null) {
			return formatPrice(amount);
		}

		String symbol;
		switch (currencyCode.toUpperCase()) {
			case "USD":
				symbol = "$";
				break;
			case "EUR":
				symbol = "€";
				break;
			case "GBP":
				symbol = "£";
				break;
			// Add more currency cases as needed
			default:
				// If the currency is unknown, just use the code itself
				return String.format("%s %s", currencyCode, formatPrice(amount));
		}

		return String.format("%s%s", symbol, formatPrice(amount));
	}
}