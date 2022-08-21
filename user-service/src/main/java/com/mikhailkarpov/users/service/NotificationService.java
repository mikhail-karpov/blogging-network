package com.mikhailkarpov.users.service;

@FunctionalInterface
public interface NotificationService<T> {

    void send(T notification);
}
