package com.dbstudy.event.validator;

import com.dbstudy.event.form.EventForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
public class EventValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return EventForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        EventForm eventForm = (EventForm) o;

        if (isNotValidEndEnrollmentDateTime(eventForm)) {
            errors.rejectValue("endEnrollmentDateTime","wrong.datetime","모임 접수 종료 일시를 정확히 입력하세요.");
        }

        if (isNotValidEndDateTime(eventForm)) {
            errors.rejectValue("endDateTime","wrong.datetime","모임 종료 일시를 정확히 입력하세요.");
        }

        if (isNotValidStartDateTime(eventForm)) {
            errors.rejectValue("startDateTime","wrong.datetime","모임 시작 일시를 정확히 입력하세요.");
        }
    }

    private boolean isNotValidStartDateTime(EventForm eventForm) {
        return eventForm.getStartDateTime().isBefore(eventForm.getEndEnrollmentDateTime());
    }

    private boolean isNotValidEndEnrollmentDateTime(EventForm eventForm) {
        return eventForm.getEndEnrollmentDateTime().isBefore(LocalDateTime.now());
    }

    private boolean isNotValidEndDateTime(EventForm eventForm) {
        return eventForm.getEndDateTime().isBefore(eventForm.getStartDateTime()) || eventForm.getEndDateTime().isBefore(eventForm.getEndEnrollmentDateTime());
    }
}