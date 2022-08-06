package org.example.config;

import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;
import org.lionsoul.ip2region.Util;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
/**
 * 设置地址ip解析方法
 * **/
public class Address {
    public  static  String getCityInfo(String ip){

        //db
        String dbPath = Address.class.getResource("/ip2region.db").getPath();
        InputStream inputStream=Address.class.getResourceAsStream("D:\\JAVA_Code\\datawarehouse\\datawarehouse\\UserBea\\src\\main\\resources\\ip2region.db") ;
        File file = new File(dbPath);




        //指定查询算法
        int algorithm = DbSearcher.BTREE_ALGORITHM; //B-tree
        //DbSearcher.BINARY_ALGORITHM //Binary
        //DbSearcher.MEMORY_ALGORITYM //Memory
        try {
            DbConfig config = new DbConfig();
            //DbSearcher searcher = new DbSearcher(config, "/home/lin/db/ip2region.db");
            //指定解析库文件位置
            DbSearcher searcher = new DbSearcher(config, "D:\\JAVA_Code\\datawarehouse\\datawarehouse\\UserBea\\src\\main\\resources\\ip2region.db");
            //define the method
            Method method = null;
            switch ( algorithm )
            {
                case DbSearcher.BTREE_ALGORITHM:
                    method = searcher.getClass().getMethod("btreeSearch", String.class);
                    break;
                case DbSearcher.BINARY_ALGORITHM:
                    method = searcher.getClass().getMethod("binarySearch", String.class);
                    break;
                case DbSearcher.MEMORY_ALGORITYM:
                    method = searcher.getClass().getMethod("memorySearch", String.class);
                    break;
            }

            DataBlock dataBlock = null;
            if ( Util.isIpAddress(ip) == false ) {
                System.out.println("Error: Invalid ip address");
            }

            dataBlock  = (DataBlock) method.invoke(searcher, ip);

            return dataBlock.getRegion();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
