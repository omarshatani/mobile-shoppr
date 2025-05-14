package com.shoppr.model;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 * Ensures the event is only handled once.
 * <p>
 * See https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150
 */
public class Event<T> {

	private T content;
	private boolean hasBeenHandled = false;

	public Event(T content) {
		if (content == null) {
			throw new IllegalArgumentException("null values in Event are not supported.");
		}
		this.content = content;
	}

	/**
	 * Returns the content and prevents its use again.
	 */
	@Nullable
	public T getContentIfNotHandled() {
		if (hasBeenHandled) {
			return null;
		} else {
			hasBeenHandled = true;
			return content;
		}
	}

	/**
	 * Returns the content, even if it's already been handled.
	 */
	public T peekContent() {
		return content;
	}

	public boolean hasBeenHandled() {
		return hasBeenHandled;
	}

	/**
	 * An {@link Observer} for {@link Event}s, simplifying the pattern of checking if the {@link Event}'s content has
	 * already been handled.
	 *
	 * @param <T> The type of the content.
	 */
	public static class EventObserver<T> implements Observer<Event<T>> {
		private final OnEventUnhandledContent<T> onEventUnhandledContent;

		public EventObserver(OnEventUnhandledContent<T> onEventUnhandledContent) {
			this.onEventUnhandledContent = onEventUnhandledContent;
		}

		@Override
		public void onChanged(@Nullable Event<T> event) {
			if (event != null) {
				T content = event.getContentIfNotHandled();
				if (content != null) {
					onEventUnhandledContent.onEventUnhandled(content);
				}
			}
		}

		public interface OnEventUnhandledContent<T> {
			void onEventUnhandled(T content);
		}
	}
}
