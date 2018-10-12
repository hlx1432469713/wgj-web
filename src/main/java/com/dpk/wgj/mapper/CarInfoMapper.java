package com.dpk.wgj.mapper;

import com.dpk.wgj.bean.CarInfo;
import com.dpk.wgj.bean.tableInfo.CarInfoTableMessage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CarInfoMapper {

    public CarInfo getCarInfoByCarNumber(String carNumber) throws Exception;

    public int addCarInfo(CarInfo carInfo) throws Exception;

    public int deleteCarInfoByCarId(int carId) throws Exception;

    public CarInfo getCarInfoByCarId(int carId) throws Exception;

    public int updateCarInfoByCarId(CarInfo carInfo) throws Exception;

    public List<CarInfo> findCarInfoByMultiCondition(CarInfoTableMessage carInfoTableMessage) throws Exception;

    public int findCarInfoByMultiConditionCount(CarInfoTableMessage carInfoTableMessage) throws Exception;

    public List<CarInfo> getCarDriverIdInfo()throws  Exception;

    public CarInfo getCarInfoByDriverId(int DriverId) throws  Exception;

    public int updateCarInfoDriverIdByCarId(CarInfo carInfo) throws  Exception;

    public int updateDeleteCarCarInfoDriverIdByCarId(CarInfo carInfo) throws  Exception;

    public CarInfo getCarInfoNoCompatibleByCarNumber(String carNumber) throws  Exception;


}
