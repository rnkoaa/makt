package io.amoakoagyei.marketplace;

import io.amoakoagyei.Aggregate;
import io.amoakoagyei.AggregateIdentifier;
import io.amoakoagyei.CommandHandler;
import io.amoakoagyei.EventSourcingHandler;

import java.util.UUID;

/*

Name	Last commit message	Last commit date
parent directory
..
ApproveClassifiedAd.java
Move main repo to a subproject
3 years ago
CreateClassifiedAd.java
Move main repo to a subproject
3 years ago
PublishClassifiedAd.java
Move main repo to a subproject
3 years ago
UpdateClassifiedAd.java
Classified ad endpoints
3 years ago
UpdateClassifiedAdOwner.java
refactoring
3 years ago
UpdateClassifiedAdPrice.java
Move main repo to a subproject
3 years ago
UpdateClassifiedAdText.java
Move main repo to a subproject
3 years ago
UpdateClassifiedAdTitle.java
 */

public class MarketPlaceAd extends Aggregate {

    @AggregateIdentifier
    private UUID id;
    private String title;
    private String approver;
    private boolean enabled = true;
    private boolean published = false;

    public MarketPlaceAd() {
        super();
    }

    @CommandHandler
    public MarketPlaceAd(CreateAdCommand command) {
        super();
        this.apply(new AdCreatedEvent(command.id(), command.title()));
    }

    @EventSourcingHandler
    public void on(AdCreatedEvent event) {
        this.id = event.getAggregateId();
        this.title = event.title();
    }

    @EventSourcingHandler
    public void on(AdApprovedEvent event) {
        this.approver = event.approver();
    }

    @EventSourcingHandler
    public void on(AdDisabledEvent ignored) {
        this.enabled = false;
    }

    @EventSourcingHandler
    public void on(AdPublishedEvent ignored) {
        this.published = true;
    }

    @EventSourcingHandler
    public void on(TitleUpdatedEvent titleUpdatedEvent) {
        this.title = titleUpdatedEvent.title();
    }

    @CommandHandler
    public void handle(PublishAdCommand publishAdCommand) {
        this.apply(new AdPublishedEvent(publishAdCommand.getId()));
    }

    @CommandHandler
    public void handle(DisableAdCommand publishAdCommand) {
        this.apply(new AdDisabledEvent(publishAdCommand.id()));
    }

    @CommandHandler
    public void handle(ApproveAdCommand approveAdCommand) {
        this.apply(new AdApprovedEvent(approveAdCommand.getId(), approveAdCommand.getApprover()));
    }

    @CommandHandler
    public void handle(UpdateTitleCommand updateTitleCommand) {
        this.apply(new TitleUpdatedEvent(
                updateTitleCommand.aggregateId(),
                updateTitleCommand.title()
        ));
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getApprover() {
        return approver;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isPublished() {
        return published;
    }

    @Override
    public String toString() {
        return "MarketPlaceAd{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", approver='" + approver + '\'' +
                ", published='" + published + '\'' +
                ", enabled='" + enabled + '\'' +
                '}';
    }
}
