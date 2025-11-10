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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class NearbyPostsAdapter extends ListAdapter<Post, NearbyPostsAdapter.PostViewHolder> {

	private final OnPostClickListener postClickListener;
	private final OnFavoriteClickListener favoriteClickListener;
	private List<String> favoritePostIds = Collections.emptyList();

	public interface OnPostClickListener {
		void onPostClick(Post post);
	}

	public interface OnFavoriteClickListener {
		void onFavoriteClick(Post post);
	}

	public NearbyPostsAdapter(OnPostClickListener postClickListener, OnFavoriteClickListener favoriteClickListener) {
		super(PostDiffCallback.INSTANCE);
		this.postClickListener = postClickListener;
		this.favoriteClickListener = favoriteClickListener;
	}

	public void setFavoritePostIds(List<String> favoritePostIds) {
		this.favoritePostIds = favoritePostIds != null ? favoritePostIds : Collections.emptyList();
		notifyDataSetChanged();
	}

	@NonNull
	@Override
	public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListItemNearbyPostBinding binding = ListItemNearbyPostBinding.inflate(
				LayoutInflater.from(parent.getContext()), parent, false);
		return new PostViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
		Post post = getItem(position);
		if (post != null) {
			boolean isFavorite = favoritePostIds.contains(post.getId());
			holder.bind(post, isFavorite, postClickListener, favoriteClickListener);
		}
	}

	static class PostViewHolder extends RecyclerView.ViewHolder {
		private final ListItemNearbyPostBinding binding;

		public PostViewHolder(ListItemNearbyPostBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public void bind(final Post post, boolean isFavorite, final OnPostClickListener postClickListener, final OnFavoriteClickListener favoriteClickListener) {
			binding.textNearbyPostTitle.setText(post.getTitle());
			binding.textNearbyPostPrice.setText(String.format("%s %s", post.getPrice(), post.getCurrency()));
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
				binding.textListerName.setText("by " + post.getLister().getName());
				binding.imageListerAvatar.setImageResource(R.drawable.ic_person_24);
			} else {
				binding.listerInfoContainer.setVisibility(View.GONE);
			}

			if (post.getRequests() != null && !post.getRequests().isEmpty()) {
				String offersText = post.getRequests().size() + " Offer" + (post.getRequests().size() > 1 ? "s" : "");
				binding.textPostItemOffersCount.setText(offersText);
			} else {
				binding.textPostItemOffersCount.setText("0 Offers");
			}

			if (isFavorite) {
				binding.buttonFavorite.setText("In Favorites");
				binding.buttonFavorite.setIconResource(R.drawable.ic_favorite_filled);
			} else {
				binding.buttonFavorite.setText("Add to favorites");
				binding.buttonFavorite.setIconResource(R.drawable.ic_favorite_outline);
			}

			binding.buttonFavorite.setOnClickListener(v -> favoriteClickListener.onFavoriteClick(post));
			itemView.setOnClickListener(v -> postClickListener.onPostClick(post));
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