package com.shoppr.map;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.shoppr.model.Post;

import java.util.ArrayList;
import java.util.List;

public class PostClusterManager {
    private static final String TAG = "PostClusterManager";

    private final GoogleMap googleMap;
    private final Context context;
    private final ClusterManager<PostClusterItem> clusterManager;
    private final OnPostMarkerClickListener markerClickListener;
    private final OnPostClusterClickListener clusterClickListener; // New listener for clusters

    public interface OnPostMarkerClickListener {
        void onPostMarkerClicked(@NonNull Post post);
    }

    public interface OnPostClusterClickListener {
        // Called when a cluster of posts at the same location is clicked
        void onSameLocationClusterClicked(@NonNull List<Post> posts);

        // Called when a cluster of posts at different locations is clicked
        void onDifferentLocationClusterClicked(@NonNull Cluster<PostClusterItem> cluster);
    }

    public PostClusterManager(
            @NonNull Context context,
            @NonNull GoogleMap googleMap,
            @NonNull OnPostMarkerClickListener markerClickListener,
            @NonNull OnPostClusterClickListener clusterClickListener // New listener
    ) {
        this.context = context;
        this.googleMap = googleMap;
        this.markerClickListener = markerClickListener;
        this.clusterClickListener = clusterClickListener;

        this.clusterManager = new ClusterManager<>(context, googleMap);
        this.clusterManager.setRenderer(new PostClusterRenderer(context, googleMap, this.clusterManager));

        this.clusterManager.setOnClusterItemClickListener(item -> {
            if (item != null && item.post != null) {
                this.markerClickListener.onPostMarkerClicked(item.post);
            }
            return true; // Mark as handled
        });

        this.clusterManager.setOnClusterClickListener(cluster -> {
            if (cluster == null || cluster.getItems().isEmpty()) {
                return false; // Let the map handle it (zoom)
            }
            // Check if all items in the cluster have the exact same location
            LatLng firstPosition = null;
            boolean allSameLocation = true;
            List<Post> postsInCluster = new ArrayList<>();
            for (PostClusterItem item : cluster.getItems()) {
                if (firstPosition == null) {
                    firstPosition = item.getPosition();
                } else if (!firstPosition.equals(item.getPosition())) {
                    allSameLocation = false;
                }
                postsInCluster.add(item.post);
            }

            if (allSameLocation) {
                Log.d(TAG, "Clicked cluster with " + postsInCluster.size() + " items at the SAME location.");
                this.clusterClickListener.onSameLocationClusterClicked(postsInCluster);
                return true; // We handled it, don't zoom
            } else {
                Log.d(TAG, "Clicked cluster with items at DIFFERENT locations. Letting map zoom.");
                this.clusterClickListener.onDifferentLocationClusterClicked(cluster);
                return false; // Let the default behavior (zoom) happen
            }
        });

        googleMap.setOnCameraIdleListener(clusterManager);
        googleMap.setOnMarkerClickListener(clusterManager);
    }

    public void setPosts(@Nullable List<Post> posts) {
        clusterManager.clearItems();
        if (posts != null && !posts.isEmpty()) {
            Log.d(TAG, "Adding " + posts.size() + " posts to the map.");
            for (Post post : posts) {
                if (post.getLatitude() != null && post.getLongitude() != null) {
                    PostClusterItem clusterItem = new PostClusterItem(
                            post.getLatitude(), post.getLongitude(),
                            post.getTitle(), post.getPrice(), post
                    );
                    clusterManager.addItem(clusterItem);
                }
            }
        }
        clusterManager.cluster();
    }

    public void cleanup() {
        if (clusterManager != null) {
            clusterManager.setOnClusterItemClickListener(null);
            clusterManager.setOnClusterClickListener(null);
            clusterManager.clearItems();
        }
    }

    private static class PostClusterRenderer extends DefaultClusterRenderer<PostClusterItem> {
        public PostClusterRenderer(Context context, GoogleMap map, ClusterManager<PostClusterItem> clusterManager) {
            super(context, map, clusterManager);
            setMinClusterSize(2); // Start clustering when 2 or more items are close
        }

        @Override
        protected void onBeforeClusterItemRendered(@NonNull PostClusterItem item, @NonNull com.google.android.gms.maps.model.MarkerOptions markerOptions) {
            markerOptions.title(item.getTitle());
            markerOptions.snippet(item.getSnippet());
            super.onBeforeClusterItemRendered(item, markerOptions);
        }

        @Override
        protected void onClusterItemUpdated(@NonNull PostClusterItem item, @NonNull Marker marker) {
            marker.setTitle(item.getTitle());
            marker.setSnippet(item.getSnippet());
            super.onClusterItemUpdated(item, marker);
        }
    }
}