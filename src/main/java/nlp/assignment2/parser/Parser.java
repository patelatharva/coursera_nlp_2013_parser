/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nlp.assignment2.parser;

/**
 *
 * @author Atharva
 */
public class Parser {
     public static void main(String[] args) {
        

        String parsedTreeTrainingFileName = "/Users/atharva/Documents/Knowledge/Videos/Natural Language Processing/Assignment/Assignment 2/parser/docs/assignment/parse_train.dat";
        String trainingFileWithRareFileName = "E:\\Education\\Webapplications\\CodeandSDK\\practise\\coursera_nlp_2013\\docs\\assignment\\parse_train.RARE.dat";
        String sentencesFileName = "E:\\Education\\Webapplications\\CodeandSDK\\practise\\coursera_nlp_2013\\docs\\assignment\\parse_test.dat";
        String parseTreeFileName = "E:\\Education\\Webapplications\\CodeandSDK\\practise\\coursera_nlp_2013\\docs\\assignment\\parse_test.p2.out";
        RareWordsHelper rwHelper = new RareWordsHelper();
//        rwHelper.countRareWords(parsedTreeTrainingFileName);
//        rwHelper.replaceRareWords(parsedTreeTrainingFileName, trainingFileWithRareFileName);
        PCFGLearnerParser pcfgLearner  = new PCFGLearnerParser();
        pcfgLearner.learnPCFG(trainingFileWithRareFileName);
        pcfgLearner.parseSentences(sentencesFileName, parseTreeFileName);
    }
}
