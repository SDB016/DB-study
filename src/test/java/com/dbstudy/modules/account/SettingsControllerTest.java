package com.dbstudy.modules.account;

import com.dbstudy.infra.AbstractContainerBaseTest;
import com.dbstudy.infra.MockMvcTest;
import com.dbstudy.modules.tag.Tag;
import com.dbstudy.modules.zone.Zone;
import com.dbstudy.modules.tag.TagForm;
import com.dbstudy.modules.zone.ZoneForm;
import com.dbstudy.modules.tag.TagRepository;
import com.dbstudy.modules.zone.ZoneRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.dbstudy.modules.account.SettingsController.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class SettingsControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ObjectMapper objectMapper;
    @Autowired TagRepository tagRepository;
    @Autowired AccountService accountService;
    @Autowired ZoneRepository zoneRepository;

    private Zone testZone = Zone.builder().city("test").localNameOfCity("테스트시").province("테스트주").build();

    @BeforeEach
    void beforeEach() {
        zoneRepository.save(testZone);
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
        zoneRepository.deleteAll();
    }

    @WithAccount("dongbin")
    @DisplayName("계정의 태그 수정 폼")
    @Test
    void updateTagsForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + TAGS))
                .andExpect(view().name(SETTINGS + TAGS))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));
    }

    @WithAccount("dongbin")
    @DisplayName("계정에 태그 추가")
    @Test
    void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(ROOT + SETTINGS + TAGS + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Tag newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        Account account = accountRepository.findByNickname("dongbin"); // detached -> attach (transaction)
        assertTrue(account.getTags().contains(newTag));
    }

    @WithAccount("dongbin")
    @DisplayName("계정에 태그 삭제")
    @Test
    void removeTag() throws Exception {
        Account account = accountRepository.findByNickname("dongbin");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(account, newTag);

        assertTrue(account.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(ROOT + SETTINGS + TAGS + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(account.getTags().contains(newTag));
    }

    @WithAccount("dongbin")
    @DisplayName("닉네임 수정 폼")
    @Test
    void updateAccountForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + ACCOUNT))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }

    @WithAccount("dongbin")
    @DisplayName("넥네임 수정하기 - 입력값 정상")
    @Test
    void updateAccount_success() throws Exception {
        String newNickname = "sdb016";
        mockMvc.perform(post(ROOT + SETTINGS + ACCOUNT)
                .param("nickname", newNickname)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + ACCOUNT))
                .andExpect(flash().attributeExists("message"));

        assertNotNull(accountRepository.findByNickname(newNickname));
    }

    @WithAccount("dongbin")
    @DisplayName("넥네임 수정하기 - 입력값 에러")
    @Test
    void updateAccount_error() throws Exception {
        String newNickname = "_(^^)_";
        mockMvc.perform(post(ROOT + SETTINGS + ACCOUNT)
                .param("nickname", newNickname)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + ACCOUNT))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"))
                .andExpect(model().hasErrors());
    }

    @WithAccount("dongbin")
    @DisplayName("프로필 수정 폼")
    @Test
    void updateProfileForm() throws Exception {

        mockMvc.perform(get(ROOT + SETTINGS + PROFILE))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @WithAccount("dongbin")
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void updateProfile() throws Exception {

        String bio = "짧은 소개를 수정하는 경우";
        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + PROFILE))
                .andExpect(flash().attributeExists("message"));

        Account dongbin = accountRepository.findByNickname("dongbin");
        assertEquals(bio, dongbin.getBio());

    }

    @WithAccount("dongbin")
    @DisplayName("프로필 수정하기 - 입력값 에러")
    @Test
    void updateProfile_error() throws Exception {

        String bio = "길게 소개를 수정하는 경우 길게 소개를 수정하는 경우 길게 소개를 수정하는 경우 길게 소개를 수정하는 경우 길게 소개를 수정하는 경우";
        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + PROFILE))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account dongbin = accountRepository.findByNickname("dongbin");
        assertNull(dongbin.getBio());

    }

    @WithAccount("dongbin")
    @DisplayName("패스워드 수정 폼")
    @Test
    void updatePassword_form() throws Exception{
        mockMvc.perform(get(ROOT + SETTINGS + PASSWORD))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount("dongbin")
    @DisplayName("패스워드 수정하기 - 입력값 정상")
    @Test
    void updatePassword_success() throws Exception{

        String newPassword = "13131313";
        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                .param("newPassword", newPassword)
                .param("newPasswordConfirm",newPassword)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + PASSWORD))
                .andExpect(flash().attributeExists("message"));

        Account byNickname = accountRepository.findByNickname("dongbin");
        assertTrue(passwordEncoder.matches(newPassword,byNickname.getPassword()));
    }

    @WithAccount("dongbin")
    @DisplayName("패스워드 수정하기 - 입력값 에러 - 패스워드 불일치")
    @Test
    void updatePassword_fail() throws Exception{

        String newPassword = "13131313";
        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                .param("newPassword", newPassword)
                .param("newPasswordConfirm",newPassword + "1111")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + PASSWORD))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));
    }

    @WithAccount("dongbin")
    @DisplayName("계정의 지역 정보 수정 폼")
    @Test
    void updateZonesForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + ZONES))
                .andExpect(view().name(SETTINGS + ZONES))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    }

    @WithAccount("dongbin")
    @DisplayName("계정의 지역 정보 추가")
    @Test
    void addZone() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Account dongbin = accountRepository.findByNickname("dongbin");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        assertTrue(dongbin.getZones().contains(zone));
    }

    @WithAccount("dongbin")
    @DisplayName("계정의 지역 정보 삭제")
    @Test
    void removeZone() throws Exception {
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        Account dongbin = accountRepository.findByNickname("dongbin");
        accountService.addZone(dongbin, zone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(dongbin.getZones().contains(zone));
    }
}