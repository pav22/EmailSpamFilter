package sparkml

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.mllib.classification.LogisticRegressionWithSGD
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.regression.LabeledPoint

object EmailFilter {
  
  def main(args: Array[String]): Unit = {

    
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)
    
    
    val conf = new SparkConf().setMaster("local").setAppName("EmailFilter")
    val sc = new SparkContext(conf)
    
    val spamMails = sc.textFile("/home/cloudera/DataSets/spam_filtering/spam")
    val goodMails = sc.textFile("/home/cloudera/DataSets/spam_filtering/good")
    
    val features = new HashingTF(numFeatures = 1000)
    val featuresSpam = spamMails.map(mail => features.transform(mail.split(" ")))
    val featuresGood = goodMails.map(mail=> features.transform(mail.split(" ")))
    
    val negativeData = featuresSpam.map(features => LabeledPoint(1,features))
    val positiveData = featuresGood.map(features => LabeledPoint(0,features))
    val data = positiveData.union(negativeData)
    
    data.cache()
    
    val Array(training,test) = data.randomSplit(Array(0.6,0.4))
    val logisticReg = new LogisticRegressionWithSGD()
    val model = logisticReg.run(training)
    
    val predectionLabel = test.map(x => (model.predict(x.features),x.label))
    
    predectionLabel.foreach(x=> println("First is : " + x._1 +  " sec: " + x._2))
    val accuracy = 1.0 * predectionLabel.filter(x => x._1 == x._2).count() / training.count()
    
    println("Model Accuracy is : " + accuracy)
    
    
    
  }
  
}