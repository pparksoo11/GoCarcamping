package com.example.GoAutoCamping;

import java.util.List;

public class SuppliesDTO {

    private String post_id;
    private String post_Image;
    private String post_name;
    private String post_memo;
    private String post_link;
    private Integer post_like;

    public String getPost_id() {
        return post_id;
    }

    public String getPost_Image() {
        return post_Image;
    }

    public String getPost_name() {
        return post_name;
    }

    public String getPost_memo() {
        return post_memo;
    }

    public String getPost_link() {
        return post_link;
    }

    public Integer getPost_like() {
        return post_like;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public void setPost_Image(String post_Image) {
        this.post_Image = post_Image;
    }

    public void setPost_name(String post_name) {
        this.post_name = post_name;
    }

    public void setPost_memo(String post_memo) {
        this.post_memo = post_memo;
    }

    public void setPost_link(String post_link) {
        this.post_link = post_link;
    }

    public void setPost_like(Integer post_like) {
        this.post_like = post_like;
    }
}
