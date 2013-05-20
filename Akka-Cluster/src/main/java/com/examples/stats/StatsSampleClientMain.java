package com.examples.stats;

import akka.actor.*;

/**
 * Created with IntelliJ IDEA.
 * User: mala
 */
public class StatsSampleClientMain {
    public static void main(String[] args) throws Exception {
        // note that client is not a compute node, role not defined
        ActorSystem system = ActorSystem.create("ClusterSystem");
        system.actorOf(new Props(new UntypedActorFactory(){

            /**
             * This method must return a different instance upon every call.
             */
                    @Override
                    public Actor create() throws Exception {
                    return   new StatsSampleClient("/user/statsService");  //To change body of implemented methods use File | Settings | File Templates.
            }
        }), "client");
    }
}
