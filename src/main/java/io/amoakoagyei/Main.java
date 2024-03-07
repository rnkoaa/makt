package io.amoakoagyei;

import io.amoakoagyei.infra.AggregateEventStore;
import io.amoakoagyei.marketplace.ApproveAdCommand;
import io.amoakoagyei.marketplace.CreateAdCommand;
import io.amoakoagyei.marketplace.MarketPlaceAd;
import io.amoakoagyei.marketplace.UpdateTitleCommand;

import java.util.UUID;
import java.util.function.Function;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");

//        Function<Object, Result<?>> createAdHandler = createAdCommand -> {
//            MarketPlaceAd ad = new MarketPlaceAd((CreateAdCommand) createAdCommand);
//            return Result.success(ad);
//        };

//        CommandHandlers commandHandlers = new CommandHandlers();
//        commandHandlers.register(CreateAdCommand.class, createAdHandler);
//        commandHandlers.register(UpdateTitleCommand.class, updateTitleCommand -> {
//            MarketPlaceAd ad = new MarketPlaceAd();
//            ad.handle((UpdateTitleCommand) updateTitleCommand);
//            return Result.success(ad);
//        });
//        final AggregateStore aggregateStore = new AggregateStore();
        CommandGateway commandGateway = new CommandGateway(AggregateEventStore.getInstance());

        var aggregateId = UUID.randomUUID();
         commandGateway.send(new CreateAdCommand(aggregateId, "First Command"));
        var result = commandGateway.send(new ApproveAdCommand(aggregateId, "Richard"));

        switch (result) {
            case Failure(Exception ex) -> System.out.println(ex.getMessage());
            case Success(MarketPlaceAd marketPlaceAd) -> System.out.println(marketPlaceAd);
            case Success(Object object) -> System.out.println(object.getClass());
        }
    }
}