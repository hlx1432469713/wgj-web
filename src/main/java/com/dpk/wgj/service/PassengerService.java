package com.dpk.wgj.service;

import com.dpk.wgj.bean.Passenger;

public interface PassengerService {

    public Passenger getPassengerByWxId(String passengerWxId) throws Exception;

     public Passenger getPassengerByPhoneNumber(String passengerPhoneNumber) throws Exception;

    public int addPassenger(Passenger passenger) throws Exception;

    public int updatePassengerStatus(Passenger passenger) throws Exception;

    public int updatePassengerPhoneNumber(Passenger passenger) throws Exception;

    public Passenger getPassengerByPassengerId(int passengerId) throws Exception;

}
