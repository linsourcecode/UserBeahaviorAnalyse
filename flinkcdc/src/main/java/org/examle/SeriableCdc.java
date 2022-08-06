package org.examle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.ververica.cdc.debezium.DebeziumDeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.util.Collector;
import org.apache.kafka.connect.source.SourceRecord;

public class SeriableCdc implements DebeziumDeserializationSchema<String> {
    @Override
    public void deserialize(SourceRecord sourceRecord, Collector<String> collector) throws Exception {
        //1.创建JSON对象用于存储最终数据
        JSONObject result = new JSONObject();
        //2.获取库名&表名
        String topic = sourceRecord.topic();

    }

    @Override
    public TypeInformation<String> getProducedType() {
        return null;
    }
}
