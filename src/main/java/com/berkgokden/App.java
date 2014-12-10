package com.berkgokden;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.apache.log4j.Logger;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final Logger logger = Logger.getLogger(App.class);

    public static void main( String[] args )
    {
        startHazelcastAndWriteWeAreStarted();
    }

    public static boolean startHazelcastAndWriteWeAreStarted() {

        // Prepare Hazelcast cluster - adding some configuration for easier testing
        Config config = new Config() ;
        config.setProperty( "hazelcast.logging.type", "log4j" );
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);

        logger.debug("Started");

        boolean success = App.printWeAreStartedWhenReady(hazelcastInstance);

        logger.debug("Completed");
        hazelcastInstance.shutdown();

        return success;
    }

    public static boolean printWeAreStartedWhenReady(HazelcastInstance hazelcastInstance)
    {

        boolean result = false;
        try {
            //Added this to check network if any other hazelcast instance exist before using maps
            //While testing it is observed that a map is created even if it exists on another node
            //It behaves like they are recovering from a split brain syndrome
            Hazelcast.getAllHazelcastInstances();

            //This is Double-checked_locking
            //Behaves as if Hazelcast Cluster is a huge singleton
            IMap<String, Boolean> map = hazelcastInstance.getMap("debugging");


            Boolean isWeAreStartedPrinted = map.get("is-we-are-started-printed");

            if (isWeAreStartedPrinted == null) {
                map.putIfAbsent("is-we-are-started-printed", false);
                if (map.tryLock("is-we-are-started-printed")) {
                    isWeAreStartedPrinted = map.get("is-we-are-started-printed");
                    if (!isWeAreStartedPrinted) {
                        map.put("is-we-are-started-printed", true);
                        System.out.println("We are started!");
                        result = true;
                    }
                    map.unlock("is-we-are-started-printed");
                }
            }
        } catch (Exception e) {
            System.err.println("Something may have gone wrong. We are not fully started! :" + e.getMessage());
        }

        return result;
    }

}
