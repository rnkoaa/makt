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

    public MarketPlaceAd() {
        super();
    }

    @CommandHandler
    public MarketPlaceAd(CreateAdCommand command) {
        super();
        this.apply(new AdCreatedEvent(UUID.randomUUID(), command.title()));
    }

    @EventSourcingHandler
    public void on(AdCreatedEvent event) {
        this.title = event.title();
    }

    @Override
    public String toString() {
        return "MarketPlaceAd{" +
                "title='" + title + '\'' +
                '}';
    }

    @CommandHandler
    public void handle(PublishAdCommand publishAdCommand) {

    }

    @CommandHandler
    public void handle(DisableAdCommand publishAdCommand) {

    }

    @CommandHandler
    public void handle(ApproveAdCommand approveAdCommand) {

    }

    @CommandHandler
    public void handle(UpdateTitleCommand updateTitleCommand) {
        this.apply(new TitleUpdatedEvent(
                updateTitleCommand.aggregateId(),
                updateTitleCommand.title()
        ));
    }
}
