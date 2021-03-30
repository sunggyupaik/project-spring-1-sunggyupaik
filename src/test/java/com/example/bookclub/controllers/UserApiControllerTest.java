package com.example.bookclub.controllers;

import com.example.bookclub.application.UserService;
import com.example.bookclub.domain.User;
import com.example.bookclub.dto.UserCreateDto;
import com.example.bookclub.dto.UserResultDto;
import com.example.bookclub.dto.UserUpdateDto;
import com.example.bookclub.errors.UserEmailDuplicatedException;
import com.example.bookclub.errors.UserNicknameDuplicatedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserApiController.class)
class UserApiControllerTest {
    private static final Long EXISTED_ID = 1L;
    private static final String SETUP_NAME = "name";
    private static final String SETUP_EMAIL = "email";
    private static final String SETUP_NICKNAME = "nickname";
    private static final String SETUP_PASSWORD = "1234567890";
    private static final String SETUP_PROFILEIMAGE = "image";

    private static final Long CREATED_ID = 2L;
    private static final String CREATED_NAME = "creatName";
    private static final String CREATED_EMAIL = "createEmail";
    private static final String CREATED_NICKNAME = "createNickname";
    private static final String CREATED_PASSWORD = "0987654321";
    private static final String CREATED_PROFILEIMAGE = "createdImage";

    private static final String UPDATED_NICKNAME = "qwer";
    private static final String UPDATED_PASSWORD = "5678";
    private static final String UPDATED_PROFILEIMAGE = "picture";

    private static final String EXISTED_EMAIL = "email";

    private User setUpUser;
    private User createdUser;

    private UserCreateDto userCreateDto;
    private UserUpdateDto userUpdateDto;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(print())
                .build();

        setUpUser = User.builder()
                .id(EXISTED_ID)
                .name(SETUP_NAME)
                .email(SETUP_EMAIL)
                .nickname(SETUP_NICKNAME)
                .password(SETUP_PASSWORD)
                .profileImage(SETUP_PROFILEIMAGE)
                .build();

        createdUser = User.builder()
                .name(CREATED_NAME)
                .email(CREATED_EMAIL)
                .nickname(CREATED_NICKNAME)
                .password(CREATED_PASSWORD)
                .profileImage(CREATED_PROFILEIMAGE)
                .build();

        userCreateDto = UserCreateDto.builder()
                .name(CREATED_NAME)
                .email(CREATED_EMAIL)
                .nickname(CREATED_NICKNAME)
                .password(CREATED_PASSWORD)
                .profileImage(CREATED_PROFILEIMAGE)
                .build();

        userUpdateDto = UserUpdateDto.builder()
                .nickname(UPDATED_NICKNAME)
                .password(UPDATED_PASSWORD)
                .profileImage(UPDATED_PROFILEIMAGE)
                .build();
    }

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private WebApplicationContext ctx;

    @MockBean
    UserService userService;

    @Test
    void detailWithExistedId() throws Exception {
        given(userService.getUser(EXISTED_ID)).willReturn(setUpUser);

        mockMvc.perform(
                get("/api/users/{id}", EXISTED_ID)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(setUpUser.getId()))
                .andExpect(jsonPath("name").value(setUpUser.getName()))
                .andExpect(jsonPath("email").value(setUpUser.getEmail()))
                .andExpect(jsonPath("nickname").value(setUpUser.getNickname()))
                .andExpect(jsonPath("password").value(setUpUser.getPassword()))
                .andExpect(jsonPath("profileImage").value(setUpUser.getProfileImage()))
                .andExpect(jsonPath("deleted").value(false));
    }

    @Test
    void createWithValidAttribute() throws Exception {
        given(userService.createUser(any(UserCreateDto.class))).will(invocation -> {
            UserCreateDto userCreateDto = invocation.getArgument(0);
            return UserResultDto.builder()
                    .id(CREATED_ID)
                    .name(userCreateDto.getName())
                    .email(userCreateDto.getEmail())
                    .nickname(userCreateDto.getNickname())
                    .password(userCreateDto.getPassword())
                    .profileImage(userCreateDto.getProfileImage())
                    .build();
        });

        mockMvc.perform(
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto))
        )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value(CREATED_ID))
                .andExpect(jsonPath("name").value(CREATED_NAME))
                .andExpect(jsonPath("email").value(CREATED_EMAIL))
                .andExpect(jsonPath("nickname").value(CREATED_NICKNAME))
                .andExpect(jsonPath("password").value(CREATED_PASSWORD))
                .andExpect(jsonPath("profileImage").value(CREATED_PROFILEIMAGE))
                .andExpect(jsonPath("deleted").value(false));
    }

    @Test
    void createWithExistedEmail() throws Exception {
        given(userService.createUser(any(UserCreateDto.class)))
                .willThrow(UserEmailDuplicatedException.class);

        mockMvc.perform(
                post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto))
        )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWithExistedNickname() throws Exception {
        given(userService.createUser(any(UserCreateDto.class)))
                .willThrow(UserNicknameDuplicatedException.class);

        mockMvc.perform(
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto))
        )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteWithExistedId() throws Exception {
        mockMvc.perform(
                delete("/api/users/{id}", EXISTED_ID)
        )
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void updateWithValidAttribute() throws Exception {
        mockMvc.perform(
                patch("/api/users/{id}", EXISTED_ID)
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDto))
        )
                .andDo(print())
                .andExpect(status().isOk());
    }
}