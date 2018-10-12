package com.dpk.wgj.bean.DTO;

import com.dpk.wgj.bean.CarInfo;
import com.dpk.wgj.bean.DriverInfo;
import com.dpk.wgj.bean.OrderInfo;
import com.dpk.wgj.bean.Passenger;

import java.io.Serializable;

/**
 * Created by zhoulin on 2018/7/11.
 * 订单关联类
 */
public class OrderInfoDTO implements Serializable {

    private OrderInfo orderInfo = new OrderInfo();

    private CarInfo carInfo = new CarInfo();

    private DriverInfo driverInfo = new DriverInfo();

    private Passenger passenger = new Passenger();

    public OrderInfoDTO() {
    }

    public OrderInfoDTO(OrderInfo orderInfo, CarInfo carInfo, DriverInfo driverInfo, Passenger passenger) {
        this.orderInfo = orderInfo;
        this.carInfo = carInfo;
        this.driverInfo = driverInfo;
        this.passenger = passenger;
    }

    public OrderInfo getOrderInfo() {
        return orderInfo;
    }

    public void setOrderInfo(OrderInfo orderInfo) {
        this.orderInfo = orderInfo;
    }

    public CarInfo getCarInfo() {
        return carInfo;
    }

    public void setCarInfo(CarInfo carInfo) {
        this.carInfo = carInfo;
    }

    public DriverInfo getDriverInfo() {
        return driverInfo;
    }

    public void setDriverInfo(DriverInfo driverInfo) {
        this.driverInfo = driverInfo;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public void setPassenger(Passenger passenger) {
        this.passenger = passenger;
    }
}
