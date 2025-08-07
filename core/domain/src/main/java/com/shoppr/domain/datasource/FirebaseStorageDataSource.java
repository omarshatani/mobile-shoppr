package com.shoppr.domain.datasource;

import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.List;

public interface FirebaseStorageDataSource {

	interface UploadCallbacks {
		void onSuccess(@NonNull List<String> downloadUrls);

		void onError(@NonNull String message);
	}

	/**
	 * Uploads a list of images from local URIs to cloud storage.
	 *
	 * @param imageUris The list of local content URIs for the images to upload.
	 * @param callbacks The callbacks to be invoked on completion with a list of public download URLs.
	 */
	void uploadImages(@NonNull List<Uri> imageUris, @NonNull UploadCallbacks callbacks);
}