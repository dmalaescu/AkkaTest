package com.examples.stats;

import akka.actor.*;
import akka.cluster.routing.ClusterRouterConfig;
import akka.cluster.routing.ClusterRouterSettings;
import akka.routing.ConsistentHashingRouter;
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope;
import akka.routing.FromConfig;

/**
 * Created with IntelliJ IDEA.
 * User: mala
 */
public class StatsService extends UntypedActor{
    ActorRef workerRouter = getContext().actorOf(
            new Props(StatsService.class)
                    .withRouter(FromConfig.getInstance()),
            "workerRouter");

    @Override
    public void onReceive(Object message) {
        if (message instanceof StatsMessages.StatsJob) {
            StatsMessages.StatsJob job = (StatsMessages.StatsJob) message;
            if (job.getText().equals("")) {
                unhandled(message);
            } else {
                final String[] words = job.getText().split(" ");
                final ActorRef replyTo = getSender();

                // create actor that collects replies from workers
                Actor aggregator = null;
                try {
                    aggregator = new UntypedActorFactory(){
                        /**
                         * This method must return a different instance upon every call.
                         */
                         @Override
                         public Actor create() throws Exception {
                            return new StatsAggregator(words.length, replyTo);  //To change body of implemented methods use File | Settings | File Templates.
                        }
                    }.create();
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
//                 = getContext().actorOf(
//                        Props.create(StatsAggregator.class, words.length, replyTo));

                // send each word to a worker
                for (String word : words) {
                    workerRouter.tell(word, (ActorRef)aggregator);
                }
            }

        } else {
            unhandled(message);
        }
    }
}
