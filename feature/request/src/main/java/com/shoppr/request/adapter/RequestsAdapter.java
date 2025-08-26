package com.shoppr.request.adapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.shoppr.model.Post;
import com.shoppr.model.Request;
import com.shoppr.request.RequestUiModel;
import com.shoppr.request.databinding.ListItemRequestBinding;
import com.shoppr.ui.utils.FormattingUtils;
import com.shoppr.ui.utils.ImageLoader;

import java.util.Objects;

public class RequestsAdapter extends ListAdapter<RequestUiModel, RequestsAdapter.RequestViewHolder> {
	public interface OnRequestClickListener {
		void onRequestClicked(RequestUiModel requestUiModel);
	}

	private final OnRequestClickListener clickListener;
	private String currentUserId;

	public RequestsAdapter(OnRequestClickListener clickListener) {
		super(DIFF_CALLBACK);
		this.clickListener = clickListener;
	}

	public void setCurrentUserId(String currentUserId) {
		this.currentUserId = currentUserId;
	}

	@NonNull
	@Override
	public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListItemRequestBinding binding = ListItemRequestBinding.inflate(
				LayoutInflater.from(parent.getContext()), parent, false);
		return new RequestViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
		RequestUiModel item = getItem(position);
		if (item != null) {
			holder.bind(item, currentUserId, clickListener);
		}
	}

	static class RequestViewHolder extends RecyclerView.ViewHolder {
		private final ListItemRequestBinding binding;

		public RequestViewHolder(ListItemRequestBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public void bind(RequestUiModel uiModel, String currentUserId, OnRequestClickListener listener) {
			Request request = uiModel.getRequest();
			Post post = uiModel.getPost();

			binding.textPostTitle.setText(post.getTitle());

			if (currentUserId != null && post.getLister() != null && currentUserId.equals(post.getLister().getId())) {
				binding.textListerName.setText("Your listing");
			} else if (post.getLister() != null && post.getLister().getName() != null) {
				String listerText = "Listed by " + post.getLister().getName();
				binding.textListerName.setText(listerText);
			} else {
				binding.textListerName.setVisibility(View.GONE);
			}

			if (post.getPrice() != null && !post.getPrice().isEmpty()) {
				String listPriceText = String.format("List price: %s %s", FormattingUtils.formatPrice(post.getPrice()), post.getCurrency());
				binding.textListPrice.setText(listPriceText);
				binding.textListPrice.setVisibility(View.VISIBLE);
			} else {
				binding.textListPrice.setText("No base offer");
			}

			String imageUrl = (post.getImageUrl() != null && !post.getImageUrl().isEmpty())
					? post.getImageUrl().get(0) : null;
			ImageLoader.loadImage(binding.imagePost, imageUrl);

			if (currentUserId != null && currentUserId.equals(request.getBuyerId())) {
				binding.textOfferLabel.setText("Your offer");
			} else {
				binding.textOfferLabel.setText("Their offer");
			}

			String offerPrice = String.format("%s %s", FormattingUtils.formatPrice(request.getOfferAmount()), request.getOfferCurrency());
			binding.textOfferPrice.setText(offerPrice);

			if (request.getCreatedAt() != null) {
				CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
						request.getCreatedAt().getTime(),
						System.currentTimeMillis(),
						DateUtils.MINUTE_IN_MILLIS);
				binding.textRequestDate.setText(relativeTime);
				binding.textRequestDate.setVisibility(android.view.View.VISIBLE);
			} else {
				binding.textRequestDate.setVisibility(View.GONE);
			}

			if (request.getStatus() != null) {
				binding.chipStatus.setText(String.format("%s%s", request.getStatus().toString().substring(0, 1).toUpperCase(), request.getStatus().toString().substring(1).toLowerCase())); // Capitalize
				binding.chipStatus.setVisibility(View.VISIBLE);

				int backgroundColorRes;
				int textColorRes;

				switch (request.getStatus()) {
					case COMPLETED:
						backgroundColorRes = com.shoppr.core.ui.R.color.color_background_completed;
						textColorRes = com.shoppr.core.ui.R.color.color_text_completed;
						break;
					case REJECTED:
						backgroundColorRes = com.shoppr.core.ui.R.color.color_background_rejected;
						textColorRes = com.shoppr.core.ui.R.color.color_text_rejected;
						break;
					case PENDING:
					default:
						backgroundColorRes = com.shoppr.core.ui.R.color.color_background_pending;
						textColorRes = com.shoppr.core.ui.R.color.color_text_pending;
						break;
				}

				binding.chipStatus.setChipBackgroundColor(
						ContextCompat.getColorStateList(itemView.getContext(), backgroundColorRes)
				);
				binding.chipStatus.setTextColor(
						ContextCompat.getColor(itemView.getContext(), textColorRes)
				);

			} else {
				binding.chipStatus.setVisibility(View.GONE);
			}

			itemView.setOnClickListener(v -> listener.onRequestClicked(uiModel));
		}
	}

	private static final DiffUtil.ItemCallback<RequestUiModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<RequestUiModel>() {
		@Override
		public boolean areItemsTheSame(@NonNull RequestUiModel oldItem, @NonNull RequestUiModel newItem) {
			return Objects.equals(oldItem.getRequest().getId(), newItem.getRequest().getId());
		}

		@Override
		public boolean areContentsTheSame(@NonNull RequestUiModel oldItem, @NonNull RequestUiModel newItem) {
			return Objects.equals(oldItem.getRequest().getStatus(), newItem.getRequest().getStatus()) &&
					Objects.equals(oldItem.getRequest().getOfferAmount(), newItem.getRequest().getOfferAmount());
		}
	};
}