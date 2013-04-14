/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nlp.assignment2.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author atharva
 */
public class PCFGLearnerParser {

    private Map<String, Integer> NTCounts;
    private Map<String, Map<String, BinaryRule>> binaryRuleCounts;
    private Map<String, Map<String, UnaryRule>> unaryRuleCounts;
    private List<String> vocabulary = new ArrayList<String>();

    public PCFGLearnerParser() {
        NTCounts = new HashMap<String, Integer>();
        binaryRuleCounts = new HashMap<String, Map<String, BinaryRule>>();
        unaryRuleCounts = new HashMap<String, Map<String, UnaryRule>>();
    }

    public void parseSentences(String sentencesFile, String parseTreeFileName) {
        System.out.println("Starting to generate parse trees for sentences.");
        FileReader fr = null;
        BufferedReader br = null;
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fr = new FileReader(sentencesFile);
            br = new BufferedReader(fr);
            fw = new FileWriter(parseTreeFileName);
            bw = new BufferedWriter(fw);
            String line = br.readLine();

            while (line != null) {

                String parseTreeString = generateParseTreeWithCKY(line.trim());
                bw.write(parseTreeString);
                bw.newLine();
                line = br.readLine();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(PCFGLearnerParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PCFGLearnerParser.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (br != null) {
                try {

                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(PCFGLearnerParser.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (bw != null) {
                try {

                    bw.close();
                } catch (IOException ex) {
                    Logger.getLogger(PCFGLearnerParser.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }


        System.out.println("Completed writing parse trees to target file.");
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
//            System.out.println("NTCounts:\n" + NTCounts);
//            System.out.println("BinaryRuleCounts:\n" + binaryRuleCounts);
//            System.out.println("UnaryRuleCounts:\n" + unaryRuleCounts);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(PCFGLearnerParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PCFGLearnerParser.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(PCFGLearnerParser.class.getName()).log(Level.SEVERE, null, ex);
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
                Map<String, BinaryRule> rulesForRoot = binaryRuleCounts.get(root);
                if (rulesForRoot.containsKey(leftNT + rightNT)) {
                    BinaryRule binaryRule = rulesForRoot.get(leftNT + rightNT);
                    binaryRule.counts++;
                    binaryRule.updateQ(NTCounts.get(root));

                } else {
                    BinaryRule binaryRule = new BinaryRule();
                    binaryRule.Y = leftNT;
                    binaryRule.Z = rightNT;
                    binaryRule.updateQ(NTCounts.get(root));
                    rulesForRoot.put(leftNT + rightNT, binaryRule);
                }
            } else {
                Map<String, BinaryRule> rulesForRoot = new HashMap<String, BinaryRule>();
                BinaryRule binaryRule = new BinaryRule();
                binaryRule.updateQ(NTCounts.get(root));
                binaryRule.Y = leftNT;
                binaryRule.Z = rightNT;
                rulesForRoot.put(leftNT + rightNT, binaryRule);
                binaryRuleCounts.put(root, rulesForRoot);
            }
        }
    }

    private void updateUnaryRuleQ(JsonArray rule) {
        String root = rule.get(0).getAsString();

        String word = rule.get(1).getAsString();
        vocabulary.add(word);
        NTCounts.put(root, NTCounts.containsKey(root) ? NTCounts.get(root) + 1 : 1);

        if (unaryRuleCounts.containsKey(root)) {
            Map<String, UnaryRule> rulesForRoot = unaryRuleCounts.get(root);
            if (rulesForRoot.containsKey(word)) {
                UnaryRule ruleStats = rulesForRoot.get(word);
                ruleStats.counts++;
                ruleStats.updateQ(NTCounts.get(root));
            } else {
                UnaryRule ruleStats = new UnaryRule();
                ruleStats.updateQ(NTCounts.get(root));
                ruleStats.word = word;
                rulesForRoot.put(word, ruleStats);
            }
        } else {
            UnaryRule ruleStats = new UnaryRule();
            ruleStats.updateQ(NTCounts.get(root));
            ruleStats.word = word;
            Map<String, UnaryRule> rulesForRoot = new HashMap<String, UnaryRule>();
            rulesForRoot.put(word, ruleStats);

            unaryRuleCounts.put(root, rulesForRoot);

        }


    }

    private String generateParseTreeWithCKY(String sentence) {
        String[] words = sentence.split(" ");
        Map<String, Double> pi = new HashMap<String, Double>();
        Map<String, BinaryRule> bpBinaryRules = new HashMap<String, BinaryRule>();
        Map<String, UnaryRule> bpUnaryRules = new HashMap<String, UnaryRule>();

        int n = words.length;
        
        for (int i = 0; i < n; i++) {
            for (String X : NTCounts.keySet()) {
                Map<String, UnaryRule> unaryRulesForX = unaryRuleCounts.get(X);
                if (unaryRulesForX != null) {
                    pi.put((i + 1) + "_" + (i + 1) + "_" + X, unaryRulesForX.containsKey(words[i]) ? unaryRulesForX.get(words[i]).q : vocabulary.contains(words[i]) ? 0 : unaryRulesForX.containsKey("_RARE_") ? unaryRulesForX.get("_RARE_").q : 0);

                    UnaryRule unaryRule = new UnaryRule();
                    unaryRule.X = X;
                    unaryRule.word = words[i];
                    bpUnaryRules.put((i + 1) + "_" + (i + 1) + "_" + X, unaryRule);
                } else {
                    pi.put((i + 1) + "_" + (i + 1) + "_" + X, 0.0);
                }
            }
        }

        for (int l = 1; l <= (n - 1); l++) {
            for (int i = 1; i <= (n - l); i++) {
                int j = i + l;

                for (String X : binaryRuleCounts.keySet()) {

                    BinaryRule binaryRuleMax = new BinaryRule();
                    double piMax = 0;
                    for (String yz : binaryRuleCounts.get(X).keySet()) {
                        for (int s = i; s <= j - 1; s++) {
                            BinaryRule binaryRule = binaryRuleCounts.get(X).get(yz);
                            double piValue = 0;
                            if (pi.containsKey(i + "_" + s + "_" + binaryRule.Y) && pi.containsKey((s + 1) + "_" + j + "_" + binaryRule.Z)) {
                                piValue = binaryRule.q;

                                piValue *= pi.get(i + "_" + s + "_" + binaryRule.Y);

                                piValue *= pi.get((s + 1) + "_" + j + "_" + binaryRule.Z);
                            }else{
                                piValue = 0.0;
                            }
                            if (piValue > piMax) {
                                piMax = piValue;
                                binaryRuleMax.Y = binaryRule.Y;
                                binaryRuleMax.Z = binaryRule.Z;
                                binaryRuleMax.s = s;
                            }
                        }
                    }
                    pi.put(i + "_" + j + "_" + X, piMax);
                    
                    bpBinaryRules.put(i + "_" + j + "_" + X, binaryRuleMax);

                }
            }
        }

        //Traverse backpointer to generate parse tree
        Rule parseTree;
        if(words[words.length-1].equals("?")){
            parseTree = generateParseTreeWithBP(1, n, "SBARQ", bpBinaryRules, bpUnaryRules);
        }else {
            parseTree = generateParseTreeWithBP(1, n, "S", bpBinaryRules, bpUnaryRules);
        }
        return parseTree.toString();
    }

    private Rule generateParseTreeWithBP(int i, int j, String X, Map<String, BinaryRule> bpBinaryRules, Map<String, UnaryRule> bpUnaryRules) {
        if (i != j) {
            //binary rule
            BinaryRule binaryRule = new BinaryRule();
            binaryRule.X = X;
            BinaryRule binaryRuleBP = bpBinaryRules.get(i + "_" + j + "_" + X);
            
//            binaryRule.Y = binaryRuleBP.Y;
//            binaryRule.Z = binaryRuleBP.Z;
            binaryRule.leftRule = generateParseTreeWithBP(i, binaryRuleBP.s, binaryRuleBP.Y, bpBinaryRules, bpUnaryRules);
            binaryRule.rightRule = generateParseTreeWithBP(binaryRuleBP.s + 1, j, binaryRuleBP.Z, bpBinaryRules, bpUnaryRules);
            return binaryRule;
        } else {
            //unary rule
            UnaryRule unaryRule = new UnaryRule();
            unaryRule.X = X;
            UnaryRule unaryRuleBP = bpUnaryRules.get(i + "_" + j + "_" + X);
            if(unaryRuleBP == null){
                System.out.println("BP Rule unary was null.");
            }
            unaryRule.word = unaryRuleBP.word;
            return unaryRule;
        }
    }

    private static class Rule {

        public int counts;
        public double q;

        public Rule() {
            counts = 1;
            q = 0.0;
        }

        public void updateQ(Integer rootCounts) {
            q = ((double) counts) / rootCounts;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("counts: ").append(counts).append(" q:").append(q);
            return sb.toString();
        }
    }

    private class BinaryRule extends Rule {

        public String Y;
        public String Z;
        public int s;
        public String X;
        public Rule leftRule;
        public Rule rightRule;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("[").append("\"").append(X).append("\"").append(", ").append(leftRule.toString()).append(", ").append(rightRule.toString()).append("]");
            return sb.toString();
        }
    }

    private class UnaryRule extends Rule {

        public String word;
        public String X;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("[").append("\"").append(X).append("\"").append(", ").append("\"").append(word).append("\"").append("]");
            return sb.toString();
        }
    }
}
