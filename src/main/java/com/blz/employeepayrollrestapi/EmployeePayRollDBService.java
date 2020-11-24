package com.blz.employeepayrollrestapi;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blz.employeepayrollrestapi.EmployeePayRollException.ExceptionType;


public class EmployeePayRollDBService {
	
	private static EmployeePayRollDBService employeePayRollDBService;
	private PreparedStatement employeePayRollDataStatement;
	private int connectionCounter = 0;
	private EmployeePayRollDBService() {
		
	}
	
	public static EmployeePayRollDBService getInstance() {
		if(employeePayRollDBService == null)
			employeePayRollDBService = new EmployeePayRollDBService();
		return employeePayRollDBService;
	}
	
	private synchronized Connection getConnection() throws SQLException {
		connectionCounter++;
		String jdbcURL = "jdbc:mysql://localhost:3306/employee_payroll?useSSL=false";
 		String userName = "root";
		String password = "root";
		Connection connection;
		System.out.println("Processing Thread: " + Thread.currentThread().getName() + 
							"connecting to database with Id"+ connectionCounter);
		connection = DriverManager.getConnection(jdbcURL, userName, password);
		System.out.println("Processing Thread: " + Thread.currentThread().getName() + 
				"connecting to database with Id"+ connectionCounter+"Connection Successful"+connection);

		return connection;
	}
	public List<EmployeePayRollData> readData() {
		String sql = "SELECT* FROM employee_payroll_basic;";
		return this.getEmployeePayRollDataUsingDB(sql);
		
	}
	public List<EmployeePayRollData> getEmployeePayRollData(String name) {
		List<EmployeePayRollData> employeePayRollList = null;
		if(this.employeePayRollDataStatement == null)
			this.prepareStatementForEmployeeData();
		try {
			employeePayRollDataStatement.setString(1, name);
			ResultSet resultSet = employeePayRollDataStatement.executeQuery();
			employeePayRollList = this.getEmployeePayRollData(resultSet);
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return employeePayRollList;
	}
	private List<EmployeePayRollData> getEmployeePayRollData(ResultSet resultSet) {
		List<EmployeePayRollData> employeePayrollList = new ArrayList<EmployeePayRollData>();
		try{		
			while(resultSet.next()) {
				int id = resultSet.getInt("id");
				String name = resultSet.getString("name");
				double salary = resultSet.getDouble("salary");
				LocalDate startDate = resultSet.getDate("start").toLocalDate();
				employeePayrollList.add(new EmployeePayRollData(id, name, salary,startDate));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	private void prepareStatementForEmployeeData() {
		try {
			Connection connection = this.getConnection();
			String sql = "SELECt * FROM employee_payroll_basic WHERE name = ?";
			employeePayRollDataStatement = connection.prepareStatement(sql);
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	public int updateEmployeeData(String name,double salary) throws EmployeePayRollException {
		return this.updateEmployeeDataUsingStatement(name,salary);
	}
	
	public int updateEmployeeDataUsingStatement(String name,double salary) throws EmployeePayRollException {
		String sql = String.format("UPDATE employee_payroll_basic set salary=%.2d where name=%s", name,salary);
		try(Connection connection = this.getConnection();){		
			Statement statement = connection.createStatement();
			return (statement.executeUpdate(sql));
		} catch (SQLException e) {
			throw new EmployeePayRollException("Wrong SQL query given", ExceptionType.WRONG_SQL);
		}
	}
	
	public int updateSalaryUsingSQL(String name,Double salary) throws SQLException {
		String sql="UPDATE employee_payroll_basic SET salary=? WHERE name=?";
		try(Connection connection=getConnection()){
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setDouble(1, salary);
			preparedStatement.setString(2, name);
			return preparedStatement.executeUpdate();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public List<EmployeePayRollData> getEmployeePayRollForDateRange(LocalDate startDate, LocalDate endDate) {
		String sql = String.format("SELECT * FROM EMPLOYEE_PAYROLL_BASIC WHERE START BETWEEN '%s' AND '%s' ;",
									Date.valueOf(startDate),Date.valueOf(endDate));
		return this.getEmployeePayRollDataUsingDB(sql);
	}

	private List<EmployeePayRollData> getEmployeePayRollDataUsingDB(String sql) {
		List<EmployeePayRollData> employeePayrollList = new ArrayList<EmployeePayRollData>();
		try(Connection connection = this.getConnection();){		
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			employeePayrollList = this.getEmployeePayRollData(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	public Map<String, Double> getAverageSalaryByGender() throws SQLException {
		String sql = "SELECT GENDER,AVG(SALARY) AS AVG_SALARY FROM employee_payroll_basic GROUP BY GENDER;";
		Map<String,Double> genderToAverageSalaryMap = new HashMap<>();
		try(Connection connection = this.getConnection()){
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			while(resultSet.next()) {
				String gender = resultSet.getString("GENDER");
				double salary = resultSet.getDouble("AVG_SALARY");
				genderToAverageSalaryMap.put(gender, salary);
			}
		}
		return genderToAverageSalaryMap;
	}

	public EmployeePayRollData addEmployeeToPayRollUC7(String name, String gender, double salary, LocalDate date) throws SQLException {
		int employeeId = -1;
		EmployeePayRollData employeePayRollData = null;
		String sql = String.format("INSERT INTO employee_payroll_basic (name, gender, salary, start)" +
									"VALUES ( '%s', '%s', '%s', '%s')", name,gender,salary,Date.valueOf(date));
		try(Connection connection = this.getConnection()){
			Statement statement = connection.createStatement();
			int rowAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
			if(rowAffected == 1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if(resultSet.next()) employeeId = resultSet.getInt(1);
			}
			employeePayRollData = new EmployeePayRollData(employeeId, name, salary,date);
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return employeePayRollData;
	}

	public EmployeePayRollData addEmployeeToPayRoll(String name, String gender, double salary, LocalDate date) throws SQLException {
		int employeeId = -1;
		Connection connection  = null;
		EmployeePayRollData employeePayRollData = null;
		try {
			connection = this.getConnection();
			connection.setAutoCommit(false);
		}catch(SQLException e) {
			e.printStackTrace();
		}
		try(Statement statement = connection.createStatement();){
			String sql = String.format("INSERT INTO employee_payroll_basic (name, gender, salary, start)" +
					"VALUES ( '%s', '%s', '%s', '%s')", name,gender,salary,Date.valueOf(date));
			int rowAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
			if(rowAffected == 1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if(resultSet.next()) employeeId = resultSet.getInt(1);
			}
			employeePayRollData = new EmployeePayRollData(employeeId, name, salary,date);
		}catch(SQLException e) {
			e.printStackTrace();
			connection.rollback();
		}
		try(Statement statement = connection.createStatement();){
			double deductions = salary * 0.2 ;
			double taxablePay = salary - deductions;
			double tax = taxablePay * 0.1;
			double netPay = salary - tax;
			String sql = String.format("INSERT INTO payroll_details (employee_id, basic_pay, deductions, taxable_pay,tax,net_pay)" +
						"VALUES ( %s, %s, %s, %s, %s, %s)", employeeId,salary,deductions,taxablePay,tax,tax,netPay);
			int rowAffected = statement.executeUpdate(sql);
			if(rowAffected == 1) {
				employeePayRollData =  new EmployeePayRollData(employeeId,name,salary,date);					
			}
					
		}catch(SQLException e) {
			e.printStackTrace();
			connection.rollback();
		}
		try {
			connection.commit();
		}catch(SQLException e) {
			e.printStackTrace();
		}finally {
			if(connection != null) connection.close();
		}
		
		return employeePayRollData;		
		
	}

	public EmployeePayRollData addEmployeeToPayRollUC11(String name, String gender, double salary, LocalDate date,
			String companyName, int companyId, String department) {
		int employeeId = -1;
		Connection connection  = null;
		EmployeePayRollData employeePayRollData = null;
		try {
			connection = this.getConnection();
			connection.setAutoCommit(false);
		}catch(SQLException e) {
			e.printStackTrace();
		}
		try(Statement statement = connection.createStatement();){
			String sql = String.format("INSERT INTO employee_payroll_basic (name, gender, salary, start)" +
					"VALUES ( '%s', '%s', '%s', '%s')", name,gender,salary,Date.valueOf(date));
			int rowAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
			if(rowAffected == 1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if(resultSet.next()) employeeId = resultSet.getInt(1);
			}
			employeePayRollData = new EmployeePayRollData(employeeId, name, salary,date);
		}catch(SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		try(Statement statement = connection.createStatement();){
			double deductions = salary * 0.2 ;
			double taxablePay = salary - deductions;
			double tax = taxablePay * 0.1;
			double netPay = salary - tax;
			String sql = String.format("INSERT INTO payroll_details (employee_id, basic_pay, deductions, taxable_pay,tax,net_pay)" +
						"VALUES ( %s, %s, %s, %s, %s, %s)", employeeId,salary,deductions,taxablePay,tax,tax,netPay);
			int rowAffected = statement.executeUpdate(sql);
			if(rowAffected == 1) {
				employeePayRollData =  new EmployeePayRollData(employeeId,name,salary,date);					
			}
					
		}catch(SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		try(Statement statement = connection.createStatement();){
			String department_Name = department; 
			String sql = String.format("INSERT INTO department_details (department_name,employee_id )" +
						"VALUES ( %s, %s)", department_Name,employeeId);
			int rowAffected = statement.executeUpdate(sql);
			if(rowAffected == 1) {
				employeePayRollData =  new EmployeePayRollData(employeeId,name,salary,date);					
			}
					
		}catch(SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		try(Statement statement = connection.createStatement();){
			String company_Name = companyName;
			int company_id = companyId;
			String sql = String.format("INSERT INTO company_details (company_id,company_name,employee_id )" +
						"VALUES ( %s, %s, %s)",company_id,company_Name ,employeeId);
			int rowAffected = statement.executeUpdate(sql);
			if(rowAffected == 1) {
				employeePayRollData =  new EmployeePayRollData(employeeId,name,salary,date);					
			}
					
		}catch(SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		try {
			connection.commit();
		}catch(SQLException e) {
			e.printStackTrace();
		}finally {
			if(connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		
		return employeePayRollData;		
		
	}
	
}
