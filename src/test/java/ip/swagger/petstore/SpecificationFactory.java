package ip.swagger.petstore;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.hamcrest.Matchers;



public class SpecificationFactory extends BasetestClass{

    public static synchronized ResponseSpecification getGenericResponseSpec(){
        ResponseSpecBuilder responseSpec;
        ResponseSpecification responseSpecification;

        responseSpec = new ResponseSpecBuilder();
        responseSpec.expectHeader("Content-Type","application/json");
        responseSpec.expectResponseTime(Matchers.lessThan(5000L));
        responseSpecification=responseSpec.build();
        return responseSpecification;
    }

}
