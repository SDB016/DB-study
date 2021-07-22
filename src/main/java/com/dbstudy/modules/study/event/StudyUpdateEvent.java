package com.dbstudy.modules.study.event;

import com.dbstudy.modules.study.Study;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;

@Getter
@RequiredArgsConstructor
public class StudyUpdateEvent  {

    private final Study study;

    private final String message;

}
