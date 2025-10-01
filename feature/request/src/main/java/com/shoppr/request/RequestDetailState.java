package com.shoppr.request;

import com.shoppr.model.Post;
import com.shoppr.model.Request;
import com.shoppr.model.RequestStatus;
import com.shoppr.model.User;

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
	public final String listerName;

	public RequestDetailState(Post post, Request request, User currentUser) {
		this.post = post;
		this.request = request;
		this.currentUser = currentUser;

		this.isCurrentUserSeller = currentUser != null && post.getLister().getId() != null && currentUser.getId().equals(post.getLister().getId());
		this.isCurrentUserBuyer = currentUser != null && request.getBuyerId() != null && currentUser.getId().equals(request.getBuyerId());

		this.showActionButtons = request.getStatus() != RequestStatus.COMPLETED && request.getStatus() != RequestStatus.REJECTED;

		// --- NEW, SIMPLIFIED LOGIC BASED ON THE DIAGRAM ---
		boolean isSellerTurn = isCurrentUserSeller && request.getStatus() == RequestStatus.SELLER_PENDING;
		boolean isBuyerTurn = isCurrentUserBuyer && request.getStatus() == RequestStatus.BUYER_PENDING;
		boolean isBuyerConfirmation = isCurrentUserBuyer && request.getStatus() == RequestStatus.ACCEPTED;

		if (isSellerTurn || isBuyerTurn) {
			// It's my turn to respond to an offer.
			this.showAcceptButton = true;
			this.showRejectButton = true;
			this.showCounterButton = true;
			this.showEditOfferButton = false;
		} else if (isBuyerConfirmation) {
			// It's my turn to confirm the deal.
			this.showAcceptButton = true;
			this.showRejectButton = true;
			this.showCounterButton = false;
			this.showEditOfferButton = false;
		} else {
			// It's the other person's turn. I can only edit or withdraw my last offer.
			this.showAcceptButton = false;
			this.showRejectButton = true; // "Reject" here means "Withdraw"
			this.showCounterButton = false;
			this.showEditOfferButton = true;
		}

		this.acceptButtonText = isBuyerConfirmation ? "Confirm & Pay" : "Accept";
		this.listerName = isCurrentUserSeller ? "Your Listing" : String.format("@%s", post.getLister().getName());
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