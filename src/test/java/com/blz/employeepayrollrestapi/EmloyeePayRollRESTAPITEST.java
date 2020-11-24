package com.blz.employeepayrollrestapi;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.blz.employeepayrollrestapi.EmployeePayRollService.IOService;
import com.google.gson.Gson;

import io.restassured.RestAssured;
import io.restassured.response.Response;

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
	@Test
	public void givenEmployeeDataInJSONServer_WhenRetrived_SouldMatchCount() {
		EmployeePayRollData[] arrayOfEmps = getEmployeeList();
		EmployeePayRollService employeePayRollService = new EmployeePayRollService(Arrays.asList(arrayOfEmps));
		long enteries = employeePayRollService.countEnteries(IOService.REST_IO);
		assertEquals(2,enteries);
		
	}

}
