package com.dbstudy.study;

import com.dbstudy.WithAccount;
import com.dbstudy.domain.Account;
import com.dbstudy.domain.Study;
import com.dbstudy.domain.Tag;
import com.dbstudy.study.form.TagForm;
import com.dbstudy.study.form.ZoneForm;
import com.dbstudy.tag.TagRepository;
import com.dbstudy.tag.TagService;
import com.dbstudy.zone.ZoneService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
class StudySettingsControllerTest extends StudyControllerTest{

    @Autowired private ObjectMapper objectMapper;
    @Autowired private TagRepository tagRepository;
    @Autowired private TagService tagService;


    @Test
    @WithAccount("dongbin")
    @DisplayName("스터디 소개 수정 폼 - 실패 (권한 없는 유저)")
    void updateDescriptionForm_fail() throws Exception {
        Account account = createAccount("ehdqls");
        Study study = createStudy("testStudy", account);

        mockMvc.perform(get(getUpdateUrl(study, "description")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAccount("dongbin")
    @DisplayName("스터디 소개 수정 폼 - 성공")
    void updateDescriptionForm_success() throws Exception {
        Account account = accountRepository.findByNickname("dongbin");
        Study study = createStudy("testStudy", account);

        mockMvc.perform(get(getUpdateUrl(study, "description")))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/description"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("studyDescriptionForm"));
    }

    @Test
    @WithAccount("dongbin")
    @DisplayName("스터디 소개 수정 - 실패")
    void updateStudyDescription_fail() throws Exception {
        Account account = accountRepository.findByNickname("dongbin");
        Study study = createStudy("testStudy", account);

        mockMvc.perform(post(getUpdateUrl(study, "description"))
                .param("shortDescription", "")
                .param("fullDescription", "full description")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("studyDescriptionForm"));
    }

    @Test
    @WithAccount("dongbin")
    @DisplayName("스터디 소개 수정 - 성공")
    void updateStudyDescription_success() throws Exception {
        Account account = accountRepository.findByNickname("dongbin");
        Study study = createStudy("testStudy", account);

        String short_descriptiom = "short descriptiom";
        String full_description = "full description";
        mockMvc.perform(post(getUpdateUrl(study, "description"))
                .param("shortDescription", short_descriptiom)
                .param("fullDescription", full_description)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/"+study.getEncodedPath()+"/settings/description"))
                .andExpect(redirectedUrl(getUpdateUrl(study, "description")))
                .andExpect(flash().attributeExists("message"));

        assertEquals(short_descriptiom, study.getShortDescription());
        assertEquals(full_description, study.getFullDescription());
    }

    @Test
    @WithAccount("dongbin")
    @DisplayName("스터디 배너 수정 폼 - 실패 (권한 없는 유저)")
    void updateBannerForm_fail() throws Exception {
        Account account = createAccount("ehdqls");
        Study study = createStudy("testStudy", account);

        mockMvc.perform(get(getUpdateUrl(study, "banner")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAccount("dongbin")
    @DisplayName("스터디 배너 수정 폼 - 성공")
    void updateBannerForm_success() throws Exception {
        Account account = accountRepository.findByNickname("dongbin");
        Study study = createStudy("testStudy", account);

        mockMvc.perform(get(getUpdateUrl(study, "banner")))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/banner"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }

    @Test
    @WithAccount("dongbin")
    @DisplayName("스터디 태그 수정 폼 - 실패 (권한 없는 유저)")
    void updateTagsForm_fail() throws Exception {
        Account account = createAccount("ehdqls");
        Study study = createStudy("testStudy", account);

        mockMvc.perform(get(getUpdateUrl(study, "tags")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAccount("dongbin")
    @DisplayName("스터디 태그 수정 폼 - 성공")
    void updateTagsForm_success() throws Exception {
        Account account = accountRepository.findByNickname("dongbin");
        Study study = createStudy("testStudy", account);

        mockMvc.perform(get(getUpdateUrl(study, "tags")))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/tags"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("tags"))
                .andExpect(model().attributeExists("whitelist"));
    }

    @Test
    @WithAccount("dongbin")
    @DisplayName("스터디 태그 추가")
    void addTag() throws Exception {
        Account account = accountRepository.findByNickname("dongbin");
        Study study = createStudy("testStudy", account);

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("spring");

        mockMvc.perform(post(getUpdateUrl(study, "tags/add"))
                .content(objectMapper.writeValueAsString(tagForm))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isOk());

        assertNotNull(tagRepository.findByTitle("spring"));
        assertTrue(study.getTags().stream().anyMatch(t -> t.getTitle().equals("spring")));
    }

    @Test
    @WithAccount("dongbin")
    @DisplayName("스터디 태그 삭제 실패 & 성공")
    void removeTag() throws Exception {
        Account account = accountRepository.findByNickname("dongbin");
        Study study = createStudy("testStudy", account);

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("aaa");

        mockMvc.perform(post(getUpdateUrl(study, "tags/remove"))
                .content(objectMapper.writeValueAsString(tagForm))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isBadRequest());

        tagForm.setTagTitle("spring");

        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        studyService.addTag(study, tag);

        mockMvc.perform(post(getUpdateUrl(study, "tags/remove"))
                .content(objectMapper.writeValueAsString(tagForm))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isOk());

        assertNotNull(tagRepository.findByTitle("spring"));
        assertFalse(study.getTags().stream().anyMatch(t -> t.getTitle().equals("spring")));
    }

    @Test
    @WithAccount("dongbin")
    @DisplayName("스터디 지역 수정 폼 - 실패 (권한 없는 유저)")
    void updateZonesForm_fail() throws Exception {
        Account account = createAccount("ehdqls");
        Study study = createStudy("testStudy", account);

        mockMvc.perform(get(getUpdateUrl(study, "zones")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAccount("dongbin")
    @DisplayName("스터디 지역 수정 폼 - 성공")
    void updateZonesForm_success() throws Exception {
        Account account = accountRepository.findByNickname("dongbin");
        Study study = createStudy("testStudy", account);

        mockMvc.perform(get(getUpdateUrl(study, "zones")))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/zones"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("zones"))
                .andExpect(model().attributeExists("whitelist"));
    }

    @Test
    @WithAccount("dongbin")
    @DisplayName("스터디 정보 수정 폼 - 실패 (권한 없는 유저)")
    void updateStudySettingsForm_fail() throws Exception {
        Account account = createAccount("ehdqls");
        Study study = createStudy("testStudy", account);

        mockMvc.perform(get(getUpdateUrl(study, "study")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAccount("dongbin")
    @DisplayName("스터디 정보 수정 폼 - 성공")
    void updateStudySettingsForm_success() throws Exception {
        Account account = accountRepository.findByNickname("dongbin");
        Study study = createStudy("testStudy", account);

        mockMvc.perform(get(getUpdateUrl(study, "study")))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/study"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }

    private String getUpdateUrl(Study study, String menu) {
        return "/study/" + study.getPath() + "/settings/" + menu;
    }

}