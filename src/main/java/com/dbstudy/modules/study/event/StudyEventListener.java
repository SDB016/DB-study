package com.dbstudy.modules.study.event;

import com.dbstudy.infra.config.AppProperties;
import com.dbstudy.infra.mail.EmailMessage;
import com.dbstudy.infra.mail.EmailService;
import com.dbstudy.modules.account.Account;
import com.dbstudy.modules.account.AccountPredicates;
import com.dbstudy.modules.account.AccountRepository;
import com.dbstudy.modules.notification.Notification;
import com.dbstudy.modules.notification.NotificationRepository;
import com.dbstudy.modules.notification.NotificationType;
import com.dbstudy.modules.study.Study;
import com.dbstudy.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static com.dbstudy.modules.notification.NotificationType.*;

@Slf4j
@Async
@Transactional
@Component
@RequiredArgsConstructor
public class StudyEventListener {

    private final StudyRepository studyRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleStudyCreatedEvent(StudyCreatedEvent studyCreatedEvent) {
        Study study = studyRepository.findStudyWithTagsAndZonesById(studyCreatedEvent.getStudy().getId());
        Iterable<Account> accounts = accountRepository.findAll(AccountPredicates.findByTagsAndZones(study.getTags(), study.getZones()));
        accounts.forEach(account -> {
            if (account.isStudyCreatedByEmail()) {
                String contextMessage = "새로운 스터디가 생겼습니다.";
                String emailSubject = "DB 스터디, '" + study.getTitle() + "' 스터디가 생겼습니다.";
                sendEventEmail(study, account, contextMessage, emailSubject);
            }
            if (account.isStudyCreatedByWeb()) {
                createNotification(study, account, study.getShortDescription(), STUDY_CREATED);
            }
        });
    }

    @EventListener
    public void handleStudyUpdateEvent(StudyUpdateEvent studyUpdateEvent) {
        Study study = studyRepository.findStudyWithManagersAndMembersById(studyUpdateEvent.getStudy().getId());
        Set<Account> accounts = new HashSet<>();
        accounts.addAll(study.getManagers());
        accounts.addAll(study.getMembers());

        accounts.forEach(account -> {
            if (account.isStudyUpdatedByEmail()) {

                String contextMessage = studyUpdateEvent.getMessage();
                String emailSubject = "DB 스터디, '"+ study.getTitle()+"' 스터디에 새소식이 있습니다.";
                sendEventEmail(study, account, contextMessage, emailSubject);
            }

            if (account.isStudyUpdatedByWeb()) {
                createNotification(study, account, studyUpdateEvent.getMessage(), STUDY_UPDATED);
            }
        });


    }

    private void createNotification(Study study, Account account, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setTitle(study.getTitle());
        notification.setLink("/study/"+ study.getEncodedPath());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(message);
        notification.setAccount(account);
        notification.setNotificationType(type);
        notificationRepository.save(notification);
    }

    private void sendEventEmail(Study study, Account account, String contextMessage, String emailSubject) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/study/"+ study.getEncodedPath());
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message", contextMessage);
        context.setVariable("host",appProperties.getHost());

        String message = templateEngine.process("mail/simple-link", context);
        EmailMessage emailMessage = EmailMessage.builder()
                .subject(emailSubject)
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }
}
