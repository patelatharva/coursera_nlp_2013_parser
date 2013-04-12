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
public class PCFGLearner {

    private Map<String, Integer> NTCounts;
    private Map<String, Map<String, RuleStats>> binaryRuleCounts;
    private Map<String, Map<String, RuleStats>> unaryRuleCounts;
    public PCFGLearner(){
        NTCounts = new HashMap<String ,Integer>();
        binaryRuleCounts = new HashMap<String, Map<String, RuleStats>>();
        unaryRuleCounts  = new HashMap<String, Map<String, RuleStats>>();
    }
    public void learnPCFG(String trainingFileName) {
        System.out.println("Started learning PCFG.");
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(trainingFileName);
            br = new BufferedReader(fr);

            String line = br.readLine();

            while (line != null) {
                if (!line.trim().equals("")) {
                    learnFromParseTreeString(line.trim());
                }

                line = br.readLine();
            }
            System.out.println("Completed learning PCFG.");
            System.out.println("NTCounts:\n"+NTCounts);
            System.out.println("BinaryRuleCounts:\n" + binaryRuleCounts);
            System.out.println("UnaryRuleCounts:\n" + unaryRuleCounts);
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PCFGLearner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PCFGLearner.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(PCFGLearner.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    private void learnFromParseTreeString(String parseTreeString) {
        JsonParser parser = new JsonParser();
        JsonElement parseTreeJSON = parser.parse(parseTreeString);
        learnFromParseTreeJson(parseTreeJSON);
    }

    private void learnFromParseTreeJson(JsonElement parseTreeJSON) {
        if (parseTreeJSON.isJsonArray()) {
            JsonArray rule = parseTreeJSON.getAsJsonArray();
            if (rule.size() == 3) {
                //Binary rule
                updateBinaryRuleStats(rule);
                learnFromParseTreeJson(rule.get(1).getAsJsonArray());
                learnFromParseTreeJson(rule.get(2).getAsJsonArray());
            } else if (rule.size() == 2) {
                //unary rule
                updateUnaryRuleQ(rule);
            } else {
                System.out.println("Malformated rule:\n" + rule);
            }
        } else {
            System.out.println("Parse tree was malformatted.\nHere is the tree.\n"
                    + parseTreeJSON);
        }
    }

    private void updateBinaryRuleStats(JsonArray rule) {
        String root = rule.get(0).getAsString();
        String leftNT = null, rightNT = null;

        JsonElement leftNTRule = rule.get(1);
        if (leftNTRule.isJsonArray()) {
            leftNT = leftNTRule.getAsJsonArray().get(0).getAsString();
        } else {
            System.out.println("Rule doesn't seem to follow CNF.\n" + leftNTRule);
        }
        JsonElement rightNTRule = rule.get(2);
        if (rightNTRule.isJsonArray()) {
            rightNT = rightNTRule.getAsJsonArray().get(0).getAsString();
        } else {
            System.out.println("Rule doesn't seem to follow CNF.\n" + leftNTRule);
        }

        if (leftNT != null && rightNT != null) {

            NTCounts.put(root, NTCounts.containsKey(root) ? NTCounts.get(root) + 1 : 1);

            if (binaryRuleCounts.containsKey(root)) {
                Map<String, RuleStats> rulesForRoot = binaryRuleCounts.get(root);
                if (rulesForRoot.containsKey(leftNT + rightNT)) {
                    RuleStats binaryRule = rulesForRoot.get(leftNT + rightNT);
                    binaryRule.counts++;
                    binaryRule.updateQ(NTCounts.get(root));
                } else {
                    RuleStats binaryRule = new RuleStats();
                    binaryRule.updateQ(NTCounts.get(root));
                    rulesForRoot.put(leftNT + rightNT, binaryRule);
                }
            } else {
                Map<String, RuleStats> rulesForRoot = new HashMap<String, RuleStats>();
                RuleStats binaryRule = new RuleStats();
                binaryRule.updateQ(NTCounts.get(root));
                rulesForRoot.put(leftNT + rightNT, binaryRule);
                binaryRuleCounts.put(root, rulesForRoot);
            }
        }
    }

    private void updateUnaryRuleQ(JsonArray rule) {
        String root = rule.get(0).getAsString();

        String word = rule.get(1).getAsString();

        NTCounts.put(root, NTCounts.containsKey(root) ? NTCounts.get(root) + 1 : 1);

        if (unaryRuleCounts.containsKey(root)) {
            Map<String, RuleStats> rulesForRoot = unaryRuleCounts.get(root);
            if (rulesForRoot.containsKey(word)) {
                RuleStats ruleStats = rulesForRoot.get(word);
                ruleStats.counts++;
                ruleStats.updateQ(NTCounts.get(root));
            } else {
                RuleStats ruleStats = new RuleStats();
                ruleStats.updateQ(NTCounts.get(root));
                rulesForRoot.put(word, ruleStats);
            }
        } else {
            RuleStats ruleStats = new RuleStats();
            ruleStats.updateQ(NTCounts.get(root));
            Map<String,RuleStats> rulesForRoot = new HashMap<String, RuleStats>();
            rulesForRoot.put(word, ruleStats);
            unaryRuleCounts.put(root, rulesForRoot);

        }


    }

    private static class RuleStats {

        public int counts;
        public double q;

        public RuleStats() {
            counts = 1;
            q = 0.0;
        }

        private void updateQ(Integer rootCounts) {
            q = ((double) counts) / rootCounts;
        }
        
        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append("counts: ").append(counts).append(" q:").append(q);
            return sb.toString();
        }
    }
}
