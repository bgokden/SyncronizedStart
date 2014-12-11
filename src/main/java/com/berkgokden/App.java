package com.berkgokden;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import org.apache.log4j.Logger;

import java.util.Random;

/**
 * Synconized Start App
 *
 */
public class App 
{
    private static final Logger logger = Logger.getLogger(App.class);

    public static void main( String[] args )
    {
        ResultWithInstance resultWithInstance = startHazelcastAndWriteWeAreStarted(new Random().nextInt());
        if (resultWithInstance.hazelcastInstance != null) {
            resultWithInstance.hazelcastInstance.shutdown();
        }
    }

    public static ResultWithInstance startHazelcastAndWriteWeAreStarted(int groupId) {

        // Prepare Hazelcast cluster - adding some configuration for easier testing
        Config config = new Config() ;
        config.setProperty( "hazelcast.logging.type", "log4j" );
        config.getGroupConfig().setName( "groupId-"+ groupId );
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);

        logger.debug("Started");

        boolean result = App.printWeAreStartedWhenReady(hazelcastInstance);

        logger.debug("Completed");

        return new ResultWithInstance(hazelcastInstance, result);
    }

    public static boolean printWeAreStartedWhenReady(HazelcastInstance hazelcastInstance)
    {

        try {
            IAtomicLong counter = hazelcastInstance.getAtomicLong("counter");
            Long value = counter.incrementAndGet();
            logger.debug("Counter Value: "+value);
            //There is no null check here since an error should be printed when value is null
            if (value.longValue() == 1L) {
                //We are first to got here
                System.out.println("We are started!");
                return true;
            }
        } catch (Exception e) {
            System.err.println("Something have gone wrong: " + e.getMessage());
        }

        return false;
    }

    public static class ResultWithInstance {

        ResultWithInstance(HazelcastInstance hazelcastInstance, Boolean result) {
            this.hazelcastInstance = hazelcastInstance;
            this.result = result;
        }
        public HazelcastInstance hazelcastInstance;
        public Boolean result;
    }

}
