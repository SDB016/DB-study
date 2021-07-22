package com.dbstudy.modules.event.event;

import com.dbstudy.modules.event.Enrollment;

public class EnrollmentAcceptedEvent  extends EnrollmentEvent{
    public EnrollmentAcceptedEvent(Enrollment enrollment) {
        super(enrollment, "모임 참가 신청을 확인했습니다. 모임에 참석하세요.");
    }
}
