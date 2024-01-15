package com.threeatom.stefen2024.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/stefentest/redis")
class redisTestController  {

    @Autowired
    private com.threeatom.stefen2024.controller.mapper.mapper mapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("/test")
    public String test() {
        this.redisTest("stefentest");
        System.out.println("stefen 1123 test");
        return "jmeter 成功调用";
    }

    private String redisTest(String userName){
        //首先请求进来，直接从redis查询查看是否存在
        String redisResult = stringRedisTemplate.opsForValue().get("stefentest");
        //判断是否存在
        if(redisResult != null){
            //如果存在，就直接返回
            return "redis查询成功，result结果为"+redisResult;
        }

        //如果不存在，就从数据库中查找
        String localResult = mapper.select(userName);
        if(localResult == null){
            //如果数据库中也没有查找到，此时就需要我们缓存一个空值进redis中,这样在缓存穿透的的时候，即使有大量请求进来
            //也不会因为该值找不到而一直将海量请求打到数据库中，导致数据库宕机
            stringRedisTemplate.opsForValue().set(userName,null);
            //同时返回异常信息
            return "未找到对应结果";
        }

        //如果存在，则直接写入redis中
        stringRedisTemplate.opsForValue().set(userName,localResult);
        return "m数据库查询成功，查询结果为"+localResult;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setHostName("127.0.0.1");
        jedisConnectionFactory.setPort(6379);
        return jedisConnectionFactory;
    }


    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
        return stringRedisTemplate;
    }


}
