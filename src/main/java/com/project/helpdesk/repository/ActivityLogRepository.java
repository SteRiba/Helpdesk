package com.project.helpdesk.repository;

import com.project.helpdesk.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Integer> {
}
