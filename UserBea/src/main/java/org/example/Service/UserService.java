package org.example.Service;

import lombok.extern.slf4j.Slf4j;
import org.example.Entiy.Order_Item;
import org.example.Entiy.Pro;
import org.example.Entiy.User_bea;
import org.springframework.stereotype.Service;

import java.util.List;


public interface UserService {

    void add(User_bea user_bea);
    void generate();
    void userbea(User_bea user_bea) throws Exception;
    void batchUpdatePro(List<Pro> proList);
    void order_service(Order_Item order_item);
}
