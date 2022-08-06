package org.example.controlller;



import lombok.extern.slf4j.Slf4j;
import org.example.Entiy.Order_Item;
import org.example.Entiy.User_bea;
import org.example.Service.UserService;
import org.example.Service.impl.UserServiceimpl;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.*;

@RestController
@Slf4j
public class UserController {
    /***
     * 放弃java原生的线程池方案，由Spring托管
    ExecutorService executorService=new ThreadPoolExecutor(3,8,
            2, TimeUnit.SECONDS,new LinkedBlockingDeque<>(3)
            , Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.CallerRunsPolicy());**/
    @Resource
    UserService userService;
    @ResponseBody
    @RequestMapping("/user_bea/{user_id}/{item_id}/{flag}")
    public void count_user(@PathVariable Integer item_id, @PathVariable Integer user_id,@PathVariable int flag) throws ParseException {
        //,@RequestParam(value = "songid") BigInteger songid
        //@PathVariable("userid") BigInteger userid
        User_bea user_bea=new User_bea();

        user_bea.setUser_id(Long.parseLong(String.valueOf(user_id)));
        user_bea.setFlag(flag);
        user_bea.setItem_id(Long.parseLong(String.valueOf(item_id)));
        String dateStr = Long.toString(System.currentTimeMillis()/1000L);
        user_bea.setTimes(Long.parseLong(dateStr));

        userService.add(user_bea);


    }
    @ResponseBody
    @RequestMapping("/order/{user_id}/{flag}/{item_id}/{timestamp}/{price}/{num}")
    public void userDetail(@PathVariable String item_id, @PathVariable String user_id,@PathVariable String timestamp,@PathVariable int num,@PathVariable float price) throws ParseException {
        Order_Item order_item=new Order_Item();
        order_item.setUser_id(Long.parseLong(user_id));
        order_item.setItem_id(Long.parseLong(item_id));
        order_item.setNum(num);
        order_item.setPrice(price);
        order_item.setTimes(Long.parseLong(timestamp));

        userService.order_service(order_item);





    }
    /**
     * 用户行为接收的控制层
     *
     * */
    @ResponseBody
    @RequestMapping("/count/{user_id}/{flag}/{item_id}/{timestamp}/{ip}")
    public String UserBeaController(@PathVariable String item_id, @PathVariable String user_id,@PathVariable String timestamp,@PathVariable String ip,@PathVariable String flag) throws Exception {
        //,@RequestParam(value = "songid") BigInteger songid
        //@PathVariable("userid") BigInteger userid
        //System.out.println(user_id+","+item_id);

        User_bea user_bea=new User_bea();
        user_bea.setUser_id( Long.parseLong(user_id));
        user_bea.setItem_id(Long.parseLong(item_id));
        user_bea.setCate_id(0L);
        user_bea.setFlag(Integer.valueOf(flag));
        user_bea.setTimes(Long.parseLong(timestamp));
        user_bea.setIp(ip);

       try{
           userService.userbea(user_bea);
       } catch (Exception e){
           e.printStackTrace();
       }



        return "success";

    }

}
