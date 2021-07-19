package com.dbstudy.modules.event;

import com.dbstudy.modules.account.WithAccount;
import com.dbstudy.modules.account.Account;
import com.dbstudy.modules.study.Study;
import com.dbstudy.modules.study.StudyControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EventControllerTest extends StudyControllerTest {

    @Autowired EventService eventService;
    @Autowired EnrollmentRepository enrollmentRepository;

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 자동 수락")
    @WithAccount("dongbin")
    void newEnroll_to_FCFS_event_accepted() throws Exception {
        Account account = createAccount("SDB016");
        Study study = createStudy("test-path", account);
        Event event = createEvent("test-event", "test-desc",EventType.FCFS, 2, study, account);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account dongbin = accountRepository.findByNickname("dongbin");
        isAccepted(dongbin, event);

    }

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 대기중 (이미 만석)")
    @WithAccount("dongbin")
    void newEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account account = createAccount("SDB016");
        Account ehdqls = createAccount("ehdqls");
        Study study = createStudy("test-path", account);
        Event event = createEvent("test-event","test-desc", EventType.FCFS, 2, study, account);

        eventService.newEnrollment(event, account);
        eventService.newEnrollment(event, ehdqls);

        mockMvc.perform(post("/study/"+study.getPath()+"/events/"+event.getId()+"/enroll")
        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account dongbin = accountRepository.findByNickname("dongbin");
        isNotAccepted(dongbin, event);

    }

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 취소")
    @WithAccount("dongbin")
    void cancelEnrollment_to_FCFS_event() throws Exception {
        Account dongbin = accountRepository.findByNickname("dongbin");
        Account nov = createAccount("nov");
        Account oct = createAccount("oct");
        Account dec = createAccount("dec");
        Study study = createStudy("test-path", dongbin);
        Event event = createEvent("test-event","test-desc", EventType.FCFS, 2, study, dongbin);

        eventService.newEnrollment(event, dongbin);
        eventService.newEnrollment(event, nov);
        eventService.newEnrollment(event, oct);
        eventService.newEnrollment(event, dec);

        isAccepted(dongbin, event);
        isAccepted(nov, event);
        isNotAccepted(oct, event);
        isNotAccepted(dec, event);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        assertNull(enrollmentRepository.findByEventAndAccount(event, dongbin));
        isAccepted(nov, event);
        isAccepted(oct, event);
        isNotAccepted(dec, event);
    }

    @Test
    @DisplayName("관리자 확인 모임에 참가 신청 - 대기중")
    @WithAccount("dongbin")
    void newEnroll_to_CONFIRMATIVE_event_not_accepted() throws Exception {
        Account nov = createAccount("nov");
        Study study = createStudy("test-path", nov);
        Event event = createEvent("test-event","test-desc", EventType.CONFIRMATIVE, 2, study, nov);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account dongbin = accountRepository.findByNickname("dongbin");
        isNotAccepted(dongbin, event);
    }

    @Test
    @DisplayName("선착순 모임 수정 - 인원 증가")
    @WithAccount("dongbin")
    void edit_content_of_FCFS_event() throws Exception {
        Account dongbin = accountRepository.findByNickname("dongbin");
        Account nov = createAccount("nov");
        Account oct = createAccount("oct");

        Study study = createStudy("test-path", dongbin);
        Event event = createEvent("test-event","test-desc", EventType.FCFS, 2, study, dongbin);

        eventService.newEnrollment(event, dongbin);
        eventService.newEnrollment(event, nov);
        eventService.newEnrollment(event, oct);

        isAccepted(dongbin,event);
        isAccepted(nov,event);
        isNotAccepted(oct,event);

        String newTitle = "test-event-edit";
        String newDescription = "new description";
        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/edit")
                .param("title", newTitle)
                .param("description", newDescription)
                .param("limitOfEnrollments", "3")
                .param("endEnrollmentDateTime",event.getEndEnrollmentDateTime().format(DateTimeFormatter.ISO_DATE_TIME))
                .param("startDateTime",event.getStartDateTime().format(DateTimeFormatter.ISO_DATE_TIME))
                .param("endDateTime",event.getEndDateTime().format(DateTimeFormatter.ISO_DATE_TIME))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        assertEquals(newTitle,event.getTitle());
        assertEquals(newDescription,event.getDescription());

        isAccepted(dongbin,event);
        isAccepted(nov,event);
        isAccepted(oct,event);
    }

    @Test
    @DisplayName("모임 리스트 뷰")
    @WithAccount("dongbin")
    void viewEvents() throws Exception {
        Account nov = createAccount("nov");
        Study study = createStudy("test-path", nov);
        Event event1 = createEvent("test-event-1", "test desc", EventType.FCFS, 2, study, nov);
        Event event2 = createEvent("test-event-2", "test desc", EventType.CONFIRMATIVE, 4, study, nov);

        mockMvc.perform(get("/study/" + study.getPath() + "/events"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("newEvents"))
                .andExpect(model().attributeExists("oldEvents"));
    }

    private Event createEvent(String eventTitle, String description, EventType eventType, int limit, Study study, Account account) {
        Event event = new Event();
        event.setTitle(eventTitle);
        event.setEventType(eventType);
        event.setDescription(description);
        event.setLimitOfEnrollments(limit);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setEndEnrollmentDateTime(LocalDateTime.now().plusDays(1));
        event.setStartDateTime(LocalDateTime.now().plusDays(1).plusHours(5));
        event.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(7));
        return eventService.createEvent(event, study, account);
    }

    private void isAccepted(Account account, Event event) {
        assertTrue(enrollmentRepository.findByEventAndAccount(event, account).isAccepted());
    }

    private void isNotAccepted(Account account, Event event) {
        assertFalse(enrollmentRepository.findByEventAndAccount(event, account).isAccepted());
    }

}