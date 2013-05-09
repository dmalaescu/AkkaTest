package com.examples;

import akka.actor.UntypedActor;

/**
 * Created with IntelliJ IDEA.
 * User: mala
 */
public class SimpleClusterListener extends UntypedActor{
    /**
     * To be implemented by concrete UntypedActor, this defines the behavior of the
     * UntypedActor.
     */
    @Override
    public void onReceive(Object message) throws Exception {
        System.out.println(message);
    }
}
