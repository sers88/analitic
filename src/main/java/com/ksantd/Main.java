package com.ksantd;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final int TEXTS_AMOUNT = 10000;
    private static final int TEXT_LENGTH = 100000;
    private static final int QUEUE_CAPACITY = 100;

    private static BlockingQueue<String> aQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    private static BlockingQueue<String> bQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    private static BlockingQueue<String> cQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);

    public static void main(String[] args) {
        Thread textGenerator = new Thread(() -> {
            for (int i = 0; i < TEXTS_AMOUNT; i++) {
                StringBuilder text = new StringBuilder();
                for (int j = 0; j < TEXT_LENGTH; j++) {
                    text.append((char) ('a' + Math.random() * 3));
                }
                try {
                    aQueue.put(text.toString());
                    bQueue.put(text.toString());
                    cQueue.put(text.toString());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        textGenerator.start();

        AtomicInteger maxA = new AtomicInteger(0);
        AtomicInteger maxB = new AtomicInteger(0);
        AtomicInteger maxC = new AtomicInteger(0);

        Thread aAnalyzer = createAnalyzerThread(aQueue, maxA, 'a');
        Thread bAnalyzer = createAnalyzerThread(bQueue, maxB, 'b');
        Thread cAnalyzer = createAnalyzerThread(cQueue, maxC, 'c');

        aAnalyzer.start();
        bAnalyzer.start();
        cAnalyzer.start();

        try {
            textGenerator.join();
            aQueue.put("END");
            bQueue.put("END");
            cQueue.put("END");

            aAnalyzer.join();
            bAnalyzer.join();
            cAnalyzer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Максимальное количество символов 'a': " + maxA.get());
        System.out.println("Максимальное количество символов 'b': " + maxB.get());
        System.out.println("Максимальное количество символов 'c': " + maxC.get());
    }

    private static Thread createAnalyzerThread(BlockingQueue<String> queue, AtomicInteger maxCount, char targetChar) {
        return new Thread(() -> {
            while (true) {
                try {
                    String text = queue.take();
                    if (text.equals("END")) break;

                    int count = countChar(text, targetChar);
                    if (count > maxCount.get()) {
                        maxCount.set(count);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static int countChar(String text, char targetChar) {
        int count = 0;
        for (char ch : text.toCharArray()) {
            if (ch == targetChar) count++;
        }
        return count;

    }
}