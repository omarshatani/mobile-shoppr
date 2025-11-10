package com.shoppr.data.datasource;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.shoppr.domain.datasource.FirebaseStorageDataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseStorageDataSourceImpl implements FirebaseStorageDataSource {

	private final FirebaseStorage storage;
	private static final String POST_IMAGES_PATH = "post_images";

	@Inject
	public FirebaseStorageDataSourceImpl(FirebaseStorage storage) {
		this.storage = storage;
	}

	@Override
	public void uploadImages(@NonNull List<Uri> imageUris, @NonNull UploadCallbacks callbacks) {
		if (imageUris.isEmpty()) {
			callbacks.onSuccess(new ArrayList<>());
			return;
		}

		List<Task<Uri>> uploadTasks = new ArrayList<>();
		StorageReference storageRef = storage.getReference().child(POST_IMAGES_PATH);

		for (Uri uri : imageUris) {
			// Create a unique path for each image
			final StorageReference imageRef = storageRef.child(UUID.randomUUID().toString() + "_" + uri.getLastPathSegment());
			UploadTask uploadTask = imageRef.putFile(uri);

			// Chain tasks to get the download URL after the upload is complete
			Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
				if (!task.isSuccessful()) {
					throw task.getException();
				}
				return imageRef.getDownloadUrl();
			});
			uploadTasks.add(urlTask);
		}

		// Wait for all upload and URL retrieval tasks to complete
		Tasks.whenAllSuccess(uploadTasks).addOnSuccessListener(results -> {
			List<String> downloadUrls = new ArrayList<>();
			for (Object result : results) {
				downloadUrls.add(result.toString());
			}
			callbacks.onSuccess(downloadUrls);
		}).addOnFailureListener(e -> {
			callbacks.onError("Image upload failed: " + e.getMessage());
		});
	}
}