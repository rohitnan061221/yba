package com.YourBank.service.impl;

import com.YourBank.dto.EmployeeDto;
import com.YourBank.entity.Employee;
import com.YourBank.exception.ResourceNotFoundException;
import com.YourBank.mapper.EmployeeMapper;
import com.YourBank.repository.EmployeeRepository;
import com.YourBank.service.EmployeeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private EmployeeRepository employeeRepository;
    @Override
    public EmployeeDto createEmployee(EmployeeDto employeeDto) {

        Employee employee= EmployeeMapper.mapToEmployee(employeeDto);
        Employee savedEmployee=employeeRepository.save(employee);
        return EmployeeMapper.mapToEmployeeDto(savedEmployee);
    }

    @Override
    public EmployeeDto getEmployeeById(Long employeeId) {
        Employee employee=employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee is not exists by the given Id:"+ employeeId));
        return EmployeeMapper.mapToEmployeeDto(employee);
    }
}
