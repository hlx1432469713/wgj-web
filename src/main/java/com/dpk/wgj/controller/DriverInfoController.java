package com.dpk.wgj.controller;

import com.dpk.wgj.POI.Excel;
import com.dpk.wgj.bean.CarInfo;
import com.dpk.wgj.bean.DTO.CarInfoDTO;
import com.dpk.wgj.bean.DTO.UserDTO;
import com.dpk.wgj.bean.DriverInfo;
import com.dpk.wgj.bean.LogInfo;
import com.dpk.wgj.bean.Message;
import com.dpk.wgj.bean.tableInfo.DriverInfoTableMessage;
import com.dpk.wgj.mapper.AdminGroupAuthorityMapper;
import com.dpk.wgj.service.CarInfoService;
import com.dpk.wgj.service.DriverInfoService;
import com.dpk.wgj.service.LogInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping(value = "/admin/driver")
public class DriverInfoController {

    @Autowired
    private DriverInfoService driverInfoService;

    @Autowired
    private CarInfoService carInfoService;

    @Autowired
    private LogInfoService logInfoService;

    @Autowired
    private Excel excel;

    @Autowired
    private AdminGroupAuthorityMapper adminGroupAuthorityMapper;

    private final Logger logger = LoggerFactory.getLogger(DriverInfoController.class);

    /**
     * 获取全部信息
     */

    @RequestMapping(value = "/getAllDriverInfo",method = RequestMethod.GET)
    public Message getAllDriverInfo()  {
        List<CarInfoDTO> carInfoDTO = new ArrayList<CarInfoDTO>();
        try {
            List<DriverInfo> driverInfo = driverInfoService.getAllDriverInfo();

            if (driverInfo != null){
                for(DriverInfo driverInfo1 : driverInfo) {
                    CarInfo carInfo = carInfoService.getCarInfoByCarId(driverInfo1.getCarId());
                    carInfoDTO.add(new CarInfoDTO(carInfo, driverInfo1));
                }
                return new Message(Message.SUCCESS, "查询全部司机信息 >> 成功", carInfoDTO);
            }
            else{
                return new Message(Message.FAILURE, "查询司机信息 >> 失败", "未查询到司机信息");
            }

        } catch (Exception e) {
            return new Message(Message.ERROR, "查询司机信息 >> 异常", e.getMessage());
        }

    }

    /**
     * 根据司机名字查找司机信息，同时可以关联上车辆信息
     */
    @RequestMapping(value = "/getDriveInfoByDriverName/{driverName}", method = RequestMethod.GET)
    public Message getDriverInfoByDriverName(@PathVariable(value = "driverName") String driverName){
        DriverInfo driverInfo;
        try {
            driverInfo = driverInfoService.getDriverInfoByDriverName(driverName);

            if (driverInfo != null){
                CarInfo carInfo = carInfoService.getCarInfoByCarId(driverInfo.getCarId());
                CarInfoDTO carInfoDTO = new CarInfoDTO(carInfo, driverInfo);
                return new Message(Message.SUCCESS, "查询司机信息 >> 成功", carInfoDTO);
            }
            return new Message(Message.FAILURE, "查询司机信息 >> 失败", "未查询到司机姓名为 [" + driverName + "] 的信息");
        } catch (Exception e) {
            return new Message(Message.ERROR, "查询司机信息 >> 异常", e.getMessage());
        }

    }

    /**
     * 根据司机手机号查找司机信息，同时可以关联上司机的车辆信息
     */
    @RequestMapping(value = "/getDriveInfoByDriverPhoneNumber/{driverPhoneNumber}", method = RequestMethod.GET)
    public Message getDriverInfoByDriverPhoneNumber(@PathVariable(value = "driverPhoneNumber") String driverPhoneNumber){
        DriverInfo driverInfo;
        try {
            driverInfo = driverInfoService.getDriverInfoByDriverPhoneNumber(driverPhoneNumber);
            if (driverInfo != null){
                CarInfo carInfo = carInfoService.getCarInfoByCarId(driverInfo.getCarId());
                CarInfoDTO carInfoDTO = new CarInfoDTO(carInfo, driverInfo);
                return new Message(Message.SUCCESS, "查询司机信息 >> 成功", carInfoDTO);
            }
            return new Message(Message.FAILURE, "查询司机信息 >> 失败", "未查询到司机手机号码为 [" + driverPhoneNumber + "] 的信息");
        } catch (Exception e) {
            return new Message(Message.ERROR, "查询司机信息 >> 异常", e.getMessage());
        }

    }

    /**
     * 根据司机星级查找司机信息，同时可以关联上司机的车辆信息
     */
    @RequestMapping(value = "/getDriveInfoByDriverLevelStar/{driverLevelStar}", method = RequestMethod.GET)
    public Message getDriveInfoByDriverLevelStar(@PathVariable(value = "driverLevelStar") int driverLevelStar){
        DriverInfo driverInfo;
        try {
            driverInfo = driverInfoService.getDriveInfoByDriverLevelStar(driverLevelStar);
            if (driverInfo != null){
                CarInfo carInfo = carInfoService.getCarInfoByCarId(driverInfo.getCarId());
                CarInfoDTO carInfoDTO = new CarInfoDTO(carInfo, driverInfo);
                return new Message(Message.SUCCESS, "查询司机信息 >> 成功", carInfoDTO);
            }
            return new Message(Message.FAILURE, "查询司机信息 >> 失败", "未查询到司机星级为 [" + driverLevelStar + "] 的信息");
        } catch (Exception e) {
            return new Message(Message.ERROR, "查询司机信息 >> 异常", e.getMessage());
        }

    }
    /**
     * 多条件查询司机信息，同时给司机匹配和绑定车辆信息
     * @param tableMessage
     * @return
     */
    @RequestMapping(value = "/getDriverInfoByMultiCondition", method = RequestMethod.POST)
    public Message getDriverByMultiCondition(@RequestBody DriverInfoTableMessage tableMessage){
        List<DriverInfo> driverInfo;
        CarInfo carInfo;
        List <CarInfo> carInfoCarDriverId;
        List<CarInfoDTO> carInfoDTOList = new ArrayList<>();
        int count = 0;
        Map<String, Object> map = new HashMap<>();
        tableMessage.getDriverInfo().setDriverName("%" + tableMessage.getDriverInfo().getDriverName() + "%");
        try {
          //  carInfoCarDriverId = carInfoService.getCarDriverIdInfo();    //获取car_driver_id_a和car_driver_id_b为空的车辆信息

            driverInfo = driverInfoService.getDriverByMultiCondition(tableMessage);
            count = driverInfoService.getDriverByMultiConditionCount(tableMessage);
            if (driverInfo != null)
            {
                for (DriverInfo driverInfos : driverInfo)
                {
//                        for (CarInfo carInfoCarDriverId1:carInfoCarDriverId)
//                        {
//                            if(driverInfos.getCarId()==0 )
//                            {
//                                if (carInfoCarDriverId1.getCarDriverIdA()==0)
//                                {
//                                    driverInfos.setCarId(carInfoCarDriverId1.getCarId());
//                                    carInfoCarDriverId1.setCarDriverIdA(driverInfos.getDriverId());
//                                    driverInfoService.updateDriverInfoByDriverId(driverInfos);
//                                    carInfoService.updateCarInfoByCarId(carInfoCarDriverId1);
//                                    break;
//                                }
//                                else if (carInfoCarDriverId1.getCarDriverIdB()==0)
//                                {
//                                    driverInfos.setCarId(carInfoCarDriverId1.getCarId());
//                                    carInfoCarDriverId1.setCarDriverIdB(driverInfos.getDriverId());
//                                    driverInfoService.updateDriverInfoByDriverId(driverInfos);
//                                    carInfoService.updateCarInfoByCarId(carInfoCarDriverId1);
//                                    break;
//                                }
//                            else
//                                continue;
//                            }
//                        }
                        carInfo = carInfoService.getCarInfoByCarId(driverInfos.getCarId());
                        CarInfoDTO carInfoDTO = new CarInfoDTO(carInfo, driverInfos);
                        carInfoDTOList.add(carInfoDTO);
                }
                map.put("count", count);
                map.put("driverInfos", carInfoDTOList);
                return new Message(Message.SUCCESS, "查询车辆信息 >> 成功", map);
            }
            return new Message(Message.FAILURE, "查询车辆信息 >> 失败", "无符合条件车辆");
        } catch (Exception e) {
            return new Message(Message.ERROR, "查询车辆信息 >> 异常",  e.getMessage());
        }
    }
    /**
     * 更新司机信息
     */
    @RequestMapping(value = "/updateDriverInfoByDriverId",method = RequestMethod.POST)
    public Message updateDriverInfoByDriverId(@RequestBody DriverInfo driverInfo) {
        UserDTO userInfo = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getDetails();
        int upStatus = 0;
        try { String authorityContent = adminGroupAuthorityMapper.getAdminGroupAuthorityById(userInfo.getAuthorityId()).getAdminGroupName();
            if ( authorityContent.equals("高级管理员") || authorityContent.equals("超级管理员")) {
                upStatus = driverInfoService.updateDriverInfoByDriverId(driverInfo);//更新司机所绑定的车辆的Id
                if (upStatus == 1)
                {
                    return new Message(Message.SUCCESS, "更新司机信息 >> 成功", upStatus);
                }
                else {
                    return new Message(Message.FAILURE, "更新司机信息 >> 失败", "更新失败");
                }
                }
            return new Message(Message.NOT_LEGAL, "权限不合法",  "您是"+authorityContent+",权限不足，无法进行修改处理！");
        } catch (Exception e) {
            return new Message(Message.ERROR, "更新司机信息 >> 异常", e.getMessage());
        }
    }

    /**
     * 根据司机Id删除司机信息
     * @param driverId
     */
    @RequestMapping(value = "/deleteDriverInfoByDriverId", method = RequestMethod.POST)
    public Message deleteDriverInfoByDriverId(@RequestParam(value = "driverId") int driverId){
        UserDTO userInfo = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getDetails();
        int delStatus = 0;
        int delStatus1 = 0;
        CarInfo carInfo;
        try {
            String authorityContent = adminGroupAuthorityMapper.getAdminGroupAuthorityById(userInfo.getAuthorityId()).getAdminGroupName();
            if (authorityContent.equals("高级管理员") || authorityContent.equals("超级管理员")) {
            carInfo = carInfoService.getCarInfoByDriverId(driverId);//获取要删除司机的车辆信息
            if(carInfo.getCarDriverIdA() == driverId)    //获取车辆中哪个carDriverId绑定了司机Id
            {
                carInfo.setCarDriverIdA(0);    //设置所绑定的carDriverId为空
                delStatus1 = carInfoService.updateDeleteCarCarInfoDriverIdByCarId(carInfo);//更新所绑定的车辆的信息
                delStatus = driverInfoService.deleteDriverInfoByDriverId(driverId);//删除司机信息
            }
            else if(carInfo.getCarDriverIdB() == driverId)
            {
                carInfo.setCarDriverIdB(0);
                delStatus1 = carInfoService.updateDeleteCarCarInfoDriverIdByCarId(carInfo);
                delStatus = driverInfoService.deleteDriverInfoByDriverId(driverId);
            }

            if (delStatus == 1 && delStatus1==1){
                return new Message(Message.SUCCESS, "删除司机信息 >> 成功", delStatus);
            }
            return new Message(Message.FAILURE, "删除司机信息 >> 失败", delStatus);
            }
            return new Message(Message.NOT_LEGAL, "权限不合法",  "您是"+authorityContent+",权限不足，无法进行删除处理！");
        } catch (Exception e) {
            return new Message(Message.ERROR, "删除司机信息 >> 异常",  e.getMessage());
        }
    }

    /**
     * 导入Excel表中司机的信息
     */
    @RequestMapping(value="/importExcel", method=RequestMethod.POST)
    @ResponseBody
    public Message importExcel(@RequestParam(value = "file", required = false) MultipartFile sourceRiskFile,
                               HttpServletRequest request, Model model) throws Exception {
        UserDTO userInfo = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getDetails();
        String authorityContent = adminGroupAuthorityMapper.getAdminGroupAuthorityById(userInfo.getAuthorityId()).getAdminGroupName();
        if ( authorityContent.equals("超级管理员")){
        String fileName = sourceRiskFile.getOriginalFilename();
        logger.info(fileName);
        //判断文件是否为空
        if (sourceRiskFile != null) {
            //判断该文件是否为Excel文件
            if (fileName.matches("^.+\\.(?i)((xls)|(xlsx))$")) {
                try {
                    int res = driverInfoService.importExcel(sourceRiskFile, sourceRiskFile.getOriginalFilename());
                    return  new Message(Message.SUCCESS,"导入信息 >> 成功","成功添加" + res + "条记录");
                } catch (Exception e) {
                    e.printStackTrace();
                    return  new Message(Message.FAILURE,"导入信息 >> 失败","添加失败，请检查文件是否符合要求");
                }
            } else {
                return  new Message(Message.FAILURE,"导入信息 >> 失败","添加失败，请检查文件是否为Excel文件");
            }
        }

        return null;
    }
        return new Message(Message.NOT_LEGAL, "权限不合法",  "您是"+authorityContent+",权限不足，无法进行导入处理！");
    }
    /**
     * 导出司机信息到Excel表
     */
    @RequestMapping(value = "/makeExcel",method = RequestMethod.POST)
    @ResponseBody
    public Message makeExcel(){
        try{
            excel.pushExcel(driverInfoService.getPutDriverInfo());
            return new Message(1,"导出成功,文件位置存放在D盘根目录下",null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(0,"导出失败",null);
        }

    }

    /**
     * 获取所有当前上岗司机位置
     * @return
     */
    @RequestMapping(value = "/getAllCarLocation", method = RequestMethod.GET)
    public Message getAllCarLocation(){
        List<DriverInfo> driverInfoList;

        List<CarInfoDTO> carInfoDTOList = new ArrayList<>();

        try {
            driverInfoList = driverInfoService.getAllCarLocation();
            if (driverInfoList != null){
                for (DriverInfo driverInfo : driverInfoList) {
                    CarInfo carInfo = carInfoService.getCarInfoByCarId(driverInfo.getCarId());

                    CarInfoDTO carInfoDTO = new CarInfoDTO(carInfo, driverInfo);
                    carInfoDTOList.add(carInfoDTO);
                }
                return new Message(Message.SUCCESS, "获取所有车辆位置 >> 成功", carInfoDTOList);
            }
            return new Message(Message.FAILURE, "获取所有车辆位置 >> 失败", "未查询到数据");
        } catch (Exception e) {
            return new Message(Message.ERROR, "获取所有车辆位置 >> 异常", e.getMessage());
        }
    }
    /**
     * 换车操作后，更新司机信息和车辆信息
     */
    @RequestMapping(value = "/changCar",method = RequestMethod.POST)
    public Message changCar(@RequestBody DriverInfo driverInfo) {
        UserDTO userInfo = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getDetails();
        int upStatus = 0;
        int upStatus1 = 0;
        int upStatus2 = 0;
        CarInfo carInfo; //司机换车之后所绑定的车辆
        CarInfo carInfo1;//司机原本所绑定的车辆
        try {
            String authorityContent = adminGroupAuthorityMapper.getAdminGroupAuthorityById(userInfo.getAuthorityId()).getAdminGroupName();
            if (authorityContent.equals("中级管理员") || authorityContent.equals("高级管理员") || authorityContent.equals("超级管理员")) {
            carInfo1 = carInfoService.getCarInfoByDriverId(driverInfo.getDriverId());//获取要换车司机的原本的车辆信息
            carInfo = carInfoService.getCarInfoByCarId(driverInfo.getCarId());//获取换车的车辆信息
            if(carInfo!=null && carInfo1 !=null)  //设置新车的CarDriverId为司机的Id
            {
                if(carInfo.getCarDriverIdA() == 0)
                {
                    carInfo.setCarDriverIdA(driverInfo.getDriverId());
                }
                else if(carInfo.getCarDriverIdB() == 0)
                {
                    carInfo.setCarDriverIdB(driverInfo.getDriverId());
                }
                if(carInfo1.getCarDriverIdA() == driverInfo.getDriverId())    //获取旧车中哪个carDriverId绑定了司机Id
                {
                    carInfo1.setCarDriverIdA(0);    //设置所绑定的carDriverId为空
                }
                else if(carInfo1.getCarDriverIdB() == driverInfo.getDriverId())
                {
                    carInfo1.setCarDriverIdB(0);
                }
                upStatus  = carInfoService.updateCarInfoDriverIdByCarId(carInfo1);//更新旧车所绑定的司机的Id
                upStatus1 = carInfoService.updateCarInfoByCarId(carInfo); //更新新车所绑定的司机的Id
                upStatus2 = driverInfoService.updateDriverInfoByDriverId(driverInfo);   //更新司机所绑定车辆的Id
                if (upStatus == 1 && upStatus1==1 && upStatus2 == 1)
                {
                    logInfoService.addLogInfo(new LogInfo("后台端 >>Id为"+driverInfo.getDriverId()+"的司机换成车牌号为"+carInfo.getCarNumber()+"的车", 1, new Date(), 0));
                    return new Message(Message.SUCCESS, "更新信息 >> 成功", upStatus);
                }
                else {
                    return new Message(Message.FAILURE, "更新信息 >> 失败", "更新失败");
                }
                }
            else if(carInfo!=null && carInfo1 ==null)   //原来没有绑定车辆
            {
                if(carInfo.getCarDriverIdA() == 0)
                {
                    carInfo.setCarDriverIdA(driverInfo.getDriverId());
                }
                else if(carInfo.getCarDriverIdB() == 0)
                {
                    carInfo.setCarDriverIdB(driverInfo.getDriverId());
                }
                upStatus1 = carInfoService.updateCarInfoByCarId(carInfo); //更新新车所绑定的司机的Id
                upStatus2 = driverInfoService.updateDriverInfoByDriverId(driverInfo);   //更新司机所绑定车辆的Id
                if (upStatus2 == 1 && upStatus1==1)
                {
                    logInfoService.addLogInfo(new LogInfo("后台端 >>Id为"+driverInfo.getDriverId()+"的司机换成车牌号为"+carInfo.getCarNumber()+"的车", 1, new Date(), 0));
                    return new Message(Message.SUCCESS, "更新信息 >> 成功", upStatus);
                }
                else {
                    return new Message(Message.FAILURE, "更新信息 >> 失败", "更新失败");
                }
                }
            else {
                return new Message(Message.FAILURE, "更新信息 >> 失败", "所选车辆信息为为空");
            }
            }
            return new Message(Message.NOT_LEGAL, "权限不合法",  "您是"+authorityContent+",权限不足，无法进行换车操作处理！");
        } catch (Exception e) {
            return new Message(Message.ERROR, "更新信息 >> 异常", e.getMessage());
        }
    }
    /**
     * 解绑操作后，更新司机信息和车辆信息
     */
    @RequestMapping(value = "/getCarOff",method = RequestMethod.POST)
    public Message getCarOff(@RequestBody DriverInfo driverInfo) {
        UserDTO userInfo = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getDetails();
        int upStatus = 0;
        int upStatus1 = 0;
        int upStatus2 = 0;
        CarInfo carInfo1;//司机原本所绑定的车辆
        try {
            String authorityContent = adminGroupAuthorityMapper.getAdminGroupAuthorityById(userInfo.getAuthorityId()).getAdminGroupName();
            if (authorityContent.equals("中级管理员") || authorityContent.equals("高级管理员") || authorityContent.equals("超级管理员")) {
            carInfo1 = carInfoService.getCarInfoByDriverId(driverInfo.getDriverId());//获取要解绑司机的原本的车辆信息
            if(carInfo1 !=null)
            {
                if(carInfo1.getCarDriverIdA() == driverInfo.getDriverId())    //获取旧车中哪个carDriverId绑定了司机Id
                {
                    carInfo1.setCarDriverIdA(0);    //设置所绑定的carDriverId为空
                }
                else if(carInfo1.getCarDriverIdB() == driverInfo.getDriverId())
                {
                    carInfo1.setCarDriverIdB(0);
                }
                driverInfo.setCarId(0);  //司机绑定的车辆Id设置为空
                upStatus  = carInfoService.updateCarInfoDriverIdByCarId(carInfo1);//更新旧车所绑定的司机的Id
                upStatus2 = driverInfoService.updateCarId(driverInfo);   //更新司机所绑定车辆的Id
                if (upStatus == 1  && upStatus2 == 1)
                {
                    // 插入用户成为日志
                    logInfoService.addLogInfo(new LogInfo("后台端 >>Id为"+driverInfo.getDriverId()+"的司机成功解绑车牌号为"+carInfo1.getCarNumber()+"的车", 1, new Date(), 0));
                    return new Message(Message.SUCCESS, "更新信息 >> 成功", upStatus);
                }
                else {
                    return new Message(Message.FAILURE, "更新信息 >> 失败", "更新失败");
                }
                }
            else {
                return new Message(Message.FAILURE, "更新信息 >> 失败", "所选车辆信息为为空");
            }
            }
            return new Message(Message.NOT_LEGAL, "权限不合法",  "您是"+authorityContent+",权限不足，无法进行解绑处理！");
        } catch (Exception e) {
            return new Message(Message.ERROR, "更新信息 >> 异常", e.getMessage());
        }
    }
    /**
     * 根据司机id查找司机信息
     */
    @RequestMapping(value = "/getDriverInfoByDriverId/{driverId}", method = RequestMethod.GET)
    public Message getDriverInfoByDriverId(@PathVariable(value = "driverId") int driverId){
        DriverInfo driverInfo;
        try {
            driverInfo = driverInfoService.getDriverInfoByDriverId(driverId);
            if (driverInfo != null){
                return new Message(Message.SUCCESS, "查询司机信息 >> 成功", driverInfo);
            }
            return new Message(Message.FAILURE, "查询司机信息 >> 失败", "未查询到司机Id为 [" + driverId + "] 的信息");
        } catch (Exception e) {
            return new Message(Message.ERROR, "查询司机信息 >> 异常", e.getMessage());
        }

    }

}
