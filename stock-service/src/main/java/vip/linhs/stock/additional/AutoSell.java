package vip.linhs.stock.additional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vip.linhs.stock.api.TradeResultVo;
import vip.linhs.stock.api.request.GetStockListRequest;
import vip.linhs.stock.api.request.SubmitRequest;
import vip.linhs.stock.api.response.GetStockListResponse;
import vip.linhs.stock.api.response.SubmitResponse;
import vip.linhs.stock.model.vo.trade.StockVo;
import vip.linhs.stock.service.StockService;
import vip.linhs.stock.service.TradeApiService;
import vip.linhs.stock.service.TradeService;
import vip.linhs.stock.util.StockUtil;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AutoSell {

    @Autowired
    TradeApiService tradeApiService;

    @Autowired
    TradeService tradeService;

    @Autowired
    SellGuard sellGuard;

    @Autowired
    StockService stockService;

    @Autowired
    MyCrawlerService myCrawlerService;

    List<StockVo> myStockList = new ArrayList<>(); //我的持仓，如果卖出后，需要重新更新我的持仓


    Map<String,Integer> availableVolume = new HashMap<>();

    @Scheduled(initialDelay = 10000,fixedDelay = 1000)
    public void monitoring(){
        if(sellGuard.sellFlag()){
            if(myStockList.size() == 0){
                //如果myStockList为空的话，就刷新出数据。
                refreshMyStock();
            }else {
                //爬取实时价格
                List<PriceDTO> priceDTOS = refreshPriceDTO();

                //止盈策略，根据涨幅区间判断，如果在区间5以上，离最高点3个点以上出，
                // 在2.5到5之间，离最高点1.5个点以上出，
                // 2.5到0之间，离最高点1个点就出，
                // -3.5到-1之间离最低点2个点就出，
                // -1到0之间不出，待定观望

                for (PriceDTO priceDTO : priceDTOS) {
                    if(availableVolume.get(priceDTO.getCode())>0){ //要检查可用数量，如果是等于0的，当它跌到符合我卖出规则时，就会死命刷卖出方法，然后被东方财富电话警告
                        //强制止损，止损点3.5,我觉得开盘走到3个点以下就很过分了
                        if (new BigDecimal(priceDTO.getPercent()).compareTo(new BigDecimal("-3.5")) <= 0) {
                            sell(priceDTO.getCode(),priceDTO.getName(),priceDTO.getDropStopPrice());
                        }

                        //单位价格为（涨停价-跌停价/20）
                        BigDecimal unitPrice = new BigDecimal(priceDTO.getHardenPrice()).subtract(new BigDecimal(priceDTO.getDropStopPrice())).divide(new BigDecimal("20"));

                        //涨幅5或5个点以上 止盈
                        if (new BigDecimal(priceDTO.getPercent()).compareTo(new BigDecimal("5")) >= 0) {
                            if (new BigDecimal(priceDTO.getCurrentPrice()).compareTo(new BigDecimal(priceDTO.getHighest()).subtract(new BigDecimal("3").multiply(unitPrice))) < 0) {//当前价格<最高价减3个单位价，卖出
                                sell(priceDTO.getCode(),priceDTO.getName(),priceDTO.getDropStopPrice());
                            }
                        }

                        //涨幅大于等于2.5小于5 止盈
                        if (new BigDecimal(priceDTO.getPercent()).compareTo(new BigDecimal("2.5")) >= 0
                                && new BigDecimal(priceDTO.getPercent()).compareTo(new BigDecimal("5")) < 0) {
                            if (new BigDecimal(priceDTO.getCurrentPrice()).compareTo(new BigDecimal(priceDTO.getHighest()).subtract(new BigDecimal("1.5").multiply(unitPrice))) < 0) {//当前价格<最高价减1.5个单位价，卖出
                                sell(priceDTO.getCode(),priceDTO.getName(),priceDTO.getDropStopPrice());
                            }
                        }

                        //涨幅大于0，小于2.5 止盈
                        if (new BigDecimal(priceDTO.getPercent()).compareTo(new BigDecimal("0")) >= 0
                                && new BigDecimal(priceDTO.getPercent()).compareTo(new BigDecimal("2.5")) < 0) {
                            if (new BigDecimal(priceDTO.getCurrentPrice()).compareTo(new BigDecimal(priceDTO.getHighest()).subtract(new BigDecimal("1").multiply(unitPrice))) < 0) {//当前价格<最高价减1个单位价，卖出
                                sell(priceDTO.getCode(),priceDTO.getName(),priceDTO.getDropStopPrice());
                            }
                        }

                        //涨幅大于-3，小于等于-1  止损
                        if (new BigDecimal(priceDTO.getPercent()).compareTo(new BigDecimal("-3.5")) > 0
                                && new BigDecimal(priceDTO.getPercent()).compareTo(new BigDecimal("-1")) <= 0) {
                            if (new BigDecimal(priceDTO.getCurrentPrice()).compareTo(new BigDecimal(priceDTO.getLowest()).add(new BigDecimal("2").multiply(unitPrice))) >= 0) {//当前价格>最低点加两个单位价，卖出
                                sell(priceDTO.getCode(),priceDTO.getName(),priceDTO.getDropStopPrice());
                            }
                        }

                    }
                }
            }
        }
    }

    /**
    * 刷新我的持仓
    */
    private void refreshMyStock(){
        //如果myStockList是空的，就拉取持仓数据赋值给它
        GetStockListRequest request = new GetStockListRequest(1);
        TradeResultVo<GetStockListResponse> response = tradeApiService.getStockList(request);
        if (response.success()) {
            myStockList = tradeService.getTradeStockList(response.getData());
            for (StockVo stockVo : myStockList) {
                availableVolume.put(stockVo.getStockCode(),stockVo.getAvailableVolume());
            }
            //StockVo(stockCode=159828, name=医疗ETF, exchange=sz, abbreviation=yletf, totalVolume=15000, price=0.890, availableVolume=15000, costPrice=0.899, profit=-138.38, rate=0.000000)
        }
    }

    /**
    * 刷新价格
    */
    private List<PriceDTO> refreshPriceDTO(){
        String[] codeArr = myStockList.stream().map(stockVo -> StockUtil.getFullCode(stockVo.getStockCode())).collect(Collectors.toList()).toArray(new String[myStockList.size()]);
        String codeStr = String.join(",", codeArr);
        return myCrawlerService.getPriceDtoList(codeStr);
    }

    /**
     * 修改（将name和dropStopPrice的数据传过来，就少一点查询，提升速率）
     * 卖出要做的事情：
     * 提交订单，然后刷新我的持仓
     *
    */
    private void sell(String code,String stockName,String dropStopPrice){
        double stopPrice = Double.valueOf(dropStopPrice);
        int volume = sellAmount(code);

        SubmitRequest request = new SubmitRequest(1);
        request.setAmount(volume);
        request.setPrice(stopPrice);
        request.setStockCode(code);
        request.setZqmc(stockName);
        request.setTradeType(SubmitRequest.S);

        TradeResultVo<SubmitResponse> response = tradeApiService.submit(request);
        if (response.success()) {
            log.info("sell success! [name:{},code:{}]",stockName,code);
        }
        this.refreshMyStock();
    }


    private int sellAmount(String code){
        int volume = 0;
        GetStockListRequest request = new GetStockListRequest(1);
        TradeResultVo<GetStockListResponse> response = tradeApiService.getStockList(request);
        ArrayList<StockVo> list = new ArrayList<>();
        if (response.success()) {
            list.addAll(tradeService.getTradeStockList(response.getData()));
        }
        for (StockVo stockVo : list) {
            if(stockVo.getStockCode().equalsIgnoreCase(code)){
                volume = stockVo.getAvailableVolume();
            }
        }
        return volume;
    }
}
