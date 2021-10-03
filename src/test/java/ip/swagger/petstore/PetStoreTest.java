package ip.swagger.petstore;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.JsonPath;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;
import dto.Category;
import dto.Pet;
import dto.Tag;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import util.PropertyReaders;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class PetStoreTest extends BasetestClass {
    private long id;
    private Pet pet;
    private Tag tag;

    static ExtentTest logger;
    static ExtentReports report;

    @BeforeClass
    public void initTest(){
        report=new ExtentReports(System.getProperty("user.dir")+"/target/ExtentReport.html",true);
    }


    /*Create the pet using the DTO and validate the data after post request using DTO and get request wrt Pet_id*/
    @Test(priority=1)
    public void createPetData() throws JsonProcessingException {
        logger=report.startTest("Creation of Pet data using Post request");
        PropertyReaders prop=new PropertyReaders();
        try{
        id=Long.parseLong(prop.readProperty("id"));}catch (Exception e){
            e.fillInStackTrace();
        }
        Category category = new Category();
        category.setId(1L);
        category.setName("Dogs");

        tag = new Tag();
        tag.setId(1L);
        tag.setName("DogTag1");

        pet = new Pet();
        pet.setId(id);
        pet.setName("Puppy");
        pet.setStatus("available");
        pet.setCategory(category);
        pet.setTags(Arrays.asList(tag));
        pet.setPhotoUrls(Arrays.asList("https://images.dog.ceo//breeds//labrador//n02099712_5008.jpg"));
        logger.log(LogStatus.PASS,"Pet data DTO created successfully");
        
        //ObjectMapper objMap= new ObjectMapper();
        //String mydata=objMap.writerWithDefaultPrettyPrinter().writeValueAsString(pet);
        RestAssured.given().auth().basic("test","abc123").when()
                .contentType(ContentType.JSON)
                .with()
                .body(pet)
                .post("/pet").then().assertThat().statusCode(200);
        logger.log(LogStatus.PASS,"Post request status code is 200");

        RestAssured.given().get("/pet/"+pet.getId()).then().spec(SpecificationFactory.getGenericResponseSpec())
                .assertThat().statusCode(200)
                .log().body().body("id",equalTo(pet.getId().intValue()))
                .body("name",equalToCompressingWhiteSpace(pet.getName()))
                .body("status",equalToCompressingWhiteSpace(pet.getStatus()))
                .body("category.id",equalTo(pet.getCategory().getId().intValue()))
                .body("category.name",equalToCompressingWhiteSpace(pet.getCategory().getName()))
                .body("tags[0].id",equalTo(pet.getTags().get(0).getId().intValue()))
                .body("tags[0].name",equalToCompressingWhiteSpace(pet.getTags().get(0).getName()));
        logger.log(LogStatus.PASS,"Pet data DTO validated successfully");
    }

    /* Update the pet using the Put request and validate the updated data */
     @Test(priority = 2)
    public void updatePetDataUsingPut() throws JsonProcessingException {
         logger=report.startTest("Updation of Pet data using Put request");
        pet.setName("Dummy");
        //ObjectMapper objMap= new ObjectMapper();
        //String mydata=objMap.writerWithDefaultPrettyPrinter().writeValueAsString(pet);
        RestAssured.given().auth().basic("test","abc123").when().contentType(ContentType.JSON)
                .with().body(pet).put("/pet").then().assertThat().statusCode(200);
         logger.log(LogStatus.PASS,"Put request status code is 200");

        RestAssured.given().when().get("/pet/"+pet.getId()).then().spec(SpecificationFactory.getGenericResponseSpec())
                .assertThat().statusCode(200)
                .log().body().body("name",equalToCompressingWhiteSpace(pet.getName()));
         logger.log(LogStatus.PASS,"Updated data validated successfully");
    }

    /* Validate the pet data by status and validate that all the Status is same */
    @Test(priority = 3)
    public void findPetDataByStatus(){
        logger=report.startTest("Find Pet data by passing single status value");
        Response response=RestAssured.given().when().queryParam("status","available")
                .get("/pet/findByStatus");
        logger.log(LogStatus.PASS,"Get request made successfully");
        String Jstr=response.getBody().asString();
        List<String> statusList= JsonPath.read(Jstr,"$.[*].status");
        for(String check:statusList){
            Assert.assertEquals(check,"available","Status list contain other value than available");
            }
        logger.log(LogStatus.PASS,"Homogeneity of status data is verified");
    }

    /* Validate the pet data by passing multiple status and validate that all the Status are those */
    @Test(priority = 4)
    public void findPetDataByMultipleStatus(){
        logger=report.startTest("Find Pet data by passing Multiple status value");
        Response response=RestAssured.given().when().queryParam("status","available")
                .queryParam("status","pending")
                .get("/pet/findByStatus");
        logger.log(LogStatus.PASS,"Get request made successfully");
        String Jstr=response.getBody().asString();
        List<String> statusList= JsonPath.read(Jstr,"$.[*].status");
        for(String check:statusList){
            if(check.equals("available")||check.equals("pending"))
            { }
            else{
                Assert.assertTrue(false,"The status list contains other value than available and pending");
            }
        }
        logger.log(LogStatus.PASS,"Homogeneity of status data is verified");
    }

    /* Update the pet data using the Form data and validate the same after updation*/
    @Test(priority = 5)
    public void petUpdateUsingPostFormData()
    {   logger=report.startTest("Pet data update using Form");
        RestAssured.given().auth().basic("test","abc123").when().contentType(ContentType.JSON)
                .queryParam("name","Tom").queryParam("status","pending")
                .post("/pet/"+id).then().log().body().assertThat().statusCode(200);
        logger.log(LogStatus.PASS,"Post Form request status code is 200");

        RestAssured.given().when().get("/pet/"+id).then().spec(SpecificationFactory.getGenericResponseSpec())
                .assertThat().statusCode(200)
                .log().body().body("name",equalToCompressingWhiteSpace("Tom"))
                .body("status",equalToCompressingWhiteSpace("pending"));
        logger.log(LogStatus.PASS,"Updated data validated successfully");
    }

    /* get Pet data using the Tag Name and validate the id*/
    @Test(priority = 6)
    public void getPetUsingTagName()
    {   logger=report.startTest("Get Pet data using TagName");
        Response response=RestAssured.given().when().queryParam("tags",tag.getName())
                .get("/pet/findByTags");
        logger.log(LogStatus.PASS,"Get request made successfully");
        String Jstr=response.getBody().asString();
        List<String> idList= JsonPath.read(Jstr,"$.[*].id");
        Assert.assertTrue(idList.contains(pet.getId().intValue()));
        logger.log(LogStatus.PASS,"Id of the created Pet data validated successfully using TagName");
    }

    /* upload the image for the PetId ,validating for unsupported format as the supported format is not specified*/
    @Test(priority = 7)
    public void uploadImageForthePetId()
    {   logger=report.startTest("Upload image using the PetId");
        RestAssured.given().header("Content-Type","text/csv").auth().basic("test","abc123")
                .body(new File("src/test/resources/testFiles/DogPicToUpload.jpg")).when()
                .post("/pet/"+pet.getId().intValue()+"/uploadImage").then().statusCode(415);
        logger.log(LogStatus.PASS,"Post request passes Status code 415 for unidentified file format");
    }

    /* Delete the above created pet data */
    @Test(priority = 8)
    public void deletePet(){
        logger=report.startTest("Delete the Pet data created above");
         RestAssured.given().auth().basic("test","abc123")
                 .delete("/pet/"+pet.getId().intValue()).then().assertThat().statusCode(200);

         RestAssured.given().get("/pet/"+pet.getId().intValue()).then().spec(SpecificationFactory.getGenericResponseSpec())
                 .assertThat().statusCode(404);
        logger.log(LogStatus.PASS,"After deletion validated that the pet data is no more available");
    }

    /* Validate the schema of the pet data wrt the categoryId and categoryName */
    @Test(priority = 9)
    public void schemaValidationForPet(){
        logger=report.startTest("Validate the schema of the Pet data wrt CategoryId and CategoryName");
           String status=null;
        for(int k=0;k<3;k++) {
            if(k==0){
                status="available";
            }else if(k==1){
                status="pending";
            }else{
                status="sold";
            }
            Response response = RestAssured.given().when().queryParam("status", status)
                    .get("/pet/findByStatus");
            String Jstr = response.getBody().asString();
            List<Integer> categoryId = JsonPath.read(Jstr, "$.[*].category.id");
            List<String> categoryName = JsonPath.read(Jstr, "$.[*].category.name");
            for (int i = 0; i < categoryId.size(); i++) {
                if (categoryId.get(i).equals(1)) {
                    categoryName.get(i).equals("Dogs");
                } else if (categoryId.get(i).equals(2)) {
                    categoryName.get(i).equals("Cats");
                } else if (categoryId.get(i).equals(3)) {
                    categoryName.get(i).equals("Rabbits");
                } else if (categoryId.get(i).equals(4)) {
                    categoryName.get(i).equals("Lions");
                } else {
                    System.out.println("Wrong category name or id");
                    Assert.assertTrue(false);
                }
            }
            categoryId.clear();
            categoryName.clear();
        }
        logger.log(LogStatus.PASS,"The schema of the Pet data wrt CategoryId and CategoryName validated successfully");
    }

    @AfterMethod
    public void getResult(ITestResult result){
        if(result.getStatus()==ITestResult.FAILURE){
            logger.log(LogStatus.FAIL, "Test case failed:"+result.getName());
            logger.log(LogStatus.FAIL, "Test case failed:"+result.getThrowable());
        }else if(result.getStatus()==ITestResult.SKIP){
            logger.log(LogStatus.SKIP, "Test case failed:"+result.getName());
            logger.log(LogStatus.SKIP, "Test case failed:"+result.getThrowable());
        }
        report.endTest(logger);
    }

    @AfterClass
    public void disposeTest(){
        report.flush();
        report.close();
    }


}
