package com.mikhailkarpov.bloggingnetwork.feed.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserFeed {

    private final List<Post> posts;

}
