package com.berkgokden;


import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.ICountDownLatch;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Unit test for simple App.
 */
public class AppTest
{
    private static final Logger logger = Logger.getLogger(AppTest.class);
    @Test
    public void shouldReturnTrueWhenWeAreStartedPrintedOnlyOnce() {
        final int NUMBEROFINSTANCES = 10;

        ExecutorService executorService = Executors.newFixedThreadPool(NUMBEROFINSTANCES);

        List<Callable<Boolean>> callables = new ArrayList<Callable<Boolean>>(NUMBEROFINSTANCES);
        for (int i = 0; i < NUMBEROFINSTANCES; i++) {
            callables.add(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    // Prepare Hazelcast cluster
                    Config config = new Config() ;
                    config.setProperty( "hazelcast.system.log.enabled", "false" );
                    HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
                    ICountDownLatch latch = hazelcastInstance.getCountDownLatch("countDownLatch");

                    boolean success = App.printWeAreStartedWhenReady(hazelcastInstance, NUMBEROFINSTANCES);

                    System.out.println("Completed");
                    hazelcastInstance.shutdown();

                    return success;
                }

            });
        }

        int numberOfSuccesfulPrints = 0;
        try {
            List<Future<Boolean>> resultList = executorService.invokeAll(callables, 10, TimeUnit.MINUTES);

            for (int i = 0; i < NUMBEROFINSTANCES; i++) {
                if (resultList.get(i).get().booleanValue()) {
                    numberOfSuccesfulPrints++;
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            numberOfSuccesfulPrints = -1;
        } catch (ExecutionException e) {
            e.printStackTrace();
            numberOfSuccesfulPrints = -1;
        }

        executorService.shutdown();

        logger.debug("Number Of Successful :" + numberOfSuccesfulPrints);
        Assert.assertTrue("Only one print event should occur.", numberOfSuccesfulPrints == 1);

        }
    }
