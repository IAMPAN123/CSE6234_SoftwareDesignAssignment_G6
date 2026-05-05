package com.pos.ui;

import com.pos.model.Receipt;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ReceiptPanel {
    private Stage stage;
    private Receipt receipt;

    public ReceiptPanel(Receipt receipt) {
        this.receipt = receipt;
        this.stage = new Stage();
        initUI();
    }

    private void initUI() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8fafb;");

        Label title = new Label("Transaction Receipt");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextArea receiptText = new TextArea(receipt.getFormattedText());
        receiptText.setEditable(false);
        receiptText.setWrapText(true);
        receiptText.setStyle("-fx-control-inner-background: white; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        VBox.setVgrow(receiptText, Priority.ALWAYS);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button printBtn = new Button("Print");
        printBtn.setStyle("-fx-font-size: 12px; -fx-padding: 8px 20px;");
        printBtn.setOnAction(e -> printReceipt());

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-font-size: 12px; -fx-padding: 8px 20px;");
        closeBtn.setOnAction(e -> stage.close());

        buttonBox.getChildren().addAll(printBtn, closeBtn);

        root.getChildren().addAll(title, receiptText, buttonBox);

        Scene scene = new Scene(root, 500, 600);
        stage.setScene(scene);
        stage.setTitle("Receipt");
        stage.show();
    }

    private void printReceipt() {
        System.out.println("\n" + receipt.getFormattedText());
    }

    public void show() {
        stage.show();
    }
}
