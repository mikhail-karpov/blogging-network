package com.mikhailkarpov.bloggingnetwork.feed.services;

import com.mikhailkarpov.bloggingnetwork.feed.model.Post;

import java.util.List;

public interface UserFeedService {

    void startFollowing(String followerUserId, String followingUserId);

    void stopFollowing(String followerUserId, String followingUserId);

    void addPost(String creatorId, String postId);

    void removePost(String creatorId, String postId);

    List<Post> getUserFeed(String userId, int page, int size);
}
