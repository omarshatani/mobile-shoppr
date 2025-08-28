package com.shoppr.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.shoppr.core.ui.databinding.ListItemPostBinding;
import com.shoppr.model.Post;
import com.shoppr.ui.utils.FormattingUtils;
import com.shoppr.ui.utils.ImageLoader;

import java.util.List;
import java.util.Objects;

public class MyPostsAdapter extends ListAdapter<Post, MyPostsAdapter.PostViewHolder> {

	private final OnPostClickListener listener;

	public interface OnPostClickListener {
		void onPostClicked(@NonNull Post post);
	}

	public MyPostsAdapter(@NonNull OnPostClickListener listener) {
		super(PostDiffCallback.INSTANCE);
		this.listener = listener;
	}

	@NonNull
	@Override
	public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListItemPostBinding binding = ListItemPostBinding.inflate(
				LayoutInflater.from(parent.getContext()), parent, false);
		return new PostViewHolder(binding, listener);
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
		private final OnPostClickListener listener;

		public PostViewHolder(@NonNull ListItemPostBinding binding, @NonNull OnPostClickListener listener) {
			super(binding.getRoot());
			this.binding = binding;
			this.listener = listener;
		}

		public void bind(final Post post) {
			binding.buttonFavorite.setVisibility(View.GONE);

			binding.textPostItemTitle.setText(post.getTitle());
			binding.textPostItemDescription.setText(post.getDescription());

			if (post.getPrice() != null && !post.getPrice().isEmpty()) {
				String formattedPrice = FormattingUtils.formatCurrency(post.getCurrency(), Double.parseDouble(post.getPrice()));
				binding.textPostItemPrice.setText(String.format("%s", formattedPrice));
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

			String imageUrl = (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) ? post.getImageUrl().get(0) : null;
			ImageLoader.loadImage(binding.imagePostItem, imageUrl);

			itemView.setOnClickListener(v -> listener.onPostClicked(post));
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
			return Objects.equals(oldItem.getTitle(), newItem.getTitle()) &&
					Objects.equals(oldItem.getPrice(), newItem.getPrice()) &&
					Objects.equals(oldItem.getCategories(), newItem.getCategories()) &&
					Objects.equals(oldItem.getImageUrl(), newItem.getImageUrl()) &&
					(oldItem.getRequests() != null ? oldItem.getRequests().size() : 0) ==
							(newItem.getRequests() != null ? newItem.getRequests().size() : 0);
		}
	}
}