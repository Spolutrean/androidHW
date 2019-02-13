package com.example.picturewatcher;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
class ImageInformation {
    public String id;
    public String description;
    public String instagram_username = null;
    public String twitter_username = null;
    public String portfolio_url;
    public String color;
    public Urls urls;

    public ImageInformation() { }

    public class Urls {
        public String raw, full, regular, small, thumb;
    }

}
