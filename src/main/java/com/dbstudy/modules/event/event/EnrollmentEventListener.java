package com.dbstudy.modules.event.event;

import com.dbstudy.infra.config.AppProperties;
import com.dbstudy.infra.mail.EmailMessage;
import com.dbstudy.infra.mail.EmailService;
import com.dbstudy.modules.account.Account;
import com.dbstudy.modules.event.Enrollment;
import com.dbstudy.modules.event.EnrollmentRepository;
import com.dbstudy.modules.event.Event;
import com.dbstudy.modules.notification.Notification;
import com.dbstudy.modules.notification.NotificationRepository;
import com.dbstudy.modules.notification.NotificationType;
import com.dbstudy.modules.study.Study;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Async
@Transactional
@Component
@RequiredArgsConstructor
public class EnrollmentEventListener {

    private final AppProperties appProperties;
    private final TemplateEngine templateEngine;
    private final EmailService emailService;
    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleEnrollmentEvent(EnrollmentEvent enrollmentEvent) {
        Enrollment enrollment = enrollmentEvent.getEnrollment();
        Account account = enrollment.getAccount();
        Event event = enrollment.getEvent();
        Study study = event.getStudy();

        String contextMessage = "/study/" + study.getEncodedPath() + "/events/" + event.getId();
        String emailSubject = "DB 스터디, '"+event.getTitle()+"' 모임 참가 신청 결과입니다.";

        if (account.isStudyEnrollmentResultByEmail()) {
            sendEnrollmentEmail(enrollmentEvent, account, event, study);
        }

        if (account.isStudyEnrollmentResultByWeb()) {
            createNotification(enrollmentEvent, account, event, study);
        }

    }

    private void createNotification(EnrollmentEvent enrollmentEvent, Account account, Event event, Study study) {
        Notification notification = new Notification();
        notification.setTitle(event.getTitle()+" / "+event.getTitle());
        notification.setLink("/study/" + study.getEncodedPath() + "/events/" + event.getId());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(enrollmentEvent.getMessage());
        notification.setAccount(account);
        notification.setNotificationType(NotificationType.EVENT_ENROLLMENT);
        notificationRepository.save(notification);
    }

    private void sendEnrollmentEmail(EnrollmentEvent enrollmentEvent, Account account, Event event, Study study) {
        Context context = new Context();
        context.setVariable("nickname",account.getNickname());
        context.setVariable("link", "/study/" + study.getEncodedPath() + "/events/" + event.getId());
        context.setVariable("linkName",event.getTitle());
        context.setVariable("message", enrollmentEvent.getMessage());
        context.setVariable("host",appProperties.getHost());

        String message = templateEngine.process("mail/simple-link", context);
        EmailMessage emailMessage = EmailMessage.builder()
                .subject("DB 스터디, '" + event.getTitle() + "' 모임 참가 신청 결과입니다.")
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }
}
