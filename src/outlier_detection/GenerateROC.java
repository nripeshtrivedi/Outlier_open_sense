package outlier_detection;
import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import weka.core.*;
import weka.classifiers.*;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.*;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.HoeffdingTree;
import weka.gui.visualize.*;

/**
  * Generates and displays a ROC curve from a dataset. Uses a default 
  * NaiveBayes to generate the ROC data.
  *
  * @author FracPete
  */
public class GenerateROC {
  
  /**
   * takes one argument: dataset in ARFF format (expects class to 
   * be last attribute)
   */
  public static void area(HoeffdingTree h_tree, Instances newData ) throws Exception {
    // load data
	 /* BVDecompose bv = new BVDecompose();
	  bv.setDataFileName("my.arff");
	  		bv.setClassIndex(newData.classIndex());
	  		bv.setClassifier(h_tree);
	          bv.decompose(); */
    Instances data = new Instances(newData);
    data.setClassIndex(data.numAttributes() - 1);

    // train classifier
   
    Evaluation eval = new Evaluation(data);
    HoeffdingTree h_tree_1=new HoeffdingTree();
    eval.crossValidateModel(h_tree_1, data, 2, new Random(1));
    System.out.println(eval.toSummaryString());
	
	
	 double cfs[][]=eval.confusionMatrix();

	 for(int i=0;i<2;i++)
	 {
		 for(int j=0;j<2;j++)
			 System.out.println(cfs[i][j]);
	 
	 }
    // generate curve
    ThresholdCurve tc = new ThresholdCurve();
    int classIndex = 0;
    Instances result = tc.getCurve(eval.predictions(), classIndex);
   

    // plot curve
    ThresholdVisualizePanel vmc = new ThresholdVisualizePanel();
    vmc.setROCString("(Area under ROC = " + 
        Utils.doubleToString(tc.getROCArea(result), 4) + ")");
  
    vmc.setName(result.relationName());
  
 
  
  
    
   
    PlotData2D tempd = new PlotData2D(result);
   
    tempd.setPlotName(result.relationName());
    tempd.addInstanceNumberAttribute();
   
    tempd.setCustomColour(Color.white);
    // specify which points are connected
    boolean[] cp = new boolean[result.numInstances()];
    int[] shap = new int[result.numInstances()];
    int shape=Plot2D.PLUS_SHAPE;
    for (int n = 1; n < cp.length; n++)
    {
      cp[n] = true;
      shap[n]=shape;
    }
    tempd.setConnectPoints(cp);
    tempd.setShapeType(shap);
    // add plot
    vmc.addPlot(tempd);
    vmc.setXIndex(result.attribute(5).index()); 
    vmc.setYIndex(result.attribute(6).index());
    
   

    // display curve
   String plotName = vmc.getName(); 
    final javax.swing.JFrame jf = 
      new javax.swing.JFrame("Weka Classifier Visualize: "+plotName);
    jf.setSize(500,400);
   
     
    
    
 /*  JFrame jf =  new JFrame(); 
    jf.setSize(1500,1400); 
    jf.getContentPane().add(vmc); 
    JLabel blueLabel = new JLabel("Blue");
    
    blueLabel.setBounds(200, 200, 400, 400);*/
  //  jf.getContentPane().add(blueLabel);
    jf.getContentPane().setLayout(new BorderLayout());
    
    jf.getContentPane().add(vmc, BorderLayout.CENTER);
    
    
  //  jf.pack(); 

    //JComponentWriter jcw = new JPEGWriter(vmc.getPlotPanel(), new File("a.bmp")); 
    //jcw.toOutput(); 
   // System.exit(1); */
    jf.addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
      jf.dispose();
      }
    });
    jf.setVisible(true);
  }
}
 

