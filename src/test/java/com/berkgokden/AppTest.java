package com.berkgokden;


import com.hazelcast.core.Hazelcast;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Unit test for simple App.
 */
public class AppTest {
    private static final Logger logger = Logger.getLogger(AppTest.class);
    private static final int NUMBEROFINSTANCES = 10;

    @Test
    public void shouldPassWhenWeAreStartedPrintedOnlyOnce() {

        ExecutorService executorService = Executors.newFixedThreadPool(NUMBEROFINSTANCES);

        final int groupId = new Random().nextInt();
        logger.debug("GroupId :" + groupId);
        final List<Callable<App.ResultWithInstance>> callables = new ArrayList<Callable<App.ResultWithInstance>>(NUMBEROFINSTANCES);
        for (int i = 0; i < NUMBEROFINSTANCES; i++) {
            callables.add(new Callable<App.ResultWithInstance>() {
                @Override
                public App.ResultWithInstance call() throws Exception {
                    return App.startHazelcastAndWriteWeAreStarted(groupId);
                }

            });
        }

        int numberOfSuccesfulPrints = 0;
        int numberOfSuccesfulProcesses = 0;
        try {
            //10 minutes is enough for starting at least one node
            List<Future<App.ResultWithInstance>> resultList = executorService.invokeAll(callables, 10, TimeUnit.MINUTES);

            for (int i = 0; i < resultList.size(); i++) {
                App.ResultWithInstance resultWithInstance = resultList.get(i).get();
                if (resultWithInstance != null && resultWithInstance.hazelcastInstance != null) {
                    resultWithInstance.hazelcastInstance.shutdown();
                    if (resultWithInstance.result == true) {
                        numberOfSuccesfulPrints++;
                    }
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
        Assert.assertTrue("At least one of the threads should have completed successfully", numberOfSuccesfulProcesses > 0);
        Assert.assertTrue("Only one print event should occur.", numberOfSuccesfulPrints == 1);
    }

    @Before
    @After
    public void cleanup() throws Exception {
        Hazelcast.shutdownAll();
    }

}
