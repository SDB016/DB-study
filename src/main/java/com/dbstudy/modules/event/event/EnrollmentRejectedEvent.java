package com.dbstudy.modules.event.event;

import com.dbstudy.modules.event.Enrollment;

public class EnrollmentRejectedEvent extends EnrollmentEvent{
    public EnrollmentRejectedEvent(Enrollment enrollment) {
        super(enrollment, "모임 참가 신청이 거절됐습니다.");
    }
}
