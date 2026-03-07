package backend.domain;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

@MappedEntity("video_speakers")
public class VideoSpeaker {
    @Id
    private String videoId;
    @Id
    private String speakerId;
    private String bio;

    public VideoSpeaker() {
    }

    public VideoSpeaker(String videoId, String speakerId, String bio) {
        this.videoId = videoId;
        this.speakerId = speakerId;
        this.bio = bio;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getSpeakerId() {
        return speakerId;
    }

    public void setSpeakerId(String speakerId) {
        this.speakerId = speakerId;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
