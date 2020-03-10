package com.xu.xmaster.beans;

public class FileBean {

    private String url;
    private String name;
    private Long duration;
    private int width;
    private int height;

    public FileBean() {
    }

    public FileBean(String url, String name, Long duration, int width, int height) {
        this.url = url;
        this.name = name;
        this.duration = duration;
        this.width = width;
        this.height = height;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
