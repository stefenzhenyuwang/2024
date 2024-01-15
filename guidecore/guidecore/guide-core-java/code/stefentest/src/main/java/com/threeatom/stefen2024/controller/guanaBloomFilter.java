package com.threeatom.stefen2024.controller;

import java.nio.charset.StandardCharsets;

public class guanaBloomFilter {
    // 初始化布隆过滤器，设计预计元素数量为100_0000L，误差率为1%
    BloomFilter<CharSequence> bloomFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 100_0000, 0.01);
    int n = 100_0000;
    for (int i = 0; i < n; i++) {
        bloomFilter.put(String.valueOf(i));
    }
    int count = 0;
    for (int i = 0; i < (n * 2); i++) {
        if (bloomFilter.mightContain(String.valueOf(i))) {
            count++;
        }
    }
    System.out.println("过滤器误判率：" + 1.0 * (count - n) / n);
}
