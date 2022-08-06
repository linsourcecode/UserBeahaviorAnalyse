import cn.hutool.core.io.file.FileReader;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;

import java.util.Date;
import java.util.List;
import java.util.Random;

public class HttpRequests {
    public static void main(String[] args) throws InterruptedException {
        ///count/{user_id}/{flag}/{item_id}/{timestamp}/{ip}
        String url="http://localhost:8002/count/21796/3/168/1637045903/106.85.42.64";
        FileReader fileReader=new FileReader("H://ip.txt");
        String content = HttpUtil.get(url);
        List<String> stringList = fileReader.readLines();
        Random random=new Random();
        ///count/{user_id}/{flag}/{item_id}/{timestamp}/{ip}
        for(String ip:stringList){
            System.out.println(ip);
            int user_id= random.nextInt(100000);
            int item_id=random.nextInt(1400);
            int flag=random.nextInt(4);
            long times= new Date().getTime();
            System.out.println(ip+","+times);
            url="http://localhost:8002/count/"+ user_id+"/"+flag+"/"+item_id+"/"+times+"/"+ip;
            System.out.println(url);
            content =HttpRequest.get(url).execute().body();
            System.out.println(content);
            Thread.sleep(10);




        }
        System.out.println(content);
    }
}
