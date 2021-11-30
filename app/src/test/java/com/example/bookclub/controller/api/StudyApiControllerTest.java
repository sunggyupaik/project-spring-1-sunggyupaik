package com.example.bookclub.controller.api;

import com.example.bookclub.application.AccountService;
import com.example.bookclub.application.StudyService;
import com.example.bookclub.domain.Account;
import com.example.bookclub.domain.Day;
import com.example.bookclub.domain.Study;
import com.example.bookclub.domain.StudyState;
import com.example.bookclub.domain.Zone;
import com.example.bookclub.dto.StudyCreateDto;
import com.example.bookclub.dto.StudyResultDto;
import com.example.bookclub.dto.StudyUpdateDto;
import com.example.bookclub.errors.AccountNotManagerOfStudyException;
import com.example.bookclub.errors.StudyAlreadyExistedException;
import com.example.bookclub.errors.StudyAlreadyInOpenOrClose;
import com.example.bookclub.errors.StudyNotAppliedBefore;
import com.example.bookclub.errors.StudyNotFoundException;
import com.example.bookclub.errors.StudySizeFullException;
import com.example.bookclub.errors.StudyStartAndEndDateNotValidException;
import com.example.bookclub.errors.StudyStartAndEndTimeNotValidException;
import com.example.bookclub.errors.StudyStartDateInThePastException;
import com.example.bookclub.security.AccountAuthenticationService;
import com.example.bookclub.security.CustomDeniedHandler;
import com.example.bookclub.security.CustomEntryPoint;
import com.example.bookclub.security.UserAccount;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudyApiController.class)
class StudyApiControllerTest {
    private static final Long ACCOUNT_ID = 2L;
    private static final String ACCOUNT_NAME = "accountName";
    private static final String ACCOUNT_EMAIL = "email";
    private static final String ACCOUNT_NICKNAME = "accountNickname";
    private static final String ACCOUNT_PASSWORD = "accountPassword";

    private static final Long ACCOUNT_SECOND_ID = 3L;
    private static final String ACCOUNT_SECOND_NAME = "accountSecondName";
    private static final String ACCOUNT_SECOND_EMAIL = "accountSecondEmail";
    private static final String ACCOUNT_SECOND_NICKNAME = "accountSecondNickname";
    private static final String ACCOUNT_SECOND_PASSWORD = "accountSecondPassword";

    private static final Long STUDY_SETUP_EXISTED_ID = 1L;
    private static final String STUDY_SETUP_NAME = "setupStudyName";
    private static final String STUDY_SETUP_EMAIL = ACCOUNT_SECOND_EMAIL;
    private static final String STUDY_SETUP_DESCRIPTION = "setupStudyDescription";
    private static final String STUDY_SETUP_CONTACT = "setupContact";
    private static final int STUDY_SETUP_SIZE = 5;
    private static final LocalDate STUDY_SETUP_START_DATE = LocalDate.now().plusDays(1);
    private static final LocalDate STUDY_SETUP_END_DATE = LocalDate.now().plusDays(7);
    private static final Day STUDY_SETUP_DAY = Day.MONDAY;
    private static final String STUDY_SETUP_START_TIME = "13:00";
    private static final String STUDY_SETUP_END_TIME = "15:30";
    private static final StudyState STUDY_SETUP_STUDY_STATE = StudyState.OPEN;
    private static final Zone STUDY_SETUP_ZONE = Zone.SEOUL;

    private static final String STUDY_UPDATE_NAME = "studyUpdatedName";
    private static final String STUDY_UPDATE_DESCRIPTION = "studyUpdatedDescription";
    private static final String STUDY_UPDATE_CONTACT = "studyUpdatedContact";
    private static final int STUDY_UPDATE_SIZE = 10;
    private static final LocalDate STUDY_UPDATE_START_DATE = LocalDate.now().plusDays(1);
    private static final LocalDate STUDY_UPDATE_END_DATE = LocalDate.now().plusDays(5);
    private static final Day STUDY_UPDATE_DAY = Day.THURSDAY;
    private static final String STUDY_UPDATE_START_TIME = "12:00";
    private static final String STUDY_UPDATE_END_TIME = "14:30";
    private static final StudyState STUDY_UPDATE_STUDY_STATE = StudyState.OPEN;
    private static final Zone STUDY_UPDATE_ZONE = Zone.BUSAN;

    private static final Long STUDY_FULL_SIZE_ID = 4L;
    private static final int STUDY_FULL_SIZE = 10;
    private static final int STUDY_FULL_SIZE_APPLY_COUNT = 10;

    private static final Long NOT_EXIST_STUDY_ID = 999L;
    private static final LocalDate CREATE_START_DATE_PAST = LocalDate.now().minusDays(1);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext ctx;

    @MockBean
    private StudyService studyService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private AccountAuthenticationService accountAuthenticationService;

    @MockBean
    private DataSource dataSource;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomEntryPoint customEntryPoint;

    @MockBean
    private CustomDeniedHandler customDeniedHandler;

    @MockBean
    private PersistentTokenRepository tokenRepository;

    private Account accountWithoutStudy;
    private Account accountWithSetupStudy;
    private UsernamePasswordAuthenticationToken accountWithoutStudyToken;
    private UsernamePasswordAuthenticationToken accountWithSetupStudyToken;
    private Study setUpStudy;
    private Study updatedStudy;
    private Study dateNotValidStudy;
    private Study fullSizeStudy;

    private StudyCreateDto studyCreateDto;
    private StudyCreateDto studyStartDateIsPastCreateDto;
    private StudyCreateDto studyStartDateIsAfterEndDateCreateDto;
    private StudyCreateDto studyStartTimeIsAfterEndTimeCreateDto;
    private StudyCreateDto studyAlreadyInOpenOrCloseCreateDto;

    private StudyUpdateDto studyUpdateDto;
    private StudyUpdateDto studyStartDateIsPastUpdateDto;
    private StudyUpdateDto studyStartDateIsAfterEndDateUpdateDto;

    private StudyResultDto studyResultDto;
    private StudyResultDto updatedStudyResultDto;

    private List<Study> list;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(print())
                .build();

        accountWithoutStudy = Account.builder()
                .id(ACCOUNT_ID)
                .name(ACCOUNT_NAME)
                .email(ACCOUNT_EMAIL)
                .nickname(ACCOUNT_NICKNAME)
                .password(ACCOUNT_PASSWORD)
                .build();

        accountWithSetupStudy = Account.builder()
                .id(ACCOUNT_SECOND_ID)
                .name(ACCOUNT_SECOND_NAME)
                .email(ACCOUNT_SECOND_EMAIL)
                .nickname(ACCOUNT_SECOND_NICKNAME)
                .password(ACCOUNT_SECOND_PASSWORD)
                .build();

        setUpStudy = Study.builder()
                .id(STUDY_SETUP_EXISTED_ID)
                .name(STUDY_SETUP_NAME)
                .email(ACCOUNT_SECOND_EMAIL)
                .description(STUDY_SETUP_DESCRIPTION)
                .contact(STUDY_SETUP_CONTACT)
                .size(STUDY_SETUP_SIZE)
                .startDate(STUDY_SETUP_START_DATE)
                .endDate(STUDY_SETUP_END_DATE)
                .startTime(STUDY_SETUP_START_TIME)
                .endTime(STUDY_SETUP_END_TIME)
                .day(STUDY_SETUP_DAY)
                .studyState(STUDY_SETUP_STUDY_STATE)
                .zone(STUDY_SETUP_ZONE)
                .build();

        setUpStudy.addAdmin(accountWithSetupStudy);

        updatedStudy = Study.builder()
                .id(STUDY_SETUP_EXISTED_ID)
                .name(STUDY_UPDATE_NAME)
                .description(STUDY_UPDATE_DESCRIPTION)
                .contact(STUDY_UPDATE_CONTACT)
                .size(STUDY_UPDATE_SIZE)
                .startDate(STUDY_UPDATE_START_DATE)
                .endDate(STUDY_UPDATE_END_DATE)
                .startTime(STUDY_UPDATE_START_TIME)
                .endTime(STUDY_UPDATE_END_TIME)
                .day(STUDY_UPDATE_DAY)
                .studyState(STUDY_UPDATE_STUDY_STATE)
                .zone(STUDY_UPDATE_ZONE)
                .build();

        fullSizeStudy = Study.builder()
                .id(STUDY_FULL_SIZE_ID)
                .size(STUDY_FULL_SIZE)
                .applyCount(STUDY_FULL_SIZE_APPLY_COUNT)
                .build();

        accountWithoutStudyToken = new UsernamePasswordAuthenticationToken(
                new UserAccount(accountWithoutStudy, List.of(new SimpleGrantedAuthority("USER"))),
                accountWithoutStudy.getPassword(),
                List.of(new SimpleGrantedAuthority("USER")));

        accountWithSetupStudyToken = new UsernamePasswordAuthenticationToken(
                new UserAccount(accountWithSetupStudy, List.of(new SimpleGrantedAuthority("USER"))),
                accountWithSetupStudy.getPassword(),
                List.of(new SimpleGrantedAuthority("USER")));

        studyCreateDto = StudyCreateDto.builder()
                .name(STUDY_SETUP_NAME)
                .email(STUDY_SETUP_EMAIL)
                .description(STUDY_SETUP_DESCRIPTION)
                .contact(STUDY_SETUP_CONTACT)
                .size(STUDY_SETUP_SIZE)
                .startDate(STUDY_SETUP_START_DATE)
                .endDate(STUDY_SETUP_END_DATE)
                .startTime(STUDY_SETUP_START_TIME)
                .endTime(STUDY_SETUP_END_TIME)
                .day(STUDY_SETUP_DAY)
                .zone(STUDY_SETUP_ZONE)
                .build();

        studyStartDateIsPastCreateDto = StudyCreateDto.builder()
                .startDate(CREATE_START_DATE_PAST)
                .endDate(STUDY_SETUP_END_DATE)
                .build();

        studyStartDateIsAfterEndDateCreateDto = StudyCreateDto.builder()
                .startDate(STUDY_SETUP_END_DATE)
                .endDate(STUDY_SETUP_START_DATE)
                .build();

        studyStartTimeIsAfterEndTimeCreateDto = StudyCreateDto.builder()
                .startTime(STUDY_SETUP_END_TIME)
                .endTime(STUDY_SETUP_START_TIME)
                .build();

        studyUpdateDto = StudyUpdateDto.builder()
                .name(STUDY_UPDATE_NAME)
                .description(STUDY_UPDATE_DESCRIPTION)
                .contact(STUDY_UPDATE_CONTACT)
                .size(STUDY_UPDATE_SIZE)
                .startDate(STUDY_UPDATE_START_DATE)
                .endDate(STUDY_UPDATE_END_DATE)
                .startTime(STUDY_UPDATE_START_TIME)
                .endTime(STUDY_UPDATE_END_TIME)
                .day(STUDY_UPDATE_DAY)
                .zone(STUDY_UPDATE_ZONE)
                .build();

        studyStartDateIsPastUpdateDto = StudyUpdateDto.builder()
                .startDate(CREATE_START_DATE_PAST)
                .endDate(STUDY_SETUP_END_DATE)
                .build();

        studyStartDateIsAfterEndDateUpdateDto = StudyUpdateDto.builder()
                .startDate(STUDY_SETUP_END_DATE)
                .endDate(STUDY_SETUP_START_DATE)
                .build();

        studyResultDto = StudyResultDto.of(setUpStudy);
        updatedStudyResultDto = StudyResultDto.of(updatedStudy);

        list = List.of(setUpStudy, updatedStudy);
    }

    @Test
    void listAllStudies() throws Exception {
        given(studyService.getStudies()).willReturn(list);

        mockMvc.perform(
                get("/api/study")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("\"name\":\"" + STUDY_SETUP_NAME)))
                .andExpect(content().string(StringContains.containsString("\"name\":\"" + STUDY_UPDATE_NAME)));
    }

    @Test
    void detailWithExistedId() throws Exception {
        given(studyService.getStudy(STUDY_SETUP_EXISTED_ID)).willReturn(setUpStudy);

        mockMvc.perform(
                get("/api/study/{id}", STUDY_SETUP_EXISTED_ID)
        )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void detailWithNotExistedId() throws Exception {
        given(studyService.getStudy(NOT_EXIST_STUDY_ID)).willThrow(new StudyNotFoundException(NOT_EXIST_STUDY_ID));

        mockMvc.perform(
                get("/api/study/{id}", NOT_EXIST_STUDY_ID)
        )
                .andDo(print())
                .andExpect(content().string(containsString("Study not found")))
                .andExpect(status().isNotFound());

        verify(studyService).getStudy(NOT_EXIST_STUDY_ID);
    }

    @Test
    void createWithValidateAttribute() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(accountWithoutStudyToken);
        given(studyService.createStudy(eq(ACCOUNT_EMAIL), any(StudyCreateDto.class)))
                .willReturn(studyResultDto);

        mockMvc.perform(
                post("/api/study")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(studyCreateDto))
        )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value(studyResultDto.getId()))
                .andExpect(jsonPath("name").value(studyResultDto.getName()))
                .andExpect(jsonPath("description").value(studyResultDto.getDescription()));
    }

    @Test
    void createWithStartDateIsTodayOrBeforeInvalid() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(accountWithoutStudyToken);
        given(studyService.createStudy(eq(ACCOUNT_EMAIL), any(StudyCreateDto.class)))
                .willThrow(new StudyStartDateInThePastException());

        mockMvc.perform(
            post("/api/study")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(studyStartDateIsPastCreateDto))
            )
                .andDo(print())
                .andExpect(content().string(containsString("Study startDate in the past")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWithStartDateIsAfterEndDateInvalid() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(accountWithoutStudyToken);
        given(studyService.createStudy(eq(ACCOUNT_EMAIL), any(StudyCreateDto.class)))
                .willThrow(new StudyStartAndEndDateNotValidException());

        mockMvc.perform(
                post("/api/study")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studyStartDateIsAfterEndDateCreateDto))
                )
                    .andDo(print())
                    .andExpect(content().string(containsString("Study StartDate and EndDate not valid")))
                    .andExpect(status().isBadRequest());
    }

    @Test
    void createWithStartTimeIsAfterTimeInvalid() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(accountWithoutStudyToken);
        given(studyService.createStudy(eq(ACCOUNT_EMAIL), any(StudyCreateDto.class)))
                .willThrow(new StudyStartAndEndTimeNotValidException());

        mockMvc.perform(
                        post("/api/study")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(studyStartTimeIsAfterEndTimeCreateDto))
                )
                .andDo(print())
                .andExpect(content().string(containsString("Study StartTime and EndTime not valid")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWithAccountAlreadyInStudyOpenOrClose() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(accountWithSetupStudyToken);
        given(studyService.createStudy(eq(ACCOUNT_SECOND_EMAIL), any(StudyCreateDto.class)))
                .willThrow(new StudyAlreadyInOpenOrClose());

        mockMvc.perform(
                        post("/api/study")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(studyCreateDto))
                )
                .andDo(print())
                .andExpect(content().string(containsString("account already has study in open or close")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateWithValidateAttribute() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(accountWithSetupStudyToken);
        given(studyService.updateStudy(eq(ACCOUNT_SECOND_EMAIL), eq(STUDY_SETUP_EXISTED_ID), any(StudyUpdateDto.class)))
                .willReturn(updatedStudyResultDto);

        mockMvc.perform(
                patch("/api/study/{id}", STUDY_SETUP_EXISTED_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studyUpdateDto))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(updatedStudyResultDto.getName()))
                .andExpect(jsonPath("description").value(updatedStudyResultDto.getDescription()))
                .andExpect(jsonPath("contact").value(updatedStudyResultDto.getContact()));
    }

    @Test
    void updateWithNotManager() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(accountWithoutStudyToken);
        given(studyService.updateStudy(eq(ACCOUNT_EMAIL), eq(STUDY_SETUP_EXISTED_ID), any(StudyUpdateDto.class)))
                .willThrow(AccountNotManagerOfStudyException.class);

        mockMvc.perform(
                        patch("/api/study/{id}", STUDY_SETUP_EXISTED_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(studyUpdateDto))
                )
                .andDo(print())
//                .andExpect(content().string(containsString("Not Manager Of Study")))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateWithStartDateInPastInvalid() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(accountWithSetupStudyToken);
        given(studyService.updateStudy(eq(ACCOUNT_SECOND_EMAIL), eq(STUDY_SETUP_EXISTED_ID), any(StudyUpdateDto.class)))
                .willThrow(StudyStartDateInThePastException.class);

        mockMvc.perform(
                        patch("/api/study/{id}", STUDY_SETUP_EXISTED_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(studyStartDateIsPastUpdateDto))
                )
                .andDo(print())
                //.andExpect(content().string(containsString("Study startDate in the past")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateWithStartDateIsAfterEndDateInvalid() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(accountWithSetupStudyToken);
        given(studyService.updateStudy(eq(ACCOUNT_SECOND_EMAIL), eq(STUDY_SETUP_EXISTED_ID), any(StudyUpdateDto.class)))
                .willThrow(StudyStartAndEndDateNotValidException.class);

        mockMvc.perform(
                        patch("/api/study/{id}", STUDY_SETUP_EXISTED_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(studyStartDateIsPastUpdateDto))
                )
                .andDo(print())
//                .andExpect(content().string(containsString("Study StartDate and EndDate not valid")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateWithStartTimeIsAfterEndTimeInvalid() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(accountWithSetupStudyToken);
        given(studyService.updateStudy(eq(ACCOUNT_SECOND_EMAIL), eq(STUDY_SETUP_EXISTED_ID), any(StudyUpdateDto.class)))
                .willThrow(StudyStartAndEndDateNotValidException.class);

        mockMvc.perform(
                        patch("/api/study/{id}", STUDY_SETUP_EXISTED_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(studyStartDateIsPastUpdateDto))
                )
                .andDo(print())
                //.andExpect(content().string(containsString("Study StartDate and EndDate not valid")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteByExistedId() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(accountWithSetupStudyToken);
        given(studyService.deleteStudy(eq(ACCOUNT_SECOND_EMAIL), eq(STUDY_SETUP_EXISTED_ID)))
                .willReturn(studyResultDto);

        mockMvc.perform(
                delete("/api/study/{id}", STUDY_SETUP_EXISTED_ID)
        )
                .andDo(print())
                .andExpect(jsonPath("name").value(setUpStudy.getName()))
                .andExpect(jsonPath("description").value(setUpStudy.getDescription()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteWithNotManager() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(accountWithoutStudyToken);
        given(studyService.deleteStudy(eq(ACCOUNT_EMAIL), eq(STUDY_SETUP_EXISTED_ID)))
                .willThrow(AccountNotManagerOfStudyException.class);

        mockMvc.perform(
                        delete("/api/study/{id}", STUDY_SETUP_EXISTED_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
//                .andExpect(content().string(containsString("Not Manager Of Study")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteByNotExistedId() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(accountWithSetupStudyToken);
        given(studyService.deleteStudy(eq(ACCOUNT_SECOND_EMAIL), eq(NOT_EXIST_STUDY_ID)))
                .willThrow(StudyNotFoundException.class);

        mockMvc.perform(
                        delete("/api/study/{id}", NOT_EXIST_STUDY_ID)
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void applyStudyByExistedAccount() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(accountWithoutStudyToken);
        given(studyService.applyStudy(any(UserAccount.class), eq(STUDY_SETUP_EXISTED_ID)))
                .willReturn(STUDY_SETUP_EXISTED_ID);

        mockMvc.perform(
                post("/api/study/apply/{id}", STUDY_SETUP_EXISTED_ID)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void applyStudyByNotExistedStudy() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(accountWithoutStudyToken);
        given(studyService.applyStudy(any(UserAccount.class), eq(NOT_EXIST_STUDY_ID)))
                .willThrow(StudyNotFoundException.class);

        mockMvc.perform(
                        post("/api/study/apply/{id}", NOT_EXIST_STUDY_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void applyStudyWithAlreadyHasStudy() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(accountWithoutStudyToken);
        given(studyService.applyStudy(any(UserAccount.class), eq(STUDY_SETUP_EXISTED_ID)))
                .willThrow(StudyAlreadyExistedException.class);

        mockMvc.perform(
                        post("/api/study/apply/{id}", STUDY_SETUP_EXISTED_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void applyStudyWithFullSize() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(accountWithoutStudyToken);
        given(studyService.applyStudy(any(UserAccount.class), eq(STUDY_FULL_SIZE_ID)))
                .willThrow(StudySizeFullException.class);

        mockMvc.perform(
                        post("/api/study/apply/{id}", STUDY_FULL_SIZE_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelStudyByExistedAccount() throws Exception {
        setUpStudy.addAccount(accountWithoutStudy);
        SecurityContextHolder.getContext().setAuthentication(accountWithoutStudyToken);
        given(studyService.cancelStudy(any(UserAccount.class), eq(STUDY_SETUP_EXISTED_ID)))
                .willReturn(STUDY_SETUP_EXISTED_ID);

        mockMvc.perform(
                post("/api/study/cancel/{id}", STUDY_SETUP_EXISTED_ID)
        )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void cancelStudyByNotExistedStudy() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(accountWithoutStudyToken);
        given(studyService.cancelStudy(any(UserAccount.class), eq(NOT_EXIST_STUDY_ID)))
                .willThrow(StudyNotFoundException.class);

        mockMvc.perform(
                        post("/api/study/cancel/{id}", NOT_EXIST_STUDY_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelStudyByNotAppliedStudy() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(accountWithoutStudyToken);
        given(studyService.cancelStudy(any(UserAccount.class), eq(STUDY_SETUP_EXISTED_ID)))
                .willThrow(StudyNotAppliedBefore.class);

        mockMvc.perform(
                        post("/api/study/cancel/{id}", STUDY_SETUP_EXISTED_ID)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}