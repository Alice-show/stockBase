package vip.linhs.stock.additional;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PriceDTO {
    /**
     * 1.名字
     */
    public String name;
    /**
     * 2.代码
     */
    public String code;
    /**
     * 3.当前价格
     */
    public String currentPrice;
    /**
     * 4.昨日收盘价
     */
    public String yesDayPrice;
    /**
     * 5.今日开盘价
     */
    public String toDayOpenPrice;
    /**
     * 7.外盘
     */
    public String outPan;
    /**
     * 8.内盘
     */
    public String innerPan;
    /**
     * 32.涨跌幅度
     * 比如今天涨停，percent就等于10
     */
    public String percent;
    /**
     * 33.当天最高价格
     */
    public String highest;
    /**
     * 34.当天最低价格
     */
    public String lowest;
    /**
     * 36.成交量
     */
    public String dealCount;
    /**
     * 37.成交额
     */
    public String dealMoney;
    /**
     * 38.换手率
     */
    public String changeHand;
    /**
     * 39.市盈率
     */
    public String PE;
    /**
     * 47.涨停价
     */
    public String hardenPrice;
    /**
     * 48.跌停价
     */
    public String dropStopPrice;

}
