/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nlp.assignment2.parser;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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
    private static final Gson gson = new Gson();

   
    public RareWordsHelper(){
        wordCounts = new HashMap<String,Integer>();
    }
    public void countRareWords(String parsedTreeTrainingFileName) {

        FileReader trainFR = null;
        BufferedReader br = null;
        try {
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

    private  void updateWordCounts(String jsonString) {
        //parese the line for json
        JsonParser parser = new JsonParser();
        JsonElement treeJsonElem = parser.parse(jsonString.trim());
        updateWordCounts(treeJsonElem);
    }

    private void updateWordCounts(JsonElement treeJsonElem) {
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

    public  void replaceRareWords(String parsedTreeTrainingFileName, String trainingFileWithRareFileName) {
        FileReader fr = null;
        BufferedReader br = null;
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fr = new FileReader(parsedTreeTrainingFileName);
            br = new BufferedReader(fr);
            fw = new FileWriter(trainingFileWithRareFileName);
            bw = new BufferedWriter(fw);
            String line = br.readLine();
            while (line != null) {
                if (!line.trim().equals("")) {
//                    System.out.println("Original string:\n" + line.trim());
//                    System.out.println("Updated string:\n" + updateLineWithReplacementForRare(line.trim()));
                    bw.write(updateLineWithReplacementForRare(line.trim()));
                    bw.write("\n");
                }
                
                
                line = br.readLine();
            }
        } catch (FileNotFoundException ex) {

            Logger.getLogger(RareWordsHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RareWordsHelper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(RareWordsHelper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if (bw != null) {
                try {
                    
                    bw.close();
                } catch (IOException ex) {
                    Logger.getLogger(RareWordsHelper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    private static class Rule {

        public String nonTerminalRoot;
    }

    private static class BinaryRule extends Rule {

        public Rule nonTerminalLeft;
        public Rule nonTerminalRight;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("[").append("\"").append(nonTerminalRoot).append("\"").append(", ").append(nonTerminalLeft.toString()).append(", ").append(nonTerminalRight.toString()).append("]");
            return sb.toString();
        }
    }

    private static class UnaryRule extends Rule {

        public String terminal;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("[").append("\"").append(nonTerminalRoot).append("\"").append(", ").append("\"").append(terminal).append("\"").append("]");
            return sb.toString();
        }
    }

    private  String updateLineWithReplacementForRare(String parseTreeString) {
        JsonParser parser = new JsonParser();
        JsonElement parseTreeJson = parser.parse(parseTreeString.trim());
        //Generate parse tree in POJO
        if (parseTreeJson.isJsonArray()) {
            JsonArray rule = parseTreeJson.getAsJsonArray();
            Rule updatedRulesTree = getUpdatedRulesTree(rule);
            String updatedRulesTreeJsonString;



            return updatedRulesTree.toString();

        } else {
            System.out.println("Malformatted parse tree:\n" + parseTreeJson);
            return "{}";
        }
    }

    private Rule getUpdatedRulesTree(JsonArray ruleInJson) {

        if (ruleInJson.size() == 3) {
            //binary rule
            BinaryRule updatedRule = new BinaryRule();
            updatedRule.nonTerminalRoot = ruleInJson.get(0).getAsString();
            updatedRule.nonTerminalLeft = getUpdatedRulesTree(ruleInJson.get(1).getAsJsonArray());
            updatedRule.nonTerminalRight = getUpdatedRulesTree(ruleInJson.get(2).getAsJsonArray());
            return updatedRule;
        } else {
            //unary rule
            UnaryRule updatedRule = new UnaryRule();
            updatedRule.nonTerminalRoot = ruleInJson.get(0).getAsString();
            //Check the terminal word, replace it if it's rare
            String word = ruleInJson.get(1).getAsString();
            if (wordCounts.containsKey(word)) {
                if (wordCounts.get(word) < 5) {
                    //consider it as rare word, do the replacement
                    updatedRule.terminal = "_RARE_";
                } else {
                    updatedRule.terminal = word;
                }
            } else {
                //consider it as rare word
                updatedRule.terminal = "_RARE_";
            }
            return updatedRule;
        }




    }
}
