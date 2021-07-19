package com.dbstudy.event;

import com.dbstudy.domain.Account;
import com.dbstudy.domain.Enrollment;
import com.dbstudy.domain.Event;
import com.dbstudy.domain.Study;
import com.dbstudy.event.form.EventForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EnrollmentRepository enrollmentRepository;

    public Event createEvent(Event event, Study study, Account account) {
        event.setCreatedBy(account);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setStudy(study);
        return eventRepository.save(event);
    }

    public void updateEvent(Event event, EventForm eventForm) {
        modelMapper.map(eventForm, event);
        event.acceptWaitingList();
    }

    public void deleteEvent(Event event) {
        eventRepository.delete(event);
    }

    public void newEnrollment(Event event, Account account) {
        if (!enrollmentRepository.existsByEventAndAccount(event, account)) {
            Enrollment enrollment = new Enrollment();
            enrollment.setEnrolledAt(LocalDateTime.now());
            enrollment.setAccepted(event.isAbleToAcceptWaitingEnrollment());
            enrollment.setAccount(account);
            event.addEnrollment(enrollment);
            enrollmentRepository.save(enrollment);
        }
    }

    public void disEnrollment(Event event, Account account) {
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account);
        if (enrollment == null) {
            throw new IllegalArgumentException("삭제할 Enrollment가 없습니다.");
        }

        event.removeEnrollment(enrollment);
        enrollmentRepository.delete(enrollment);

        event.acceptNextWaitingEnrollment();
    }
}
