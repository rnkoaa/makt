package io.amoakoagyei.marketplace;

import io.amoakoagyei.TargetAggregateId;

import java.util.UUID;

public class ApproveAdCommand {
    @TargetAggregateId
    private final UUID id;

    private final String approver;

    public ApproveAdCommand(UUID id, String approver) {
        this.id = id;
        this.approver = approver;
    }

    public String getApprover() {
        return approver;
    }

    public UUID getId() {
        return id;
    }
}
