package com.CC.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.CC.constant.MessageConstant;
import com.CC.constant.PasswordConstant;
import com.CC.constant.StatusConstant;
import com.CC.dto.EmployeeDTO;
import com.CC.dto.EmployeeLoginDTO;
import com.CC.dto.EmployeePageQueryDTO;
import com.CC.entity.Employee;
import com.CC.exception.AccountLockedException;
import com.CC.exception.AccountNotFoundException;
import com.CC.exception.PasswordErrorException;
import com.CC.mapper.EmployeeMapper;
import com.CC.result.PageResult;
import com.CC.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //对明文密码进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * @param employeeDTO
     */

    public void save(EmployeeDTO employeeDTO) {
        System.out.println("当前线程id：" + Thread.currentThread().getId());


        Employee employee = new Employee();
        //这样太繁琐
        // employee.setName(employeeDTO.getName());
        //使用beanutils库直接复制
        BeanUtils.copyProperties(employeeDTO,employee);

        //设置账号状态，默认正常为1，0为锁定 此处使用常量类StatusConstant.ENABLE为1
        employee.setStatus(StatusConstant.ENABLE);

        //设置密码,默认密码123456的md5编码。123456使用密码常量PasswordConstant
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        //设置创建时间和修改时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//
//        //设置当前记录创建人的id和修改人的id
//        //利用每个线程的存储空间，存入id，在此处取出id
//        employee.setCreateUser(BaseContext.getCurrentId());
//        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);
    }


    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO){
        //开始分页查询

        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);

        long total = page.getTotal();
        List<Employee> records = page.getResult();



        return new PageResult(total,records);
    }

    /**
     * 启用或禁用员工账号
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, long id) {
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();
        //传统写法
//      Employee employee = new Employee();
//        employee.setStatus(status);
//        employee.setId(id);
        //update employee set status
        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    public Employee getById(long id) {
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("******");
        return employee;
    }

    /**
     * 编辑员工信息
     * @param employee
     */


    public void update(Employee employee) {
        BeanUtils.copyProperties(employee,employee);
//        employee.setUpdateUser(BaseContext.getCurrentId());
//        employee.setUpdateTime(LocalDateTime.now());
        employeeMapper.update(employee);
    }
}
