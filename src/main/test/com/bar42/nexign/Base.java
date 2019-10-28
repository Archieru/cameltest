package com.bar42.nexign;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.BeforeClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.request;
import static io.restassured.RestAssured.with;
import static org.junit.Assert.assertTrue;

public class Base
{
    private String path = "/rs/users";
    
    @BeforeClass
    public static void setup()
    {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 28080;
    }
    
    public ValidatableResponse createUser(String firstName, String lastName)
    {
        return with()
            .header("firstName", firstName)
            .header("lastName", lastName)
            .when()
            .request("POST", path)
            .then();
    }
    
    public ValidatableResponse listUsers()
    {
        return request("GET", path).then();
    }
    
    public ValidatableResponse getUser(int id)
    {
        return request("GET", path+"/"+id).then();
    }
    
    public ValidatableResponse updateUser(int id, String newFirstName, String newLastName)
    {
        return with()
            .header("firstName", newFirstName)
            .header("lastName", newLastName)
            .when()
            .request("PUT", path+"/"+id)
            .then();
    }
    
    public ValidatableResponse deleteUser(int id)
    {
        return request("DELETE", path+"/"+id).then();
    }
    
    public String getUserList()
    {
        return listUsers().extract().asString();
    }
    
    public int getId(ValidatableResponse response)
    {
        Matcher search = Pattern.compile("ID=([^,]*),")
            .matcher(response.extract().asString());
        search.find();
        return Integer.parseInt(search.group(1));
    }
    
    public String trimBrackets(String original)
    {
        return original.substring(1, original.length()-1);
    }
    
    protected void assertContains(String haystack, String needle)
    {
        assertTrue("\nExpected   "+haystack+"\nto contain "+ needle, haystack.contains(needle));
    }
}
