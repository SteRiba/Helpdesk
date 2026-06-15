package com.project.helpdesk.service;

import com.project.helpdesk.entity.ActivityLog;
import com.project.helpdesk.entity.ActivityLogType;
import com.project.helpdesk.entity.User;
import com.project.helpdesk.repository.ActivityLogRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ActivityLogService {
    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public List<ActivityLog> findAll() {
        return activityLogRepository.findAll();
    }

    public ActivityLog logUserCreated(User user, User author) {
        validateUser(user);
        validateUser(author);
        ActivityLog activityLog = ActivityLog.builder()
                .type(ActivityLogType.USER_CREATED)
                .title("New user created")
                .description(user.getFirstName() + " " + user.getLastName() + " - " + user.getEmail())
                .entityType("USER")
                .entityId(user.getId())
                .author(author)
                .build();

        return activityLogRepository.save(activityLog);
    }

    public ActivityLog logUserActivated(User user, User author) {
        validateUser(user);
        validateUser(author);
        ActivityLog activityLog = ActivityLog.builder()
                .type(ActivityLogType.USER_STATUS_CHANGED)
                .title("User activated")
                .description(user.getEmail() + " - activated")
                .entityType("USER")
                .entityId(user.getId())
                .author(author)
                .build();

        return activityLogRepository.save(activityLog);
    }

    public ActivityLog logUserDisabled(User user, User author) {
        validateUser(user);
        validateUser(author);
        ActivityLog activityLog = ActivityLog.builder()
                .type(ActivityLogType.USER_STATUS_CHANGED)
                .title("User disabled")
                .description(user.getEmail() + " - disabled")
                .entityType("USER")
                .entityId(user.getId())
                .author(author)
                .build();

        return activityLogRepository.save(activityLog);
    }

//    Helpers

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User can't be null");
        }
    }
}
