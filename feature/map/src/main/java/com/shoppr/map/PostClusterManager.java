package com.shoppr.map;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.shoppr.model.Post;

import java.util.List;

public class PostClusterManager {
	private static final String TAG = "PostClusterManager";

	private final GoogleMap googleMap;
	private final Context context;
	private final ClusterManager<PostClusterItem> clusterManager;
	private final OnPostMarkerClickListener markerClickListener;

	public interface OnPostMarkerClickListener {
		void onPostMarkerClicked(@NonNull Post post);
	}

	public PostClusterManager(
			@NonNull Context context,
			@NonNull GoogleMap googleMap,
			@NonNull OnPostMarkerClickListener markerClickListener
	) {
		this.context = context;
		this.googleMap = googleMap;
		this.markerClickListener = markerClickListener;

		// Initialize the ClusterManager
		this.clusterManager = new ClusterManager<>(context, googleMap);

		// Set a custom renderer to control how individual markers look (optional but recommended)
		this.clusterManager.setRenderer(new PostClusterRenderer(context, googleMap, this.clusterManager));

		// Set listeners on the ClusterManager
		this.clusterManager.setOnClusterItemClickListener(item -> {
			if (item != null && item.post != null) {
				this.markerClickListener.onPostMarkerClicked(item.post);
			}
			// Return true to indicate we've handled the click and prevent the default
			// behavior (which is to open an info window and center the camera).
			return true;
		});

		// Point the map's listeners to the ClusterManager
		googleMap.setOnCameraIdleListener(clusterManager);
		googleMap.setOnMarkerClickListener(clusterManager);
	}

	/**
	 * Clears all existing posts from the map and adds the new ones.
	 *
	 * @param posts The new list of posts to display.
	 */
	public void setPosts(@Nullable List<Post> posts) {
		clusterManager.clearItems();
		if (posts != null && !posts.isEmpty()) {
			Log.d(TAG, "Adding " + posts.size() + " posts to the map.");
			for (Post post : posts) {
				if (post.getLatitude() != null && post.getLongitude() != null) {
					PostClusterItem clusterItem = new PostClusterItem(
							post.getLatitude(),
							post.getLongitude(),
							post.getTitle(),
							post.getPrice(), // Snippet can be price or category
							post
					);
					clusterManager.addItem(clusterItem);
				}
			}
		}
		// Re-cluster all the items on the map
		clusterManager.cluster();
	}

	/**
	 * Cleans up listeners to prevent memory leaks.
	 */
	public void cleanup() {
		if (clusterManager != null) {
			clusterManager.setOnClusterItemClickListener(null);
			clusterManager.clearItems();
		}
	}

	/**
	 * Custom renderer to customize the appearance of individual post markers.
	 */
	private static class PostClusterRenderer extends DefaultClusterRenderer<PostClusterItem> {
		public PostClusterRenderer(Context context, GoogleMap map, ClusterManager<PostClusterItem> clusterManager) {
			super(context, map, clusterManager);
			// Set minimum cluster size if desired (e.g., don't cluster groups of less than 4)
			setMinClusterSize(4);
		}

		@Override
		protected void onBeforeClusterItemRendered(@NonNull PostClusterItem item, @NonNull com.google.android.gms.maps.model.MarkerOptions markerOptions) {
			// This is where you customize the individual marker before it's rendered.
			// You can set a custom icon, title, snippet, etc.
			// For now, it will use the default marker appearance.
			// Example:
			// markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_custom_post_marker));
			markerOptions.title(item.getTitle());
			markerOptions.snippet(item.getSnippet());

			super.onBeforeClusterItemRendered(item, markerOptions);
		}

		@Override
		protected void onClusterItemUpdated(@NonNull PostClusterItem item, @NonNull Marker marker) {
			// Called when an item is already rendered and needs updating.
			marker.setTitle(item.getTitle());
			marker.setSnippet(item.getSnippet());
			super.onClusterItemUpdated(item, marker);
		}
	}
}