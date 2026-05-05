package com.pos;

import atlantafx.base.theme.PrimerLight;
import com.pos.db.DatabaseConfig;
import com.pos.db.ProductDAO;
import com.pos.model.Product;
import com.pos.ui.AdminPanel;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.control.Separator;
import java.sql.SQLException;
import java.util.List;

public class App extends Application {
    private ProductDAO productDAO;
    private FlowPane productGrid;
    private List<Product> products;

    @Override
    public void start(Stage stage) {
        try {
            DatabaseConfig.getInstance();
            productDAO = new ProductDAO();
            products = productDAO.getAllProducts();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());


        //Top Navbar
        HBox navbar = new HBox();
        navbar.setPadding(new Insets(15,30,15,30));
        navbar.setStyle("-fx-background-color: white; -fx-border-color: #EEE; -fx-border-width:0 0 1 0;");
        Label logo = new Label("Group 6 Shop");
        logo.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button adminBtn = new Button("Admin Panel");
        adminBtn.getStyleClass().add("teal-button");
        adminBtn.setOnAction(e -> {
            AdminPanel admin = new AdminPanel(() -> refreshProducts(stage));
            admin.show();
        });

        navbar.getChildren().addAll(logo, spacer, adminBtn);

        // Left Column
        VBox leftCol = new VBox(25);
        leftCol.setPadding(new Insets(30, 50, 30, 50));
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        VBox membership = createMembershipSection();

        VBox addItem = createAddItemSection();



        //Product Grid
        productGrid = new FlowPane(15,15);
        productGrid.setPadding(new Insets(10, 0, 0, 0));

        refreshProductGrid();

        leftCol.getChildren().addAll(membership, productGrid, addItem);


        // Right Column (Cart)
        VBox cart = new VBox(20);
        cart.getStyleClass().add("card");
        cart.setPrefWidth(380);
        cart.setPadding(new Insets(25));

        Label cartTitle = new Label("Cart (0)");
        cartTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Region cartSpacer = new Region();
        VBox.setVgrow(cartSpacer, Priority.ALWAYS);

        VBox cartSummary = createCartSummary();

        Button chargeBtn = new Button("Charge RM 0.00");
        chargeBtn.getStyleClass().add("button-charge");
        chargeBtn.setMaxWidth(Double.MAX_VALUE);

        cart.getChildren().addAll( cartTitle, cartSpacer, cartSummary, chargeBtn);

        //Main Layout
        HBox mainLayout = new HBox(20, leftCol, cart);
        mainLayout.setStyle("-fx-background-color: #F8FAFB;");

        BorderPane root = new BorderPane();
        root.setTop(navbar);
        root.setCenter(mainLayout);

        Scene scene = new Scene(root, 1200, 800);
        try{
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("Stylesheet not found: " + e.getMessage());
        }

        stage.setTitle("Group 6 Shop");
        stage.setScene(shopScene);
        stage.show();




    }



    private void refreshProducts(Stage stage) {
        try {
            products = productDAO.getAllProducts();
            refreshProductGrid();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshProductGrid() {
        productGrid.getChildren().clear();
        for (Product product : products) {
            productGrid.getChildren().add(createProductCard(product));
        }
    }

    private void applyStylesheet(Scene scene) {
        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("Stylesheet error: " + e.getMessage());
        }
    }

    // --- UI Component Helpers ---

    private HBox createNavbar(Stage stage) {
        HBox navbar = new HBox();
        navbar.setPadding(new Insets(15, 30, 15, 30));
        navbar.setStyle("-fx-background-color: white; -fx-border-color: #EEE; -fx-border-width:0 0 1 0;");
        
        Label logo = new Label("Group 6 Shop");
        logo.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button adminBtn = new Button("Admin Panel");
        adminBtn.getStyleClass().add("teal-button");

        navbar.getChildren().addAll(logo, spacer, adminBtn);
        return navbar;
    }

    private VBox createMembershipSection() {
        VBox box = new VBox(15);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(25));

        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.getChildren().addAll(new Label("👤"), new Label("Membership (optional)"));

        HBox searchBox = new HBox(10);
        TextField memberSearch = new TextField();
        memberSearch.setPromptText("Type member name, email, or ID...");
        HBox.setHgrow(memberSearch, Priority.ALWAYS);
        Button findBtn = new Button("Find");
        findBtn.getStyleClass().add("teal-button");
        searchBox.getChildren().addAll(memberSearch, findBtn);

        Label helpText = new Label("Try ali@example.com (Gold, 10% off).");
        helpText.setStyle("-fx-text-fill: #777; -fx-font-size: 13px;");

        box.getChildren().addAll(titleBox, searchBox, helpText);
        return box;
    }

    private VBox createAddItemSection() {
        VBox box = new VBox(15);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(25));

        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.getChildren().addAll(new Label("|||"), new Label("Add item - scan barcode or type item name"));

        HBox addBox = new HBox(10);
        TextField itemInput = new TextField();
        itemInput.setPromptText("e.g. 8801234500011 or Croissant");
        HBox.setHgrow(itemInput, Priority.ALWAYS);
        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("teal-button");
        addBox.getChildren().addAll(itemInput, addBtn);

        box.getChildren().addAll(titleBox, addBox);
        return box;
    }

    private VBox createCartSection() {
        VBox cart = new VBox(20);
        cart.getStyleClass().add("card");
        cart.setPrefWidth(380);
        cart.setPadding(new Insets(25));

        Label cartTitle = new Label("Cart (0)");
        cartTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button chargeBtn = new Button("Charge RM 0.00");
        chargeBtn.getStyleClass().add("button-charge");
        chargeBtn.setMaxWidth(Double.MAX_VALUE);

        cart.getChildren().addAll(cartTitle, spacer, createCartSummary(), chargeBtn);
        return cart;
    }

    private VBox createCartSummary() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10, 0, 10, 0));

        HBox subtotal = new HBox(new Label("Subtotal"), new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, new Label("RM0.00"));
        HBox discount = new HBox(new Label("Discount"), new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, new Label("-RM 0.00"));
        discount.getChildren().get(2).setStyle("-fx-text-fill: #777;");

        Label totalVal = new Label("RM 0.00");
        totalVal.setStyle("-fx-font-size:20px; -fx-font-weight: bold; -fx-text-fill: #008B8B;");
        HBox total = new HBox(new Label("Total") {{ setStyle("-fx-font-size: 18px; -fx-font-weight: bold;"); }}, 
                             new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, totalVal);

        box.getChildren().addAll(subtotal, discount, new Separator(), total);
        return box;
    }

    private VBox createProductCard(Product product) {
        VBox box = new VBox(8);
        box.getStyleClass().add("product-card");

        Label barcodeLabel = new Label(product.getBarcode());
        barcodeLabel.setStyle("-fx-font-size:11px; -fx-text-fill: #AAA;");

        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #AAA;");
        nameLabel.setWrapText(true);

        HBox bottomRow = new HBox();
        bottomRow.setAlignment(Pos.BOTTOM_LEFT);

        Label priceLabel = new Label(String.format("RM %.2f", product.getPrice()));
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #008B8B;");

        Region bottomSpacer = new Region ();
        HBox.setHgrow(bottomSpacer, Priority.ALWAYS);

        String stockText = product.getStock() + " LEFT";
        Label stockLabel = new Label(stockText);
        stockLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #AAA;");

        if (product.getStock() < 10) {
            stockLabel.getStyleClass().add("stock-critical");
        }

        bottomRow.getChildren().addAll(priceLabel, bottomSpacer, stockLabel);

        box.getChildren().addAll(barcodeLabel, nameLabel, bottomRow);
        return box;
    }

    private VBox createComplexProductCard(String barcode, String name, String price, String stock){
        VBox box = new VBox(8);
        box.getStyleClass().add("product-card");

        //Barcode
        Label barcodeLabel = new Label(barcode);
        barcodeLabel.setStyle("-fx-font-size:11px; -fx-text-fill: #AAA;");

        //Product Name
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #AAA;");
        nameLabel.setWrapText(true);

        //Price and stock
        HBox bottomRow = new HBox();
        bottomRow.setAlignment(Pos.BOTTOM_LEFT);

        Label priceLabel = new Label(price);
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #008B8B;");

        Region bottomSpacer = new Region ();
        HBox.setHgrow(bottomSpacer, Priority.ALWAYS);

        Label stockLabel = new Label(stock);
        stockLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #AAA;");

        if (stock.contains("LEFT")&& stock.length() < 9) {
            stockLabel.getStyleClass().add("stock-critical");
        }

        bottomRow.getChildren().addAll(priceLabel, bottomSpacer, stockLabel);

        box.getChildren().addAll(barcodeLabel, nameLabel, bottomRow);
        return box;
    }
    public static void main(String[] args) { launch(args); }
}