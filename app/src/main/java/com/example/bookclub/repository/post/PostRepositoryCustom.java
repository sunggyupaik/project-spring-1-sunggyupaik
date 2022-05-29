package com.example.bookclub.repository.post;

import com.example.bookclub.domain.Post;

import java.util.List;

public interface PostRepositoryCustom {
	List<Post> findByContent(String content);
}
