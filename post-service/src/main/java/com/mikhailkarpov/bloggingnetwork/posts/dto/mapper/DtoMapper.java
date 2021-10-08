package com.mikhailkarpov.bloggingnetwork.posts.dto.mapper;

public interface DtoMapper<U, V> {

    V map(U u);
}
