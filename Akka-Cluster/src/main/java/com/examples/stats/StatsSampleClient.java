package com.examples.stats;

/**
 * Created with IntelliJ IDEA.
 * User: mala
 */
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import scala.concurrent.forkjoin.ThreadLocalRandom;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorSelection;
import akka.actor.Address;
import akka.actor.Cancellable;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.Member;
import akka.cluster.MemberStatus;

public class StatsSampleClient extends UntypedActor {

    final String servicePath;
    final Cancellable tickTask;
    final Set<Address> nodes = new HashSet<Address>();

    Cluster cluster = Cluster.get(getContext().system());

    public StatsSampleClient(String servicePath) {
        this.servicePath = servicePath;
        FiniteDuration interval = Duration.create(2, TimeUnit.SECONDS);
        tickTask = getContext()
                .system()
                .scheduler()
                .schedule(interval, interval, getSelf(), "tick",
                        getContext().dispatcher(), null);
    }

    //subscribe to cluster changes, MemberEvent
    @Override
    public void preStart() {
        cluster.subscribe(getSelf(), MemberEvent.class);
    }

    //re-subscribe when restart
    @Override
    public void postStop() {
        cluster.unsubscribe(getSelf());
        tickTask.cancel();
    }

    @Override
    public void onReceive(Object message) {
        if (message.equals("tick") && !nodes.isEmpty()) {
            // just pick any one
            List<Address> nodesList = new ArrayList<Address>(nodes);
            Address address = nodesList.get(ThreadLocalRandom.current().nextInt(
                    nodesList.size()));
            ActorSelection service = getContext().actorSelection(address + servicePath);
            service.tell(new StatsMessages.StatsJob("this is the text that will be analyzed"),
                    getSelf());

        } else if (message instanceof StatsMessages.StatsResult) {
            StatsMessages.StatsResult result = (StatsMessages.StatsResult) message;
            System.out.println(result);

        } else if (message instanceof StatsMessages.JobFailed) {
            StatsMessages.JobFailed failed = (StatsMessages.JobFailed) message;
            System.out.println(failed);

        } else if (message instanceof CurrentClusterState) {
            CurrentClusterState state = (CurrentClusterState) message;
            nodes.clear();
            for (Member member : state.getMembers()) {
                if (member.hasRole("compute") && member.status().equals(MemberStatus.up())) {
                    nodes.add(member.address());
                }
            }

        } else if (message instanceof MemberUp) {
            MemberUp mUp = (MemberUp) message;
            if (mUp.member().hasRole("compute"))
                nodes.add(mUp.member().address());

        } else if (message instanceof MemberEvent) {
            MemberEvent other = (MemberEvent) message;
            nodes.remove(other.member().address());

        } else {
            unhandled(message);
        }
    }

}
