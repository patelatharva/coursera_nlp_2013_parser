/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nlp.assignment2.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author atharva
 */
public class RareWordsHelper {

    private static Map<String, Integer> wordCounts;

   

    public static void main(String[] args) {
        wordCounts = new HashMap<String, Integer>();
        FileReader trainFR = null;
        BufferedReader br = null;
        try {
            String parsedTreeTrainingFileName = "/Users/atharva/Documents/Knowledge/Videos/Natural Language Processing/Assignment/Assignment 2/parser/docs/assignment/parse_train.dat";
            String trainingFileWithRareFileName = "/Users/atharva/Documents/Knowledge/Videos/Natural Language Processing/Assignment/Assignment 2/parser/docs/assignment/parse_train.RARE.dat";
            trainFR = new FileReader(parsedTreeTrainingFileName);
            br = new BufferedReader(trainFR);
            String line = br.readLine();


            while (line != null) {
                if (!line.trim().equals("")) {
                }

                updateWordCounts(line.trim());

                line = br.readLine();
            }
            System.out.println(wordCounts.toString());

        } catch (FileNotFoundException ex) {
            Logger.getLogger(RareWordsHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RareWordsHelper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                trainFR.close();
            } catch (IOException ex) {
                Logger.getLogger(RareWordsHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(RareWordsHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


    }

    private static void updateWordCounts(String jsonString) {
        //parese the line for json
        JsonParser parser = new JsonParser();
        JsonElement treeJsonElem = parser.parse(jsonString.trim());
        updateWordCounts(treeJsonElem);
    }

    private static void updateWordCounts(JsonElement treeJsonElem) {
        if (treeJsonElem.isJsonArray()) {
            JsonArray rule = treeJsonElem.getAsJsonArray();
            if (rule.size() == 2) {
                //Rule with terminal symbol

                String word = rule.get(1).getAsString();
                //Increment the count for this word
                if (wordCounts.containsKey(word)) {
                    wordCounts.put(word, wordCounts.get(word) + 1);
                } else {
                    wordCounts.put(word, 1);
                }
            } else {
                //size == 3
                //Rule with non terminal symbol
                updateWordCounts(rule.get(1));
                updateWordCounts(rule.get(2));
            }
        } else {
            System.out.println("Tree is not in a json array format.");

        }
    }
}
