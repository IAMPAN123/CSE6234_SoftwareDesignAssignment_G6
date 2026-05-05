package com.pos;

import java.sql.SQLException;
import java.util.List;

import com.pos.db.DatabaseConfig;
import com.pos.db.ProductDAO;
import com.pos.model.Product;
import com.pos.ui.AdminPanel;
import com.pos.ui.LoginPanel;
import com.pos.ui.MembershipPage;

import atlantafx.base.theme.PrimerLight;
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

public class App extends Application {
    private ProductDAO productDAO;
    private FlowPane productGrid;
    private List<Product> products;
    private Button registerBtn;

    @Override
    public void start(Stage stage) {
        // 1. Initialize Database
        try {
            DatabaseConfig.getInstance();
            productDAO = new ProductDAO();
            products = productDAO.getAllProducts();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 2. Set Theme
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        // --- 3. Build Main POS Layout ---
        HBox navbar = createNavbar(stage);
        
        VBox leftCol = new VBox(25);
        leftCol.setPadding(new Insets(30, 50, 30, 50));
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        productGrid = new FlowPane(15, 15);
        productGrid.setPadding(new Insets(10, 0, 0, 0));
        refreshProductGrid();

        leftCol.getChildren().addAll(createMembershipSection(), productGrid, createAddItemSection());

        VBox cart = createCartSection();

        HBox mainLayout = new HBox(20, leftCol, cart);
        mainLayout.setStyle("-fx-background-color: #F8FAFB;");

        BorderPane root = new BorderPane();
        root.setTop(navbar);
        root.setCenter(mainLayout);
        Scene shopScene = new Scene(root, 1200, 800);
        applyStylesheet(shopScene);

        MembershipPage membershipPage = new MembershipPage(() -> stage.setScene(shopScene));
        Scene membershipScene = new Scene(membershipPage.getView(), 1200, 800);
        applyStylesheet(membershipScene);

        // --- 4. Setup Login Page & Scene ---
        LoginPanel loginPanel = new LoginPanel(
            () -> stage.setScene(shopScene), // Back button returns to Shop
            (success) -> {
                if (success) {
                    AdminPanel admin = new AdminPanel(() -> refreshProducts(stage));
                    admin.show();
                    stage.setScene(shopScene); // Stay on Shop screen after Admin launches
                }
            }
        );
        Scene loginScene = new Scene(loginPanel.getView(), 1200, 800);
        applyStylesheet(loginScene);

        // --- 5. Link Admin Button to Login Scene ---
        Button adminBtn = (Button) navbar.getChildren().get(2); 
        adminBtn.setOnAction(e -> stage.setScene(loginScene));

        if (registerBtn != null) {
        registerBtn.setOnAction(e -> stage.setScene(membershipScene));
    }

        stage.setTitle("Group 6 Shop");
        stage.setScene(shopScene);
        stage.show();
    }

    // --- Navigation & Refresh Helpers ---

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

        registerBtn = new Button("New Member");
        registerBtn.getStyleClass().add("button-outline");
        box.getChildren().addAll(titleBox, searchBox, helpText, registerBtn);

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

        Label barcode = new Label(product.getBarcode());
        barcode.setStyle("-fx-font-size:11px; -fx-text-fill: #AAA;");
        
        Label name = new Label(product.getName());
        name.setStyle("-fx-font-size: 11px; -fx-text-fill: #AAA;");
        name.setWrapText(true);

        HBox bottom = new HBox();
        bottom.setAlignment(Pos.BOTTOM_LEFT);
        Label price = new Label(String.format("RM %.2f", product.getPrice()));
        price.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #008B8B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label stock = new Label(product.getStock() + " LEFT");
        stock.setStyle("-fx-font-size: 12px; -fx-text-fill: #AAA;");

        bottom.getChildren().addAll(price, spacer, stock);
        box.getChildren().addAll(barcode, name, bottom);
        return box;
    }

    public static void main(String[] args) { launch(args); }
}