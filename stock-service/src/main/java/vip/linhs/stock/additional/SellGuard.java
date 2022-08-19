package vip.linhs.stock.additional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SellGuard {

    private boolean isAutoSell = false;

    public boolean sellFlag(){
        return isAutoSell;
    }

    public void openFlag(){
        isAutoSell = true;
        log.info("开启自动卖出开关");
    }

    public void closeFlag(){
        isAutoSell = false;
        log.info("关闭自动卖出开关");
    }

}
