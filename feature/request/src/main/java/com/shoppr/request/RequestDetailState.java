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

		boolean isSellerTurn = isCurrentUserSeller && request.getStatus() == RequestStatus.SELLER_PENDING;
		boolean isBuyerTurn = isCurrentUserBuyer && request.getStatus() == RequestStatus.BUYER_PENDING;
		boolean isSellerConfirmation = isCurrentUserSeller && request.getStatus() == RequestStatus.BUYER_ACCEPTED;
		boolean isBuyerConfirmation = isCurrentUserBuyer && request.getStatus() == RequestStatus.SELLER_ACCEPTED;

		if (isSellerTurn || isBuyerTurn) {
			this.showAcceptButton = true;
			this.showRejectButton = true;
			this.showCounterButton = true;
			this.showEditOfferButton = false;
		} else if (isSellerConfirmation || isBuyerConfirmation) {
			this.showAcceptButton = true;
			this.showRejectButton = true;
			this.showCounterButton = false;
			this.showEditOfferButton = false;
		} else {
			// It's the other person's turn to act.
			this.showAcceptButton = false;
			this.showRejectButton = true; // Can always withdraw
			this.showCounterButton = false;
			this.showEditOfferButton = (isCurrentUserBuyer && request.getStatus() == RequestStatus.SELLER_PENDING);
		}

		if (isBuyerConfirmation) {
			this.acceptButtonText = "Confirm & Pay";
		} else if (isSellerConfirmation) {
			this.acceptButtonText = "Confirm Deal";
		} else {
			this.acceptButtonText = "Accept";
		}

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