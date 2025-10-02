package com.shoppr.request.adapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.shoppr.model.ActivityEntry;
import com.shoppr.request.databinding.ListItemTimelineEntryBinding;

import java.util.Objects;

public class ActivityTimelineAdapter extends ListAdapter<ActivityEntry, ActivityTimelineAdapter.TimelineViewHolder> {

	private String currentUserId;
	private String buyerId;
	private String sellerId;

	public ActivityTimelineAdapter() {
		super(DIFF_CALLBACK);
	}

	public void setActorIds(String currentUserId, String buyerId, String sellerId) {
		this.currentUserId = currentUserId;
		this.buyerId = buyerId;
		this.sellerId = sellerId;
		// We can optionally call notifyDataSetChanged() here if IDs might change
		// after the list is submitted, but it's often not needed.
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
			// The bind method is now much simpler.
			holder.bind(item, this);
		}
	}

	static class TimelineViewHolder extends RecyclerView.ViewHolder {
		private final ListItemTimelineEntryBinding binding;

		public TimelineViewHolder(ListItemTimelineEntryBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public void bind(ActivityEntry entry, ActivityTimelineAdapter adapter) {
			boolean isCurrentUserTheActor = adapter.currentUserId != null && adapter.currentUserId.equals(entry.getActorId());

			binding.textActorName.setText(isCurrentUserTheActor ? "You" : entry.getActorName());
			binding.textEntryDescription.setText(entry.getDescription());

			if (entry.getCreatedAt() != null) {
				CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
						entry.getCreatedAt().getTime(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
				binding.textEntryDate.setText(relativeTime);
			}

			// Set Role Label
			if (entry.getActorId() != null) {
				if (entry.getActorId().equals(adapter.buyerId)) {
					binding.textActorRole.setText("(Buyer)");
				} else if (entry.getActorId().equals(adapter.sellerId)) {
					binding.textActorRole.setText("(Seller)");
				} else {
					binding.textActorRole.setText("");
				}
			}

			// --- FINAL, CORRECTED ALIGNMENT LOGIC ---
			ConstraintLayout.LayoutParams cardParams = (ConstraintLayout.LayoutParams) binding.cardTimelineEntry.getLayoutParams();

			if (isCurrentUserTheActor) {
				// --- Current User's Bubble (Right side) ---
				binding.imageActorAvatarStart.setVisibility(View.GONE);
				binding.imageActorAvatarEnd.setVisibility(View.VISIBLE);
				binding.imageActorAvatarEnd.setImageResource(com.shoppr.core.ui.R.drawable.ic_account_circle);

				// Ensure it's constrained to the right avatar
				cardParams.endToStart = binding.imageActorAvatarEnd.getId();

				binding.cardTimelineEntry.setCardBackgroundColor(
						itemView.getContext().getColor(com.google.android.material.R.color.material_dynamic_primary90)
				);

			} else {
				// --- Other User's Bubble (Left side) ---
				binding.imageActorAvatarStart.setVisibility(View.VISIBLE);
				binding.imageActorAvatarEnd.setVisibility(View.GONE);
				binding.imageActorAvatarStart.setImageResource(com.shoppr.core.ui.R.drawable.ic_account_circle);

				// Ensure it's constrained to the left avatar
				cardParams.startToEnd = binding.imageActorAvatarStart.getId();

				binding.cardTimelineEntry.setCardBackgroundColor(
						itemView.getContext().getColor(com.google.android.material.R.color.material_dynamic_primary80)
				);
			}
			// Re-apply the modified layout parameters
			binding.cardTimelineEntry.setLayoutParams(cardParams);
		}
	}

	private static final DiffUtil.ItemCallback<ActivityEntry> DIFF_CALLBACK = new DiffUtil.ItemCallback<ActivityEntry>() {
		@Override
		public boolean areItemsTheSame(@NonNull ActivityEntry oldItem, @NonNull ActivityEntry newItem) {
			return oldItem == newItem;
		}

		@Override
		public boolean areContentsTheSame(@NonNull ActivityEntry oldItem, @NonNull ActivityEntry newItem) {
			return Objects.equals(oldItem.getDescription(), newItem.getDescription()) &&
					Objects.equals(oldItem.getCreatedAt(), newItem.getCreatedAt());
		}
	};
}