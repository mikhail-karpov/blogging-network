package com.mikhailkarpov.bloggingnetwork.feed.repository;

import com.mikhailkarpov.bloggingnetwork.feed.model.Post;
import org.springframework.data.repository.CrudRepository;

public interface PostRepository extends CrudRepository<Post, String> {
}
