package com.automobilista.giro.service;

import com.automobilista.giro.model.AuditAction;
import com.automobilista.giro.model.entity.AuditLog;
import com.automobilista.giro.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

  private static final String REQUEST_ID_MDC_KEY = "requestId";
  private static final String USER_MDC_KEY = "user";

  private final AuditLogRepository auditLogRepository;

  public void logChange(AuditAction action, String entityType, Long entityId) {
    auditLogRepository.save(
        AuditLog.builder()
            .actorUsername(resolveActorUsername())
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .requestId(MDC.get(REQUEST_ID_MDC_KEY))
            .build());
  }

  private String resolveActorUsername() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken)) {
      String principalName = authentication.getName();
      if (principalName != null && !principalName.isBlank()) {
        return principalName;
      }
    }

    String mdcUser = MDC.get(USER_MDC_KEY);
    if (mdcUser != null && !mdcUser.isBlank()) {
      return mdcUser;
    }

    return "system";
  }
}
