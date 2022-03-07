package com.example.bookclub.controller.api;

import com.example.bookclub.application.AccountService;
import com.example.bookclub.application.UploadFileService;
import com.example.bookclub.domain.Account;
import com.example.bookclub.domain.UploadFile;
import com.example.bookclub.dto.AccountCreateDto;
import com.example.bookclub.dto.AccountResultDto;
import com.example.bookclub.dto.AccountUpdateDto;
import com.example.bookclub.dto.AccountUpdatePasswordDto;
import com.example.bookclub.security.CurrentAccount;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

/**
 * 회원가입, 정보수정, 사용자 비밀번호 변경, 삭제를 요청한다
 */
@RestController
@RequestMapping("/api/users")
public class AccountApiController {
    private final AccountService accountService;
    private final UploadFileService uploadFileService;

    public AccountApiController(AccountService accountService,
                                UploadFileService uploadFileService) {
        this.accountService = accountService;
        this.uploadFileService = uploadFileService;
    }

    /**
     * 주어진 아이디에 해당하는 사용자를 조회한다
     *
     * @param id 사용자 아이디
     * @return 사용자
     */
    @GetMapping("/{id}")
    public AccountResultDto get(@PathVariable Long id) {
        return accountService.getUser(id);
    }

    /**
     * 주어진 사진, 가입정보로 사용자를 생성한다
     *
     * @param uploadFile 사용자 사진
     * @param accountCreateDto 회원가입 정보
     * @return 사용자
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResultDto create(@RequestPart(required = false) MultipartFile uploadFile,
                                   AccountCreateDto accountCreateDto) {
        if (uploadFile == null) {
            return accountService.createUser(accountCreateDto, null);
        }

        UploadFile accountFile = uploadFileService.upload(uploadFile);
        return accountService.createUser(accountCreateDto, accountFile);
    }

    /**
     * 주어진 아이디, 사진, 수정내용으로 사용자를 수정한다
     *
     * @param account 로그인한 사용자
     * @param id 사용자 아이디
     * @param uploadFile 사용자 사진
     * @param accountUpdateDto 변경 할 사용자 정보
     * @return 사용자
     * @throws AccessDeniedException 경로 아이디와 로그인한 사용자의 아이디가 다른 경우
     */
    @PreAuthorize("#account.id == #id")
    @PostMapping("/{id}")
    public AccountResultDto update(@CurrentAccount Account account,
                                   @PathVariable Long id,
                                   @RequestPart(required = false) MultipartFile uploadFile,
                                   AccountUpdateDto accountUpdateDto) {
    if (uploadFile == null) {
            return accountService.updateUser(id, accountUpdateDto, null);
        }

        UploadFile accountFile = uploadFileService.upload(uploadFile);
        return accountService.updateUser(id, accountUpdateDto, accountFile);
    }

    /**
     * 주어진 아이디, 변경 할 비밀번호로 사용자 비밀번호를 변경한다
     *
     * @param account 로그인한 사용자
     * @param id 사용자 아이디
     * @param accountUpdatePasswordDto 변경 할 사용자 비밀번호
     * @return 사용자
     */
    @PreAuthorize("#account.id == #id")
    @PatchMapping("/{id}/password")
    public AccountResultDto updatePassword(@CurrentAccount Account account,
                                           @PathVariable Long id,
                                           @Valid @RequestBody AccountUpdatePasswordDto accountUpdatePasswordDto) {
        return accountService.updatePassword(id, accountUpdatePasswordDto);
    }

    /**
     * 주어진 아이디로 사용자를 삭제한다
     *
     * @param account 로그인한 사용자
     * @param id 사용자 아이디
     * @return 사용자
     */
    @PreAuthorize("#account.id == #id")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public AccountResultDto delete(@CurrentAccount Account account,
                                   @PathVariable Long id) {
        return accountService.deleteUser(id);
    }
}
