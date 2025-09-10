package com.shoppr.request;

import com.shoppr.model.ActivityEntry;
import com.shoppr.model.Post;
import com.shoppr.model.Request;
import com.shoppr.model.RequestStatus;
import com.shoppr.model.User;

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
	public final String offerLabel = "Latest Offer";

	public RequestDetailState(Post post, Request request, User currentUser) {
		this.post = post;
		this.request = request;
		this.currentUser = currentUser;

		this.isCurrentUserSeller = currentUser != null && post.getLister().getId() != null && currentUser.getId().equals(post.getLister().getId());
		this.isCurrentUserBuyer = currentUser != null && request.getBuyerId() != null && currentUser.getId().equals(request.getBuyerId());

		this.showActionButtons = request.getStatus() != RequestStatus.COMPLETED && request.getStatus() != RequestStatus.REJECTED;

		String lastActorId = getLastActorId();
		boolean wasLastActionMine = currentUser != null && currentUser.getId().equals(lastActorId);

		if (this.showActionButtons) {
			// --- Finalized Logic ---
			if (wasLastActionMine) {
				// My last move: I can only edit or withdraw.
				this.showEditOfferButton = request.getStatus() != RequestStatus.ACCEPTED;
				this.showRejectButton = true; // "Reject" here means "Withdraw"
				this.showAcceptButton = false;
				this.showCounterButton = false;
			} else {
				// The other user's last move: I can respond.
				this.showEditOfferButton = false;
				this.showAcceptButton = true;
				this.showRejectButton = true;
				this.showCounterButton = request.getStatus() != RequestStatus.ACCEPTED;
			}

			// The "Confirm & Pay" button is ONLY visible to the buyer when the offer has been accepted by the seller.
			if (isCurrentUserBuyer && request.getStatus() == RequestStatus.ACCEPTED) {
				this.acceptButtonText = "Confirm & Pay";
			} else {
				this.acceptButtonText = "Accept";
			}

		} else {
			// If the action bar isn't shown, hide all buttons.
			this.showAcceptButton = false;
			this.showRejectButton = false;
			this.showCounterButton = false;
			this.showEditOfferButton = false;
			this.acceptButtonText = "Accept";
		}
	}

	private String getLastActorId() {
		List<ActivityEntry> timeline = request.getActivityTimeline();
		if (timeline == null || timeline.isEmpty()) {
			return null; // Should not happen
		}
		return timeline.get(timeline.size() - 1).getActorId();
	}
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