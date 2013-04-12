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
        RareWordsHelper rwHelper = new RareWordsHelper();
//        rwHelper.countRareWords(parsedTreeTrainingFileName);
//        rwHelper.replaceRareWords(parsedTreeTrainingFileName, trainingFileWithRareFileName);
        PCFGLearner pcfgLearner  = new PCFGLearner();
        pcfgLearner.learnPCFG(trainingFileWithRareFileName);
        
    }
}
