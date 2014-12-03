package com.berkgokden;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICountDownLatch;
import com.hazelcast.core.IMap;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final Logger logger = Logger.getLogger(App.class);
    public static final int NUMBEROFINSTANCES = 10;
    public static final int TIMEOUTINMUNITES = 1;
    public static void main( String[] args )
    {
        // Prepare Hazelcast cluster
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
        ICountDownLatch latch = hazelcastInstance.getCountDownLatch( "countDownLatch" );

        boolean success = printWeAreStartedWhenReady(hazelcastInstance, NUMBEROFINSTANCES);

        logger.debug("Completed");
        hazelcastInstance.shutdown();

    }

    public static boolean printWeAreStartedWhenReady(HazelcastInstance hazelcastInstance, int numberOfNodes)
    {

        ICountDownLatch latch = hazelcastInstance.getCountDownLatch( "countDownLatch" );
        latch.trySetCount( numberOfNodes ); //this would run only once until it is reset

        latch.countDown();

        boolean result = false;
        try {
            boolean success = latch.await(TIMEOUTINMUNITES, TimeUnit.MINUTES );
            if (success) {
                IMap<String, Boolean> map = hazelcastInstance.getMap("debugging");

                Boolean isWeAreStartedPrinted = map.get("is-we-are-started-printed");

                if (isWeAreStartedPrinted == null) {
                    map.putIfAbsent("is-we-are-started-printed", false);
                    if (map.tryLock("is-we-are-started-printed")) {
                        isWeAreStartedPrinted = map.get("is-we-are-started-printed");
                        if (!isWeAreStartedPrinted) {
                            System.out.println("We are started!");
                            map.put("is-we-are-started-printed", true);
                            result = true;
                        }
                        map.unlock("is-we-are-started-printed");
                    }
                }
            } else {
                System.err.println("Something may have gone wrong. We are not fully started!");
            }
        } catch (InterruptedException e) {
            System.err.println("Something may have gone wrong. We are not fully started! :"+ e.getMessage());
        } catch (Exception e) {
            System.err.println("Something may have gone wrong. We are not fully started! :"+e.getMessage());
        }

        return result;
    }

}
