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

    @GetMapping("/start")
    @ResponseBody
    public void startAutoBuy(){
        buyGuard.openFlag();
        sellGuard.openFlag();
    }

    @GetMapping("/stop")
    @ResponseBody
    public void stopAutoBuy(){
        buyGuard.closeFlag();
        sellGuard.closeFlag();
    }

    @GetMapping("/buyStartOnly")
    @ResponseBody
    public void buyStartOnly(){
        buyGuard.openFlag();
    }

    @GetMapping("/sellStartOnly")
    @ResponseBody
    public void sellStartOnly(){
        sellGuard.openFlag();
    }

    @GetMapping("/buyStopOnly")
    @ResponseBody
    public void buyStopOnly(){
        buyGuard.closeFlag();
    }

    @GetMapping("/sellStopOnly")
    @ResponseBody
    public void sellStopOnly(){
        sellGuard.closeFlag();
    }


}
