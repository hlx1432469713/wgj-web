package com.dpk.wgj.controller;

import com.dpk.wgj.bean.*;
import com.dpk.wgj.bean.DTO.AccessDriverDTO;
import com.dpk.wgj.bean.DTO.OrderDTO;
import com.dpk.wgj.bean.DTO.OrderInfoDTO;
import com.dpk.wgj.bean.DTO.UserDTO;
import com.dpk.wgj.bean.tableInfo.LocationMessage;
import com.dpk.wgj.bean.tableInfo.OrderInfoTableMessage;
import com.dpk.wgj.bean.tableInfo.OrderMessage;
import com.dpk.wgj.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
//@RequestMapping(value = "/admin/order")
public class OrderInfoController {

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private PassengerService passengerService;

    @Autowired
    private DriverInfoService driverInfoService;

    @Autowired
    private CarInfoService carInfoService;

    @Autowired
    private LogInfoService logInfoService;

    /**
     * 多条件查询车辆轨迹
     * @param locationMessage
     * @return
     */
    @RequestMapping(value = "/admin/order/getLocationInfoByDate", method = RequestMethod.POST)
    public Message getLocationInfoByDate(@RequestBody LocationMessage locationMessage){
        try {
//            String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(locationMessage.getStartTime());//将时间格式转换成符合Timestamp要求的格式.
//            Timestamp newdate = Timestamp.valueOf(nowTime);//把时间转换
//
//            locationMessage.setStartTime(newdate);

            List<OrderInfo> locations = orderInfoService.getLocationInfoByDate(locationMessage);
            if (locations != null){
                return new Message(Message.SUCCESS, "获取车辆轨迹 >> 成功", locations);
            }
            return new Message(Message.FAILURE, "获取车辆轨迹 >> 失败", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(Message.ERROR, "获取车辆轨迹 >> 异常", e.getMessage());
        }
    }

    /**
     * 乘客端 一键呼车 功能
     */
    @RequestMapping(value = "/api/passenger/addOrderInfo", method = RequestMethod.POST)
    @Transactional
    public Message addOrderInfo(@RequestBody OrderMessage orderMessage){

        OrderInfo orderInfo = new OrderInfo();
        int addStatus = 0;

        UserDTO userInfo = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getDetails();
        int passengerId = userInfo.getUserId();

        orderInfo.setStartLocation(orderMessage.getStartLocation());
        orderInfo.setEndLocation(orderMessage.getEndLocation());
        orderInfo.setPassengerId(passengerId);
        orderInfo.setLocationInfo(orderMessage.getLocationInfo());

        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());//将时间格式转换成符合Timestamp要求的格式.
        Timestamp newdate = Timestamp.valueOf(nowTime);//把时间转换

        orderInfo.setStartTime(nowTime);

        // 订单切换至 下单状态
        orderInfo.setOrderStatus(0);

        try {
            addStatus = orderInfoService.addOrderInfo(orderInfo);
            if (addStatus == 1){

                // 插入用户成为日志
                logInfoService.addLogInfo(new LogInfo("乘客端 >> 一键呼车", 2, new Date(), orderInfo.getOrderId()));

                Passenger passenger = new Passenger();
                passenger.setPassengerId(passengerId);
                //乘客状态切换至 服务中
                passenger.setPassengerStatus(0);
//                passenger.setPassengerStatus(1);
                int upStatus = passengerService.updatePassengerStatus(passenger);
                if (upStatus == 1){
                    OrderInfo targetOrderInfo = orderInfoService.getOrderInfoByOrderId(orderInfo.getOrderId());
//                    DriverInfo driverInfo = driverInfoService.getDriverInfoByDriverId(targetOrderInfo.getDriverId());
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("OrderInfo", targetOrderInfo);
//                    map.put("DriverInfo", driverInfo);
                    return new Message(Message.SUCCESS, "创建订单信息&切换用户状态 >> 成功 >> 获得目标订单", targetOrderInfo);
                }
                return new Message(Message.FAILURE, "创建订单信息&切换用户状态 >> 失败 ", addStatus + " " + upStatus);
            }else {
                return new Message(Message.FAILURE, "创建订单信息 >> 失败", addStatus);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(Message.ERROR, "创建订单信息 >> 异常", e.getMessage());
        }
    }

    /**
     * 乘客端 >> 获取订单列表
     * @return
     */
    @RequestMapping(value = "/api/passenger/getOrderInfoByPassengerId", method = RequestMethod.GET)
    @Transactional
    public Message getOrderInfoByPassengerId(){

        List<OrderInfo> orderInfos;
        List<OrderDTO> orderDTOList = new ArrayList<>();
        UserDTO userInfo = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getDetails();
        int passengerId = userInfo.getUserId();
        try {
            orderInfos = orderInfoService.getOrderInfoByPassengerId(passengerId);
            if (orderInfos != null){
                for (OrderInfo orderInfo : orderInfos){
                    DriverInfo driverInfo = new DriverInfo();
                    CarInfo carInfo = new CarInfo();
                    OrderDTO dto = new OrderDTO();
                    driverInfo = driverInfoService.getDriverInfoByDriverId(orderInfo.getDriverId());
                    carInfo = carInfoService.getCarInfoByCarId(driverInfo.getCarId());
                    dto.setDriverInfo(driverInfo);
                    dto.setOrderInfo(orderInfo);
                    dto.setCarInfo(carInfo);
                    orderDTOList.add(dto);
                }
//                System.out.println(orderDTOList.get(0).getOrderInfo().ges);
                return new Message(Message.SUCCESS, "乘客端 >> 获取订单列表 >> 成功", orderDTOList);
            } else {
                return new Message(Message.FAILURE, "乘客端 >> 获取订单列表 >> 失败", orderDTOList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(Message.ERROR, "乘客端 >> 获取订单列表 >> 异常", e.getMessage());
        }

    }

    /**
     * 司机端 >> 获取订单列表
     * @return
     */
    @RequestMapping(value = "/api/driver/getOrderInfoByDriverId", method = RequestMethod.GET)
    public Message getOrderInfoByDriverId(){
        List<OrderInfo> orderInfos;
        UserDTO userInfo = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getDetails();
        int driverId = userInfo.getUserId();
        try {
            orderInfos = orderInfoService.getOrderInfoByDriverId(driverId);
            if (orderInfos != null){
                return new Message(Message.SUCCESS, "司机端 >> 获取订单列表 >> 成功", orderInfos);
            } else {
                return new Message(Message.FAILURE, "司机端 >> 获取订单列表 >> 失败", orderInfos);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(Message.ERROR, "司机端 >> 获取订单列表 >> 异常", e.getMessage());
        }
    }

    /**
     * 司机端 >> 申请改派
     */
    @RequestMapping(value = "/api/driver/updateOrderInfoByOrderId", method = RequestMethod.POST)
    @Transactional
    public Message updateOrderInfoByOrderId(@RequestBody OrderInfo order){
        int upStatus = 0;
        // 防止恶意注入
        UserDTO userInfo = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getDetails();
        int driverId = userInfo.getUserId();

        try {
            OrderInfo orderInfo = orderInfoService.getOrderInfoByOrderId(order.getOrderId());
            // 插入用户成为日志
            logInfoService.addLogInfo(new LogInfo("司机端 >> 申请改派", 1, new Date(), orderInfo.getOrderId()));

            if (orderInfo != null && driverId == orderInfo.getDriverId()){
                orderInfo.setDriverId(0);
                orderInfo.setOrderStatus(0);
                upStatus = orderInfoService.updateOrderInfoByOrderId(orderInfo);
                if (upStatus == 1){
                    // 重新匹配新的司机

                    DriverInfo driverInfo = new DriverInfo();
                    driverInfo.setDriverId(driverId);
                    // 用时切换司机状态 至 服务前（接单前）
                    //driverInfo.setFlag(1);
                    driverInfo.setFlag(0);
                    int upFlag = driverInfoService.updateFlag(driverInfo);
                    if(upFlag == 1){
                        return new Message(Message.SUCCESS, "司机端 >> 申请改派 && 司机状态切换至 服务前（接单前） >> 成功", upStatus);
                    }
                }
            }
            return new Message(Message.FAILURE, "司机端 >> 申请改派 >> 失败", "错误请求");
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(Message.ERROR, "司机端 >> 申请改派 >> 异常", e.getMessage());
        }

    }

    /**
     * 司机端 >> 接到乘客后确认时 调用
     */
    @RequestMapping(value = "/api/driver/accessToServiceForDriver", method = RequestMethod.POST)
    @Transactional
    public Message accessToServiceForDriver(@RequestBody AccessDriverDTO accessDriverDTO){
        int upStatus = 0;

        // 防止恶意注入
        UserDTO userInfo = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getDetails();
        int driverId = userInfo.getUserId();
        String driverWxId = userInfo.getUsername();

        try {
            OrderInfo orderInfo = orderInfoService.getOrderInfoByOrderId(accessDriverDTO.getOrderId());

            DriverInfo driverInfo = driverInfoService.getDriverInfoByWxId(driverWxId);

            // 插入用户成为日志
            logInfoService.addLogInfo(new LogInfo("司机端 >> 接到乘客", 1, new Date(), orderInfo.getOrderId()));

            String[] current = accessDriverDTO.getCurrentLocation().split(",");

            String[] target = accessDriverDTO.getTargetLocation().split(",");

            double x = Math.abs(Double.parseDouble(current[0]) - Double.parseDouble(target[0]));
            double y = Math.abs(Double.parseDouble(current[1]) - Double.parseDouble(target[1]));

            // 在一定范围才能 确定接到用户
            if (x <= 0.016 && y <= 0.016){
                // 当司机当前位置 与 用户所定的起始位置 一致才能切换 订单状态
                if (orderInfo != null && driverId == orderInfo.getDriverId()){
                    orderInfo.setOrderStatus(2);
                    upStatus = orderInfoService.updateOrderInfoByOrderId(orderInfo);
                    if (upStatus == 1){
                        return new Message(Message.SUCCESS, "司机端 >> 已接到乘客 >> 进入派送状态", upStatus);
                    }
                }
            }
            return new Message(Message.FAILURE, "司机端 >> 未到目的地 ", "错误请求");
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(Message.ERROR, "司机端 >> 申请改派 >> 异常", e.getMessage());
        }

    }
    /**
     * 乘客端 >> 取消订单（司机还未接单）
     */
    @RequestMapping(value = "/api/passenger/cancelOrderForPassenger", method = RequestMethod.POST)
    @Transactional
    public Message cancelOrderForPassenger (@RequestBody OrderInfo order){
        int upStatus = 0;

        // 防止恶意注入
        UserDTO userInfo = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getDetails();
        int passengerId = userInfo.getUserId();

        try {
            OrderInfo orderInfo = orderInfoService.getOrderInfoByOrderId(order.getOrderId());

            // 插入用户成为日志
            logInfoService.addLogInfo(new LogInfo("乘客端 >> 取消订单", 2, new Date(), orderInfo.getOrderId()));

            if (orderInfo != null && passengerId == orderInfo.getPassengerId()){
                orderInfo.setOrderStatus(4);
                String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());//将时间格式转换成符合Timestamp要求的格式.
                Timestamp newdate = Timestamp.valueOf(nowTime);//把时间转换

                orderInfo.setEndTime(nowTime);
                upStatus = orderInfoService.updateOrderInfoByOrderId(orderInfo);
                if (upStatus == 1){
//                    //乘客状态切换至 服务后
                    Passenger passenger = new Passenger();
                    passenger.setPassengerId(passengerId);
                    passenger.setPassengerStatus(2);

                    int upPassengerStatus = passengerService.updatePassengerStatus(passenger);
                    //int upFlag = driverInfoService.updateFlag(driverInfo);
                    if (upPassengerStatus == 1){
                        return new Message(Message.SUCCESS, "乘客端 >> 取消订单 && 乘客状态切换 >> 成功", upStatus + upPassengerStatus );
                    }
                }
            }
            return new Message(Message.FAILURE, "乘客端 >> 取消订单 >> 失败", "错误请求");
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(Message.ERROR, "乘客端 >> 取消订单 >> 异常", e.getMessage());
        }

    }


    /**
     * 乘客端 >> 取消订单（司机已经接单）
     */
    @RequestMapping(value = "/api/passenger/updateOrderInfoByOrderId", method = RequestMethod.POST)
    @Transactional
    public Message cancelOfOrderForPassenger (@RequestBody OrderInfo order){
        int upStatus = 0;

        // 防止恶意注入
        UserDTO userInfo = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getDetails();
        int passengerId = userInfo.getUserId();

        try {
            OrderInfo orderInfo = orderInfoService.getOrderInfoByOrderId(order.getOrderId());

            // 插入用户成为日志
            logInfoService.addLogInfo(new LogInfo("乘客端 >> 取消订单", 2, new Date(), orderInfo.getOrderId()));

            int driverId = orderInfo.getDriverId();
            if (orderInfo != null && passengerId == orderInfo.getPassengerId()){
                orderInfo.setOrderStatus(4);
                String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());//将时间格式转换成符合Timestamp要求的格式.
                Timestamp newdate = Timestamp.valueOf(nowTime);//把时间转换

                orderInfo.setEndTime(nowTime);
                upStatus = orderInfoService.updateOrderInfoByOrderId(orderInfo);
                if (upStatus == 1){

                    //乘客状态切换至 服务后 同时也要修改司机服务状态为 接单前
                    DriverInfo driverInfo = new DriverInfo();
                    driverInfo.setDriverId(driverId);
                    //driverInfo.setDriverStatus(0);
                    driverInfo.setFlag(0);

                    Passenger passenger = new Passenger();
                    passenger.setPassengerId(passengerId);
                    passenger.setPassengerStatus(2);

                    int upPassengerStatus = passengerService.updatePassengerStatus(passenger);
                    int upFlag = driverInfoService.updateFlag(driverInfo);
                    if (upPassengerStatus == 1 && upFlag == 1){
                        return new Message(Message.SUCCESS, "乘客端 >> 取消订单 && 乘客/司机状态切换 >> 成功", upStatus + upPassengerStatus + upFlag);
                    }
                }
            }
            return new Message(Message.FAILURE, "乘客端 >> 取消订单 >> 失败", "错误请求");
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(Message.ERROR, "乘客端 >> 取消订单 >> 异常", e.getMessage());
        }

    }

    /**
     * 司机端 >> 送达目的地后 确认
     * @return
     */
    @RequestMapping(value = "/api/driver/arrivedTargetLocation", method = RequestMethod.POST)
    @Transactional
    public Message arrivedTargetLocation (@RequestBody AccessDriverDTO accessDriverDTO){
        int upStatus = 0;

        // 防止恶意注入
        UserDTO userInfo = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getDetails();
        int driverId = userInfo.getUserId();
        String driverWxId = userInfo.getUsername();

        try {
            OrderInfo orderInfo = orderInfoService.getOrderInfoByOrderId(accessDriverDTO.getOrderId());

            // 插入用户成为日志
            logInfoService.addLogInfo(new LogInfo("司机端 >> 送达目的地后", 1, new Date(), orderInfo.getOrderId()));

            DriverInfo driverInfo = driverInfoService.getDriverInfoByWxId(driverWxId);

            String[] current = accessDriverDTO.getCurrentLocation().split(",");
            String[] target = accessDriverDTO.getTargetLocation().split(",");

            double x = Math.abs(Double.parseDouble(current[0]) - Double.parseDouble(target[0]));

            double y = Math.abs(Double.parseDouble(current[1]) - Double.parseDouble(target[1]));

            if (x <= 0.016 && y <= 0.016) {
                // 当司机当前位置 与 用户所定的目的位置 一致才能切换 订单状态
                if (orderInfo != null && driverId == orderInfo.getDriverId()) {
                    orderInfo.setOrderStatus(3);
                    String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());//将时间格式转换成符合Timestamp要求的格式.
                    Timestamp newdate = Timestamp.valueOf(nowTime);//把时间转换

                    orderInfo.setEndTime(nowTime);
                    orderInfo.setLocationInfo(accessDriverDTO.getLocationInfo());//获取移动轨迹

                    upStatus = orderInfoService.updateOrderInfoByOrderId(orderInfo);
                    if (upStatus == 1) {

                        //乘客状态切换至 服务后 同时也要修改司机服务状态为 接单前
                        int passengerId = orderInfo.getPassengerId();
                        Passenger passenger = new Passenger();
                        passenger.setPassengerId(passengerId);
                        passenger.setPassengerStatus(2);
                        // driverInfo.setDriverStatus(0);
                        driverInfo.setFlag(0);

                        int upFlag = driverInfoService.updateFlag(driverInfo);
                        int upPassengerStatus = passengerService.updatePassengerStatus(passenger);

                        if (upPassengerStatus == 1 && upFlag == 1) {
                            return new Message(Message.SUCCESS, "司机端 >> 完成订单 && 乘客/用户 状态切换 >> 成功", upStatus + upPassengerStatus);
                        }
                    }
                }
            }
            return new Message(Message.FAILURE, "司机端 >> 完成订单 && 乘客/用户 状态切换 >> 失败 ", "错误请求");
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(Message.ERROR, "司机端 >> 申请改派 >> 异常", e.getMessage());
        }

    }

    /**
     * 司机端 >> 获得订单id
     * @param tableMessage
     * @return
     */
    @RequestMapping(value = "/api/driver/getOrderIdForDriver", method = RequestMethod.POST)
    @Transactional
    public Message getOrderIdForDriver (@RequestBody OrderInfoTableMessage tableMessage){
        List<OrderInfo> orderInfos = new ArrayList<>();
        List<OrderInfoDTO> infoDTOList = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();

        try {
            orderInfos = orderInfoService.findOrderInfoByMultiCondition(tableMessage);
            if (orderInfos != null){
                for(OrderInfo orderInfo : orderInfos){
                    int driverId = orderInfo.getDriverId();
                    int passengerId = orderInfo.getPassengerId();
                    DriverInfo driverInfo = driverInfoService.getDriverInfoByDriverId(driverId);
                    Passenger passenger = passengerService.getPassengerByPassengerId(passengerId);
                    CarInfo carInfo = carInfoService.getCarInfoByCarId(driverInfo.getCarId());
                    // 判断车辆所有权 未完成

                    OrderInfoDTO orderInfoDTO = new OrderInfoDTO(orderInfo, carInfo, driverInfo, passenger);
                    infoDTOList.add(orderInfoDTO);
                }
                int count = orderInfoService.findOrderInfoByMultiConditionCount(tableMessage);
                map.put("orderList", infoDTOList);
                map.put("count", count);
                return new Message(Message.SUCCESS, "司机端 >> 获得订单id >> 成功", map);
            }
            return new Message(Message.FAILURE, "司机端 >> 获得订单id >> 成功", "无查询结果");
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(Message.ERROR, "司机端 >> 获得订单id >> 异常", "请求异常 ");
        }
    }

    /**
     * 后台端 >> 多条件查询订单
     * @return
     */
    @RequestMapping(value = "/admin/findOrderInfoByMultiCondition", method = RequestMethod.POST)
    @Transactional
    public Message findOrderInfoByMultiCondition(@RequestBody OrderInfoTableMessage tableMessage){

        List<OrderInfo> orderInfos = new ArrayList<>();
        List<OrderInfoDTO> infoDTOList = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();

//        tableMessage.getDriverInfo().setDriverName("%" + tableMessage.getDriverInfo().getDriverName() + "%");
//        tableMessage.getPassenger().setPassengerPhoneNumber("%" + tableMessage.getPassenger().getPassengerPhoneNumber() + "%");
//        tableMessage.getCarInfo().setCarNumber("%" + tableMessage.getCarInfo().getCarNumber() + "%");

        try {
            orderInfos = orderInfoService.findOrderInfoByMultiCondition(tableMessage);
            if (orderInfos != null){
                for(OrderInfo orderInfo : orderInfos){
                    int driverId = orderInfo.getDriverId();
                    int passengerId = orderInfo.getPassengerId();
                    DriverInfo driverInfo = driverInfoService.getDriverInfoByDriverId(driverId);
                    Passenger passenger = passengerService.getPassengerByPassengerId(passengerId);
                    CarInfo carInfo = carInfoService.getCarInfoByCarId(driverInfo.getCarId());
                    OrderInfoDTO orderInfoDTO = new OrderInfoDTO(orderInfo, carInfo, driverInfo, passenger);
                    infoDTOList.add(orderInfoDTO);
                }
                int count = orderInfoService.findOrderInfoByMultiConditionCount(tableMessage);
                map.put("orderList", infoDTOList);
                map.put("count", count);
                return new Message(Message.SUCCESS, "后台端 >> 多条件查询订单 >> 成功", map);
            }
            return new Message(Message.FAILURE, "后台端 >> 多条件查询订单 >> 成功", "无查询结果");
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(Message.ERROR, "后台端 >> 多条件查询订单 >> 异常", "请求异常 ");
        }
    }

    /**
     * 司机端 >> 获得单个订单
     * @param orderInfo
     * @return
     */
    @RequestMapping(value = "/api/driver/getOrderByOrderId", method = RequestMethod.POST)
    @Transactional
    public Message getOrderByOrderId (@RequestBody OrderInfo orderInfo){
        OrderInfo orderResult = new OrderInfo();
        OrderInfoDTO infoDTO = new OrderInfoDTO();

        Map<String, Object> map = new HashMap<>();

        try {
            orderResult = orderInfoService.getOrderInfoByOrderId(orderInfo.getOrderId());
            if (orderResult != null){
                int driverId = orderResult.getDriverId();
                int passengerId = orderResult.getPassengerId();
                DriverInfo driverInfo = driverInfoService.getDriverInfoByDriverId(driverId);
                Passenger passenger = passengerService.getPassengerByPassengerId(passengerId);
                CarInfo carInfo = carInfoService.getCarInfoByCarId(driverInfo.getCarId());
                // 判断车辆所有权 未完成
                OrderInfoDTO orderInfoDTO = new OrderInfoDTO(orderResult, carInfo, driverInfo, passenger);
                map.put("order", orderInfoDTO);
                return new Message(Message.SUCCESS, "司机端 >> 获得订单 >> 成功", map);
            }

            return new Message(Message.FAILURE, "司机端 >> 获得订单 >> 成功", "无查询结果");
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(Message.ERROR, "司机端 >> 获得订单 >> 异常", "请求异常 ");
        }
    }

}
