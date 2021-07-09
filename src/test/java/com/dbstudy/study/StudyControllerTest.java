package com.dbstudy.study;

import com.dbstudy.WithAccount;
import com.dbstudy.account.AccountRepository;
import com.dbstudy.domain.Account;
import com.dbstudy.domain.Study;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class StudyControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired StudyService studyService;
    @Autowired StudyRepository studyRepository;
    @Autowired AccountRepository accountRepository;

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @Test
    @WithAccount("dongbin")
    @DisplayName("스터디 개설 폼 조회")
    void createStudyForm() throws Exception {
        mockMvc.perform(get("/new-study"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("studyForm"));
    }

    @Test
    @WithAccount("dongbin")
    @DisplayName("스터디 개설 - 완료")
    void createStudy_success() throws Exception {
        String testPath = "test-path";
        mockMvc.perform(post("/new-study")
                .param("path", testPath)
                .param("title", "study title")
                .param("shortDescription", "short description of a study")
                .param("fullDescription", "full description of a study")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/test-path"));

        Study study = studyRepository.findByPath(testPath);
        assertNotNull(study);
        Account account = accountRepository.findByNickname("dongbin");
        assertTrue(study.getManagers().contains(account));
    }

    @Test
    @WithAccount("dongbin")
    @DisplayName("스터디 개설 - 실패")
    void createStudy_fail() throws Exception {
        String testPath = "wrong path";
        mockMvc.perform(post("/new-study")
                .param("path", testPath)
                .param("title", "study title")
                .param("shortDescription", "short description of a study")
                .param("fullDescription", "full description of a study")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("studyForm"))
                .andExpect(model().attributeExists("account"));

        Study study = studyRepository.findByPath(testPath);
        assertNull(study);
    }

    @Test
    @WithAccount("dongbin")
    @DisplayName("스터디 조회")
    void viewStudy() throws Exception {
        String path = "test-path";

        Study study = new Study();
        study.setPath(path);
        study.setTitle("test study");
        study.setShortDescription("short description");
        study.setFullDescription("<p>full description<p>");

        Account dongbin = accountRepository.findByNickname("dongbin");
        studyService.createNewStudy(study, dongbin);

        mockMvc.perform(get("/study/" + path))
                .andExpect(view().name("study/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }
}