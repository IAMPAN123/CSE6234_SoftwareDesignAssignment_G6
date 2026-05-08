package com.pos.ui;

import java.sql.SQLException;

import com.pos.db.ActionLogDAO;
import com.pos.db.MembersDAO;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class MembershipPage {
    private VBox view;
    private Runnable onBackAction;

    public MembershipPage(Runnable onBackAction){
        this.onBackAction = onBackAction;
        initUI();
    }

    private void initUI() {
        view = new VBox(20);
        view.setPadding(new Insets(50));
        view.setAlignment(Pos.CENTER);
        view.setStyle("-fx-background-color: #f8fafb;");

        VBox form = new VBox(15);
        form.setMaxWidth(400);
        form.getStyleClass().add("card");
        form.setPadding(new Insets(30));

        Label title = new Label ("Membership Registration");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; ");

        TextField nameField = new TextField();
        nameField.setPromptText("Your Name");
        TextField emailField = new TextField();
        emailField.setPromptText  ("Email Address");
        TextField phoneField = new TextField();
        phoneField.setPromptText ("Phone Number");

        Button registrationBtn = new Button("Register Member");
        registrationBtn.getStyleClass().add("teal-button");
        registrationBtn.setMaxWidth(Double.MAX_VALUE);

        registrationBtn.setOnAction(e -> {
            System.out.println("Registering:" + nameField.getText());
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Registration Failed", "Please fill in all fields.");
                return;
            } 

            try {
                MembersDAO membersDAO = new MembersDAO();

                if (membersDAO.checkEmailExists(email)) {
                    showAlert(Alert.AlertType.WARNING, "Registration Failed", "This email is already in our system.");
                    return;
                }
                membersDAO.addMember(name, email, phone);

                ActionLogDAO actionLog = new ActionLogDAO();
                actionLog.logAction("REGISTER_MEMBER",
                "Name: " + name + ", Email: " + email + ", Phone: " + phone);

                showAlert(Alert.AlertType.INFORMATION, "Success", "Member registered successfully!");
                 onBackAction.run();
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to register member. Please try again.");
            }
           
            
        });

        Button backBtn = new Button("Cancel");
        backBtn.setOnAction(e-> onBackAction.run());

        form.getChildren().addAll (title, nameField, emailField, phoneField, registrationBtn, backBtn);
        view.getChildren().add(form);
    }

    public VBox getView(){
        return view;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
