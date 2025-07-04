package com.shoppr.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.shoppr.core.ui.R;
import com.shoppr.model.Post;

import java.util.Objects;

public class NearbyPostsAdapter extends ListAdapter<Post, NearbyPostsAdapter.PostViewHolder> {

	private final OnPostClickListener clickListener;

	public interface OnPostClickListener {
		void onPostClicked(@NonNull Post post);
	}

	public NearbyPostsAdapter(@NonNull OnPostClickListener listener) {
		super(PostDiffCallback.INSTANCE);
		this.clickListener = listener;
	}

	@NonNull
	@Override
	public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		// Inflate the layout directly without using a binding class
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.list_item_nearby_post, parent, false);
		return new PostViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
		Post post = getItem(position);
		if (post != null) {
			holder.bind(post, clickListener);
		}
	}

	/**
	 * ViewHolder for a single post item in the nearby list.
	 */
	static class PostViewHolder extends RecyclerView.ViewHolder {
		// Store references to the views
		private final ImageView imageNearbyPost;
		private final TextView textNearbyPostTitle;
		private final TextView textNearbyPostPrice;

		public PostViewHolder(@NonNull View itemView) {
			super(itemView);
			// Find views by their ID from the inflated itemView
			imageNearbyPost = itemView.findViewById(R.id.image_nearby_post);
			textNearbyPostTitle = itemView.findViewById(R.id.text_nearby_post_title);
			textNearbyPostPrice = itemView.findViewById(R.id.text_nearby_post_price);
		}

		public void bind(final Post post, final OnPostClickListener listener) {
			textNearbyPostTitle.setText(post.getTitle());

			if (post.getPrice() != null && !post.getPrice().isEmpty()) {
				textNearbyPostPrice.setText(post.getPrice());
				textNearbyPostPrice.setVisibility(View.VISIBLE);
			} else {
				// If there's no price, you might want to show the listing type or hide the view
				if (post.getType() != null) {
					textNearbyPostPrice.setText(post.getType().getLabel());
				} else {
					textNearbyPostPrice.setVisibility(View.GONE);
				}
			}

			// Load the primary image using Glide
			String imageUrl = (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) ? post.getImageUrl().get(0) : null;
			Glide.with(imageNearbyPost.getContext())
					.load(imageUrl)
					.placeholder(R.drawable.ic_placeholder_image) // Your placeholder drawable
					.error(R.drawable.ic_error_image)       // Your error drawable
					.centerCrop()
					.into(imageNearbyPost);

			// Set the click listener for the entire card
			itemView.setOnClickListener(v -> {
				if (listener != null) {
					listener.onPostClicked(post);
				}
			});
		}
	}

	/**
	 * DiffUtil.ItemCallback for efficiently updating the list of posts.
	 */
	public static class PostDiffCallback extends DiffUtil.ItemCallback<Post> {
		public static final PostDiffCallback INSTANCE = new PostDiffCallback();

		@Override
		public boolean areItemsTheSame(@NonNull Post oldItem, @NonNull Post newItem) {
			return Objects.equals(oldItem.getId(), newItem.getId());
		}

		@Override
		public boolean areContentsTheSame(@NonNull Post oldItem, @NonNull Post newItem) {
			// Compare fields that are visible in this specific list item
			return Objects.equals(oldItem.getTitle(), newItem.getTitle()) &&
					Objects.equals(oldItem.getPrice(), newItem.getPrice()) &&
					Objects.equals(
							(oldItem.getImageUrl() != null && !oldItem.getImageUrl().isEmpty()) ? oldItem.getImageUrl().get(0) : null,
							(newItem.getImageUrl() != null && !newItem.getImageUrl().isEmpty()) ? newItem.getImageUrl().get(0) : null
					);
		}
	}
}