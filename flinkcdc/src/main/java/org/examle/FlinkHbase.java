package org.examle;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class FlinkHbase {
    public static void main(String[] args) {
        //1.获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

    }

}
