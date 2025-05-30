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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MyPostsAdapter extends ListAdapter<Post, MyPostsAdapter.PostViewHolder> {

    private final OnPostClickListener clickListener;

    public interface OnPostClickListener {
        void onPostClicked(@NonNull Post post);
    }

    public MyPostsAdapter(@NonNull OnPostClickListener listener) {
        super(PostDiffCallback.INSTANCE);
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private final ListItemPostBinding binding;
        private final OnPostClickListener clickListener;

        public PostViewHolder(@NonNull ListItemPostBinding binding, OnPostClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.clickListener = listener;
        }

        public void bind(final Post post) {
            binding.textPostItemTitle.setText(post.getTitle());
            binding.textPostItemDescription.setText(post.getDescription());

            if (post.getPrice() != null && !post.getPrice().isEmpty()) {
                binding.textPostItemPrice.setText(post.getPrice()); // Consider formatting with currency
                binding.textPostItemPrice.setVisibility(View.VISIBLE);
            } else {
                binding.textPostItemPrice.setVisibility(View.GONE);
            }

            if (post.getType() != null) {
                binding.textPostItemType.setText(post.getType().getLabel().toUpperCase(Locale.getDefault()));
                binding.textPostItemType.setVisibility(View.VISIBLE);
            } else {
                binding.textPostItemType.setVisibility(View.GONE);
            }

            if (post.getState() != null) {
                binding.chipPostItemStatus.setText(post.getState().name()); // Or a more user-friendly string
                // TODO: Set chip background/text color based on state (e.g., Active, Sold, Expired)
                binding.chipPostItemStatus.setVisibility(View.VISIBLE);
            } else {
                binding.chipPostItemStatus.setVisibility(View.GONE);
            }

            // Image Loading using Glide
            // Assuming getImageUrl() returns List<String> and we display the first one.
            // You'll need to have placeholder and error drawables in your res/drawable
            String imageUrl = (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) ? post.getImageUrl().get(0) : null;
            Glide.with(binding.imagePostItem.getContext())
                    .load(imageUrl)
                    .placeholder(com.shoppr.core.ui.R.drawable.ic_placeholder_image) // Replace with your placeholder
                    .error(com.shoppr.core.ui.R.drawable.ic_placeholder_image)       // Replace with your error drawable
                    .centerCrop()
                    .into(binding.imagePostItem);

            // Example for offers count and date
            // You would replace this with actual data formatting
            if (post.getRequests() != null) {
                binding.textPostItemOffersCount.setText(post.getRequests().size() + " Offers");
            } else {
                binding.textPostItemOffersCount.setText("0 Offers");
            }

            // Example Date Formatting (assuming Post has a getCreatedAt() returning a Date or Timestamp)
            // if (post.getCreatedAt() != null) {
            //    binding.textPostItemDate.setText(formatDate(post.getCreatedAt()));
            // } else {
            //    binding.textPostItemDate.setText("Date N/A");
            // }
            binding.textPostItemDate.setText("Date TODO"); // Placeholder

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onPostClicked(post);
                }
            });
        }

        // Helper method for date formatting (example)
        private String formatDate(Date date) {
            if (date == null) return "Date N/A";
            // Simple "time ago" or formatted date
            long timeMs = date.getTime();
            long nowMs = System.currentTimeMillis();
            long diff = nowMs - timeMs;

            long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            long days = TimeUnit.MILLISECONDS.toDays(diff);

            if (seconds < 60) return "Just now";
            if (minutes < 60) return minutes + "m ago";
            if (hours < 24) return hours + "h ago";
            if (days < 7) return days + "d ago";

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return sdf.format(date);
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
            // Add more comprehensive checks if needed
            return Objects.equals(oldItem.getTitle(), newItem.getTitle()) &&
                    Objects.equals(oldItem.getDescription(), newItem.getDescription()) &&
                    Objects.equals(oldItem.getPrice(), newItem.getPrice()) &&
                    oldItem.getState().equals(newItem.getState()) &&
                    oldItem.getType().equals(newItem.getType());
        }
    }
}