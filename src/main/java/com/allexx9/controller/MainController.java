package com.allexx9.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.*;

/**
 * Created by adm on 04.12.2014.
 */
public class MainController {
    public static HashMap<Integer,Double> testValues = new HashMap<Integer,Double>();
    private static final String FILENAME = "/questions.base";
    private static HashMap<Integer, Boolean> testProperties = new HashMap<Integer, Boolean>();
    private static Record[] recordsArray;
    private static Record[] fullRecordsArray;
    private static List<Integer> integerQuestionTypes = new ArrayList<Integer>();
    private static Record currentRecord;
    private static int questionNumber;
    private static int stringCount;
    private static final Logger logger = LogManager.getLogger("MainController");
    @FXML private ToggleButton typeSelectToggleButton;
    @FXML private ListView typeListView;
    @FXML private TabPane mainWindowTabPane;
    @FXML private Tab testTab;
    @FXML private Tab resultTab;
    @FXML private Tab settingsTab;
    @FXML private Tab helpTab;
    @FXML private Label welcomeLabel;
    @FXML private Button start;
    @FXML private Label yes;
    @FXML private Label no;
    @FXML private Label question;
    @FXML public BarChart<String,Double> barChart;
    @FXML private CategoryAxis xAxis;


    public BarChart improvedBarChart;
    public CategoryAxis ixAxis = new CategoryAxis();
    public NumberAxis iyAxis = new NumberAxis();

    private ObservableList<String> categoryIds = FXCollections.observableArrayList();
    final ContextMenu contextMenu = new ContextMenu();
    List<String> typesList = new ArrayList<String>();
    @FXML
    private void initialize() throws IOException {

        integerQuestionTypes.addAll(getIntegerQuestionsType());
        logger.error(integerQuestionTypes);
        typesList.addAll(getQuestionsType());
        final ObservableList<String> typesObservableList = FXCollections.observableList(typesList);
        typeListView.setItems(typesObservableList);
        MenuItem save = new MenuItem("Save as PNG");
        contextMenu.getItems().add(save);
        save.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    saveAsPng();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        logger.error(FILENAME);
        stringCount = getStringCount(FILENAME);
        recordsArray = new Record[stringCount];
        fillRecordsArray(recordsArray, FILENAME);
        fullRecordsArray = recordsArray.clone();
        questionNumber = 0;
        currentRecord = recordsArray[questionNumber];
        fillHashMapZero(FILENAME);
        typeSelectToggleButton.setVisible(false);
        question.setVisible(false);
        yes.setVisible(false);
        no.setVisible(false);
        String welcome = "Здравствуйте, вас приветствует программа тестирования безопасности придприятия!\n Для начала теста нажмите на кнопку 'Старт'!";
        welcomeLabel.setText(welcome);
        testTab.setText("Тест");
        resultTab.setText("Результат");
        settingsTab.setText("Настройки");
        helpTab.setText("Справка");
        WebView wv = new WebView();
        helpTab.setContent(wv);
        resultTab.setDisable(true);
        loadingHelpHtml();


        mainWindowTabPane.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Tab>() {
                    @Override
                    public void changed(ObservableValue<? extends Tab> ov, Tab t, Tab t1) {
                        filterQuestions();
                    }
                }
        );

        typeListView.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                String item = typeListView.getSelectionModel().getSelectedItem().toString();
                Boolean value = testProperties.get(Integer.parseInt(item.substring(item.indexOf("(")+1,item.lastIndexOf(')'))));
                typeSelectToggleButton.setVisible(true);
                if (value) {typeSelectToggleButton.setSelected(true);} else {typeSelectToggleButton.setSelected(false);}
            }
        });

        typeSelectToggleButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (oldValue){
                    typeSelectToggleButton.setText("Выключить");
                } else {
                    typeSelectToggleButton.setText("Включить");
                }
                String item = typeListView.getSelectionModel().getSelectedItem().toString();
                testProperties.replace(Integer.parseInt(item.substring(item.indexOf("(")+1,item.lastIndexOf(')'))), newValue);
                logger.error(testProperties.toString());
            }
        });
        start.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                filterQuestions();
                if (recordsArray.length==0){
                    welcomeLabel.setText("Пожалуйста, выберите хотя бы один раздел в настройках");
                } else
                {
                    start.setVisible(false);
                    welcomeLabel.setVisible(false);
                    question.setVisible(true);
                    yes.setVisible(true);
                    no.setVisible(true);
                    filterQuestions();
                    question.setText(currentRecord.getQuestion());
                }

            }
        });


        yes.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED,new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                performResultData();
                if(questionNumber==recordsArray.length-1){
                    logger.error(testValues);
                    yes.setDisable(true);
                    no.setDisable(true);
                    question.setText("Тест завершен!");
                    try {
//                        Main.showSecondStage();
//                        ((Node)(event.getSource())).getScene().getWindow().hide();
                        logger.error("завершено");
                        initializeBarChart();
                        mainWindowTabPane.getSelectionModel().select(resultTab);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    hasNext();
                    logger.error(questionNumber);
                    question.setText(currentRecord.getQuestion());
//                    question.setText(recordsArray[questionNumber].getQuestion());
                    logger.error(currentRecord);
                }
            }
        });

        no.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                if(questionNumber==recordsArray.length-1){
                    logger.error(testValues);
                    yes.setDisable(true);
                    no.setDisable(true);
                    question.setText("Тест завершен!");
                    try {
                        logger.error("завершено");
                        initializeBarChart();
                        resultTab.setDisable(false);
                        mainWindowTabPane.getSelectionModel().select(resultTab);
//                        ((Node)(event.getSource())).getScene().getWindow().hide();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    hasNext();
                    logger.error(questionNumber);
                    question.setText(currentRecord.getQuestion());
                    logger.error(currentRecord);
                    logger.error(questionNumber);
                }
            }
        });
        logger.error(getQuestionsType());
    }

    private void filterQuestions() {
        List<Record> filteredQuestion = new ArrayList<Record>();
        for (Map.Entry<Integer, Boolean> entry : testProperties.entrySet()){
            int current_id = entry.getKey();
            boolean state = entry.getValue();
            if (state){
                for (Record record:fullRecordsArray){
                    if (record.getId()==current_id)
                    {
                        filteredQuestion.add(record);
                    }
                }
            }
        }
        recordsArray = filteredQuestion.toArray(new Record[filteredQuestion.size()]);
        logger.error(Arrays.asList(recordsArray).toString());
    }

    private void loadingHelpHtml() throws FileNotFoundException {
        Task<String> reloader = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return returnXmlContext(getClass().getResourceAsStream("/html/help.html"));
            }
        };
        final WebEngine webEngine = ((WebView) helpTab.getContent()).getEngine();
        reloader.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                final  Object value  = event.getSource().getValue();
                if (value!=null){
                    webEngine.loadContent(value.toString(), "text/html");
                }
            }
        });

        reloader.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                webEngine.loadContent(event.getSource().getException().toString(), "text/plain");
            }
        });
        reloader.run();
    }

    private String returnXmlContext(InputStream filename) throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(filename));
        String line;
        StringBuilder builder = new StringBuilder();
        while ((line = reader.readLine()) !=null){
            builder.append(line);
        }
        reader.close();
        return builder.toString();
    }

    public static int getStringCount(String filename) throws IOException{
        try {
            LineNumberReader reader = new LineNumberReader(new InputStreamReader(MainController.class.getResourceAsStream(FILENAME)));
            int cnt = 0;
            String lineRead = "";
            while ((lineRead = reader.readLine()) != null){}
            cnt = reader.getLineNumber();
            reader.close();
            return cnt;
        } catch (FileNotFoundException exception){
            logger.error(exception);
            logger.error(FILENAME);
            logger.error(filename);
        }
        return 0;
    }
    private static void performResultData(){
        Double value = testValues.get(currentRecord.getId()) + currentRecord.getCost();
        testValues.put(currentRecord.getId(), value );
    }

    public static void fillHashMapZero(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(MainController.class.getResourceAsStream(FILENAME)));
        String line;
        while ((line = reader.readLine()) !=null){
            String baseParts[] = line.split("==");
            testValues.put(Integer.parseInt(baseParts[1]), 0.0);
        }
        reader.close();
    }

    private void fillRecordsArray(Record[] recordsArray, String filename) throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(MainController.class.getResourceAsStream(FILENAME)));
        String line;
        int stringCount = 0;
        while ((line = reader.readLine()) !=null){
            String baseParts[] = line.split("==");
            recordsArray[stringCount] = new Record(baseParts[0],Integer.parseInt(baseParts[1]),Double.parseDouble(baseParts[2]));
            stringCount++;
        }
        reader.close();
    }

    private static void hasNext(){
        if (questionNumber < recordsArray.length-1){
            questionNumber++;
            currentRecord = recordsArray[questionNumber];
        } else {
            questionNumber = 0;
            currentRecord = recordsArray[questionNumber];
        }
    }

    public void setSecurityTestData(HashMap<Integer,Double> securityTestData){
        XYChart.Series<String, Double> series = createTestDataSeries(securityTestData);
        barChart.getData().add(series);
    }

    private XYChart.Series<String, Double> createTestDataSeries(HashMap<Integer,Double> securityTestData) {
        XYChart.Series<String,Double> series = new XYChart.Series<String,Double>();
        Iterator it = securityTestData.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry pairs = (HashMap.Entry)it.next();
            XYChart.Data<String, Double> testData = new XYChart.Data<String, Double>(String.valueOf(pairs.getKey()), (Double) pairs.getValue());
            it.remove();
            series.getData().add(testData);
        }
        return series;
    }
    @FXML
    public void saveAsPng() throws IOException {
        WritableImage image = barChart.snapshot(new SnapshotParameters(), null);
        File file = new File("chart.png");
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException e) {
        }
    }

    private void performBarChart() throws IOException {
        barChart.setBarGap(0.2);
        List<Integer> listIDs = new ArrayList<Integer>(testValues.keySet());
        List<String> stringListIDs = new ArrayList<String>(listIDs.size());
        for (Integer myInt : listIDs) {
            stringListIDs.add(String.valueOf(myInt));
        }
        Collections.sort(stringListIDs);
        List<String> questionsTypeList = new ArrayList<String>();
        questionsTypeList.addAll(getQuestionsType());
        logger.error(stringListIDs);
        categoryIds.addAll(questionsTypeList);
        logger.error(categoryIds);
        xAxis.setCategories(categoryIds);
        setSecurityTestData(testValues);
    }

    private Set<String> getQuestionsType() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(MainController.class.getResourceAsStream(FILENAME)));
        String line;
        Set<String> questionsType = new HashSet<String>();
        while ((line = reader.readLine()) !=null){
            String baseParts[] = line.split("==");
            questionsType.add(baseParts[3]+" ("+baseParts[1]+")");
            testProperties.put(Integer.parseInt(baseParts[1]), true);
            logger.error(questionsType);
        }
        reader.close();
        return questionsType;
    }
    private Set<Integer> getIntegerQuestionsType() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(MainController.class.getResourceAsStream(FILENAME)));
        String line;
        Set<Integer> questionsType = new HashSet<Integer>();
        while ((line = reader.readLine()) !=null){
            String baseParts[] = line.split("==");
            questionsType.add(Integer.parseInt(baseParts[1]));
        }
        reader.close();
        return questionsType;
    }

    public ObservableList<XYChart.Series<String, Double>> getChartData() throws IOException {
        ObservableList<XYChart.Series<String, Double>> answer = FXCollections.observableArrayList();
        List<String> questionsTypeList = new ArrayList<String>();
        questionsTypeList.addAll(getQuestionsType());
        XYChart.Series<String, Double> series = new XYChart.Series<String, Double>();
        for (int i = 0;i<questionsTypeList.size();i++){
            String item = questionsTypeList.get(i);
            int key = Integer.parseInt(item.substring(item.indexOf("(")+1,item.lastIndexOf(')')));
            double value = testValues.get(key);
            series.getData().add(new XYChart.Data(item, value));
        }

        //double aValue = 17.56;
        //double cValue = 17.06;
        //ObservableList<XYChart.Series<String, Double>> answer = FXCollections.observableArrayList();
        //XYChart.Series<String, Double> aSeries = new XYChart.Series<String, Double>();
        //XYChart.Series<String, Double> cSeries = new XYChart.Series<String, Double>();
        //aSeries.setName("a");
        //cSeries.setName("C");

//        for (int i = 2011; i < 2021; i++) {
//            aSeries.getData().add(new XYChart.Data(Integer.toString(i), aValue));
//            aValue = aValue + Math.random() - .5;
//            cSeries.getData().add(new XYChart.Data(Integer.toString(i), cValue));
//            cValue = cValue + Math.random() - .5;
//        }

        answer.addAll(series);
        return answer;
    }
    private void initializeBarChart() throws IOException {
        improvedBarChart = new  BarChart(ixAxis,iyAxis, getChartData());
        improvedBarChart.setBarGap(0.2);
        resultTab.setContent(improvedBarChart);
        resultTab.setDisable(false);
        improvedBarChart.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                if (MouseButton.SECONDARY.equals(event.getButton())) {
                    contextMenu.show(((Node) (event.getSource())).getScene().getWindow(), event.getScreenX(), event.getScreenY());
                }
            }
        });
    }
}
