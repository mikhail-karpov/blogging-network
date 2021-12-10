package com.mikhailkarpov.bloggingnetwork.feed.services;

import com.mikhailkarpov.bloggingnetwork.feed.client.PostServiceClient;
import com.mikhailkarpov.bloggingnetwork.feed.model.Post;
import com.mikhailkarpov.bloggingnetwork.feed.model.UserFeed;
import com.mikhailkarpov.bloggingnetwork.feed.repository.PostCacheRepository;
import com.mikhailkarpov.bloggingnetwork.feed.repository.UserFeedCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserFeedServiceImpl implements UserFeedService {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserFeedCacheRepository userFeedCacheRepository;
    private final PostCacheRepository postCacheRepository;
    private final PostServiceClient postServiceClient;

    @Override
    @Transactional
    public void startFollowing(String followerUserId, String followingUserId) {
        this.redisTemplate.opsForSet().add(getFollowersKey(followingUserId), followerUserId);
    }

    @Override
    @Transactional
    public void stopFollowing(String followerUserId, String followingUserId) {
        this.redisTemplate.opsForSet().remove(getFollowersKey(followingUserId), followerUserId);
    }

    @Override
    @Transactional
    public void addPost(String creatorId, String postId) {

        Set<String> followers = this.redisTemplate.opsForSet().members(getFollowersKey(creatorId));
        ListOperations<String, String> feed = this.redisTemplate.opsForList();

        for (String follower : followers) {
            String feedKey = getFeedKey(follower);
            feed.leftPush(feedKey, postId);
            feed.trim(feedKey, 0L, 100L);
        }
    }

    @Override
    @Transactional
    public void removePost(String creatorId, String postId) {
        this.postCacheRepository.deleteById(postId);
    }

    @Override
    @Transactional
    public void generateUserFeed(String userId) {
        List<Post> posts = this.redisTemplate.opsForList()
                .range(getFeedKey(userId), 0L, -1L)
                .stream()
                .map(this::getPost)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        this.userFeedCacheRepository.save(new UserFeed(userId, posts));
    }

    @Override
    @Transactional
    public List<Post> getUserFeed(String userId, int page, int size) {

        Optional<UserFeed> userFeed = this.userFeedCacheRepository.findById(userId);
        if (!userFeed.isPresent())
            return Collections.emptyList();

        return userFeed.get().getPosts(page, size);
    }

    private String getFollowersKey(String userId) {
        return String.format("followers:%s", userId);
    }

    private String getFeedKey(String userId) {
        return String.format("feed:%s", userId);
    }

    private Optional<Post> getPost(String postId) {
        Optional<Post> post = this.postCacheRepository.findById(postId);
        if (post.isPresent()) {
            return post;
        }

        post = this.postServiceClient.getPostById(postId);
        if (post.isPresent()) {
            this.postCacheRepository.save(post.get());
            return post;
        }

        return Optional.empty();
    }
}
