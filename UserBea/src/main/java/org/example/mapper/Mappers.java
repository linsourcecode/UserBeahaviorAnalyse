package org.example.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.example.Entiy.Order_Item;
import org.example.Entiy.Pro;
import org.example.Entiy.User_bea;

import java.util.List;

@Mapper
public interface Mappers {
    void add(User_bea user_bea);

    void updatePro(String proname);
    void batchUpdatePro(List<Pro> proList);
    void order_service(Order_Item order_item);
}
