package com.allexx9.controller;

/**
 * Created by adm on 04.12.2014.
 */
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/mainForm.fxml"));
        primaryStage.setTitle("Проверка безопасности предприятия v1.4");
        primaryStage.getIcons().add(new Image("images/icon2.png"));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
