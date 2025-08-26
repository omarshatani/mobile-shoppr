package com.shoppr.request;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.shoppr.model.ActivityEntry;
import com.shoppr.request.databinding.ListItemTimelineEntryBinding;

import java.util.Objects;

public class ActivityTimelineAdapter extends ListAdapter<ActivityEntry, ActivityTimelineAdapter.TimelineViewHolder> {

	private String currentUserId;

	public ActivityTimelineAdapter() {
		super(DIFF_CALLBACK);
	}

	public void setCurrentUserId(String currentUserId) {
		this.currentUserId = currentUserId;
	}

	@NonNull
	@Override
	public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListItemTimelineEntryBinding binding = ListItemTimelineEntryBinding.inflate(
				LayoutInflater.from(parent.getContext()), parent, false);
		return new TimelineViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
		ActivityEntry item = getItem(position);
		if (item != null) {
			holder.bind(item, currentUserId);
		}
	}

	static class TimelineViewHolder extends RecyclerView.ViewHolder {
		private final ListItemTimelineEntryBinding binding;

		public TimelineViewHolder(ListItemTimelineEntryBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public void bind(ActivityEntry entry, String currentUserId) {
			// Determine if the actor is the current user
			boolean isCurrentUserTheActor = currentUserId != null && currentUserId.equals(entry.getActorId());

			// Set the actor's name to "You" if it's the current user
			binding.textActorName.setText(isCurrentUserTheActor ? "You" : entry.getActorName());

			// Set the description of the event
			binding.textEntryDescription.setText(entry.getDescription());

			// Set the relative timestamp
			if (entry.getCreatedAt() != null) {
				CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
						entry.getCreatedAt().getTime(),
						System.currentTimeMillis(),
						DateUtils.MINUTE_IN_MILLIS);
				binding.textEntryDate.setText(relativeTime);
			}

			// Set the actor's avatar (using a placeholder for now)
			binding.imageActorAvatar.setImageResource(com.shoppr.core.ui.R.drawable.ic_account_circle);

			// Style the card to differentiate between users
			if (isCurrentUserTheActor) {
				// Style for the current user's actions (e.g., your offers)
				binding.cardTimelineEntry.setCardBackgroundColor(
						itemView.getContext().getColor(com.google.android.material.R.color.material_dynamic_primary90)
				);
			} else {
				// Style for the other user's actions
				binding.cardTimelineEntry.setCardBackgroundColor(
						itemView.getContext().getColor(com.google.android.material.R.color.material_dynamic_secondary90)
				);
			}
		}
	}

	private static final DiffUtil.ItemCallback<ActivityEntry> DIFF_CALLBACK = new DiffUtil.ItemCallback<ActivityEntry>() {
		@Override
		public boolean areItemsTheSame(@NonNull ActivityEntry oldItem, @NonNull ActivityEntry newItem) {
			// For a simple timeline, we can assume position is a stable identifier
			return oldItem == newItem;
		}

		@Override
		public boolean areContentsTheSame(@NonNull ActivityEntry oldItem, @NonNull ActivityEntry newItem) {
			return Objects.equals(oldItem.getDescription(), newItem.getDescription()) &&
					Objects.equals(oldItem.getCreatedAt(), newItem.getCreatedAt());
		}
	};
}