package com.shoppr.data.di;

import com.shoppr.data.usecase.CreateTransactionUseCaseImpl;
import com.shoppr.data.usecase.DeleteOfferUseCaseImpl;
import com.shoppr.data.usecase.GetAllRequestsUseCaseImpl;
import com.shoppr.data.usecase.GetCurrentDeviceLocationUseCaseImpl;
import com.shoppr.data.usecase.GetCurrentUserUseCaseImpl;
import com.shoppr.data.usecase.GetFavoritePostsUseCaseImpl;
import com.shoppr.data.usecase.GetLLMSuggestionsUseCaseImpl;
import com.shoppr.data.usecase.GetMapPostsUseCaseImpl;
import com.shoppr.data.usecase.GetMyPostsUseCaseImpl;
import com.shoppr.data.usecase.GetPostByIdUseCaseImpl;
import com.shoppr.data.usecase.GetRequestByIdUseCaseImpl;
import com.shoppr.data.usecase.GetRequestForPostUseCaseImpl;
import com.shoppr.data.usecase.GetUserByIdUseCaseImpl;
import com.shoppr.data.usecase.LogoutUseCaseImpl;
import com.shoppr.data.usecase.MakeOfferUseCaseImpl;
import com.shoppr.data.usecase.SavePostUseCaseImpl;
import com.shoppr.data.usecase.SubmitFeedbackUseCaseImpl;
import com.shoppr.data.usecase.ToggleFavoriteUseCaseImpl;
import com.shoppr.data.usecase.UpdateRequestUseCaseImpl;
import com.shoppr.data.usecase.UpdateUserDefaultLocationUseCaseImpl;
import com.shoppr.domain.usecase.CreateTransactionUseCase;
import com.shoppr.domain.usecase.DeleteOfferUseCase;
import com.shoppr.domain.usecase.GetAllRequestsUseCase;
import com.shoppr.domain.usecase.GetCurrentDeviceLocationUseCase;
import com.shoppr.domain.usecase.GetCurrentUserUseCase;
import com.shoppr.domain.usecase.GetFavoritePostsUseCase;
import com.shoppr.domain.usecase.GetLLMSuggestionsUseCase;
import com.shoppr.domain.usecase.GetMapPostsUseCase;
import com.shoppr.domain.usecase.GetMyPostsUseCase;
import com.shoppr.domain.usecase.GetPostByIdUseCase;
import com.shoppr.domain.usecase.GetRequestByIdUseCase;
import com.shoppr.domain.usecase.GetRequestForPostUseCase;
import com.shoppr.domain.usecase.GetUserByIdUseCase;
import com.shoppr.domain.usecase.LogoutUseCase;
import com.shoppr.domain.usecase.MakeOfferUseCase;
import com.shoppr.domain.usecase.SavePostUseCase;
import com.shoppr.domain.usecase.SubmitFeedbackUseCase;
import com.shoppr.domain.usecase.ToggleFavoriteUseCase;
import com.shoppr.domain.usecase.UpdateRequestUseCase;
import com.shoppr.domain.usecase.UpdateUserDefaultLocationUseCase;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ViewModelComponent;

@Module
@InstallIn(ViewModelComponent.class)
public abstract class UseCaseModule {
	@Binds
	public abstract GetCurrentUserUseCase bindGetCurrentUserUseCase(
			GetCurrentUserUseCaseImpl getCurrentUserUseCaseImpl
	);

	@Binds
	public abstract GetLLMSuggestionsUseCase bindGetLLMSuggestionsUseCase(
			GetLLMSuggestionsUseCaseImpl getLLMSuggestionsUseCaseImpl
	);

	@Binds
	public abstract GetCurrentDeviceLocationUseCase bindGetCurrentDeviceLocationUseCase(GetCurrentDeviceLocationUseCaseImpl impl);

	@Binds
	public abstract UpdateUserDefaultLocationUseCase bindUpdateUserDefaultLocationUseCase(
			UpdateUserDefaultLocationUseCaseImpl updateUserDefaultLocationUseCaseImpl
	);

	@Binds
	public abstract GetMyPostsUseCase bindGetMyPostsUseCase(
			GetMyPostsUseCaseImpl getMyPostsUseCaseImpl
	);

	@Binds
	public abstract GetMapPostsUseCase bindGetMapPostsUseCase(
			GetMapPostsUseCaseImpl getMapPostsUseCaseImpl
	);

	@Binds
	public abstract GetPostByIdUseCase bindGetPostByIdUseCase(
			GetPostByIdUseCaseImpl getPostByIdUseCaseImpl
	);

	@Binds
	public abstract SavePostUseCase bindSavePostUseCase(
			SavePostUseCaseImpl savePostUseCaseImpl
	);

	@Binds
	public abstract ToggleFavoriteUseCase bindToggleFavoriteUseCase(
			ToggleFavoriteUseCaseImpl toggleFavoriteUseCaseImpl
	);

	@Binds
	public abstract GetFavoritePostsUseCase bindGetFavoritePostsUseCase(
			GetFavoritePostsUseCaseImpl getFavoritePostsUseCaseImpl
	);

	@Binds
	public abstract LogoutUseCase bindLogoutUseCase(
			LogoutUseCaseImpl logoutUseCaseImpl
	);

	@Binds
	public abstract MakeOfferUseCase bindMakeOfferUseCase(
			MakeOfferUseCaseImpl makeOfferUseCaseImpl
	);

	@Binds
	public abstract GetRequestForPostUseCase bindGetRequestForPostUseCase(GetRequestForPostUseCaseImpl impl);

	@Binds
	public abstract GetAllRequestsUseCase bindGetAllRequestsUseCase(GetAllRequestsUseCaseImpl impl);

	@Binds
	public abstract GetRequestByIdUseCase bindGetRequestByIdUseCase(GetRequestByIdUseCaseImpl impl);

	@Binds
	public abstract UpdateRequestUseCase bindUpdateRequestUseCase(UpdateRequestUseCaseImpl impl);

	@Binds
	public abstract DeleteOfferUseCase bindDeleteOfferUseCase(DeleteOfferUseCaseImpl impl);

	@Binds
	public abstract GetUserByIdUseCase bindGetUserByIdUseCase(GetUserByIdUseCaseImpl impl);

	@Binds
	public abstract CreateTransactionUseCase bindCreateTransactionUseCase(CreateTransactionUseCaseImpl impl);

	@Binds
	public abstract SubmitFeedbackUseCase bindSubmitFeedbackUseCase(SubmitFeedbackUseCaseImpl impl);
}