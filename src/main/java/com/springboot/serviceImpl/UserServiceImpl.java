package com.springboot.serviceImpl;

import com.springboot.entity.JsonResult;
import com.springboot.entity.ResultCode;
import com.springboot.entity.User;
import com.springboot.mapper.UserMapper;
import com.springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by user on 2017/5/24.
 */
@Service
@Transactional(value="txManager1")
public class UserServiceImpl  implements UserService {

   @Autowired
   private UserMapper userMapper;

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    @Cacheable(value="user") //缓存,这里没有指定key.
    public User findUserById(Long id){
       System.err.println("=========从数据库中进行获取的....id="+id);
       return userMapper.findUserById(id);
        // return user;
    }

    @Cacheable(value="user") //缓存,这里没有指定key.
    public User findUserByTwoPama(Long id, String name){
        System.err.println("=========从数据库中进行获取的....id="+id + "name="+name);
        return userMapper.findUserById(id);
        // return user;
    }

    @Cacheable(value="user") //缓存,这里没有指定key.
    public User findUserByIdRequestBody(User user){
        System.err.println("=========从数据库中进行获取的....id="+user.getId() + "    name="+user.getName());
        return userMapper.findUserById(user.getId());
        // return user;
    }

    @CacheEvict(value="user")
    public void deleteUserFromCache(Long id) {
        System.out.println("user从缓存中删除.");
    }

    public JsonResult updateUsers(User user){
        JsonResult jsonResult = null;
        try {
            int updatecount = userMapper.updateUsers(user.getId());
            int updatecount2 = userMapper.updateUsersIds(user.getSecondId());
            jsonResult = new JsonResult(ResultCode.SUCCESS, "更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            jsonResult = new JsonResult(ResultCode.PARAMS_ERROR, "更新失败");
        }

        return jsonResult;
    }
    public int save(String name){
        System.out.println(name + "thread save begin");
        userMapper.save();
        System.out.println(name + "thread save end");
        Date begin =  new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(begin);
        System.out.println("end------" + dateString);
        return 0;
    }

    public String currentLimiting(String name){
        String result = "";
        System.out.println(name + "thread save begin");
        userMapper.save();
        System.out.println(name + "thread save end");
        Date begin =  new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(begin);
        System.out.println("end------" + dateString);
        return result;
    }
}
