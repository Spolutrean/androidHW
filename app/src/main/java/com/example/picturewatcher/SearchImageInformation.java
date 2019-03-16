package com.example.picturewatcher;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
class SearchImageInformation implements Serializable {
    public String total;
    public String total_pages;
    public List<ImageInformation> results;
}
