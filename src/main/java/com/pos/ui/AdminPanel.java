package com.pos.ui;

import java.sql.SQLException;

import com.pos.db.ProductDAO;
import com.pos.model.Product;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AdminPanel {
    private ProductDAO productDAO;
    private TableView<Product> productTable;
    private Runnable onProductsChanged;

    public AdminPanel(Runnable onProductsChanged) {
        this.onProductsChanged = onProductsChanged;
    }

    public void show() {
        try {
            productDAO = new ProductDAO();
            Stage stage = new Stage();
            stage.setTitle("Admin Panel - Product Management");
            stage.setWidth(900);
            stage.setHeight(600);

            VBox root = new VBox(20);
            root.setPadding(new Insets(20));
            root.setStyle("-fx-background-color: #F8FAFB;");

            root.getChildren().addAll(
                    createAddProductSection(),
                    createProductsTableSection()
            );

            Scene scene = new Scene(root);
            try {
                scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            } catch (Exception e) {
                System.out.println("Stylesheet not found: " + e.getMessage());
            }

            stage.setScene(scene);
            stage.show();

            refreshProductTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createAddProductSection() {
        VBox box = new VBox(15);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(25));

        Label title = new Label("Add New Product");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        HBox barcodeBox = createInputField("Barcode:");
        TextField barcodeField = (TextField) barcodeBox.getChildren().get(1);

        HBox nameBox = createInputField("Product Name:");
        TextField nameField = (TextField) nameBox.getChildren().get(1);

        HBox priceBox = createInputField("Price (RM):");
        TextField priceField = (TextField) priceBox.getChildren().get(1);

        HBox stockBox = createInputField("Stock Quantity:");
        TextField stockField = (TextField) stockBox.getChildren().get(1);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button addBtn = new Button("Add Product");
        addBtn.getStyleClass().add("teal-button");
        addBtn.setPrefWidth(120);
        addBtn.setOnAction(e -> {
            try {
                if (validateInputs(barcodeField, nameField, priceField, stockField)) {
                    Product product = new Product(
                            barcodeField.getText().trim(),
                            nameField.getText().trim(),
                            Double.parseDouble(priceField.getText().trim()),
                            Integer.parseInt(stockField.getText().trim())
                    );
                    productDAO.addProduct(product);
                    clearFields(barcodeField, nameField, priceField, stockField);
                    refreshProductTable();
                    onProductsChanged.run();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        Button clearBtn = new Button("Clear");
        clearBtn.setPrefWidth(100);
        clearBtn.setOnAction(e -> clearFields(barcodeField, nameField, priceField, stockField));

        buttonBox.getChildren().addAll(clearBtn, addBtn);

        box.getChildren().addAll(title, barcodeBox, nameBox, priceBox, stockBox, buttonBox);
        return box;
    }

    private VBox createProductsTableSection() {
        VBox box = new VBox(15);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(25));
        VBox.setVgrow(box, Priority.ALWAYS);

        Label title = new Label("Product Inventory");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        productTable = createProductTable();
        VBox.setVgrow(productTable, Priority.ALWAYS);

        box.getChildren().addAll(title, productTable);
        return box;
    }

    private TableView<Product> createProductTable() {
        TableView<Product> table = new TableView<>();

        TableColumn<Product, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Product, String> barcodeCol = new TableColumn<>("Barcode");
        barcodeCol.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        barcodeCol.setPrefWidth(140);

        TableColumn<Product, String> nameCol = new TableColumn<>("Product Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(250);

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price (RM)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(100);

        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        stockCol.setPrefWidth(80);

        TableColumn<Product, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            {
                editBtn.setStyle("-fx-padding: 5px 10px; -fx-font-size: 11px;");
                deleteBtn.setStyle("-fx-padding: 5px 10px; -fx-font-size: 11px; -fx-text-fill: #d32f2f;");

                editBtn.setOnAction(e -> {
                    Product product = getTableView().getItems().get(getIndex());
                    showEditDialog(product);
                });

                deleteBtn.setOnAction(e -> {
                    Product product = getTableView().getItems().get(getIndex());
                    try {
                        productDAO.deleteProduct(product.getId());
                        refreshProductTable();
                        onProductsChanged.run();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(5);
                    hbox.getChildren().addAll(editBtn, deleteBtn);
                    setGraphic(hbox);
                }
            }
        });

        table.getColumns().addAll(idCol, barcodeCol, nameCol, priceCol, stockCol, actionCol);
        return table;
    }

    private void showEditDialog(Product product) {
        Stage stage = new Stage();
        stage.setTitle("Edit Product");
        stage.setWidth(400);
        stage.setHeight(300);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #F8FAFB;");

        Label title = new Label("Edit Product");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        HBox barcodeBox = createInputField("Barcode:");
        TextField barcodeField = (TextField) barcodeBox.getChildren().get(1);
        barcodeField.setText(product.getBarcode());

        HBox nameBox = createInputField("Product Name:");
        TextField nameField = (TextField) nameBox.getChildren().get(1);
        nameField.setText(product.getName());

        HBox priceBox = createInputField("Price (RM):");
        TextField priceField = (TextField) priceBox.getChildren().get(1);
        priceField.setText(String.valueOf(product.getPrice()));

        HBox stockBox = createInputField("Stock Quantity:");
        TextField stockField = (TextField) stockBox.getChildren().get(1);
        stockField.setText(String.valueOf(product.getStock()));

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button saveBtn = new Button("Save");
        saveBtn.getStyleClass().add("teal-button");
        saveBtn.setOnAction(e -> {
            try {
                if (validateInputs(barcodeField, nameField, priceField, stockField)) {
                    product.setBarcode(barcodeField.getText().trim());
                    product.setName(nameField.getText().trim());
                    product.setPrice(Double.parseDouble(priceField.getText().trim()));
                    product.setStock(Integer.parseInt(stockField.getText().trim()));
                    productDAO.updateProduct(product);
                    refreshProductTable();
                    onProductsChanged.run();
                    stage.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> stage.close());

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        root.getChildren().addAll(title, barcodeBox, nameBox, priceBox, stockBox, buttonBox);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private HBox createInputField(String label) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label(label);
        lbl.setPrefWidth(120);

        TextField field = new TextField();
        HBox.setHgrow(field, Priority.ALWAYS);

        box.getChildren().addAll(lbl, field);
        return box;
    }

    private boolean validateInputs(TextField... fields) {
        for (TextField field : fields) {
            if (field.getText().trim().isEmpty()) {
                return false;
            }
        }
        try {
            Double.parseDouble(fields[2].getText().trim());
            Integer.parseInt(fields[3].getText().trim());
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private void clearFields(TextField... fields) {
        for (TextField field : fields) {
            field.clear();
        }
    }

    private void refreshProductTable() {
        try {
            productTable.getItems().clear();
            productTable.getItems().addAll(productDAO.getAllProducts());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
