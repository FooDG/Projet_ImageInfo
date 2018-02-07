package com.projetinfo.piimv2;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by fuji on 07/02/18.
 */

public class Brand {
    String brandName;
    String url;
    File classifier;

    public Brand(String brandName, String url, File classifier) {
        this.brandName = brandName;
        this.url = url;
        this.classifier = classifier;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public File getClassifier() {
        return classifier;
    }

    public void setClassifier(File classifier) {
        this.classifier = classifier;
    }
}
