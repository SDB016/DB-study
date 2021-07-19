package com.dbstudy.modules.study;

import com.dbstudy.modules.account.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudyFactory {

    @Autowired StudyService studyService;
    @Autowired StudyRepository studyRepository;

    public Study createStudy(String path, Account account) {
        Study study = new Study();
        study.setPath(path);
        studyService.createNewStudy(study, account);
        return study;
    }
}
