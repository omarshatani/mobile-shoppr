package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.shoppr.model.Post;
import com.shoppr.post.databinding.ListItemPostBinding;

import java.util.Objects;

public class MyPostsAdapter extends ListAdapter<Post, MyPostsAdapter.PostViewHolder> {

    private final OnPostClickListener clickListener;

    public interface OnPostClickListener {
        void onPostClicked(@NonNull Post post);
    }

    public MyPostsAdapter(@NonNull OnPostClickListener listener) {
        // Using a static instance of the DiffUtil.ItemCallback for efficiency
        super(PostDiffCallback.INSTANCE);
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflating the layout using the generated ViewBinding class
        ListItemPostBinding binding = ListItemPostBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new PostViewHolder(binding, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = getItem(position);
        if (post != null) {
            holder.bind(post);
        }
    }

    /**
     * ViewHolder for the Post item card.
     */
    static class PostViewHolder extends RecyclerView.ViewHolder {
        private final ListItemPostBinding binding; // Using the generated binding class
        private final OnPostClickListener clickListener;

        public PostViewHolder(@NonNull ListItemPostBinding binding, OnPostClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.clickListener = listener;
        }

        public void bind(final Post post) {
            // Bind data to the views using the binding object
            binding.textPostItemTitle.setText(post.getTitle());
            binding.textPostItemDescription.setText(post.getDescription());

            if (post.getPrice() != null && !post.getPrice().isEmpty()) {
                binding.textPostItemPrice.setText(String.format("%s %s", post.getPrice(), post.getCurrency()));
                binding.textPostItemPrice.setVisibility(View.VISIBLE);
            } else {
                binding.textPostItemPrice.setVisibility(View.GONE);
            }

            if (post.getCategory() != null && !post.getCategory().isEmpty()) {
                binding.chipPostItemCategory.setText(post.getCategory());
                binding.chipPostItemCategory.setVisibility(View.VISIBLE);
            } else {
                binding.chipPostItemCategory.setVisibility(View.GONE);
            }

            binding.chipPostItemType.setText(post.getType().getLabel().toUpperCase());
            binding.chipPostItemType.setVisibility(View.VISIBLE);

            if (post.getRequests() != null && !post.getRequests().isEmpty()) {
                String offersText = post.getRequests().size() + " Offer" + (post.getRequests().size() > 1 ? "s" : "");
                binding.textPostItemOffersCount.setText(offersText);
                binding.textPostItemOffersCount.setVisibility(View.VISIBLE);
            } else {
                binding.textPostItemOffersCount.setText("0 Offers");
                binding.textPostItemOffersCount.setVisibility(View.VISIBLE);
            }

            // Load the primary image using Glide
            String imageUrl = (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) ? post.getImageUrl().get(0) : null;
            Glide.with(binding.imagePostItem.getContext())
                .load(imageUrl)
                .placeholder(com.shoppr.core.ui.R.drawable.ic_placeholder_image) // Your placeholder drawable
                .error(com.shoppr.core.ui.R.drawable.ic_placeholder_image) // Your error drawable
                .fitCenter()
                .into(binding.imagePostItem);

            // Set the click listener for the entire card
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onPostClicked(post);
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
            // Check for unique ID
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Post oldItem, @NonNull Post newItem) {
            // Check if the visual representation of the item has changed
            return Objects.equals(oldItem.getTitle(), newItem.getTitle()) &&
                Objects.equals(oldItem.getPrice(), newItem.getPrice()) &&
                Objects.equals(oldItem.getCategory(), newItem.getCategory()) &&
                Objects.equals(oldItem.getImageUrl(), newItem.getImageUrl()) &&
                // Compare request list sizes as a simple check for content change
                (oldItem.getRequests() != null ? oldItem.getRequests().size() : 0) ==
                    (newItem.getRequests() != null ? newItem.getRequests().size() : 0);
        }
    }
}