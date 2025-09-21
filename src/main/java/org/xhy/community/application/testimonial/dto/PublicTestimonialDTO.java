package org.xhy.community.application.testimonial.dto;

public class PublicTestimonialDTO {

    private String id;
    private String userNickname;
    private String content;
    private Integer rating;

    public PublicTestimonialDTO() {
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserNickname() { return userNickname; }
    public void setUserNickname(String userNickname) { this.userNickname = userNickname; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
}