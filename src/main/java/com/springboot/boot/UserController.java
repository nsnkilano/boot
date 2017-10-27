package com.springboot.boot;

import com.google.common.util.concurrent.RateLimiter;
import com.springboot.entity.JsonResult;
import com.springboot.entity.ResultCode;
import com.springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by user on 2017/5/24.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private AtomicInteger succesd = new AtomicInteger(0);
    private AtomicInteger failed = new AtomicInteger(0);
    RateLimiter rateLimiter = RateLimiter.create(10);

    @Autowired
    private UserService userService;

    /**
     *
     * @return
     */
    @RequestMapping(value = "/currentLimiting", method = RequestMethod.POST)
    public JsonResult currentLimiting(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, String> map) throws InterruptedException {

        JsonResult jsonResult = null;
        String count = map.get("limitCount");

        //模拟并发量
        CountDownLatch mainlatch = new CountDownLatch(1);
        CountDownLatch latch = new CountDownLatch(Integer.parseInt(count));
        for (int i = 0; i < Integer.parseInt(count); i++) {
            LimitRun limitRun = new LimitRun(mainlatch, latch);
            Thread thread = new Thread(limitRun);
            thread.setName("limitThread" + i);
            thread.start();
//            Thread.sleep(500);
        }
        Thread.sleep(1000);
        System.out.println("begin");
        long beginTime = System.currentTimeMillis();
        mainlatch.countDown();
        latch.await();
        long endTime = System.currentTimeMillis();
        System.out.println("totalTime  " + (endTime - beginTime));
        Thread.sleep(1000);
        jsonResult = new JsonResult();
        jsonResult.setCode(ResultCode.SUCCESS);
        jsonResult.setMessage("success " + succesd.intValue() + ",failed " + failed.intValue());
        succesd = new AtomicInteger(0);
        failed = new AtomicInteger(0);
        return jsonResult;
    }

    public class LimitRun implements Runnable {

        private CountDownLatch latch;

        private CountDownLatch mainLatch;

        public LimitRun(CountDownLatch mainLatch, CountDownLatch latch) {
            this.mainLatch = mainLatch;
            this.latch = latch;
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + " is waiting buy");
            try {
                mainLatch.await();
                //领取一个令牌
                if (rateLimiter.tryAcquire()) {
                    succesd.getAndIncrement();
                    System.out.println(Thread.currentThread().getName() + " buy success");
                } else {
                    failed.getAndIncrement();
                    System.out.println(Thread.currentThread().getName() + " buy failed");
                }
                latch.countDown();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
