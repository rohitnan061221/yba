package com.YourBank.repository;

import com.YourBank.entity.Employee;
import com.YourBank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
