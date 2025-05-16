package com.shoppr.post;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shoppr.model.Event;
import com.shoppr.navigation.NavigationRoute;

public class CreatePostViewModel extends ViewModel {
	private final MutableLiveData<Event<NavigationRoute>> _navigationCommand = new MutableLiveData<>();

	public LiveData<Event<NavigationRoute>> getNavigationCommand() {
		return _navigationCommand;
	}

}