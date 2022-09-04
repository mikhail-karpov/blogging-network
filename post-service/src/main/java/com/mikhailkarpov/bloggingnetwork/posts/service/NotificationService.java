package com.mikhailkarpov.bloggingnetwork.posts.service;

@FunctionalInterface
public interface NotificationService<T> {

    void send(T t);
}
