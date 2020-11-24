package com.blz.employeepayrollrestapi;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;

public class EmployeePayRollData {
	public int id;
	public String name;
	public double salary;
	public LocalDate startDate;
	public String companyName;
	public int companyId;
	public String department;
	public String gender;

	public EmployeePayRollData(int id, String name, double salary) {
		super();
		this.id = id;
		this.name = name;
		this.salary = salary;
	}
	public EmployeePayRollData(int id, String name, double salary, LocalDate startDate) {
		super();
		this.id = id;
		this.name = name;
		this.salary = salary;
		this.startDate = startDate;
	}
	
	public EmployeePayRollData(int id, String name, double salary, LocalDate startDate, String companyName,
			int companyId, String department) {
		super();
		this.id = id;
		this.name = name;
		this.salary = salary;
		this.startDate = startDate;
		this.companyName = companyName;
		this.companyId = companyId;
		this.department = department;
	}
	public EmployeePayRollData(int id, String name, String gender, double salary, LocalDate startDate) {
		this(id,name,salary,startDate);
		this.gender = gender;
	}
	@Override
	public int hashCode() {
		return Objects.hash(name,gender,salary,startDate);
	}
	

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		EmployeePayRollData that = (EmployeePayRollData) obj;
		return id == that.id && Double.compare(that.salary, salary) == 0 && name.equals(that.name);
	}
}

