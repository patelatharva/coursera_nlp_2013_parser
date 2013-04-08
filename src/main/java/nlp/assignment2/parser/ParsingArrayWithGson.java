/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nlp.assignment2.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 *
 * @author atharva
 */
public class ParsingArrayWithGson {
    public static void main(String [] args){
        //Gson gson = new Gson();
        String sampleJsonString = "['mango',['apple','banana']]";
        JsonParser parser = new JsonParser();
        JsonElement sampleJson = parser.parse(sampleJsonString);
        ParsingArrayWithGson main = new ParsingArrayWithGson();
        main.printNestedJsonArray(sampleJson);
        
        
    }
    public void printNestedJsonArray(JsonElement jsonElem){
        if(jsonElem.isJsonArray()){
            System.out.println("Detected the sampleString as JSON array");
            for (JsonElement elem : jsonElem.getAsJsonArray()){
                if(elem.isJsonArray()){
                    printNestedJsonArray(elem);
                }else if(elem.isJsonPrimitive()){
                    System.out.println(elem.getAsString());
                }
            }
        }
    }
}
