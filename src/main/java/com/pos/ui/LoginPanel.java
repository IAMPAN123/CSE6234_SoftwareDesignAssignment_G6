package com.pos.ui;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginPanel {
    private VBox view;
    private Runnable onBackAction;
    private Consumer<Boolean> onLoginResult;

    public LoginPanel(Runnable onBackAction, Consumer<Boolean> onLoginResult) {
        this.onBackAction = onBackAction;
        this.onLoginResult = onLoginResult;
        initUI();
    }

    private void initUI() {
        view = new VBox(20);
        view.setPadding(new Insets(50));
        view.setAlignment(Pos.CENTER);
        view.setStyle("-fx-background-color: #f4f4f4;"); // Or use your .card CSS class

        Label title = new Label("Admin Login");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");

        VBox form = new VBox(10);
        form.setMaxWidth(300);
        
        TextField username = new TextField();
        username.setPromptText("Username");
        
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        Button loginBtn = new Button("Login");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.getStyleClass().add("teal-button");
        
        // Login Logic
        loginBtn.setOnAction(e -> {
            // Hardcoded for now; can be replaced with a Database check
            if ("admin".equals(username.getText()) && "1234".equals(password.getText())) {
                onLoginResult.accept(true);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Credentials");
                alert.show();
            }
        });

        Button backBtn = new Button("Back to Shop");
        backBtn.setMnemonicParsing(false);
        backBtn.setOnAction(e -> onBackAction.run());

        view.getChildren().addAll(title, username, password, loginBtn, backBtn);
    }

    public VBox getView() {
        return view;
    }
}
