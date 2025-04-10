package com.CC.service;

import com.CC.dto.EmployeeDTO;
import com.CC.dto.EmployeeLoginDTO;
import com.CC.dto.EmployeePageQueryDTO;
import com.CC.entity.Employee;
import com.CC.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     * @param employeeDTO
     */
    public void save(EmployeeDTO employeeDTO);

    /* 分页查询
     * @param emp
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 启用或禁用员工账号
     * @param status
     * @param id
     */
    void startOrStop(Integer status, long id);

    /**
     * 根据id查询员工信息
     * @param id
     */
    Employee getById(long id);

    /**
     * 编辑员工信息
     * @param employeeDTO
     */
    void update(Employee employee);
}
