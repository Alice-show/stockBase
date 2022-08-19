package vip.linhs.stock.additional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BuyGuard {

    private boolean trialFlag = false;

    public boolean getTrialFlag(){
        return trialFlag;
    }

   public void openFlag(){
        this.trialFlag = true;
       log.info("开启自动买入开关");
   }

   public void closeFlag(){
        this.trialFlag = false;
       log.info("关闭自动买入开关");
   }


    /**
     * 当审判的结果为true时，可以买入
     * 1.自己设置的买卖开关
     * 2.资金量（设置一个资金量阈值，可以让我慢慢调试）
     * 3.
     */
    public boolean trial(String code){
        if(!getTrialFlag()) return false;


        return true;
    }
}
