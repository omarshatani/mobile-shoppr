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

		// The action bar is visible unless the negotiation is complete.
		this.showActionButtons = request.getStatus() != RequestStatus.COMPLETED &&
				request.getStatus() != RequestStatus.REJECTED &&
				request.getStatus() != RequestStatus.REJECTED_COUNTERED;

		boolean sellerTurn = isCurrentUserSeller && request.getStatus() == RequestStatus.PENDING;
		boolean buyerTurn = isCurrentUserBuyer && request.getStatus() == RequestStatus.COUNTERED;
		boolean buyerConfirmationTurn = isCurrentUserBuyer && request.getStatus() == RequestStatus.ACCEPTED;
		boolean sellerConfirmationTurn = isCurrentUserSeller && request.getStatus() == RequestStatus.ACCEPTED_COUNTERED;

		if (sellerTurn) {
			this.showAcceptButton = true;
			this.showRejectButton = true;
			this.showCounterButton = true;
			this.showEditOfferButton = false;
		} else if (buyerTurn) {
			this.showAcceptButton = true;
			this.showRejectButton = true;
			this.showCounterButton = true;
			this.showEditOfferButton = false;
		} else if (buyerConfirmationTurn) {
			this.showAcceptButton = true;
			this.showRejectButton = true;
			this.showCounterButton = false;
			this.showEditOfferButton = false;
		} else if (sellerConfirmationTurn) {
			this.showAcceptButton = true;
			this.showRejectButton = true;
			this.showCounterButton = false;
			this.showEditOfferButton = false;
		} else {
			this.showAcceptButton = request.getStatus() == RequestStatus.COUNTERED;
			this.showRejectButton = true;
			this.showCounterButton = false;
			this.showEditOfferButton = request.getStatus() == RequestStatus.PENDING || request.getStatus() == RequestStatus.COUNTERED;
		}

		if (buyerConfirmationTurn) {
			this.acceptButtonText = "Confirm & Pay";
		} else if (sellerConfirmationTurn) {
			this.acceptButtonText = "Confirm";
		} else {
			this.acceptButtonText = "Accept";
		}

		this.listerName = isCurrentUserSeller ? "Your Listing" : String.format("@%s", post.getLister().getName());
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