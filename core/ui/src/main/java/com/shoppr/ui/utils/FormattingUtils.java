package com.shoppr.ui.utils;

import java.text.DecimalFormat;
import java.util.Locale;

public class FormattingUtils {

	/**
	 * Formats a Double price into a String.
	 * - If the price is an integer (e.g., 300.0), it returns "300".
	 * - If the price has decimals (e.g., 299.99), it returns "299.99".
	 * - If the price is null, it returns an empty string.
	 *
	 * @param price The price to format.
	 * @return The formatted price string.
	 */
	public static String formatPrice(Double price) {
		if (price == null) {
			return "";
		}

		// Check if the number has no fractional part
		if (price % 1 == 0) {
			return String.format(Locale.getDefault(), "%.0f", price);
		} else {
			DecimalFormat df = new DecimalFormat("#.##");
			return df.format(price);
		}
	}

	/**
	 * Overloaded method to handle String prices safely.
	 */
	public static String formatPrice(String price) {
		if (price == null || price.trim().isEmpty()) {
			return "";
		}
		try {
			return formatPrice(Double.parseDouble(price));
		} catch (NumberFormatException e) {
			return price;
		}
	}
}