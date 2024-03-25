package org.example.test;

import java.util.concurrent.Semaphore;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CrptApi {
    private final Semaphore semaphore;
    private final ScheduledExecutorService scheduler;
    private final int requestLimit;
    private final TimeUnit timeUnit;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestLimit = requestLimit;
        this.timeUnit = timeUnit;
        semaphore = new Semaphore(requestLimit);

        scheduler = Executors.newScheduledThreadPool(1);
        long period = convertTimeUnitToSeconds(timeUnit);
        scheduler.scheduleAtFixedRate(this::releasePermits, period, period, TimeUnit.SECONDS);
    }

    private long convertTimeUnitToSeconds(TimeUnit timeUnit) {
        return timeUnit.toSeconds(1);
    }

    private void releasePermits() {
        semaphore.release(requestLimit - semaphore.availablePermits());
    }

    public boolean createProductDocument(Document document, String signature) throws InterruptedException {
        boolean requestSuccess = false;
        semaphore.acquire();

        try {
            // тут отправим HTTP POST request
            // document -> JSON, и отправим вместе с signature
            String jsonResponse = sendHttpPostRequest(document, signature);
            // проверяем что успешно ли
            requestSuccess = isRequestSuccessful(jsonResponse);


        } finally {
            semaphore.release();
        }

        return requestSuccess;
    }
}
