package vip.linhs.stock.additional;

import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vip.linhs.stock.util.HttpUtil;
import vip.linhs.stock.util.StockUtil;

import java.util.ArrayList;
import java.util.List;

@Service
public class MyCrawlerService {

    @Autowired
    private CloseableHttpClient httpClient;

    /**
     *
     * http://qt.gtimg.cn/q=fullCode 接口返回参数含义
     *0: 未知
     * 1: 名字
     * 2: 代码
     * 3: 当前价格
     * 4: 昨收
     * 5: 今开
     * 6: 成交量（手）
     * 7: 外盘
     * 8: 内盘
     * 9: 买一
     * 10: 买一量（手）
     * 11-18: 买二 买五
     * 19: 卖一
     * 20: 卖一量
     * 21-28: 卖二 卖五
     * 29: 最近逐笔成交
     * 30: 时间
     * 31: 涨跌
     * 32: 涨跌%
     * 33: 最高
     * 34: 最低
     * 35: 价格/成交量（手）/成交额
     * 36: 成交量（手）
     * 37: 成交额（万）
     * 38: 换手率
     * 39: 市盈率
     * 40:
     * 41: 最高
     * 42: 最低
     * 43: 振幅
     * 44: 流通市值
     * 45: 总市值
     * 46: 市净率
     * 47: 涨停价
     * 48: 跌停价
     */

    public List<PriceDTO> getPriceDtoList(String codes) {
        String url = "http://qt.gtimg.cn/q="+codes;
        String context = HttpUtil.sendGet(httpClient,url,"gbk");
        String[] arrs = context.split(";");

        List<PriceDTO> priceDTOS = new ArrayList<>();
        for (int i = 0; i < arrs.length; i++) {
            if(arrs[i]!=arrs[arrs.length-1]){
                String[] arr = arrs[i].split("~");
                PriceDTO priceDTO = new PriceDTO();
                priceDTO.setName(arr[1]);
                priceDTO.setCode(arr[2]);
                priceDTO.setCurrentPrice(arr[3]);
                priceDTO.setYesDayPrice(arr[4]);
                priceDTO.setToDayOpenPrice(arr[5]);
                priceDTO.setOutPan(arr[7]);
                priceDTO.setInnerPan(arr[8]);
                priceDTO.setPercent(arr[32]);
                priceDTO.setHighest(arr[33]);
                priceDTO.setLowest(arr[34]);
                priceDTO.setDealCount(arr[36]);
                priceDTO.setDealMoney(arr[37]);
                priceDTO.setChangeHand(arr[38]);
                priceDTO.setPE(arr[39]);
                priceDTO.setHardenPrice(arr[47]);
                priceDTO.setDropStopPrice(arr[48]);
                priceDTOS.add(priceDTO);
            }
        }
        return priceDTOS;
    }


    public String getHardenPrice(String code) {
        String url = "http://qt.gtimg.cn/q="+ StockUtil.getFullCode(code);
        String context = HttpUtil.sendGet(httpClient,url,"gbk");
        String HardenPrice =  context.split("~")[47];
        return HardenPrice;
    }
}
