package com.example.bookclub.application;

import com.example.bookclub.domain.Account;
import com.example.bookclub.domain.Study;
import com.example.bookclub.domain.StudyComment;
import com.example.bookclub.domain.StudyCommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class StudyCommentServiceTest {
	private static final Long STUDY_COMMENT_EXISTED_ID = 1L;
	private static final String STUDY_COMMENT_CONTENT = "studyCommentContent";

	private static final Long ACCOUNT_SETUP_ID = 2L;

	private static final Long STUDY_SETUP_ID = 3L;

	private Account account;
	private Study study;
	private StudyComment studyComment;

	private StudyCommentService studyCommentService;
	private StudyCommentRepository studyCommentRepository;
	private StudyService studyService;

	@BeforeEach
	void setUp() {
		studyCommentRepository = mock(StudyCommentRepository.class);
		studyService = mock(StudyService.class);
		studyCommentService = new StudyCommentService(
				studyCommentRepository, studyService
		);

		account = Account.builder()
				.id(ACCOUNT_SETUP_ID)
				.build();

		study = Study.builder()
				.id(STUDY_SETUP_ID)
				.build();

		studyComment = StudyComment.builder()
				.id(STUDY_COMMENT_EXISTED_ID)
				.account(account)
				.study(study)
				.build();
	}

	@Test
	void getStudyCommentWithExistedId() {
		given(studyCommentRepository.findById(STUDY_COMMENT_EXISTED_ID)).willReturn(Optional.of(studyComment));

		StudyComment getStudyComment = studyCommentService.getStudyComment(STUDY_COMMENT_EXISTED_ID);

		assertThat(getStudyComment.getId()).isEqualTo(STUDY_COMMENT_EXISTED_ID);
	}
}
