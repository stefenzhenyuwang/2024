package com.threeatom.stefen2024.controller;

import com.threeatom.mapper.queryMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@RestController
@RequestMapping("/stefentest/redis")
class redisTestController  {

//
//    @Resource
//    private com.threeatom.mapper.queryMapper queryMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("/test")
    public String test() {
        return this.redisTest("stefentest");
    }

    //缓存穿透实现
    private String redisTest(String userName){
        //首先请求进来，直接从redis查询查看是否存在
        String redisResult = stringRedisTemplate.opsForValue().get("stefentest");
        //判断是否存在
        if(redisResult != null){
            //如果存在，就直接返回
            return "redis查询成功，result结果为"+redisResult;
        }
        //如果不存在，就从数据库中查找
        String localResult = this.selectByUsername(userName);
        if(localResult == null){
            //如果数据库中也没有查找到，此时就需要我们缓存一个空值进redis中,这样在缓存穿透的的时候，即使有大量请求进来
            //也不会因为该值找不到而一直将海量请求打到数据库中，导致数据库宕机
            stringRedisTemplate.opsForValue().set(userName,"null");
            //同时返回异常信息
            return "未找到对应结果";
        }

        //如果存在，则直接写入redis中
        stringRedisTemplate.opsForValue().set(userName,localResult);
        return "m数据库查询成功，查询结果为"+localResult;
    }


    //缓存击穿实现
    private String redisTest1(String userName) throws Exception {
        //首先请求进来，直接从redis查询查看是否存在
        String redisResult = stringRedisTemplate.opsForValue().get("stefentest");
        //判断redis中是否存在
        if(redisResult != null){
            //如果存在，就直接返回
            return "redis查询成功，result结果为"+redisResult;
        }
        //如果redis中数据未命中，则构建数据库查询
        ReentrantLock lock = new ReentrantLock();
        try{
            lock.tryLock();
            if(lock.tryLock()){
                System.out.println("上锁，不允许其他线程构建数据库查询");
                String localResult = this.selectByUsername(userName);
                if(localResult != null){
                    //如果数据库中也没有查找到，此时就需要我们缓存一个空值进redis中,这样在缓存穿透的的时候，即使有大量请求进来
                    //也不会因为该值找不到而一直将海量请求打到数据库中，导致数据库宕机
                    stringRedisTemplate.opsForValue().set(userName,localResult);
                    //同时返回异常信息
                    return "数据库中查询到对应数据，将数据写入redis中";
                }else {
                    //如果数据库中也没有查找到，此时就需要我们缓存一个空值进redis中,这样在缓存穿透的的时候，即使有大量请求进来
                    //也不会因为该值找不到而一直将海量请求打到数据库中，导致数据库宕机
                    stringRedisTemplate.opsForValue().set(userName,"null");
                }
            }else {
                Thread.sleep(100);
                return redisTest1(userName);
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        return "";
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

    String selectByUsername(String username) {
        return null;
    }


}
