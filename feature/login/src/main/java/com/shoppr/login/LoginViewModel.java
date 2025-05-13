package com.shoppr.login;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.shoppr.domain.HandleSignInResultUseCase;
import com.shoppr.domain.ObserveAuthStateUseCase;
import com.shoppr.model.User;
import com.shoppr.navigation.NavigationRoute;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends AndroidViewModel implements ObserveAuthStateUseCase.AuthCallbacks {
	private static final String TAG = "LoginViewModel";

	private final ObserveAuthStateUseCase observeAuthStateUseCase;
	private final HandleSignInResultUseCase handleSignInResultUseCase;

	public final LiveData<User> loggedInUserLiveData;

	private final MutableLiveData<NavigationRoute> _navigationRoute = new MutableLiveData<>();

	public LiveData<NavigationRoute> getNavigationRoute() {
		return _navigationRoute;
	}

	private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();

	public LiveData<String> getToastMessage() {
		return _toastMessage;
	}

	@Inject
	public LoginViewModel(@NonNull Application application,
												ObserveAuthStateUseCase observeAuthStateUseCase,
												HandleSignInResultUseCase handleSignInResultUseCase) {
		super(application);
		this.observeAuthStateUseCase = observeAuthStateUseCase;
		this.handleSignInResultUseCase = handleSignInResultUseCase;

		this.observeAuthStateUseCase.setAuthCallbacks(this);
		this.loggedInUserLiveData = this.observeAuthStateUseCase.getLoggedInUser();
	}

	@Override
	public void onUserAuthenticatedAndProfileReady(User user, NavigationRoute route) {
		Log.d(TAG, "ViewModel: onUserAuthenticatedAndProfileReady. User: " + user.getId() + ", Route: " + route.getClass().getSimpleName());
		_navigationRoute.postValue(route);
	}

	@Override
	public void onAuthenticationError(String message) {
		Log.e(TAG, "ViewModel: onAuthenticationError. Message: " + message);
		_toastMessage.postValue(message);
	}

	@Override
	public void onUserLoggedOut() {
		Log.d(TAG, "ViewModel: onUserLoggedOut. User is now null.");
	}

	public void registerAuthStateListener() {
		Log.d(TAG, "ViewModel: Telling ObserveAuthStateUseCase to start observing.");
		observeAuthStateUseCase.startObserving();
	}

	public void unregisterAuthStateListener() {
		Log.d(TAG, "ViewModel: Telling ObserveAuthStateUseCase to stop observing.");
		observeAuthStateUseCase.stopObserving();
	}

	public void processSignInResult(FirebaseAuthUIAuthenticationResult firebaseResult) {
		Log.d(TAG, "ViewModel: processSignInResult from Fragment. ResultCode: " + firebaseResult.getResultCode());
		boolean isSuccess = firebaseResult.getResultCode() == Activity.RESULT_OK;
		IdpResponse idpResponse = firebaseResult.getIdpResponse();
		boolean isCancellation = false;
		String errorMessage = null;

		if (!isSuccess) {
			if (idpResponse == null) {
				isCancellation = true;
			} else if (idpResponse.getError() != null) {
				errorMessage = idpResponse.getError().getMessage();
			} else {
				errorMessage = "Sign-in flow didn't complete successfully. Code: " + firebaseResult.getResultCode();
			}
		}

		handleSignInResultUseCase.process(isSuccess, isCancellation, errorMessage, new HandleSignInResultUseCase.SignInResultCallbacks() {
			@Override
			public void onSuccess() {
				Log.d(TAG, "ViewModel: HandleSignInResultUseCase reported SUCCESS.");
			}

			@Override
			public void onCancelled() {
				Log.w(TAG, "ViewModel: HandleSignInResultUseCase reported CANCELLED.");
				_toastMessage.setValue("Sign-in cancelled by user.");
			}

			@Override
			public void onError(String message) {
				Log.e(TAG, "ViewModel: HandleSignInResultUseCase reported ERROR: " + message);
				_toastMessage.setValue("Sign-in failed: " + message);
			}
		});
	}

	public void onNavigationComplete() {
		Log.d(TAG, "onNavigationComplete: Resetting navigation route.");
		_navigationRoute.setValue(null);
	}

	public void onToastMessageShown() {
		Log.d(TAG, "onToastMessageShown: Resetting toast message.");
		_toastMessage.setValue(null);
	}

	@Override
	protected void onCleared() {
		super.onCleared();
	}
}