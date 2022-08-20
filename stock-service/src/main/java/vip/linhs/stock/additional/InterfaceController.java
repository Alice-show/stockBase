package vip.linhs.stock.additional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("cfmm")
public class InterfaceController {

    @Autowired
    AutoBuy autoBuy;

    @Autowired
    BuyGuard buyGuard;
    @Autowired
    SellGuard sellGuard;

    @GetMapping("/{code}")
    @ResponseBody
    public void autoBuy(@PathVariable String code){
        autoBuy.trigger(code);
    }

    @GetMapping("revoke/{code}")
    @ResponseBody
    public void revoke(@PathVariable String code){
        autoBuy.revoke(code);
    }

    @GetMapping("/guard/start")
    @ResponseBody
    public void startAutoBuy(){
        buyGuard.openFlag();
        sellGuard.openFlag();
    }

    @GetMapping("/guard/stop")
    @ResponseBody
    public void stopAutoBuy(){
        buyGuard.closeFlag();
        sellGuard.closeFlag();
    }

    @GetMapping("/guard/buyStartOnly")
    @ResponseBody
    public void buyStartOnly(){
        buyGuard.openFlag();
    }

    @GetMapping("/guard/sellStartOnly")
    @ResponseBody
    public void sellStartOnly(){
        sellGuard.openFlag();
    }

    @GetMapping("/guard/buyStopOnly")
    @ResponseBody
    public void buyStopOnly(){
        buyGuard.closeFlag();
    }

    @GetMapping("/guard/sellStopOnly")
    @ResponseBody
    public void sellStopOnly(){
        sellGuard.closeFlag();
    }


}
