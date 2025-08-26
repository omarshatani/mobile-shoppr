package com.shoppr.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class ActivityEntry implements Parcelable {
	private String actorId;
	private String actorName;
	private String description;
	@ServerTimestamp
	private Date createdAt;

	public ActivityEntry() {
	}

	public ActivityEntry(String actorId, String actorName, String description) {
		this.actorId = actorId;
		this.actorName = actorName;
		this.description = description;
	}

	public String getActorId() {
		return actorId;
	}

	public void setActorId(String actorId) {
		this.actorId = actorId;
	}

	public String getActorName() {
		return actorName;
	}

	public void setActorName(String actorName) {
		this.actorName = actorName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	protected ActivityEntry(Parcel in) {
		actorId = in.readString();
		actorName = in.readString();
		description = in.readString();
		long tmpDate = in.readLong();
		createdAt = tmpDate == -1 ? null : new Date(tmpDate);
	}

	public static final Creator<ActivityEntry> CREATOR = new Creator<ActivityEntry>() {
		@Override
		public ActivityEntry createFromParcel(Parcel in) {
			return new ActivityEntry(in);
		}

		@Override
		public ActivityEntry[] newArray(int size) {
			return new ActivityEntry[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(actorId);
		dest.writeString(actorName);
		dest.writeString(description);
		dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
	}
}