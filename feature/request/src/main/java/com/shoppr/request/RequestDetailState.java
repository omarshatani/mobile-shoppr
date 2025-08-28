package com.shoppr.request;

import com.shoppr.model.ActivityEntry;
import com.shoppr.model.Post;
import com.shoppr.model.Request;
import com.shoppr.model.RequestStatus;
import com.shoppr.model.User;
import com.shoppr.ui.utils.FormattingUtils;

import java.util.List;

public class RequestDetailState {

	// Raw Data
	private final Post post;
	private final Request request;
	private final User currentUser;

	// --- Pre-calculated UI Properties ---
	public final boolean isCurrentUserSeller;
	public final boolean isCurrentUserBuyer;
	public final boolean showActionButtons;

	// Button Visibility
	public final boolean showAcceptButton;
	public final boolean showRejectButton;
	public final boolean showCounterButton;
	public final boolean showEditOfferButton;

	// Button Text
	public final String acceptButtonText;

	// Other UI details
	public final String offerLabel;
	public final String listerName;

	public RequestDetailState(Post post, Request request, User currentUser) {
		this.post = post;
		this.request = request;
		this.currentUser = currentUser;

		// --- Basic Role Identification ---
		this.isCurrentUserSeller = currentUser != null && post.getLister().getId() != null && currentUser.getId().equals(post.getLister().getId());
		this.isCurrentUserBuyer = currentUser != null && request.getBuyerId() != null && currentUser.getId().equals(request.getBuyerId());

		// --- Core UI Logic ---
		this.showActionButtons = request.getStatus() != RequestStatus.COMPLETED && request.getStatus() != RequestStatus.REJECTED;

		if (this.showActionButtons) {
			// Determine who made the last move
			boolean wasLastActionMine = wasLastActionMadeByCurrentUser();

			if (wasLastActionMine) {
				this.showEditOfferButton = true;
				this.showRejectButton = true; // "Reject" here means "Withdraw"
				this.showAcceptButton = false;
				this.showCounterButton = false;
			} else {
				this.showEditOfferButton = false;
				this.showAcceptButton = true;
				this.showRejectButton = true;
				this.showCounterButton = request.getStatus() != RequestStatus.ACCEPTED;
			}

			// Set the text for the primary action button
			if (isCurrentUserBuyer && request.getStatus() == RequestStatus.ACCEPTED) {
				this.acceptButtonText = "Confirm & Pay";
			} else {
				this.acceptButtonText = "Accept current offer (" + FormattingUtils.formatCurrency(request.getOfferCurrency(), request.getOfferAmount()) + ")";
			}

		} else {
			// If the action bar isn't shown, hide all buttons
			this.showAcceptButton = false;
			this.showRejectButton = false;
			this.showCounterButton = false;
			this.showEditOfferButton = false;
			this.acceptButtonText = "Accept";
		}

		this.offerLabel = isCurrentUserBuyer ? "Your Offer" : "Their Offer";
		this.listerName = isCurrentUserSeller ? "Your Listing" : String.format("@%s", post.getLister().getName());
	}

	private boolean wasLastActionMadeByCurrentUser() {
		if (currentUser == null) return false;
		List<ActivityEntry> timeline = request.getActivityTimeline();
		if (timeline == null || timeline.isEmpty()) {
			// This case should not happen in a valid request, but as a fallback:
			// If there's no history, assume it's the seller's turn to act on the initial offer.
			return isCurrentUserBuyer;
		}
		ActivityEntry lastAction = timeline.get(timeline.size() - 1);
		return currentUser.getId().equals(lastAction.getActorId());
	}

	// --- Getters for Raw Data ---
	public Post getPost() {
		return post;
	}

	public Request getRequest() {
		return request;
	}

	public User getCurrentUser() {
		return currentUser;
	}
}