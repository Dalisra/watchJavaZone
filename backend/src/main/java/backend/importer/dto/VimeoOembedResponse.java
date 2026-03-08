package backend.importer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VimeoOembedResponse(
        @JsonProperty("thumbnail_url") String thumbnailUrl,
        @JsonProperty("thumbnail_url_with_play_button") String thumbnailUrlWithPlayButton,
        @JsonProperty("duration") Integer duration,
        @JsonProperty("title") String title,
        @JsonProperty("upload_date") String uploadDate,
        @JsonProperty("width") Integer width,
        @JsonProperty("height") Integer height
) {
}
