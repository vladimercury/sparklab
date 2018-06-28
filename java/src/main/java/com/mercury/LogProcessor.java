package com.mercury;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;
import scala.Tuple3;

import java.text.SimpleDateFormat;
import java.util.Objects;

import static org.apache.spark.sql.functions.count;
import static org.apache.spark.sql.functions.window;

public class LogProcessor {
    private static final String APP_NAME = "Log Processor";
    private static final String FIRST_TASK = "/1";
    private static final String SECOND_TASK = "/2";
    private static final String THIRD_TASK = "/3";
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    private SparkSession session;
    private JavaSparkContext context;

    private String inputUrl;
    private String outputUrl;

    public LogProcessor(String inputUrl, String outputUrl) {
        this.context = new JavaSparkContext(new SparkConf().setAppName(APP_NAME));
        this.session = SparkSession.builder().sparkContext(this.context.sc()).getOrCreate();

        this.inputUrl = inputUrl;
        this.outputUrl = outputUrl;
    }

    private JavaRDD<LogEntry> getEntries() {
        return context.textFile(this.inputUrl).map(LogEntry.fromString).filter(Objects::nonNull);
    }

    public void doWork() {
        doFirstTask();
        doSecondTask();
        doThirdTask();
    }

    /**
     * Number of 5xx requests
     */
    private void doFirstTask() {
        JavaPairRDD<String, Integer> errorPairs = this.getEntries()
                .filter(entry -> entry.getCode() >= 500 && entry.getCode() < 600)
                .mapToPair(entry -> new Tuple2<>(entry.getUrl() + " " + entry.getCode(), 1));

        errorPairs.reduceByKey((first, second) -> first + second)
                .coalesce(1)
                .saveAsTextFile(this.outputUrl + FIRST_TASK);
    }

    /**
     * Number of requests for each method-code pair
     */
    private void doSecondTask() {
        JavaPairRDD<String, Integer> entryPairs = this.getEntries()
                .mapToPair(entry -> new Tuple2<>(FORMATTER.format(entry.getDate()) + " " + entry.getMethod() + " "  + entry.getCode(), 1));

        entryPairs.reduceByKey((first, second) -> first + second)
                .filter((pair) -> pair._2 > 10)
                .sortByKey()
                .coalesce(1)
                .saveAsTextFile(this.outputUrl + SECOND_TASK);
    }

    /**
     * Number of 4xx and 5xx requests per week
     */
    private void doThirdTask() {
        JavaRDD<LogEntry> entries = this.getEntries()
                .filter(entry -> entry.getCode() >= 400 && entry.getCode() < 600);
        Dataset<Row> dataFrame = session.createDataFrame(entries, LogEntry.class);
        Column slideWindow = window(dataFrame.col("date"), "1 week", "1 day");

        dataFrame.groupBy(slideWindow)
                .agg(count("code").as("count"))
                .select("window.start", "window.end", "count")
                .orderBy("start")
                .javaRDD()
                .map(r -> new Tuple3<>(
                        FORMATTER.format(r.getTimestamp(0)), // start date
                        FORMATTER.format(r.getTimestamp(1)), // end date
                        r.getLong(2))) // count
                .coalesce(1)
                .saveAsTextFile(this.outputUrl + THIRD_TASK);
    }
}
