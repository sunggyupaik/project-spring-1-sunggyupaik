package com.example.bookclub.application.post;

import com.example.bookclub.domain.post.Post;
import com.example.bookclub.infrastructure.post.ElasticPostRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class PostService {
	private final ElasticPostRepository elasticPostRepository;

	public PostService(ElasticPostRepository elasticPostRepository) {
		this.elasticPostRepository = elasticPostRepository;
	}

	public void create(Post post) {
		elasticPostRepository.save(post);
	}

	public List<Post> findByContent(String content) {
		return elasticPostRepository.findByContent(content);
	}

	public List<Post> listAll() {
		return elasticPostRepository.findAll();
	}
}
