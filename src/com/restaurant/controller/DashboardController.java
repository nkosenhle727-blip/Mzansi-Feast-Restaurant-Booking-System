package com.restaurant.controller;

import com.restaurant.dao.BillDAO;
import com.restaurant.dao.MenuItemDAO;
import com.restaurant.dao.OrderDAO;
import com.restaurant.dao.ReservationDAO;
import com.restaurant.model.Bill;
import com.restaurant.model.MenuItem;
import com.restaurant.model.OrderItem;
import com.restaurant.model.Reservation;
import com.restaurant.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    @FXML private Label lblWelcome;
    @FXML private TabPane tabPane;
    @FXML private Tab tabAdmin;

    // Booking Tab
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<String> cmbTime;
    @FXML private Spinner<Integer> spnPartySize;
    @FXML private ComboBox<Integer> cmbTable;
    @FXML private Label lblBookingStatus;

    @FXML private VBox vboxRecommendations;
    @FXML private HBox hboxSuggestedSlots;

    @FXML private TableView<Reservation> tblReservations;
    @FXML private TableColumn<Reservation, Integer> colTable;
    @FXML private TableColumn<Reservation, LocalDate> colDate;
    @FXML private TableColumn<Reservation, LocalTime> colTime;
    @FXML private TableColumn<Reservation, Integer> colParty;
    @FXML private TableColumn<Reservation, String> colStatus;

    // Menu & Order Cart Tab
    @FXML private ListView<MenuItem> listMenu;
    @FXML private Spinner<Integer> spnOrderQty;
    @FXML private TableView<OrderItem> tblCart;
    @FXML private TableColumn<OrderItem, String> colCartItem;
    @FXML private TableColumn<OrderItem, Integer> colCartQty;
    @FXML private TableColumn<OrderItem, Double> colCartUnitPrice;
    @FXML private TableColumn<OrderItem, Double> colCartSubtotal;
    @FXML private Label lblTotalBill;
    @FXML private Label lblCartStatus;

    // Admin Sales Audit Tab
    @FXML private DatePicker dpAuditDate;
    @FXML private Label lblDailyGrossRevenue;
    @FXML private Label lblBillCount;
    @FXML private TableView<Bill> tblBillsAudit;
    @FXML private TableColumn<Bill, Integer> colBillId;
    @FXML private TableColumn<Bill, Integer> colBillOrder;
    @FXML private TableColumn<Bill, Integer> colBillUser;
    @FXML private TableColumn<Bill, LocalDate> colBillDate;
    @FXML private TableColumn<Bill, Double> colBillTotal;
    @FXML private TableColumn<Bill, String> colBillStatus;

    private User currentUser;
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final MenuItemDAO menuItemDAO = new MenuItemDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final BillDAO billDAO = new BillDAO();

    private final ObservableList<OrderItem> cartItems = FXCollections.observableArrayList();

    public void initData(User user) {
        this.currentUser = user;
        lblWelcome.setText("Welcome, " + user.getFirstName() + " " + user.getLastName() + "!");

        setupFormFields();
        setupTableColumns();
        loadUserReservations();
        loadMenuItems();

        // Control Admin tab visibility based on role_id
        if (currentUser.getRoleId() != 1) {
            tabPane.getTabs().remove(tabAdmin);
        } else {
            dpAuditDate.setValue(LocalDate.now());
            handleLoadDailySales();
        }
    }

    private void setupFormFields() {
        dpDate.setValue(LocalDate.now());
        cmbTime.setItems(FXCollections.observableArrayList("12:00", "13:00", "14:00", "17:00", "18:00", "19:00", "20:00", "21:00"));
        cmbTime.getSelectionModel().selectFirst();
        spnPartySize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 2));
        cmbTable.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        cmbTable.getSelectionModel().selectFirst();

        spnOrderQty.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));
        tblCart.setItems(cartItems);
    }

    private void setupTableColumns() {
        // Reservation columns
        colTable.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("reservationDate"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("reservationTime"));
        colParty.setCellValueFactory(new PropertyValueFactory<>("partySize"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Cart columns
        colCartItem.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colCartQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCartUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colCartSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        // Admin Audit columns
        colBillId.setCellValueFactory(new PropertyValueFactory<>("billId"));
        colBillOrder.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colBillUser.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colBillDate.setCellValueFactory(new PropertyValueFactory<>("billDate"));
        colBillTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colBillStatus.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
    }

    // --- CART & ORDERING LOGIC ---

    @FXML
    private void handleAddToCart() {
        MenuItem selected = listMenu.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblCartStatus.setText("Please select a menu item from the list.");
            return;
        }

        int qty = spnOrderQty.getValue();
        boolean exists = false;

        for (OrderItem item : cartItems) {
            if (item.getMenuItem().getItemId() == selected.getItemId()) {
                item.setQuantity(item.getQuantity() + qty);
                exists = true;
                break;
            }
        }

        if (!exists) {
            cartItems.add(new OrderItem(selected, qty));
        }

        tblCart.refresh();
        updateCartTotal();
        lblCartStatus.setText("Added " + selected.getName() + " (x" + qty + ") to cart.");
    }

    @FXML
    private void handleRemoveFromCart() {
        OrderItem selected = tblCart.getSelectionModel().getSelectedItem();
        if (selected != null) {
            cartItems.remove(selected);
            updateCartTotal();
            lblCartStatus.setText("Item removed from cart.");
        }
    }

    // Algorithm 4.5.4: Calculate total bill across all cart items
    private double calculateGrandTotal() {
        double total = 0.0;
        for (OrderItem item : cartItems) {
            total += item.getSubtotal();
        }
        return total;
    }

    private void updateCartTotal() {
        lblTotalBill.setText(String.format("R%.2f", calculateGrandTotal()));
    }

    @FXML
    private void handleCheckout() {
        if (cartItems.isEmpty()) {
            lblCartStatus.setText("Cart is empty. Add food items before checkout.");
            return;
        }

        double totalAmount = calculateGrandTotal();
        int orderId = orderDAO.createOrderWithItems(currentUser.getUserId(), cartItems, totalAmount);

        if (orderId > 0) {
            boolean billCreated = billDAO.createBill(orderId, currentUser.getUserId(), totalAmount, LocalDate.now());

            if (billCreated) {
                lblCartStatus.setText("Order #" + orderId + " placed! Itemized bill of R" + String.format("%.2f", totalAmount) + " generated.");
                cartItems.clear();
                updateCartTotal();

                if (currentUser.getRoleId() == 1) {
                    handleLoadDailySales();
                }
            } else {
                lblCartStatus.setText("Order placed, but failed to create bill record.");
            }
        } else {
            lblCartStatus.setText("Failed to process order.");
        }
    }

    // --- ADMIN DAILY SALES AUDIT LOGIC (Algorithm 4.5.5) ---

    @FXML
    private void handleLoadDailySales() {
        LocalDate auditDate = dpAuditDate.getValue();
        if (auditDate == null) return;

        double totalRevenue = billDAO.getDailySalesTotal(auditDate);
        List<Bill> bills = billDAO.getBillsByDate(auditDate);

        lblDailyGrossRevenue.setText(String.format("R%.2f", totalRevenue));
        lblBillCount.setText(String.valueOf(bills.size()));
        tblBillsAudit.setItems(FXCollections.observableArrayList(bills));
    }

    // --- RESERVATION HANDLERS ---

    @FXML
    private void handleMakeReservation() {
        hideRecommendations();
        LocalDate date = dpDate.getValue();
        String timeStr = cmbTime.getValue();
        int partySize = spnPartySize.getValue();
        Integer tableNum = cmbTable.getValue();

        if (date == null || timeStr == null || tableNum == null) {
            lblBookingStatus.setText("Please complete all reservation details.");
            return;
        }

        LocalTime time = LocalTime.parse(timeStr);
        Reservation res = new Reservation(0, currentUser.getUserId(), tableNum, date, time, partySize, "CONFIRMED");

        String result = reservationDAO.createReservation(res);

        if ("SUCCESS".equals(result)) {
            lblBookingStatus.setText("Reservation successfully created!");
            loadUserReservations();
        } else if ("SLOT_OCCUPIED".equals(result)) {
            lblBookingStatus.setText("Selected slot is occupied.");
            displayAlternativeSlots(tableNum, date, time);
        } else {
            lblBookingStatus.setText("Database error: " + result);
        }
    }

    @FXML
    private void handleCancelReservation() {
        Reservation selected = tblReservations.getSelectionModel().getSelectedItem();

        if (selected == null) {
            lblBookingStatus.setText("Please select a reservation row from the table to cancel.");
            return;
        }

        boolean success = reservationDAO.cancelReservation(selected.getReservationId());

        if (success) {
            lblBookingStatus.setText("Reservation cancelled.");
            loadUserReservations();
        } else {
            lblBookingStatus.setText("Failed to cancel reservation.");
        }
    }

    private void displayAlternativeSlots(int tableNumber, LocalDate date, LocalTime requestedTime) {
        List<LocalTime> alternatives = reservationDAO.getAlternativeSlots(tableNumber, date, requestedTime);
        hboxSuggestedSlots.getChildren().clear();

        if (alternatives.isEmpty()) {
            lblBookingStatus.setText("Table " + tableNumber + " is fully booked for this date.");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        for (LocalTime slot : alternatives) {
            String timeFormatted = slot.format(formatter);
            Button slotButton = new Button(timeFormatted);
            slotButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 14; -fx-cursor: hand;");

            slotButton.setOnAction(e -> {
                cmbTime.setValue(timeFormatted);
                hideRecommendations();
                handleMakeReservation();
            });

            hboxSuggestedSlots.getChildren().add(slotButton);
        }

        vboxRecommendations.setVisible(true);
        vboxRecommendations.setManaged(true);
    }

    private void hideRecommendations() {
        vboxRecommendations.setVisible(false);
        vboxRecommendations.setManaged(false);
    }

    private void loadUserReservations() {
        ObservableList<Reservation> list = FXCollections.observableArrayList(
                reservationDAO.getReservationsByUser(currentUser.getUserId())
        );
        tblReservations.setItems(list);
    }

    private void loadMenuItems() {
        ObservableList<MenuItem> items = FXCollections.observableArrayList(menuItemDAO.getAllMenuItems());
        listMenu.setItems(items);
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/restaurant/view/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) lblWelcome.getScene().getWindow();
            stage.setScene(new Scene(root, 360, 320));
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}