package com.mikhailkarpov.bloggingnetwork.feed.services;

import com.mikhailkarpov.bloggingnetwork.feed.domain.PostActivity;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostActivityService {

    void save(PostActivity activity);

    void delete(PostActivity activity);

    List<PostActivity> getFeed(String userId, Pageable pageable);
}
