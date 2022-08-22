package com.mikhailkarpov.bloggingnetwork.feed.services;

import com.mikhailkarpov.bloggingnetwork.feed.client.PostServiceClient;
import com.mikhailkarpov.bloggingnetwork.feed.model.Post;
import com.mikhailkarpov.bloggingnetwork.feed.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserFeedServiceImpl implements UserFeedService {

    private final RedisTemplate<String, String> redisTemplate;
    private final PostRepository postRepository;
    private final PostServiceClient postServiceClient;

    @Override
    @Transactional
    public void startFollowing(String followerUserId, String followingUserId) {
        redisTemplate.opsForSet().add(getFollowersKey(followingUserId), followerUserId);
    }

    @Override
    @Transactional
    public void stopFollowing(String followerUserId, String followingUserId) {
        redisTemplate.opsForSet().remove(getFollowersKey(followingUserId), followerUserId);
    }

    @Override
    @Transactional
    public void addPost(String creatorId, String postId) {

        Set<String> followers = redisTemplate.opsForSet().members(getFollowersKey(creatorId));
        ListOperations<String, String> feed = redisTemplate.opsForList();

        for (String follower : followers) {
            String feedKey = getFeedKey(follower);
            feed.leftPush(feedKey, postId);
            feed.trim(feedKey, 0L, 100L);
        }

        postServiceClient.getPostById(postId).ifPresent(postRepository::save);
    }

    @Override
    @Transactional
    public void removePost(String creatorId, String postId) {
        postRepository.deleteById(postId);
    }

    @Override
    @Transactional
    public List<Post> getUserFeed(String userId, int page, int size) {

        List<String> postIds = redisTemplate.opsForList().range(getFeedKey(userId), 0L, -1L);

        List<Post> posts = new ArrayList<>();
        postRepository.findAllById(postIds).forEach(posts::add);

        return posts.stream()
                .sorted(Comparator.comparing(Post::getCreatedDate).reversed())
                .skip(page * size)
                .limit(size)
                .collect(Collectors.toList());
    }

    private String getFollowersKey(String userId) {
        return String.format("followers:%s", userId);
    }

    private String getFeedKey(String userId) {
        return String.format("feed:%s", userId);
    }

}
