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
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by adm on 04.12.2014.
 */
public class MainController {
    //initialize logger
    private static final Logger logger = LogManager.getLogger("MainController");

    //values needs from perform test results
    public static HashMap<Integer,Double> testValues = new HashMap<Integer,Double>();
    private static final String FILENAME = "/questions.base";
    private static HashMap<Integer, Boolean> testProperties = new HashMap<Integer, Boolean>();
    private static Record[] recordsArray;
    private static Record[] fullRecordsArray;
    private static List<Integer> integerQuestionTypes = new ArrayList<Integer>();
    private static Record currentRecord;
    private static int questionNumber;
    private static int stringCount;


    //fxml objects initialization
    @FXML private Button stop;
    @FXML private Button apply;
    @FXML private Label number;
    @FXML private Label category;
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
        //initialize context menu
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


        integerQuestionTypes.addAll(getIntegerQuestionsType());
        typesList.addAll(getQuestionsType());
        java.util.Collections.sort(typesList);
        final ObservableList<String> typesObservableList = FXCollections.observableList(typesList);
        typeListView.setItems(typesObservableList);
        stringCount = getStringCount(FILENAME);
        recordsArray = new Record[stringCount];
        fillRecordsArray(recordsArray, FILENAME);
        fullRecordsArray = recordsArray.clone();
        questionNumber = 0;
        currentRecord = recordsArray[questionNumber];
        fillHashMapZero(FILENAME);

        //set invisible buttons
        typeSelectToggleButton.setVisible(false);
        question.setVisible(false);
        yes.setVisible(false);
        stop.setVisible(false);
        no.setVisible(false);
        number.setVisible(false);
        category.setVisible(false);
        final String welcome = "Здравствуйте, вас приветствует программа тестирования безопасности придприятия!\n Для начала теста нажмите на кнопку 'Старт'!";
        welcomeLabel.setText(welcome);
        testTab.setText("Тест");
        resultTab.setText("Результат");
        settingsTab.setText("Настройки");
        helpTab.setText("Справка");

        //initialize help tab
        WebView wv = new WebView();
        helpTab.setContent(wv);
        resultTab.setDisable(true);
        loadingHelpHtml();


        //initialize elemets listener
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
                    yes.setDisable(false);
                    no.setDisable(false);
                    settingsTab.setDisable(true);
                    stop.setVisible(true);
                    start.setVisible(false);
                    welcomeLabel.setVisible(false);
                    question.setVisible(true);
                    yes.setVisible(true);
                    no.setVisible(true);
                    number.setVisible(true);
                    filterQuestions();
                    question.setText(currentRecord.getQuestion());
                    number.setText("Вопрос "+(questionNumber+1)+" из "+(recordsArray.length-1));
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
                    settingsTab.setDisable(false);
                    welcomeLabel.setVisible(true);
                    welcomeLabel.setText("Тест завершен!");
                    question.setVisible(false);
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
                    //category.setText(currentRecord.getCategory());
                    logger.error(questionNumber);
                    question.setText(currentRecord.getQuestion());
                    number.setText("Вопрос "+(questionNumber+1)+" из "+(recordsArray.length-1));
                    category.setText("Категория: ");
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
                    settingsTab.setDisable(false);
                    welcomeLabel.setVisible(true);
                    welcomeLabel.setText("Тест завершен!");
                    question.setVisible(false);
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
                    number.setText("Вопрос "+(questionNumber+1)+" из "+(recordsArray.length-1));
                    category.setText("Категория: ");
                    logger.error(currentRecord);
                    logger.error(questionNumber);
                }
            }
        });

        apply.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                filterQuestions();
            }
        });

        stop.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                settingsTab.setDisable(false);
                typeSelectToggleButton.setVisible(false);
                question.setVisible(false);
                yes.setVisible(false);
                stop.setVisible(false);
                no.setVisible(false);
                number.setVisible(false);
                category.setVisible(false);
                String welcome = "Здравствуйте, вас приветствует программа тестирования безопасности придприятия!\n Для начала теста нажмите на кнопку 'Старт'!";
                welcomeLabel.setText(welcome);
                testTab.setText("Тест");
                resultTab.setText("Результат");
                settingsTab.setText("Настройки");
                helpTab.setText("Справка");
                start.setVisible(true);
                welcomeLabel.setVisible(true);
                questionNumber = 0;
                currentRecord = recordsArray[questionNumber];
                try {
                    fillHashMapZero(FILENAME);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //filter questions from properties
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

    //load html to helpTab
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
    //deprecated
    private String returnXmlContext(InputStream filename) throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(filename, "UTF8"));
        String line;
        StringBuilder builder = new StringBuilder();
        while ((line = reader.readLine()) !=null){
            builder.append(line);
        }
        reader.close();
        return builder.toString();
    }

    //return string number questions file
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

    //performing data per each question
    private static void performResultData(){
        Double value = testValues.get(currentRecord.getId()) + currentRecord.getCost();
        testValues.put(currentRecord.getId(), value );
    }

    //preparing hashmap befor put result
    public static void fillHashMapZero(String filename) throws IOException {
        InputStream inputStream = MainController.class.getResourceAsStream(FILENAME);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));
        String line;
        while ((line = reader.readLine()) !=null){
            String baseParts[] = line.split("==");
            testValues.put(Integer.parseInt(baseParts[1]), 0.0);
        }
        reader.close();
    }

    //filling the array data questions
    private void fillRecordsArray(Record[] recordsArray, String filename) throws IOException{
        InputStream inputStream = MainController.class.getResourceAsStream(FILENAME);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));
        String line;
        int stringCount = 0;
        while ((line = reader.readLine()) !=null){
            String baseParts[] = line.split("==");
            logger.error(baseParts[3]);
            recordsArray[stringCount] = new Record(baseParts[0],Integer.parseInt(baseParts[1]),Double.parseDouble(baseParts[2]));
            stringCount++;
        }
        reader.close();
    }

    //jump to next question
    private static void hasNext(){
        if (questionNumber < recordsArray.length-1){
            questionNumber++;
            currentRecord = recordsArray[questionNumber];
        } else {
            questionNumber = 0;
            currentRecord = recordsArray[questionNumber];
        }
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
    //"UTF8"
    //MainController.class.getResourceAsStream(FILENAME)
    private Set<String> getQuestionsType() throws IOException {
        InputStream inputStream = MainController.class.getResourceAsStream(FILENAME);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));
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
        InputStream inputStream = MainController.class.getResourceAsStream(FILENAME);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));
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
