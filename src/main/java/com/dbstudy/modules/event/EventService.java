package com.dbstudy.modules.event;

import com.dbstudy.modules.account.Account;
import com.dbstudy.modules.event.event.EnrollmentAcceptedEvent;
import com.dbstudy.modules.event.event.EnrollmentRejectedEvent;
import com.dbstudy.modules.study.Study;
import com.dbstudy.modules.event.form.EventForm;
import com.dbstudy.modules.study.event.StudyUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher publisher;

    public Event createEvent(Event event, Study study, Account account) {
        event.setCreatedBy(account);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setStudy(study);
        publisher.publishEvent(new StudyUpdateEvent(event.getStudy(),
                "'"+event.getTitle()+"' 모임을 만들었습니다."));
        return eventRepository.save(event);
    }

    public void updateEvent(Event event, EventForm eventForm) {
        modelMapper.map(eventForm, event);
        event.acceptWaitingList();
        publisher.publishEvent(new StudyUpdateEvent(event.getStudy(),
                "'"+event.getTitle()+"' 모임 정보를 수정했으니 확인하세요."));
    }

    public void deleteEvent(Event event) {
        eventRepository.delete(event);
        publisher.publishEvent(new StudyUpdateEvent(event.getStudy(),
                "'"+event.getTitle()+"' 모임을 취소했습니다."));
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

        if (!enrollment.isAttended()) {
            event.removeEnrollment(enrollment);
            enrollmentRepository.delete(enrollment);

            event.acceptNextWaitingEnrollment();
        }
    }

    public void acceptEnrollment(Event event, Enrollment enrollment) {
        event.accept(enrollment);
        publisher.publishEvent(new EnrollmentAcceptedEvent(enrollment));
    }

    public void rejectEnrollment(Event event, Enrollment enrollment) {
        event.reject(enrollment);
        publisher.publishEvent(new EnrollmentRejectedEvent(enrollment));
    }

    public void checkinEnrollment(Event event, Enrollment enrollment) {
        enrollment.setAttended(true);
    }

    public void cancelCheckinEnrollment(Event event, Enrollment enrollment) {
        enrollment.setAttended(false);
    }
}
