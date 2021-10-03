package ip.swagger.petstore;

import io.restassured.RestAssured;
import org.testng.annotations.BeforeTest;
import util.PropertyReaders;

public class BasetestClass {

    public static PropertyReaders prop;

    @BeforeTest
    public static void init() {
        PropertyReaders prop=new PropertyReaders();
        try {
            RestAssured.baseURI = prop.readProperty("baseUrl");
            RestAssured.port = Integer.valueOf(prop.readProperty("port"));
            RestAssured.basePath = "/api/v3";
        }catch (Exception e){
            e.fillInStackTrace();
        }

    }



}
