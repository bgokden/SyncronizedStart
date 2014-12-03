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


        ExecutorService executorService = Executors.newFixedThreadPool(App.NUMBEROFINSTANCES);

        List<Callable<Boolean>> callables = new ArrayList<Callable<Boolean>>(App.NUMBEROFINSTANCES);
        for (int i = 0; i < App.NUMBEROFINSTANCES; i++) {
            callables.add(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    // Prepare Hazelcast cluster - adding some configuration for easier testing
                    Config config = new Config() ;
                    config.setProperty( "hazelcast.logging.type", "log4j" );
                    HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
                    ICountDownLatch latch = hazelcastInstance.getCountDownLatch("countDownLatch");

                    logger.debug("Started");

                    boolean success = App.printWeAreStartedWhenReady(hazelcastInstance, App.NUMBEROFINSTANCES);

                    logger.debug("Completed");
                    hazelcastInstance.shutdown();

                    return success;
                }

            });
        }

        int numberOfSuccesfulPrints = 0;
        int numberOfSuccesfulProcesses = 0;
        try {
            List<Future<Boolean>> resultList = executorService.invokeAll(callables, 10, TimeUnit.MINUTES);

            for (int i = 0; i < resultList.size(); i++) {
                if (resultList.get(i).get() != null && resultList.get(i).get().booleanValue()) {
                    numberOfSuccesfulPrints++;
                }
                numberOfSuccesfulProcesses++;
            }

        } catch (InterruptedException e) {
            logger.debug(e.getMessage());
            numberOfSuccesfulPrints = -1;
        } catch (ExecutionException e) {
            logger.debug(e.getMessage());
            numberOfSuccesfulPrints = -2;
        }

        executorService.shutdown();

        logger.debug("Number Of Successful :" + numberOfSuccesfulPrints);
        Assert.assertTrue("All of the threads shoudl complete Successfully", numberOfSuccesfulProcesses == App.NUMBEROFINSTANCES);
        Assert.assertTrue("Only one print event should occur.", numberOfSuccesfulPrints == 1);

        }
    }
