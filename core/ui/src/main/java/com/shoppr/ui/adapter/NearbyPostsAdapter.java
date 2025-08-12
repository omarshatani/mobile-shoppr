package com.shoppr.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.shoppr.core.ui.R;
import com.shoppr.core.ui.databinding.ListItemNearbyPostBinding;
import com.shoppr.model.Post;
import com.shoppr.ui.utils.ImageLoader;

import java.util.List;
import java.util.Objects;

public class NearbyPostsAdapter extends ListAdapter<Post, NearbyPostsAdapter.PostViewHolder> {

	private final OnPostClickListener listener;

	public interface OnPostClickListener {
		void onPostClick(Post post);
	}

	public NearbyPostsAdapter(OnPostClickListener listener) {
		super(PostDiffCallback.INSTANCE);
		this.listener = listener;
	}

	@NonNull
	@Override
	public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListItemNearbyPostBinding binding = ListItemNearbyPostBinding.inflate(
				LayoutInflater.from(parent.getContext()),
				parent,
				false
		);
		return new PostViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
		Post post = getItem(position);
		holder.bind(post, listener);
	}

	static class PostViewHolder extends RecyclerView.ViewHolder {
		private final ListItemNearbyPostBinding binding;

		public PostViewHolder(ListItemNearbyPostBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public void bind(final Post post, final OnPostClickListener listener) {
			binding.textNearbyPostTitle.setText(post.getTitle());

			if (post.getPrice() != null && !post.getPrice().isEmpty()) {
				binding.textNearbyPostPrice.setText(String.format("%s %s", post.getPrice(), post.getCurrency()));
				binding.textNearbyPostPrice.setVisibility(View.VISIBLE);
			} else {
				binding.textNearbyPostPrice.setVisibility(View.GONE);
			}

			String imageUrl = (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) ? post.getImageUrl().get(0) : null;
			ImageLoader.loadImage(binding.imageNearbyPost, imageUrl);

			binding.chipGroupNearbyCategory.removeAllViews();
			List<String> categories = post.getCategories();
			if (categories != null && !categories.isEmpty()) {
				binding.chipGroupNearbyCategory.setVisibility(View.VISIBLE);
				for (String categoryName : categories) {
					Chip chip = new Chip(itemView.getContext());
					chip.setText(categoryName);
					binding.chipGroupNearbyCategory.addView(chip);
				}
			} else {
				binding.chipGroupNearbyCategory.setVisibility(View.GONE);
			}

			if (post.getLister() != null) {
				binding.listerInfoContainer.setVisibility(View.VISIBLE);
				binding.textListerName.setText(String.format("by %s", post.getLister().getName()));

				// --- THIS IS THE FIX ---
				// Always load the default person icon for the avatar
				binding.imageListerAvatar.setImageResource(R.drawable.ic_person_24);
				// -----------------------

			} else {
				binding.listerInfoContainer.setVisibility(View.GONE);
			}

			itemView.setOnClickListener(v -> listener.onPostClick(post));
		}
	}

	private static class PostDiffCallback extends DiffUtil.ItemCallback<Post> {
		public static final PostDiffCallback INSTANCE = new PostDiffCallback();

		@Override
		public boolean areItemsTheSame(@NonNull Post oldItem, @NonNull Post newItem) {
			return Objects.equals(oldItem.getId(), newItem.getId());
		}

		@Override
		public boolean areContentsTheSame(@NonNull Post oldItem, @NonNull Post newItem) {
			return Objects.equals(oldItem, newItem);
		}
	}
}