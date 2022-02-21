package com.example.bookclub.infra;

import com.example.bookclub.domain.StudyComment;
import com.example.bookclub.domain.StudyCommentRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface JpaStudyCommentRepository
		extends StudyCommentRepository, CrudRepository<StudyComment, Long> {
	StudyComment save(StudyComment studyComment);

	List<StudyComment> findByStudyId(Long id);

	void delete(StudyComment studyComment);

	Optional<StudyComment> findById(Long id);
}