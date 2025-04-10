package com.CC.controller.admin;

import com.CC.constant.JwtClaimsConstant;
import com.CC.dto.EmployeeDTO;
import com.CC.dto.EmployeeLoginDTO;
import com.CC.dto.EmployeePageQueryDTO;
import com.CC.entity.Employee;
import com.CC.properties.JwtProperties;
import com.CC.result.PageResult;
import com.CC.result.Result;
import com.CC.service.EmployeeService;
import com.CC.utils.JwtUtil;
import com.CC.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value ="员工登录")//接口的名称
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("员工退出登录")
    public Result<String> logout() {
        return Result.success();
    }

    @PostMapping
    @ApiOperation("新增员工")
    public Result save(@RequestBody EmployeeDTO employeeDTO){

        System.out.println("当前线程id：" + Thread.currentThread().getId());

        log.info("新增员工,{}",employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    /**
     *员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("员工分页查询")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO){
            log.info("员工分页查询，参数为："+ employeePageQueryDTO);

            PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);

            return Result.success(pageResult);
    }

    /**
     * 启用或禁用员工账号
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("/启用或禁用员工账号")
    public Result startOrStop(@PathVariable Integer status,long id){
        log.info("启用或禁用员工账号，id：{},status：{}",status,id);
        employeeService.startOrStop(status,id);

        return Result.success();
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("/根据id查询员工信息")
    public Result<Employee> getbById(@PathVariable long id){
        log.info("编辑员工信息");
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

    /**
     * 编辑员工信息
     * @param employee
     * @return
     */
    @PutMapping
    @ApiOperation("编辑员工信息")
    public Result update(@RequestBody Employee employee){
        log.info("编辑员工信息:{}",employee);
        employeeService.update(employee);


        return Result.success();
    }
}
