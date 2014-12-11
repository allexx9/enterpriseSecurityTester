package com.allexx9.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Loggers;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by adm on 31.10.2014.
 */
public class SampleController {
    public TabPane tabpane;
    @FXML public BarChart<String,Integer> barChart;
    @FXML CategoryAxis xAxis;
    private ObservableList<String> categoryIds = FXCollections.observableArrayList();
    private static final Logger logger = LogManager.getLogger("SampleController");
    public static HashMap<Integer,Integer> testValues = new HashMap<Integer, Integer>();
    final ContextMenu contextMenu = new ContextMenu();
    private static double xOffset = 0;
    private static double yOffset = 0;
    @FXML Tab result;
    @FXML Tab test;
    @FXML
    private void initialize(){
        MenuItem save = new MenuItem("Save as PNG");
        MenuItem exit = new MenuItem("Exit");
        contextMenu.getItems().add(save);
        contextMenu.getItems().add(exit);
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
        exit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.exit(0);
            }
        });
        barChart.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (MouseButton.SECONDARY.equals(event.getButton())){
                    contextMenu.show(((Node)(event.getSource())).getScene().getWindow(), event.getScreenX(), event.getScreenY());
                }
            }
        });

        tabpane.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = ((Node)(event.getSource())).getScene().getWindow().getX() - event.getScreenX();
                yOffset = ((Node)(event.getSource())).getScene().getWindow().getY() - event.getScreenY();
            }
        });

        tabpane.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ((Node)(event.getSource())).getScene().getWindow().setX(event.getScreenX() + xOffset);
                ((Node)(event.getSource())).getScene().getWindow().setY(event.getScreenY() + yOffset);
            }
        });

        testValues = (HashMap<Integer, Integer>) MainController.testValues.clone();
        List<Integer> listIDs = new ArrayList<Integer>(testValues.keySet());
        List<String> stringListIDs = new ArrayList<String>(listIDs.size());
        for (Integer myInt : listIDs) {
            stringListIDs.add(String.valueOf(myInt));
        }
        Collections.sort(stringListIDs);
        logger.error(stringListIDs);
        categoryIds.addAll(stringListIDs);
        xAxis.setCategories(categoryIds);
        setSecurityTestData(testValues);

    }

    public void setSecurityTestData(HashMap<Integer,Integer> securityTestData){
        XYChart.Series<String, Integer> series = createTestDataSeries(securityTestData);
        barChart.getData().add(series);
    }

    private XYChart.Series<String, Integer> createTestDataSeries(HashMap<Integer,Integer> securityTestData) {
        XYChart.Series<String,Integer> series = new XYChart.Series<String,Integer>();
        Iterator it = securityTestData.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry pairs = (HashMap.Entry)it.next();
            XYChart.Data<String, Integer> testData = new XYChart.Data<String, Integer>(String.valueOf(pairs.getKey()), (Integer) pairs.getValue());
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
}
