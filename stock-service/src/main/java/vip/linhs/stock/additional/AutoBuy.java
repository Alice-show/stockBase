package vip.linhs.stock.additional;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vip.linhs.stock.api.TradeResultVo;
import vip.linhs.stock.api.request.GetOrdersDataRequest;
import vip.linhs.stock.api.request.RevokeRequest;
import vip.linhs.stock.api.request.SubmitRequest;
import vip.linhs.stock.api.response.GetOrdersDataResponse;
import vip.linhs.stock.api.response.RevokeResponse;
import vip.linhs.stock.api.response.SubmitResponse;
import vip.linhs.stock.model.vo.trade.OrderVo;
import vip.linhs.stock.service.StockService;
import vip.linhs.stock.service.TradeApiService;
import vip.linhs.stock.service.TradeService;
import vip.linhs.stock.util.StockUtil;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class AutoBuy {

    @Autowired
    BuyGuard buyGuard;

    @Autowired
    MyCrawlerService myCrawlerService;

    @Autowired
    TradeApiService tradeApiService;

    @Autowired
    private TradeService tradeService;

    @Autowired
    StockService stockService;

    /**
    *code:不帶前缀的code
    */
    public void trigger(String code){
        if(buyGuard.trial(code)){
            String stockName = stockService.getStockByFullCode(StockUtil.getFullCode(code)).getName();
            double hardenPrice = Double.valueOf(myCrawlerService.getHardenPrice(code));
            int volume = getBuyVolume(hardenPrice);
            buy(code,stockName,hardenPrice,volume);
        }
    }


    public void revoke(String code){
        GetOrdersDataRequest request1 = new GetOrdersDataRequest(1);
        TradeResultVo<GetOrdersDataResponse> response = tradeApiService.getOrdersData(request1);
        if (response.success()) {
            List<OrderVo> list = tradeService.getTradeOrderList(response.getData());
            list = list.stream().filter(v -> v.getState().equals(GetOrdersDataResponse.YIBAO)).collect(Collectors.toList());
            if(list != null && list.size()>0){
                for(OrderVo orderVo:list){
                    if(code.equals(orderVo.getStockCode())){
                        RevokeRequest request = new RevokeRequest(1);
                        String revokes = String.format("%s_%s", DateFormatUtils.format(new Date(), "yyyyMMdd"), orderVo.getEntrustCode());
                        request.setRevokes(revokes);
                        TradeResultVo<RevokeResponse> response1 = tradeApiService.revoke(request);
                        if(response1.success()){
                            log.info("revoke success! [code:{}]",code);
                        }
                    }
                }
            }
        }
    }

    private void buy(String stockCode,String stockName,double price,int volume){
        SubmitRequest request = new SubmitRequest(1);
        request.setAmount(volume);
        request.setPrice(price);
        request.setStockCode(stockCode);
        request.setZqmc(stockName);
        request.setTradeType(SubmitRequest.B);
        request.setMarket(StockUtil.getStockMarket(request.getStockCode()));

        TradeResultVo<SubmitResponse> response = tradeApiService.submit(request);
        if (response.success()) {
            log.info("buy success! [name:{},code:{}]",stockName,stockCode);
        }
    }


    private static final int MAX_PRICE = 5000; //限制价格

    /**
     * 获取额定购买股数，如果购买股数价格大于5000，则只购买一手
     */
    private static Integer getBuyVolume(double price){
        double n = MAX_PRICE/(price*100);//这里的n是整手数
        int a = (int) Math.floor(n);
        if(a==0){
            return 100;
        }
        return a*100;
    }





}
