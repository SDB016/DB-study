package com.dbstudy.modules.study;

import com.dbstudy.modules.account.Account;
import com.dbstudy.modules.study.event.StudyCreatedEvent;
import com.dbstudy.modules.study.event.StudyUpdateEvent;
import com.dbstudy.modules.tag.Tag;
import com.dbstudy.modules.zone.Zone;
import com.dbstudy.modules.study.form.StudyDescriptionForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static com.dbstudy.modules.study.form.StudyForm.VALID_PATH_PATTERN;


@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher publisher;

    public Study createNewStudy(Study study, Account account) {
        Study newStudy = studyRepository.save(study);
        newStudy.addManager(account);
        //publisher.publishEvent(new StudyCreatedEvent(newStudy));
        return newStudy;
    }

    public Study getStudyToUpdateMember(String path) {
        Study study = studyRepository.findStudyWithMembersByPath(path);
        checkIfExistingStudy(path, study);
        return study;
    }

    public Study getStudyToUpdate(Account account, String path) {
        Study study = this.getStudy(path);
        checkIfManager(account, study);
        return study;
    }

    public Study getStudyToUpdateTag(Account account, String path) {
        Study study = studyRepository.findStudyWithTagsByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }

    public Study getStudyToUpdateZone(Account account, String path) {
        Study study = studyRepository.findStudyWithZonesByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }

    public Study getStudyToUpdateStatus(Account account, String path) {
        Study study = studyRepository.findStudyWithManagersByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }

    public Study getStudy(String path) {
        Study study = studyRepository.findByPath(path);
        checkIfExistingStudy(path, study);
        return study;
    }

    public void updateStudyDescription(Study study, StudyDescriptionForm form) {
        modelMapper.map(form, study);
        publisher.publishEvent(new StudyUpdateEvent(study,"????????? ????????? ??????????????????."));
    }

    public void updateStudyImage(Study study, String image) {
        study.setImage(image);
    }

    public void enableStudyBanner(Study study) {
        study.setUseBanner(true);
    }

    public void disableStudyBanner(Study study) {
        study.setUseBanner(false);
    }

    public Set<Tag> getTags(Study study) {
        Study byPath = studyRepository.findByPath(study.getPath());
        return byPath.getTags();
    }

    public void addTag(Study study, Tag tag) {
        study.getTags().add(tag);
    }

    public void removeTag(Study study, Tag tag) {
        study.getTags().remove(tag);
    }

    public void addZone(Study study, Zone zone) {
        study.getZones().add(zone);
    }

    public void removeZone(Study study, Zone zone) {
        study.getZones().remove(zone);
    }

    private void checkIfManager(Account account, Study study) {
        if (!study.isManagedBy(account)) {
            throw new AccessDeniedException("?????? ????????? ????????? ??? ????????????.");
        }
    }

    private void checkIfExistingStudy(String path, Study study) {
        if (study == null) {
            throw new IllegalArgumentException(path + "??? ???????????? ???????????? ????????????.");
        }
    }

    public void publish(Study study) {
        study.publish();
        this.publisher.publishEvent(new StudyCreatedEvent(study));
    }

    public void close(Study study) {
        study.close();
        publisher.publishEvent(new StudyUpdateEvent(study, "???????????? ??????????????????."));
    }

    public void startRecruiting(Study study) {
        study.startRecruiting();
        publisher.publishEvent(new StudyUpdateEvent(study, "?????? ????????? ???????????????."));
    }

    public void stopRecruiting(Study study) {
        study.stopRecruiting();
        publisher.publishEvent(new StudyUpdateEvent(study, "?????? ????????? ??????????????????."));

    }

    public boolean isValidPath(String newPath) {
        if (!newPath.matches(VALID_PATH_PATTERN)) {
            return false;
        }
        return !studyRepository.existsByPath(newPath);
    }

    public void updateStudyPath(Study study, String newPath) {
        study.setPath(newPath);
    }

    public boolean isValidTitle(String newTitle) {
        return newTitle.length() <= 50;
    }

    public void updateStudyTitle(Study study, String newTitle) {
        study.setTitle(newTitle);
    }

    public void remove(Study study) {
        if (study.isRemovable()) {
            studyRepository.delete(study);
        } else {
            throw new IllegalArgumentException("???????????? ????????? ??? ????????????.");
        }
    }

    public void addMember(Study study, Account account) {
        if (!study.getMembers().contains(account) && !study.getManagers().contains(account)) {
            study.addMember(account);
        } else {
            throw new IllegalArgumentException("?????? ???????????? ?????? ??????????????????.");
        }
    }

    public void removeMember(Study study, Account account) {
        if (study.getMembers().contains(account) || study.getManagers().contains(account)) {
            study.removeMember(account);
        } else {
            throw new IllegalArgumentException("???????????? ?????? ?????? ??????????????????.");
        }
    }

    public Study getStudyToEnroll(String path) {
        Study study = studyRepository.findStudyOnlyByPath(path);
        checkIfExistingStudy(path, study);
        return study;
    }
}
