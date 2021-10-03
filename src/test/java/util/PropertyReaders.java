package util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReaders {

    public static Properties properties= new Properties();
    InputStream inputStream=null;
    public PropertyReaders()
    {
        loadProperties();
    }

    private void loadProperties(){
        try{
            inputStream= new FileInputStream("src/test/resources/properties/testData.properties");
            properties.load(inputStream);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public String readProperty(String key) throws IOException {
        String readPropValue = properties.getProperty(key);
        inputStream.close();
        return readPropValue;
    }

    public void writeProperty(String key, String value) throws IOException {
        FileOutputStream outputStream;
        outputStream= new FileOutputStream("src/test/resources/properties/testData.properties");
        properties.setProperty(key, value);
        properties.store(outputStream,null);
        outputStream.close();
    }


}
