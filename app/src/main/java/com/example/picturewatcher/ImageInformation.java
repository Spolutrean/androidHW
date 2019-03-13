package com.example.picturewatcher;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
class ImageInformation implements Serializable {
    public String id;
    public String description;
    public String color;
    public Urls urls;
    public User user;

    public ImageInformation() { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Urls implements Serializable {
        public String raw, full, regular, small, thumb;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class User implements Serializable {
        public String name;
        public String instagram_username;
        public String twitter_username;
    }

}
