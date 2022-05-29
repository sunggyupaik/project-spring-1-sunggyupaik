package com.example.bookclub.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyFavoriteResultDto {
	private Long id;

	@Builder
	public StudyFavoriteResultDto(Long id) {
		this.id = id;
	}

	public static StudyFavoriteResultDto of(Long id) {
		return StudyFavoriteResultDto.builder()
				.id(id)
				.build();
	}
}