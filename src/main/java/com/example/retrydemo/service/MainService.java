package com.example.retrydemo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class MainService {

    @Autowired
    private SubprocessService subprocessService;

    public boolean executeAllSubprocesses() {
        ExecutorService executor = Executors.newFixedThreadPool(5); // 可以調整線程池大小
        List<Future<?>> futures = IntStream.range(0, 5) // 假設有5個子程序
                .mapToObj(id -> executor.submit(() -> subprocessService.executeSubprocess(id)))
                .collect(Collectors.toList());

        boolean allSuccessful = futures.stream().allMatch(future -> {
            try {
                future.get();
                return true;
            } catch (Exception e) {
                return false;
            }
        });

        executor.shutdown();
        return allSuccessful;
    }
}
