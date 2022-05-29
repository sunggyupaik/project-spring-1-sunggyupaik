package com.example.bookclub.dto;

import com.example.bookclub.domain.Account;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class AccountUpdateResultDto {
	private Long id;

	private String name;

	private String email;

	private String nickname;

	private String password;

	private boolean deleted;

	private UploadFileResultDto uploadFileResultDto;

	@Builder
	public AccountUpdateResultDto(Long id, String name, String email, String nickname, String password,
								  boolean deleted, UploadFileResultDto uploadFileResultDto) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.nickname = nickname;
		this.password = password;
		this.deleted = deleted;
		this.uploadFileResultDto = uploadFileResultDto;
	}

	public static AccountUpdateResultDto of(Account account) {
		return AccountUpdateResultDto.builder()
				.id(account.getId())
				.name(account.getName())
				.email(account.getEmail())
				.nickname(account.getNickname())
				.password(account.getPassword())
				.deleted(account.isDeleted())
				.uploadFileResultDto(UploadFileResultDto.of(account.getUploadFile()))
				.build();
	}

	public void setUploadFileResultDto(UploadFileResultDto uploadFileResultDto) {
		if(uploadFileResultDto == null) this.uploadFileResultDto = null;
	}
}