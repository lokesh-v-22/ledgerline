package com.ledgerline.ledgerline.audit;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository repo;

    public AuditService(AuditLogRepository repo) {
        this.repo = repo;
    }

    /** MANDATORY: must run inside the caller's database transaction. If the action rolls back, the audit row rolls back with it, so an action can never succeed without its audit row. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(UUID actorId, String action, String entity, String entityId, String details) {
        repo.save(new AuditLog(actorId, action, entity, entityId, details));
    }
}
