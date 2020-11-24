package com.blz.employeepayrollrestapi;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.blz.employeepayrollrestapi.EmployeePayRollService.IOService;
import com.google.gson.Gson;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class EmloyeePayRollRESTAPITEST {

	
	@Before
	public void setup() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 3000;
		
	}
	
	public EmployeePayRollData[] getEmployeeList(){
		Response response  = RestAssured.get("/employee_payroll");
		System.out.println("EMPLOYEE PAYROLL ENTERIES IN JSONServer" + response.asString());
		EmployeePayRollData[] arrayOfEmps = new Gson().fromJson(response.asString(), EmployeePayRollData[].class);
		return arrayOfEmps;
	}
	
	public Response addEmployeeToJsonServer(EmployeePayRollData employeePayRollData) {
		String empJson = new Gson().toJson(employeePayRollData);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type","application/json");
		request.body(empJson);
		return request.post("/employee_payroll");
	}
	@Test
	public void givenEmployeeDataInJSONServer_WhenRetrived_SouldMatchCount() {
		EmployeePayRollData[] arrayOfEmps = getEmployeeList();
		EmployeePayRollService employeePayRollService = new EmployeePayRollService(Arrays.asList(arrayOfEmps));
		long enteries = employeePayRollService.countEnteries(IOService.REST_IO);
		assertEquals(2,enteries);
	}
	
	@Test
	public void givenEmployee_WhenAdded_ShouldMatch201ResponsesAndCount() throws SQLException {
		EmployeePayRollData[] arrayOfEmps = getEmployeeList();
		EmployeePayRollService employeePayRollService = new EmployeePayRollService(Arrays.asList(arrayOfEmps));
		EmployeePayRollData employeePayRollData = new EmployeePayRollData(0, "Mark Zuckerberg", 300000.0,LocalDate.now());
		Response response = addEmployeeToJsonServer(employeePayRollData);
		int statusCode = response.getStatusCode();
		assertEquals(201, statusCode);
		
		employeePayRollData = new Gson().fromJson(response.asString(), EmployeePayRollData.class);
		employeePayRollService.addEmployeePayRollData(employeePayRollData,IOService.REST_IO);
		long enteries = employeePayRollService.countEnteries(IOService.REST_IO);
		assertEquals(3,enteries);
	}
	

}
