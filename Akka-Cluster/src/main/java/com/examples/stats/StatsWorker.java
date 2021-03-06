package com.examples.stats;

import akka.actor.UntypedActor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: mala
 */
public class StatsWorker extends UntypedActor{
    Map<String, Integer> cache = new HashMap<String, Integer>();

    @Override
    public void onReceive(Object message) {
        if (message instanceof String) {
            String word = (String) message;
            Integer length = cache.get(word);
            if (length == null) {
                length = word.length();
                cache.put(word, length);
            }
            getSender().tell(length, getSelf());

        } else {
            unhandled(message);
        }
    }

}
