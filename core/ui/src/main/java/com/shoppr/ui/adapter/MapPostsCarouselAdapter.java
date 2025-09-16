package com.shoppr.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.shoppr.core.ui.R;
import com.shoppr.core.ui.databinding.ListItemMapPostPeekBinding;
import com.shoppr.model.Post;
import com.shoppr.ui.utils.FormattingUtils;
import com.shoppr.ui.utils.ImageLoader;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MapPostsCarouselAdapter extends ListAdapter<Post, MapPostsCarouselAdapter.PostViewHolder> {

	private final OnPostClickListener postClickListener;
	private final OnFavoriteClickListener favoriteClickListener;
	private final OnMakeAnOfferClickListener makeAnOfferClickListener;
	private List<String> favoritePostIds = Collections.emptyList();
	private String currentUserId;

	public interface OnPostClickListener {
		void onPostClick(Post post);
	}

	public interface OnFavoriteClickListener {
		void onFavoriteClick(Post post);
	}

	public interface OnMakeAnOfferClickListener {
		void onMakeAnOfferClick(Post post);
	}

	public MapPostsCarouselAdapter(
			OnPostClickListener postClickListener,
			OnFavoriteClickListener favoriteClickListener,
			OnMakeAnOfferClickListener makeAnOfferClickListener
	) {
		super(PostDiffCallback.INSTANCE);
		this.postClickListener = postClickListener;
		this.favoriteClickListener = favoriteClickListener;
		this.makeAnOfferClickListener = makeAnOfferClickListener;
	}

	public void setFavoritePostIds(List<String> newFavoritePostIds) {
		// Get a copy of the old list before updating
		List<String> oldFavoritePostIds = this.favoritePostIds;
		this.favoritePostIds = newFavoritePostIds != null ? newFavoritePostIds : Collections.emptyList();

		// Loop through the currently visible items to find what changed
		for (int i = 0; i < getItemCount(); i++) {
			Post post = getItem(i);
			if (post != null && post.getId() != null) {
				boolean wasFavorite = oldFavoritePostIds.contains(post.getId());
				boolean isFavorite = this.favoritePostIds.contains(post.getId());

				// If the favorite status of this specific item has changed,
				// notify the adapter to only rebind this one item.
				if (wasFavorite != isFavorite) {
					notifyItemChanged(i);
				}
			}
		}
	}

	public void setCurrentUserId(String currentUserId) {
		this.currentUserId = currentUserId;
	}

	@NonNull
	@Override
	public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListItemMapPostPeekBinding binding = ListItemMapPostPeekBinding.inflate(
				LayoutInflater.from(parent.getContext()), parent, false);
		return new PostViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
		Post post = getItem(position);
		if (post != null) {
			boolean isFavorite = favoritePostIds.contains(post.getId());
			holder.bind(post, isFavorite, currentUserId, postClickListener, favoriteClickListener, makeAnOfferClickListener);
		}
	}

	static class PostViewHolder extends RecyclerView.ViewHolder {
		private final ListItemMapPostPeekBinding binding;

		public PostViewHolder(ListItemMapPostPeekBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public void bind(
				final Post post,
				boolean isFavorite,
				String currentUserId,
				final OnPostClickListener postClickListener,
				final OnFavoriteClickListener favoriteClickListener,
				final OnMakeAnOfferClickListener makeAnOfferClickListener
		) {
			binding.postTitle.setText(post.getTitle());
			if (post.getPrice() != null) {
				binding.postPrice.setText(String.format("%s", FormattingUtils.formatCurrency(post.getCurrency(), Double.parseDouble(post.getPrice()))));
			} else {
				binding.postPrice.setText("No base offer");
			}

			String imageUrl = (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) ? post.getImageUrl().get(0) : null;
			ImageLoader.loadImage(binding.postImage, imageUrl);

			if (isFavorite) {
				binding.buttonFavorite.setIconResource(R.drawable.ic_favorite_filled);
			} else {
				binding.buttonFavorite.setIconResource(R.drawable.ic_favorite_outline);
			}

			boolean hasOffer = currentUserId != null &&
					post.getOfferingUserIds() != null &&
					post.getOfferingUserIds().contains(currentUserId);

			if (hasOffer) {
				// State when an offer has been made
				binding.buttonMakeAnOffer.setText(R.string.offer_made);
				binding.buttonMakeAnOffer.setIconResource(R.drawable.ic_done);
				binding.buttonMakeAnOffer.setBackgroundTintList(
						ContextCompat.getColorStateList(itemView.getContext(), R.color.button_offer_made_tint)
				);
			} else {
				if (post.getType() == null) {
					return;
				}
				switch (post.getType()) {
					case WANTING_TO_BUY_ITEM:
					case WANTING_TO_OFFER_SERVICE:
						binding.buttonMakeAnOffer.setText(R.string.make_an_offer);
						break;
					default:
						binding.buttonMakeAnOffer.setText(R.string.propose_an_offer);
				}
				binding.buttonMakeAnOffer.setIconResource(R.drawable.ic_price_change);
				binding.buttonMakeAnOffer.setBackgroundTintList(
						ContextCompat.getColorStateList(itemView.getContext(), R.color.button_default_tint)
				);
			}

			binding.buttonFavorite.setOnClickListener(v -> favoriteClickListener.onFavoriteClick(post));
			binding.buttonMakeAnOffer.setOnClickListener(v -> makeAnOfferClickListener.onMakeAnOfferClick(post));
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