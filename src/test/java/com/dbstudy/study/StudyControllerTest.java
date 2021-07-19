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
public
class StudyControllerTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected StudyService studyService;
    @Autowired protected StudyRepository studyRepository;
    @Autowired protected AccountRepository accountRepository;

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

    @Test
    @WithAccount("dongbin")
    @DisplayName("스터디 가입")
    void joinStudy() throws Exception {
        Account account = createAccount("ehdqls");
        Study study = createStudy("testStudy",account);

        mockMvc.perform(get("/study/" + study.getPath() + "/join"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/members"));

        Account dongbin = accountRepository.findByNickname("dongbin");
        assertTrue(study.getMembers().contains(dongbin));
    }

    @Test
    @WithAccount("dongbin")
    @DisplayName("스터디 탈퇴")
    void leaveStudy() throws Exception {
        Account account = createAccount("ehdqls");
        Study study = createStudy("testStudy",account);

        Account dongbin = accountRepository.findByNickname("dongbin");
        studyService.addMember(study, dongbin);

        mockMvc.perform(get("/study/" + study.getPath() + "/leave"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/members"));

        assertFalse(study.getMembers().contains(dongbin));
    }

    protected Study createStudy(String path, Account account) {
        Study study = new Study();
        study.setPath(path);
        studyService.createNewStudy(study, account);
        return study;
    }

    protected Account createAccount(String nickname) {
        Account account = new Account();
        account.setNickname(nickname);
        account.setEmail(nickname+"@email.com");
        accountRepository.save(account);
        return account;
    }
}