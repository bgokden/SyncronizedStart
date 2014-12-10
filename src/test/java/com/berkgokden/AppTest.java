package com.berkgokden;


import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Unit test for simple App.
 */
public class AppTest {
    private static final Logger logger = Logger.getLogger(AppTest.class);
    private static final int NUMBEROFINSTANCES = 10;

    @Test
    public void shouldReturnTrueWhenWeAreStartedPrintedOnlyOnce() {

        ExecutorService executorService = Executors.newFixedThreadPool(NUMBEROFINSTANCES);

        List<Callable<Boolean>> callables = new ArrayList<Callable<Boolean>>(NUMBEROFINSTANCES);
        for (int i = 0; i < NUMBEROFINSTANCES; i++) {
            callables.add(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return App.startHazelcastAndWriteWeAreStarted();
                }

            });
        }

        int numberOfSuccesfulPrints = 0;
        int numberOfSuccesfulProcesses = 0;
        try {
            //10 minutes is enough for starting at least one node
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
        Assert.assertTrue("At least one of the threads should have completed successfully", numberOfSuccesfulProcesses > 0);
        Assert.assertTrue("Only one print event should occur.", numberOfSuccesfulPrints == 1);

    }

}
