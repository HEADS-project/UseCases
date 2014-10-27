package gr.atc.heads.model;

import java.io.Serializable;
import java.util.List;


public class HeadsPoint implements Serializable {

	private static final long serialVersionUID = 1L;

    private double latitude;
    private double longitude;
    private long captureTime;
    private String description;
    private String title;
    private List<Integer> tags;	//Tag IDs
    private String id;
    private String imageURL;
    private String largeImageUrl;

    private String imagePath;

    private String imageUri;

    private boolean selected;

    private String image;

    private String username;

	public HeadsPoint() {
        selected = false;
	}
	
    public HeadsPoint(double latitude, double longitude, long captureTime, String comments, String username, List<Integer> tags) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.captureTime = captureTime;
        this.description = comments;
        this.title = username;
        this.tags = tags;
        this.id = null;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
         this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getCaptureTime() {
        return captureTime;
    }

    public void setCaptureTime(long captureTime) {
        this.captureTime = captureTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Integer> getTags() {
        return tags;
    }

    public void setTags(List<Integer> tags) {
        this.tags = tags;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getImageURL() {
		return imageURL;
	}

    public void setLargeImageUrl(String largeImageUrl) {
        this.largeImageUrl = largeImageUrl;
    }

    public String getLargeImageURL() {
        return largeImageUrl;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}