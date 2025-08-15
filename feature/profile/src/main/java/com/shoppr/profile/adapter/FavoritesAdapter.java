package com.shoppr.profile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.shoppr.core.ui.R;
import com.shoppr.core.ui.databinding.ListItemPostBinding;
import com.shoppr.model.Post;
import com.shoppr.ui.utils.FormattingUtils;
import com.shoppr.ui.utils.ImageLoader;

import java.util.List;
import java.util.Objects;

public class FavoritesAdapter extends ListAdapter<Post, FavoritesAdapter.PostViewHolder> {

	private final OnPostClickListener postClickListener;
	private final OnFavoriteClickListener favoriteClickListener;

	public interface OnPostClickListener {
		void onPostClicked(@NonNull Post post);
	}

	public interface OnFavoriteClickListener {
		void onFavoriteClick(@NonNull Post post);
	}

	public FavoritesAdapter(@NonNull OnPostClickListener postClickListener, @NonNull OnFavoriteClickListener favoriteClickListener) {
		super(PostDiffCallback.INSTANCE);
		this.postClickListener = postClickListener;
		this.favoriteClickListener = favoriteClickListener;
	}

	@NonNull
	@Override
	public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListItemPostBinding binding = ListItemPostBinding.inflate(
				LayoutInflater.from(parent.getContext()), parent, false);
		return new PostViewHolder(binding, postClickListener, favoriteClickListener);
	}

	@Override
	public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
		Post post = getItem(position);
		if (post != null) {
			holder.bind(post);
		}
	}

	static class PostViewHolder extends RecyclerView.ViewHolder {
		private final ListItemPostBinding binding;
		private final OnPostClickListener postClickListener;
		private final OnFavoriteClickListener favoriteClickListener;

		public PostViewHolder(@NonNull ListItemPostBinding binding, @NonNull OnPostClickListener postClickListener, @NonNull OnFavoriteClickListener favoriteClickListener) {
			super(binding.getRoot());
			this.binding = binding;
			this.postClickListener = postClickListener;
			this.favoriteClickListener = favoriteClickListener;
		}

		public void bind(final Post post) {
			binding.textPostItemTitle.setText(post.getTitle());
			binding.textPostItemDescription.setText(post.getDescription());

			if (post.getPrice() != null && !post.getPrice().isEmpty()) {
				binding.textPostItemPrice.setText(String.format("%s %s", FormattingUtils.formatPrice(post.getPrice()), post.getCurrency()));
				binding.textPostItemPrice.setVisibility(View.VISIBLE);
			} else {
				binding.textPostItemPrice.setVisibility(View.GONE);
			}

			binding.chipGroupCategory.removeAllViews();
			List<String> categories = post.getCategories();
			if (categories != null && !categories.isEmpty()) {
				binding.chipGroupCategory.setVisibility(View.VISIBLE);
				for (String categoryName : categories) {
					Chip chip = new Chip(itemView.getContext());
					chip.setText(categoryName);
					binding.chipGroupCategory.addView(chip);
				}
			} else {
				binding.chipGroupCategory.setVisibility(View.GONE);
			}

			if (post.getRequests() != null) {
				String offersText = post.getRequests().size() + " Offer" + (post.getRequests().size() != 1 ? "s" : "");
				binding.textPostItemOffersCount.setText(offersText);
				binding.textPostItemOffersCount.setVisibility(View.VISIBLE);
			} else {
				binding.textPostItemOffersCount.setVisibility(View.GONE);
			}

			binding.buttonFavorite.setVisibility(View.VISIBLE);
			binding.buttonFavorite.setText("In Favorites");
			binding.buttonFavorite.setIconResource(R.drawable.ic_favorite_filled);
			binding.buttonFavorite.setOnClickListener(v -> favoriteClickListener.onFavoriteClick(post));

			String imageUrl = (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) ? post.getImageUrl().get(0) : null;
			ImageLoader.loadImage(binding.imagePostItem, imageUrl);

			itemView.setOnClickListener(v -> postClickListener.onPostClicked(post));
		}
	}

	public static class PostDiffCallback extends DiffUtil.ItemCallback<Post> {
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