package com.dbstudy.modules.study.event;

import com.dbstudy.modules.study.Study;
import lombok.Data;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class StudyCreatedEvent{

    private Study study;

    public StudyCreatedEvent(Study study) {
        this.study = study;
    }
}
