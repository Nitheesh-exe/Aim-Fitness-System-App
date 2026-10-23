
package Main;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.StackPane;

import javafx.scene.chart.LineChart;
import javafx.stage.Screen;
import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;

public class aimcontroller implements Initializable {

    // 1. UI & SYSTEM VARIABLES
    @FXML private AnchorPane rootPane;
    @FXML private TabPane mainTabPane;
    @FXML private Button lightThemeBtn;
    @FXML private Button darkThemeBtn;

    @FXML private VBox modalOverlay;
    @FXML private VBox renewModalOverlay;
    @FXML private VBox attendanceModalOverlay;
    @FXML private VBox deleteModalOverlay;
    @FXML private javafx.scene.control.Label deleteModalMemberName;
    private String pendingDeleteId = null; // Remembers who we are trying to delete

    // DASHBOARD & STATS VARIABLES (ADDED HERE)
    @FXML private Label dashTotalMembers;
    @FXML private Label dashActiveMembers;
    @FXML private Label dashExpiringSoon;
    @FXML private Label dashTodaysAttendance;
    
    @FXML private Label statusTotal;
    @FXML private Label statusActiveLabel;
    @FXML private Label statusExpiringLabel;
    @FXML private Label statusExpiredLabel;
    
    @FXML private Label memActivePlans;
    @FXML private Label memExpiringMonth;
    @FXML private Label memExpiredPlans;
    @FXML private Label memRenewalsToday;
    

// 2. NEW MEMBER MANAGEMENT VARIABLES
    @FXML private TextField fullNameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField ageField;
    @FXML private TextField heightField;
    @FXML private TextField weightField;
    @FXML private TableView<MemberModel> memberTable;
    @FXML private TableColumn<MemberModel, String> colMemberId;
    @FXML private TableColumn<MemberModel, String> colFullName;
    private TableColumn<MemberModel, Integer> colAge;
    @FXML private TableColumn<MemberModel, String> colStatus;
    @FXML private TableColumn<MemberModel, String> colPhone;
    private TableColumn<MemberModel, String> colGender;
    // DYNAMIC LIST VARIABLES
    @FXML private Label recentName1;
    @FXML private Label recentStatus1;
    @FXML private Label recentName2;
    @FXML private Label recentStatus2;

    @FXML private Label actionName1;
    @FXML private Label actionStatus1;
    @FXML private Label actionName2;
    @FXML private Label actionStatus2;
    
   
    // RENEW MEMBERSHIP VARIABLES
    @FXML private ComboBox<String> renewDurationCombo;
    @FXML private DatePicker renewStartDatePicker;
    @FXML private Label renewExpiryLabel;
    
    // NEW ADD MEMBER OVERLAY VARIABLES
    @FXML private ComboBox<String> genderCombo;
    @FXML private ComboBox<String> addDurationCombo;
    @FXML private DatePicker addStartDatePicker;
    @FXML private Label addExpiryLabel;
    
    @FXML private TextField renewMemberIdField;
    
    // ATTENDANCE VARIABLES
    @FXML private TableView<AttendanceModel> attendanceTable;
    @FXML private TableColumn<AttendanceModel, String> colAttMemberId;
    @FXML private TableColumn<AttendanceModel, String> colAttCheckIn;
    @FXML private TableColumn<AttendanceModel, String> colAttCheckOut;
    @FXML private TableColumn<AttendanceModel, String> colAttDuration;
    @FXML private TableColumn<AttendanceModel, String> colAttName;

    // SUBSCRIPTION VARIABLES
    @FXML private TableView<SubscriptionModel> subscriptionTable;
    @FXML private TableColumn<SubscriptionModel, String> colSubMemberId;
    @FXML private TableColumn<SubscriptionModel, String> colSubPlan;
    @FXML private TableColumn<SubscriptionModel, String> colSubStart;
    @FXML private TableColumn<SubscriptionModel, String> colSubExpiry;
    @FXML private TableColumn<SubscriptionModel, String> colSubStatus;
    @FXML private TextField subSearchField;
    @FXML private ComboBox<String> subPlanFilterCombo;
    @FXML private ComboBox<String> subStatusFilterCombo;
    @FXML private TableColumn<SubscriptionModel, Void> colSubAction;
    @FXML private TableColumn<SubscriptionModel, String> colSubAddress;
    
    // Master list to hold the unfiltered subscription data
    private ObservableList<SubscriptionModel> masterSubscriptionList = FXCollections.observableArrayList();
    
    @FXML private TextField renewAmountField;
    @FXML private ComboBox<String> renewPaymentModeCombo;
    
    // CHART VARIABLES
    @FXML private javafx.scene.chart.LineChart<String, Number> expiryChart;
    
    
    @FXML private TextField attendanceSearchField;
    @FXML private Label attTotalToday;
    @FXML private Label attAvgDuration;
    @FXML private javafx.scene.chart.BarChart<String, Number> attendanceBarChart;
    @FXML private javafx.scene.chart.PieChart planPieChart;
    @FXML private javafx.scene.control.TextArea insightsTextArea;
    
    // Master list to hold the unfiltered attendance data
    private ObservableList<AttendanceModel> masterAttendanceList = FXCollections.observableArrayList();
    // Master list to hold the unfiltered member data
    private ObservableList<MemberModel> masterMemberList = FXCollections.observableArrayList();
    
    // NEW: O(log n) Search Tree for instant autocomplete
    private java.util.TreeMap<String, MemberModel> fastSearchTree = new java.util.TreeMap<>();

    // DAOs
    AttendanceDao attDao = new AttendanceDao();
    SubscriptionDao subDao = new SubscriptionDao();
    MemberDao md = new MemberDao();
    
    private ComboBox<String> reportTypeCombo;
    @FXML private TextField attendanceTabIdField;
    @FXML private TextField attendancePopupIdField;
    @FXML private TableColumn<MemberModel, Void> colActions;
    private String editingMemberId = null; // Tracks if we are Adding or Editing
    
    
    @FXML private Label dashboardDate;
    @FXML private TextField memberSearchField;
    @FXML private ComboBox<String> memberStatusFilterCombo;

    // ==========================================
    // BILLING & TRANSACTIONS VARIABLES
    // ==========================================
    @FXML private Label dashTotalRevenue;
    @FXML private Label dashCashCollections;
    @FXML private Label dashDigitalCollections;
    @FXML private Label dashTotalTransactions;
    @FXML private TextField billingSearchField;
    @FXML private ComboBox<String> billingModeFilterCombo;
    @FXML private TableView<PaymentModel> paymentsTable;
    @FXML private TableColumn<PaymentModel, String> colPayReceiptId;
    @FXML private TableColumn<PaymentModel, String> colPayMemberId;
    @FXML private TableColumn<PaymentModel, String> colPayMemberName;
    @FXML private TableColumn<PaymentModel, String> colPayPlan;
    @FXML private TableColumn<PaymentModel, String> colPayMode;
    @FXML private TableColumn<PaymentModel, String> colPayAmount;
    @FXML private TableColumn<PaymentModel, String> colPayDate;
    @FXML private TableColumn<PaymentModel, String> colPayStatus;
    private PaymentDao paymentDao = new PaymentDao();
    private ObservableList<PaymentModel> masterPaymentsList = FXCollections.observableArrayList();

    // ==========================================
    // WHATSAPP HUB & ALERTS VARIABLES
    // ==========================================
    @FXML private Label waExpiringSoonCount;
    @FXML private Label waGatewayStatus;
    @FXML private Label waSentTodayCount;
    @FXML private TableView<WhatsAppReminderModel> whatsAppTable;
    @FXML private TableColumn<WhatsAppReminderModel, String> colWaMemberId;
    @FXML private TableColumn<WhatsAppReminderModel, String> colWaName;
    @FXML private TableColumn<WhatsAppReminderModel, String> colWaPhone;
    @FXML private TableColumn<WhatsAppReminderModel, String> colWaExpiry;
    @FXML private TableColumn<WhatsAppReminderModel, String> colWaDays;
    @FXML private TableColumn<WhatsAppReminderModel, String> colWaStatus;
    @FXML private TableColumn<WhatsAppReminderModel, Void> colWaAction;
    @FXML private TextField waDirectPhoneField;
    @FXML private TextField waDirectMsgField;
    private ObservableList<WhatsAppReminderModel> masterWhatsAppList = FXCollections.observableArrayList();

    // ==========================================
    // FITNESS & BMI CALCULATOR VARIABLES
    // ==========================================
    @FXML private TextField calcHeightField;
    @FXML private TextField calcWeightField;
    @FXML private TextField calcAgeField;
    @FXML private ComboBox<String> calcGenderCombo;
    @FXML private ComboBox<String> calcActivityCombo;
    @FXML private Label calcBmiScoreLabel;
    @FXML private Label calcBmiCategoryLabel;
    @FXML private Label calcIdealWeightLabel;
    @FXML private Label calcBmrLabel;
    @FXML private Label calcCaloriesLabel;
    @FXML private Label calcWaterLabel;

    // ==========================================
    // SETTINGS TAB SUB-PANEL VARIABLES
    // ==========================================
    @FXML private Button btnSettingsGym;
    @FXML private Button btnSettingsSecurity;
    @FXML private Button btnSettingsLicense;
    @FXML private Button btnSettingsData;
    @FXML private VBox paneSettingsGym;
    @FXML private VBox paneSettingsSecurity;
    @FXML private VBox paneSettingsLicense;
    @FXML private VBox paneSettingsData;
    @FXML private TextField cfgGymName;
    @FXML private TextField cfgGymPhone;
    @FXML private TextField cfgGymAddress;
    @FXML private TextField cfgGymHours;
    @FXML private TextField cfgGymCurrency;
    @FXML private TextField cfgAdminUser;
    @FXML private PasswordField cfgCurrentPass;
    @FXML private PasswordField cfgNewPass;
    @FXML private PasswordField cfgConfirmPass;
    @FXML private Label cfgLicenseStatus;
    @FXML private Label cfgAppVersion;
    @FXML private Label cfgWorkstationHost;
    @FXML private Label cfgDbPath;


    // INITIALIZATION (Runs when app starts)
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        // Populate Membership Tab Filters
        if (subPlanFilterCombo != null) {
            subPlanFilterCombo.setItems(FXCollections.observableArrayList("All", "1 Month", "3 Months", "6 Months", "1 Year (Annual)"));
            subPlanFilterCombo.setValue("All");
        }
        if (subStatusFilterCombo != null) {
            subStatusFilterCombo.setItems(FXCollections.observableArrayList("All", "Active", "Expiring Soon", "Expired"));
            subStatusFilterCombo.setValue("All");
        }
        javafx.application.Platform.runLater(() -> {
            String liveDate = "📅 " + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy"));
            for (javafx.scene.Node node : rootPane.lookupAll(".date-badge")) {
                if (node instanceof javafx.scene.control.Label) {
                    ((javafx.scene.control.Label) node).setText(liveDate);
                }
            }
        });
        // Populate the Member Status Filter Dropdown
        if (memberStatusFilterCombo != null) {
            memberStatusFilterCombo.setItems(FXCollections.observableArrayList("All", "Active", "Expiring Soon", "7d"));
            memberStatusFilterCombo.setValue("All"); // Set default selection
        }

        hideModal(modalOverlay);
        hideModal(renewModalOverlay);
        hideModal(attendanceModalOverlay);
        hideModal(deleteModalOverlay);
        
        // Attendance Table mapping
        colAttMemberId.setCellValueFactory(new PropertyValueFactory<>("memberId"));
        colAttName.setCellValueFactory(new PropertyValueFactory<>("memberName")); 
        colAttCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        colAttCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        colAttDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        

        // Subscription Table mapping
        colSubMemberId.setCellValueFactory(new PropertyValueFactory<>("memberInfo"));
        colSubPlan.setCellValueFactory(new PropertyValueFactory<>("planDuration"));
        colSubStart.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colSubExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        colSubStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colSubAddress.setCellValueFactory(new PropertyValueFactory<>("address"));

        // Populate Payment Modes
        if(renewPaymentModeCombo != null) {
            renewPaymentModeCombo.setItems(FXCollections.observableArrayList("Credit / Debit Card", "UPI", "Cash"));
        }

        // Set up the TableView columns to match MemberModel properties
        colMemberId.setCellValueFactory(new PropertyValueFactory<>("memberId"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        // --- Populate Add Member Gender Combo ---
        if (genderCombo != null) {
            genderCombo.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        }

        // --- Populate & Listen: Add Member Duration Combo ---
        if (addDurationCombo != null) {
            addDurationCombo.setItems(FXCollections.observableArrayList(
                "1 Month", "3 Months", "6 Months", "1 Year (Annual)"
            ));
            addDurationCombo.valueProperty().addListener((obs, oldVal, newVal) -> calculateAddExpiryDate());
        }

        // --- Listen: Add Member Start Date ---
        if (addStartDatePicker != null) {
            addStartDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> calculateAddExpiryDate());
        }
        
        // --- Populate & Listen: Renew Duration Combo ---
        if (renewDurationCombo != null) {
            renewDurationCombo.setItems(FXCollections.observableArrayList(
                "1 Month", "3 Months", "6 Months", "1 Year (Annual)"
            ));
            renewDurationCombo.valueProperty().addListener((obs, oldVal, newVal) -> calculateExpiryDate());
        }
        
        // --- Listen: Renew Start Date ---
        if (renewStartDatePicker != null) {
            renewStartDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> calculateExpiryDate());
        }

     // Load tables and update dashboard stats initially
        refreshTables(); 
        setupMemberSearch(); 
        setupSubscriptionSearch();
        setupAttendanceSearch(); 
        setupAttendanceAutocomplete(attendanceTabIdField);
        setupAttendanceAutocomplete(attendancePopupIdField);
        setupFastAutocomplete(renewMemberIdField);
        setupSubscriptionActions();
        
        setupActionButtons();
        
        // Auto-run scan every time admin logs in / opens dashboard
        triggerAutomatedWhatsAppScan();

        // Initialize Billing Tab
        setupBillingTable();
        loadBillingData();

        // Initialize WhatsApp Hub
        setupWhatsAppHubTable();
        loadWhatsAppHubData();

        // Initialize Fitness Tools
        setupFitnessTools();

        // Initialize Settings
        setupSettingsPanels();

        // Tab selection change listener for auto-refresh
        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
                int idx = newIdx.intValue();
                if (idx == 5) {
                    refreshTables();
                } else if (idx == 9) {
                    loadBillingData();
                } else if (idx == 10) {
                    loadWhatsAppHubData();
                } else if (idx == 12) {
                    updateReportsTab();
                }
            });
        }

    } // End of initialize
    // End of initialize method
    
    
    // POPULATE THE LINE CHART
    private void updateExpiryChart() {
        if (expiryChart == null) return;
        
        try {
            // Clear old chart data to prevent overlap
            expiryChart.getData().clear();
            javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
            series.setName("Members Expiring");

            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.format.DateTimeFormatter monthFormatter = java.time.format.DateTimeFormatter.ofPattern("MMM yyyy");

            // 1. Prepare the labels for the next 6 months (e.g., "Aug 2026")
            String[] monthLabels = new String[6];
            int[] monthCounts = new int[6];
            
            for (int i = 0; i < 6; i++) {
                monthLabels[i] = today.plusMonths(i).format(monthFormatter);
                monthCounts[i] = 0; // Start at 0 expiries
            }

            // 2. Scan the database and count who expires in those specific months
            java.util.ArrayList<SubscriptionModel> allSubs = subDao.viewAll();
            for (SubscriptionModel sub : allSubs) {
                try {
                    java.time.LocalDate expiry = java.time.LocalDate.parse(sub.getExpiryDate());
                    String expiryMonthStr = expiry.format(monthFormatter);
                    
                    for (int i = 0; i < 6; i++) {
                        if (monthLabels[i].equals(expiryMonthStr)) {
                            monthCounts[i]++;
                            break;
                        }
                    }
                } catch (Exception e) { /* Ignore bad dates */ }
            }

            // 3. Feed the numbers into the graph
            for (int i = 0; i < 6; i++) {
                series.getData().add(new javafx.scene.chart.XYChart.Data<>(monthLabels[i], monthCounts[i]));
            }

            expiryChart.getData().add(series);
            
        } catch (Exception e) {
            System.out.println("Chart Error: " + e.getMessage());
        }
    }
    
    private void updateDashboardStats() {
        try {
            // 1. Fetch current data lists from database
            java.util.ArrayList<MemberModel> allMembers = md.view();
            java.util.ArrayList<SubscriptionModel> allSubs = subDao.viewAll();
            java.util.ArrayList<AttendanceModel> allAttendance = attDao.viewAll();
            
            // --- 2. DYNAMIC DATE MATH FOR STATUSES ---
            int totalMembers = allMembers.size();
            int activeMembers = 0, expiringSoon = 0, expiredMembers = 0;
            
            LocalDate today = LocalDate.now();
            LocalDate nextWeek = today.plusDays(15);
            
            // We use the Subscriptions table because it has the real Expiry Dates
            for (SubscriptionModel sub : allSubs) {
                try {
                    // PostgreSQL returns dates like "2026-07-25"
                    LocalDate expiry = LocalDate.parse(sub.getExpiryDate());
                    
                    if (expiry.isBefore(today)) {
                        expiredMembers++;
                        sub.setStatus("Expired"); // Temporarily update for the Action Box below
                    } else if (!expiry.isAfter(nextWeek)) { // Expires within the next 7 days
                        expiringSoon++;
                        sub.setStatus("Expiring Soon");
                    } else {
                        activeMembers++;
                        sub.setStatus("Active");
                    }
                } catch (Exception e) {
                    activeMembers++; // Fallback if date is somehow missing
                }
            }
            
            // 3. Tally Today's Attendance (Matching "25 Jul 2026" format)
            int todayAttendance = 0;
            String todayStr = today.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")); 
            for (AttendanceModel a : allAttendance) {
                if (a.getCheckIn() != null && a.getCheckIn().startsWith(todayStr)) {
                    todayAttendance++;
                }
            }
            
            // 4. Update the Top Dashboard Labels
            if(dashTotalMembers != null) dashTotalMembers.setText(String.valueOf(totalMembers));
            if(dashActiveMembers != null) dashActiveMembers.setText(String.valueOf(activeMembers));
            if(dashExpiringSoon != null) dashExpiringSoon.setText(String.valueOf(expiringSoon));
            if(dashTodaysAttendance != null) dashTodaysAttendance.setText(String.valueOf(todayAttendance));
            if(statusTotal != null) statusTotal.setText(String.valueOf(totalMembers));
            
            // 5. Update the Percentages Safely (Prevent divide-by-zero)
            int pctActive = totalMembers == 0 ? 0 : (activeMembers * 100) / totalMembers;
            int pctExpiring = totalMembers == 0 ? 0 : (expiringSoon * 100) / totalMembers;
            int pctExpired = totalMembers == 0 ? 0 : (expiredMembers * 100) / totalMembers;
            
            if(statusActiveLabel != null) statusActiveLabel.setText("● Active: " + activeMembers + " (" + pctActive + "%)");
            if(statusExpiringLabel != null) statusExpiringLabel.setText("● Expiring: " + expiringSoon + " (" + pctExpiring + "%)");
            if(statusExpiredLabel != null) statusExpiredLabel.setText("● Expired: " + expiredMembers + " (" + pctExpired + "%)");
            
            // 6. Update Membership Tab Counters
            int renewalsToday = 0;
            String todaySqlFormat = today.toString(); // Grabs today as "2026-07-26"
            
            for (SubscriptionModel sub : allSubs) {
                // If the subscription start date is exactly today, count it!
                if (sub.getStartDate() != null && sub.getStartDate().equals(todaySqlFormat)) {
                    renewalsToday++;
                }
            }
            
            if(memActivePlans != null) memActivePlans.setText(String.valueOf(activeMembers));
            if(memExpiringMonth != null) memExpiringMonth.setText(String.valueOf(expiringSoon));
            if(memExpiredPlans != null) memExpiredPlans.setText(String.valueOf(expiredMembers));
            if(memRenewalsToday != null) memRenewalsToday.setText(String.valueOf(renewalsToday)); // <--- UPDATES THE UI

                        // 7. Populate "Recently Added Members"
            if (allMembers.size() > 0) {
                MemberModel m1 = allMembers.get(allMembers.size() - 1);
                if(recentName1 != null) recentName1.setText(m1.getFullName());
                if(recentStatus1 != null) recentStatus1.setText(m1.getStatus());
                
                if (allMembers.size() > 1) {
                    MemberModel m2 = allMembers.get(allMembers.size() - 2);
                    if(recentName2 != null) recentName2.setText(m2.getFullName());
                    if(recentStatus2 != null) recentStatus2.setText(m2.getStatus());
                } else {
                    if(recentName2 != null) recentName2.setText("---");
                    if(recentStatus2 != null) recentStatus2.setText("---");
                }
            } else {
                // ADD THIS ELSE BLOCK: Clears FXML placeholders if database is totally empty
                if(recentName1 != null) recentName1.setText("---");
                if(recentStatus1 != null) { recentStatus1.setText("---"); recentStatus1.getStyleClass().clear(); }
                if(recentName2 != null) recentName2.setText("---");
                if(recentStatus2 != null) { recentStatus2.setText("---"); recentStatus2.getStyleClass().clear(); }
            }


            // 8. Populate "Action Needed" Box using the dynamic statuses we calculated above!
            int actionCount = 0;
            for (SubscriptionModel sub : allSubs) {
                String s = sub.getStatus();
                if (s != null && (s.equals("Expiring Soon") || s.equals("Expired"))) {
                    if (actionCount == 0) {
                        if(actionName1 != null) actionName1.setText(sub.getMemberName());
                        if(actionStatus1 != null) {
                            actionStatus1.setText(s);
                            actionStatus1.getStyleClass().clear();
                            actionStatus1.getStyleClass().add(s.equals("Expired") ? "badge-expired" : "badge-expiring");
                        }
                        actionCount++;
                    } else if (actionCount == 1) {
                        if(actionName2 != null) actionName2.setText(sub.getMemberName());
                        if(actionStatus2 != null) {
                            actionStatus2.setText(s);
                            actionStatus2.getStyleClass().clear();
                            actionStatus2.getStyleClass().add(s.equals("Expired") ? "badge-expired" : "badge-expiring");
                        }
                        actionCount++;
                        break; // Stop after finding 2
                    }
                }
            }
            
            // Clear remaining Action Needs if everyone is fine
            if (actionCount == 0) {
                if(actionName1 != null) actionName1.setText("All Members Good!");
                if(actionStatus1 != null) { actionStatus1.setText("---"); actionStatus1.getStyleClass().clear(); actionStatus1.getStyleClass().add("badge-active"); }
            }
            if (actionCount <= 1) {
                if(actionName2 != null) actionName2.setText("---");
                if(actionStatus2 != null) { actionStatus2.setText("---"); actionStatus2.getStyleClass().clear(); actionStatus2.getStyleClass().add("badge-active"); }
            }

        } catch (Exception e) {
            System.out.println("Error updating dashboard stats: " + e.getMessage());
        }
    }

    // ==========================================
    // AUTO-CALCULATE EXPIRY DATE (ADD MEMBER)
    // ==========================================
    private void calculateAddExpiryDate() {
        LocalDate startDate = addStartDatePicker.getValue();
        String durationStr = addDurationCombo.getValue();

        if (startDate != null && durationStr != null) {
            LocalDate expiryDate = startDate; 
            
            if (durationStr.contains("1 Month")) {
                expiryDate = startDate.plusMonths(1);
            } else if (durationStr.contains("3 Months")) {
                expiryDate = startDate.plusMonths(3);
            } else if (durationStr.contains("6 Months")) {
                expiryDate = startDate.plusMonths(6);
            } else if (durationStr.contains("1 Year")) {
                expiryDate = startDate.plusYears(1);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
            addExpiryLabel.setText(expiryDate.format(formatter));
        } else {
            addExpiryLabel.setText("Select Date & Plan");
        }
    }
    
    // ==========================================
    // AUTO-CALCULATE EXPIRY DATE (RENEWAL)
    // ==========================================
    private void calculateExpiryDate() {
        LocalDate startDate = renewStartDatePicker.getValue();
        String durationStr = renewDurationCombo.getValue();

        if (startDate != null && durationStr != null) {
            LocalDate expiryDate = startDate; 
            
            if (durationStr.contains("1 Month")) {
                expiryDate = startDate.plusMonths(1);
            } else if (durationStr.contains("3 Months")) {
                expiryDate = startDate.plusMonths(3);
            } else if (durationStr.contains("6 Months")) {
                expiryDate = startDate.plusMonths(6);
            } else if (durationStr.contains("1 Year")) {
                expiryDate = startDate.plusYears(1);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
            renewExpiryLabel.setText(expiryDate.format(formatter));
            
        } else {
            renewExpiryLabel.setText("Select Date & Plan");
        }
    }

  @FXML
    private void handleCheckIn(ActionEvent event) {
        String id = "";
        javafx.scene.control.TextField activeField = null;

        try {
            // Safely figure out which text box you are actually using
            if (attendanceModalOverlay != null && attendanceModalOverlay.isVisible()) {
                activeField = attendancePopupIdField;
            } else {
                activeField = attendanceTabIdField;
            }
            
            // Check if the box actually has text in it
            if (activeField != null && !activeField.getText().trim().isEmpty()) {
                
                String rawText = activeField.getText().trim();
                
                // AUTOCOMPLETE FIX: If they select "AIM-123 - Veera", chop off the name!
                if (rawText.contains(" - ")) {
                    rawText = rawText.split(" - ")[0].trim(); 
                }

                // FORCE uppercase and strip all accidental spaces
                id = rawText.replaceAll("\\s+", "").toUpperCase(); 
                
                // Add AIM- if the user forgot it
                if (!id.startsWith("AIM-")) {
                    id = "AIM-" + id;  
                }

                attDao.checkIn(id); // Send to Database
                
                activeField.clear(); // Clear the box so it doesn't get stuck
                refreshTables();
                System.out.println("✅ Check-in successful for " + id);
                
            } else {
                System.out.println("⚠️ Please enter a Member ID!");
            }
        } catch (Exception e) { 
            System.out.println("❌ Check-in Error FOR [" + id + "] -> " + e.getMessage());
        }
    }

    @FXML
    private void handleCheckOut(ActionEvent event) {
        String id = "";
        javafx.scene.control.TextField activeField = null;

        try {
            // Safely figure out which text box you are actually using
            if (attendanceModalOverlay != null && attendanceModalOverlay.isVisible()) {
                activeField = attendancePopupIdField;
            } else {
                activeField = attendanceTabIdField;
            }
            
            // Check if the box actually has text in it
            if (activeField != null && !activeField.getText().trim().isEmpty()) {
                
                String rawText = activeField.getText().trim();
                
                // AUTOCOMPLETE FIX: If they select "AIM-123 - Veera", chop off the name!
                if (rawText.contains(" - ")) {
                    rawText = rawText.split(" - ")[0].trim(); 
                }

                // FORCE uppercase and strip all accidental spaces
                id = rawText.replaceAll("\\s+", "").toUpperCase(); 
                
                // Add AIM- if the user forgot it
                if (!id.startsWith("AIM-")) {
                    id = "AIM-" + id;  
                }

                attDao.checkOut(id); // Send to Database
                
                activeField.clear(); // Clear the box so it doesn't get stuck
                refreshTables();
                System.out.println("✅ Check-out successful for " + id);
                
            } else {
                System.out.println("⚠️ Please enter a Member ID!");
            }
        } catch (Exception e) { 
            System.out.println("❌ Check-out Error FOR [" + id + "] -> " + e.getMessage());
        }
    }

    @FXML
    private void processRenewalAction(ActionEvent event) {
        try {
        // 1. Grab and Validate Member ID
        String memberId = renewMemberIdField.getText();
        if (memberId == null || memberId.trim().isEmpty()) {
            System.out.println("⚠️ Please enter a Gym Member ID!");
            return; // Stops the code here if empty
        }
        memberId = memberId.trim();

        // ---------------------------------------------------------
        // 🟢 ADD THIS FIX: Chop off the name if they used autocomplete
        // ---------------------------------------------------------
        if (memberId.contains(" - ")) {
            memberId = memberId.split(" - ")[0].trim(); 
        }
        // Force uppercase just in case they typed it manually
        memberId = memberId.toUpperCase();
        // ---------------------------------------------------------

        // 2. Validate Date and Plan Dropdowns
        if (renewStartDatePicker.getValue() == null || renewDurationCombo.getValue() == null) {
            System.out.println("⚠️ Please select a Start Date and a Plan Duration!");
            return;
        }
            
            String duration = renewDurationCombo.getValue();
            String start = renewStartDatePicker.getValue().toString();
            
            // 3. Extract the Calculated Expiry Date safely
            String expiryStr = renewExpiryLabel.getText();
            if (expiryStr.equals("Select Date & Plan")) {
                System.out.println("⚠️ Waiting for Expiry Date to calculate...");
                return;
            }
            // Converts "25 July 2026" back into PostgreSQL format "2026-07-25"
            LocalDate expiryDateObj = LocalDate.parse(expiryStr, DateTimeFormatter.ofPattern("dd MMMM yyyy"));
            String expiry = expiryDateObj.toString(); 

            // 4. Safely parse the Payment Amount (removes commas if the user typed "12,000")
            String amountText = renewAmountField.getText();
            double amount = 0.0;
            if (amountText != null && !amountText.trim().isEmpty()) {
                amount = Double.parseDouble(amountText.replace(",", "").trim());
            }
            
            // 5. Grab Payment Mode
            String mode = renewPaymentModeCombo.getValue() != null ? renewPaymentModeCombo.getValue() : "Not Specified";

            // 6. Send all data to the Database
            subDao.processRenewal(memberId, duration, start, expiry, amount, mode);
            
            // 7. Clear the popup fields for the next time it opens
            renewMemberIdField.clear();
            renewDurationCombo.getSelectionModel().clearSelection();
            renewStartDatePicker.setValue(null);
            renewExpiryLabel.setText("Select Date & Plan");
            renewAmountField.clear();
            renewPaymentModeCombo.getSelectionModel().clearSelection();
            
            // 8. Close popup, refresh tables, and show success!
            closeRenewPopup(event);
            refreshTables();
            System.out.println("✅ Successfully renewed membership for " + memberId);
            
        } catch (Exception e) { 
            System.out.println("❌ Renewal Failed: Check if Member ID exists! Detailed error: " + e.getMessage());
        }
    }

    // UNIFIED REFRESH METHOD
    public void refreshTables() {
        try {
            java.util.ArrayList<MemberModel> rawMembers = md.view();
            java.util.ArrayList<SubscriptionModel> allSubs = subDao.viewAll();
            
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate nextWeek = today.plusDays(7);
            
            // 1. Calculate real dynamic status for Subscriptions based on Date
            for (SubscriptionModel sub : allSubs) {
                try {
                    java.time.LocalDate expiry = java.time.LocalDate.parse(sub.getExpiryDate());
                    if (expiry.isBefore(today)) sub.setStatus("Expired");
                    else if (!expiry.isAfter(nextWeek)) sub.setStatus("Expiring Soon");
                    else sub.setStatus("Active");
                } catch (Exception e) { sub.setStatus("Active"); }
            }

            // 2. Sync Members to match the Subscription Status we just calculated
            for (MemberModel m : rawMembers) {
                for (SubscriptionModel sub : allSubs) {
                    if (sub.getMemberId().equals(m.getMemberId())) {
                        m.setStatus(sub.getStatus());
                        break;
                    }
                }
            }
            
            // 3. Feed Master Lists (Allows Search Bars to function)
            // 3. Feed Master Lists (Allows Search Bars to function)
            if(memberTable != null) masterMemberList.setAll(rawMembers);
            if(subscriptionTable != null) masterSubscriptionList.setAll(allSubs);
            
            // --- NEW: Populate the fast O(log n) search tree ---
            fastSearchTree.clear();
            for (MemberModel m : rawMembers) {
                if (m.getFullName() != null) {
                    fastSearchTree.put(m.getFullName().toLowerCase(), m);
                }
                if (m.getMemberId() != null) {
                    fastSearchTree.put(m.getMemberId().toLowerCase(), m);
                }
            }
            // 4. Feed regular tables
            // 4. Feed Attendance Table & Calculate Live Stats
            java.util.ArrayList<AttendanceModel> allAttendance = attDao.viewAll();
            if(attendanceTable != null) masterAttendanceList.setAll(allAttendance);

            int todayCheckins = 0;
            int totalMins = 0;
            int durationCount = 0;
            String todayStr = today.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"));

            for(AttendanceModel a : allAttendance) {
                if(a.getCheckIn() != null && a.getCheckIn().contains(todayStr)) {
                    todayCheckins++;
                }
                
                String dur = a.getDuration();
                if(dur != null && !dur.equals("--")) {
                    try {
                        int h = 0, m = 0;
                        if(dur.contains("h")) {
                            h = Integer.parseInt(dur.split("h")[0].trim());
                            String mPart = dur.split("h")[1];
                            if(mPart.contains("m")) m = Integer.parseInt(mPart.replace("m","").trim());
                        } else if(dur.contains("m")) {
                            m = Integer.parseInt(dur.replace("m","").trim());
                        }
                        totalMins += (h * 60) + m;
                        durationCount++;
                    } catch(Exception e) { }
                }
            }
            
            if(attTotalToday != null) attTotalToday.setText(String.valueOf(todayCheckins));
            
            if(attAvgDuration != null) {
                if(durationCount == 0) {
                    attAvgDuration.setText("0h 0m");
                } else {
                    int avgMins = totalMins / durationCount;
                    attAvgDuration.setText((avgMins / 60) + "h " + (avgMins % 60) + "m");
                }
            }
            
            updateDashboardStats();
            updateExpiryChart();
            
        } catch (Exception e) { e.printStackTrace(); }
        
    }
    // ==========================================
    // RENEWAL TAB: O(log n) SMART AUTOCOMPLETE
    // ==========================================
    private void setupFastAutocomplete(TextField field) {
        if (field == null) return;
        javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
        
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty() || newVal.contains(" - ")) {
                contextMenu.hide();
                return;
            }
            
            contextMenu.getItems().clear();
            String search = newVal.toLowerCase();
            
            // O(log n) search using TreeMap!
            // We search from the prefix to the prefix + maximum character value
            String endSearch = search + Character.MAX_VALUE; 
            
            java.util.SortedMap<String, MemberModel> matches = fastSearchTree.subMap(search, endSearch);
            
            int count = 0;
            for (MemberModel m : matches.values()) {
                if (count >= 10) break; // Keep UI fast by limiting to top 10 results
                
                String displayText = m.getMemberId() + " - " + m.getFullName();
                
                // Prevent duplicate display entries if both ID and Name were matched
                boolean exists = contextMenu.getItems().stream().anyMatch(item -> item.getText().equals(displayText));
                
                if (!exists) {
                    javafx.scene.control.MenuItem item = new javafx.scene.control.MenuItem(displayText);
                    item.setOnAction(e -> {
                        field.setText(item.getText());
                        field.positionCaret(item.getText().length());
                    });
                    contextMenu.getItems().add(item);
                    count++;
                }
            }
            
            if (contextMenu.getItems().isEmpty()) {
                contextMenu.hide();
            } else {
                contextMenu.show(field, javafx.geometry.Side.BOTTOM, 0, 0);
            }
        });
    }

@FXML
    private void saveMemberAction(ActionEvent event) {
        try {
            String name = fullNameField.getText();
            String phone = phoneField.getText();
            String email = emailField.getText();
            String gender = genderCombo.getValue() != null ? genderCombo.getValue() : "N/A";
            int age = Integer.parseInt(ageField.getText());
            String height = heightField.getText();
            String weight = weightField.getText();

            if (editingMemberId != null) {
                // ==========================================
                // WE ARE EDITING AN EXISTING MEMBER
                // ==========================================
                MemberModel m = new MemberModel(editingMemberId, name, phone, email, gender, age, height, weight, "Active");
                md.update(m);
                
            } else {
                // ==========================================
                // WE ARE ADDING A BRAND NEW MEMBER
                // ==========================================
                String generatedId = "AIM-" + (System.currentTimeMillis() % 1000000);
                MemberModel m = new MemberModel(generatedId, name, phone, email, gender, age, height, weight, "Active");
                md.Insert(m);
                
                String duration = addDurationCombo.getValue();
                String start = addStartDatePicker.getValue().toString();
                java.time.LocalDate expiryDateObj = java.time.LocalDate.parse(addExpiryLabel.getText(), java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy"));
                
                subDao.processRenewal(generatedId, duration, start, expiryDateObj.toString(), 0.0, "N/A");
            }
            
            refreshTables();
            closeAddMemberPopup(event); // This hides the modal

        } catch (Exception e) {
             System.out.println("Error saving member. Ensure Age is a number.");
             e.printStackTrace();
        }
    }
    
    // ORIGINAL SYSTEM CONTROLS (Logout & Themes)
    @FXML
    private void handleLogout(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login/Login.fxml"));
        Parent loginRoot = loader.load();
        Stage dashboardStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        dashboardStage.close();
        Stage loginStage = new Stage();
        loginStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        try {
            loginStage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/Login/icon.png")));
        } catch (Exception e) {
            System.out.println("Icon not found on logout.");
        }
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double width = screenBounds.getWidth() * 0.70;
        double height = screenBounds.getHeight() * 0.70;
        loginStage.setScene(new Scene(loginRoot));
        loginStage.setTitle("AIM Fitness Park - Admin Login");
        loginStage.centerOnScreen();
        loginStage.show();
    }

    @FXML
    private void cancelLogout(ActionEvent event) {
        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().select(5);
        }
    }

    @FXML
    private void setDarkTheme(ActionEvent event) {
        if (rootPane != null && !rootPane.getStyleClass().contains("dark-theme")) {
            rootPane.getStyleClass().add("dark-theme");
        }
        updateThemeButtons(true);
    }

    @FXML
    private void setLightTheme(ActionEvent event) {
        if (rootPane != null) {
            rootPane.getStyleClass().remove("dark-theme");
        }
        updateThemeButtons(false);
    }

    private void updateThemeButtons(boolean isDark) {
        if (darkThemeBtn != null && lightThemeBtn != null) {
            if (isDark) {
                darkThemeBtn.setStyle("-fx-border-color: #FF7A00; -fx-background-color: #38240B; -fx-text-fill: #FF7A00; -fx-font-weight: bold; -fx-padding: 10px 25px;");
                darkThemeBtn.setText("● Dark Theme");
                darkThemeBtn.getStyleClass().remove("btn-secondary");
                
                lightThemeBtn.setStyle("-fx-padding: 10px 25px;");
                if (!lightThemeBtn.getStyleClass().contains("btn-secondary")) {
                    lightThemeBtn.getStyleClass().add("btn-secondary");
                }
                lightThemeBtn.setText("○ Light Theme");
            } else {
                lightThemeBtn.setStyle("-fx-border-color: #FF7A00; -fx-background-color: #FFF8E6; -fx-text-fill: #FF7A00; -fx-font-weight: bold; -fx-padding: 10px 25px;");
                lightThemeBtn.setText("● Light Theme");
                lightThemeBtn.getStyleClass().remove("btn-secondary");
                
                darkThemeBtn.setStyle("-fx-padding: 10px 25px;");
                if (!darkThemeBtn.getStyleClass().contains("btn-secondary")) {
                    darkThemeBtn.getStyleClass().add("btn-secondary");
                }
                darkThemeBtn.setText("○ Dark Theme");
            }
        }
    }

    // ORIGINAL MODAL CONTROLS
    private void showModal(VBox modal) {
        if (modal != null) {
            modal.setVisible(true);
            modal.setManaged(true);
            modal.toFront();
        }
    }

    private void hideModal(VBox modal) {
        if (modal != null) {
            modal.setVisible(false);
            modal.setManaged(false);
        }
    }
    // ==========================================
    // INSTANT DUAL-FILTER LOGIC (SEARCH + DROPDOWN)
    // ==========================================
    private void setupMemberSearch() {
        if (memberTable == null || memberSearchField == null || memberStatusFilterCombo == null) return;

        // 1. Wrap the Master List in a FilteredList
        javafx.collections.transformation.FilteredList<MemberModel> filteredData = 
            new javafx.collections.transformation.FilteredList<>(masterMemberList, b -> true);
        
        // 2. Listen for ANY typing in the Search Bar
        memberSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyMemberFilters(filteredData);
        });

        // 3. Listen for ANY clicks in the Dropdown Menu
        memberStatusFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyMemberFilters(filteredData);
        });
        
        // 4. Bind the SortedList to the TableView
        javafx.collections.transformation.SortedList<MemberModel> sortedData = 
            new javafx.collections.transformation.SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(memberTable.comparatorProperty());
        memberTable.setItems(sortedData);
    }

    // Helper method that checks BOTH inputs at the exact same time
    private void applyMemberFilters(javafx.collections.transformation.FilteredList<MemberModel> filteredData) {
        String searchText = memberSearchField.getText() == null ? "" : memberSearchField.getText().toLowerCase();
        String selectedStatus = memberStatusFilterCombo.getValue();

        filteredData.setPredicate(member -> {
            
            // CHECK 1: Does it match the Dropdown?
            boolean matchesStatus = true;
            if (selectedStatus != null && !selectedStatus.equals("All")) {
                if (member.getStatus() == null || !member.getStatus().equalsIgnoreCase(selectedStatus)) {
                    matchesStatus = false; // It failed the dropdown test
                }
            }

            // CHECK 2: Does it match the Search Bar?
            boolean matchesSearch = true;
            if (!searchText.isEmpty()) {
                boolean matchName = member.getFullName() != null && member.getFullName().toLowerCase().contains(searchText);
                boolean matchPhone = member.getPhone() != null && member.getPhone().toLowerCase().contains(searchText);
                boolean matchId = member.getMemberId() != null && member.getMemberId().toLowerCase().contains(searchText);
                
                if (!matchName && !matchPhone && !matchId) {
                    matchesSearch = false; // It failed the search bar test
                }
            }

            // ONLY show the row if it passes BOTH tests!
            return matchesStatus && matchesSearch;
        });
    }
    // ==========================================
    // MEMBERSHIP TAB: TRI-FILTER LOGIC
    // ==========================================
    private void setupSubscriptionSearch() {
        if (subscriptionTable == null || subSearchField == null || subPlanFilterCombo == null || subStatusFilterCombo == null) return;

        javafx.collections.transformation.FilteredList<SubscriptionModel> filteredData = 
            new javafx.collections.transformation.FilteredList<>(masterSubscriptionList, b -> true);
        
        // Listen to all 3 inputs for changes
        subSearchField.textProperty().addListener((obs, oldVal, newVal) -> applySubFilters(filteredData));
        subPlanFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applySubFilters(filteredData));
        subStatusFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applySubFilters(filteredData));
        
        javafx.collections.transformation.SortedList<SubscriptionModel> sortedData = 
            new javafx.collections.transformation.SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(subscriptionTable.comparatorProperty());
        subscriptionTable.setItems(sortedData);
    }

    private void applySubFilters(javafx.collections.transformation.FilteredList<SubscriptionModel> filteredData) {
        String searchText = subSearchField.getText() == null ? "" : subSearchField.getText().toLowerCase();
        String selectedPlan = subPlanFilterCombo.getValue();
        String selectedStatus = subStatusFilterCombo.getValue();

        filteredData.setPredicate(sub -> {
            // CHECK 1: Plan Match?
            if (selectedPlan != null && !selectedPlan.equals("All")) {
                if (sub.getPlanDuration() == null || !sub.getPlanDuration().equalsIgnoreCase(selectedPlan)) return false;
            }

            // CHECK 2: Status Match?
            if (selectedStatus != null && !selectedStatus.equals("All")) {
                if (sub.getStatus() == null || !sub.getStatus().equalsIgnoreCase(selectedStatus)) return false;
            }

            // CHECK 3: Search Bar Match?
            if (!searchText.isEmpty()) {
                boolean matchId = sub.getMemberId() != null && sub.getMemberId().toLowerCase().contains(searchText);
                boolean matchName = sub.getMemberName() != null && sub.getMemberName().toLowerCase().contains(searchText);
                if (!matchId && !matchName) return false;
            }

            return true; // Passed all tests!
        });
    }
   // ==========================================
    // ATTENDANCE TAB: SEARCH LOGIC
    // ==========================================
    private void setupAttendanceSearch() {
        if (attendanceTable == null || attendanceSearchField == null) return;

        javafx.collections.transformation.FilteredList<AttendanceModel> filteredData = 
            new javafx.collections.transformation.FilteredList<>(masterAttendanceList, b -> true);
        
        attendanceSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(att -> {
                if (newVal == null || newVal.isEmpty()) return true;
                
                String search = newVal.toLowerCase();
                boolean matchId = att.getMemberId() != null && att.getMemberId().toLowerCase().contains(search);
                boolean matchName = att.getMemberName() != null && att.getMemberName().toLowerCase().contains(search); // NEW!
                boolean matchIn = att.getCheckIn() != null && att.getCheckIn().toLowerCase().contains(search);
                
                return matchId || matchName || matchIn;
            });
        });
        
        javafx.collections.transformation.SortedList<AttendanceModel> sortedData = new javafx.collections.transformation.SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(attendanceTable.comparatorProperty());
        attendanceTable.setItems(sortedData);
    }

    // ==========================================
    // ATTENDANCE TAB: SMART AUTOCOMPLETE
    // ==========================================
    private void setupAttendanceAutocomplete(TextField field) {
        if (field == null) return;
        javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
        
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty() || newVal.contains(" - ")) {
                contextMenu.hide();
                return;
            }
            
            contextMenu.getItems().clear();
            String search = newVal.toLowerCase();
            
            // Search the master member list for matches
            for (MemberModel m : masterMemberList) {
                if (m.getFullName().toLowerCase().contains(search) || m.getMemberId().toLowerCase().contains(search)) {
                    javafx.scene.control.MenuItem item = new javafx.scene.control.MenuItem(m.getMemberId() + " - " + m.getFullName());
                    item.setOnAction(e -> {
                        field.setText(item.getText());
                        field.positionCaret(item.getText().length());
                    });
                    contextMenu.getItems().add(item);
                }
            }
            
            if (contextMenu.getItems().isEmpty()) {
                contextMenu.hide();
            } else {
                contextMenu.show(field, javafx.geometry.Side.BOTTOM, 0, 0);
            }
        });
    }
    
    // ==========================================
    // EXPORT TO EXCEL (WITH ON-SCREEN POPUP)
    // ==========================================
    private void handleExportEXL(javafx.event.ActionEvent event) {
        String type = reportTypeCombo.getValue() != null ? reportTypeCombo.getValue() : "Members List";
        try {
            // Creates a .xls file on the desktop
            java.io.File file = new java.io.File(System.getProperty("user.home") + "/Desktop/AIM_Report_" + System.currentTimeMillis() + ".xls");
            java.io.PrintWriter writer = new java.io.PrintWriter(file);
            
            // HTML table trick that Excel reads as a perfect grid
            writer.println("<html><body><table border='1' style='border-collapse: collapse; text-align: left;'>");
            writer.println("<tr style='background-color: #f2f2f2;'><th colspan='4' style='padding: 10px; font-size: 16px;'>AIM Fitness Park - " + type + "</th></tr>");
            
            if (type.contains("Members")) {
                writer.println("<tr style='background-color: #e6e6e6;'><th>Member ID</th><th>Full Name</th><th>Phone</th><th>Status</th></tr>");
                for (MemberModel m : masterMemberList) {
                    if (type.contains("Active") && !"Active".equals(m.getStatus())) continue;
                    if (type.contains("Expired") && !"Expired".equals(m.getStatus())) continue;
                    
                    writer.println("<tr><td style='padding: 5px;'>" + m.getMemberId() + "</td><td style='padding: 5px;'>" + m.getFullName() + "</td><td style='padding: 5px;'>" + m.getPhone() + "</td><td style='padding: 5px;'>" + m.getStatus() + "</td></tr>");
                }
            } else {
                writer.println("<tr><td colspan='4'>Raw Data Export for: " + type + "</td></tr>");
            }
            
            writer.println("</table></body></html>");
            writer.close();
            
            // VISUAL POPUP FOR EXCEL SUCCESS
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Excel Export Successful");
            alert.setHeaderText(null);
            alert.setContentText("✅ Excel report has been successfully saved to your Desktop!");
            alert.showAndWait();
            
        } catch (Exception e) {
            // VISUAL POPUP FOR EXCEL ERROR
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Export Failed");
            alert.setHeaderText(null);
            alert.setContentText("❌ Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    // ==========================================
    // EXPORT TO PDF (WITH ON-SCREEN POPUP)
    // ==========================================
    private void handleExportPDF(javafx.event.ActionEvent event) {
        // Triggers the native OS Print Dialog
        javafx.print.PrinterJob job = javafx.print.PrinterJob.createPrinterJob();
        
        if (job != null && job.showPrintDialog(rootPane.getScene().getWindow())) {
            
            javafx.scene.layout.VBox printLayout = new javafx.scene.layout.VBox(20);
            printLayout.setStyle("-fx-background-color: white; -fx-padding: 30;");
            
            javafx.scene.control.Label title = new javafx.scene.control.Label("AIM Fitness - Analytics Report");
            title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #FF7A00;");
            
            javafx.scene.control.Label timestamp = new javafx.scene.control.Label("Generated on: " + java.time.LocalDate.now().toString());
            timestamp.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
            
            // Take snapshots of the LIVE charts from your UI
            javafx.scene.image.ImageView barImage = new javafx.scene.image.ImageView(attendanceBarChart.snapshot(null, null));
            javafx.scene.image.ImageView pieImage = new javafx.scene.image.ImageView(planPieChart.snapshot(null, null));
            
            barImage.setFitWidth(500); barImage.setPreserveRatio(true);
            pieImage.setFitWidth(350); pieImage.setPreserveRatio(true);
            
            printLayout.getChildren().addAll(title, timestamp, barImage, pieImage);
            
            boolean success = job.printPage(printLayout);
            if (success) {
                job.endJob();
                
                // VISUAL POPUP FOR PDF SUCCESS
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("PDF Export Successful");
                alert.setHeaderText(null);
                alert.setContentText("✅ PDF Analytics Report generated successfully!");
                alert.showAndWait();
            } else {
                // VISUAL POPUP FOR PDF ERROR
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("PDF Export Failed");
                alert.setHeaderText(null);
                alert.setContentText("❌ Failed to generate the PDF layout.");
                alert.showAndWait();
            }
        }
    }
    // ==========================================
    // REPORTS & ANALYTICS TAB LOGIC
    // ==========================================
   @FXML
    private void handleGenerateReport(javafx.event.ActionEvent event) {
        System.out.println("📊 Generating Live Business Insights...");
        updateReportsTab(); 
    }

    private void updateReportsTab() {
        int m1 = 0, m3 = 0, m6 = 0, y1 = 0;
        int totalActive = 0;

        // 1. UPDATE PIE CHART (Plan Distribution)
        if (planPieChart != null) {
            planPieChart.getData().clear();
            
            for (SubscriptionModel s : masterSubscriptionList) {
                if ("Active".equals(s.getStatus()) || "Expiring Soon".equals(s.getStatus())) {
                    totalActive++;
                    if (s.getPlanDuration() != null) {
                        if (s.getPlanDuration().contains("1 Month")) m1++;
                        else if (s.getPlanDuration().contains("3 Months")) m3++;
                        else if (s.getPlanDuration().contains("6 Months")) m6++;
                        else if (s.getPlanDuration().contains("1 Year")) y1++;
                    }
                }
            }
            if(m1 > 0) planPieChart.getData().add(new javafx.scene.chart.PieChart.Data("1 Month", m1));
            if(m3 > 0) planPieChart.getData().add(new javafx.scene.chart.PieChart.Data("3 Months", m3));
            if(m6 > 0) planPieChart.getData().add(new javafx.scene.chart.PieChart.Data("6 Months", m6));
            if(y1 > 0) planPieChart.getData().add(new javafx.scene.chart.PieChart.Data("Annual", y1));
        }

        // 2. UPDATE BAR CHART (Morning vs Evening Attendance)
        int totalAm = 0, totalPm = 0;
        if (attendanceBarChart != null) {
            attendanceBarChart.getData().clear();
            javafx.scene.chart.XYChart.Series<String, Number> morning = new javafx.scene.chart.XYChart.Series<>();
            morning.setName("Morning (AM)");
            javafx.scene.chart.XYChart.Series<String, Number> evening = new javafx.scene.chart.XYChart.Series<>();
            evening.setName("Evening (PM)");

            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.format.DateTimeFormatter format = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy");
            java.time.format.DateTimeFormatter dayFormat = java.time.format.DateTimeFormatter.ofPattern("EEE");
            
            for (int i = 4; i >= 0; i--) {
                String dateStr = today.minusDays(i).format(format);
                String dayLabel = today.minusDays(i).format(dayFormat); 
                int amCount = 0, pmCount = 0;
                
                for (AttendanceModel a : masterAttendanceList) {
                    if (a.getCheckIn() != null && a.getCheckIn().contains(dateStr)) {
                        if (a.getCheckIn().contains("AM")) { amCount++; totalAm++; }
                        else { pmCount++; totalPm++; }
                    }
                }
                morning.getData().add(new javafx.scene.chart.XYChart.Data<>(dayLabel, amCount));
                evening.getData().add(new javafx.scene.chart.XYChart.Data<>(dayLabel, pmCount));
            }
            attendanceBarChart.getData().addAll(morning, evening);
        }

        // 3. GENERATE SMART TEXT INSIGHTS
        if (insightsTextArea != null) {
            StringBuilder insights = new StringBuilder();
            insights.append("📌 GYM HEALTH SUMMARY\n");
            insights.append("--------------------------------------------------\n");
            insights.append("👥 Membership: You currently have ").append(totalActive).append(" active members.\n");
            
            // Find most popular plan
            String topPlan = "1 Month"; int topCount = m1;
            if (m3 > topCount) { topPlan = "3 Months"; topCount = m3; }
            if (m6 > topCount) { topPlan = "6 Months"; topCount = m6; }
            if (y1 > topCount) { topPlan = "Annual"; topCount = y1; }
            insights.append("⭐ Top Plan: Your most popular subscription is the ").append(topPlan)
                    .append(" plan (chosen by ").append(topCount).append(" active members).\n");

            // Find busiest time
            String busyTime = (totalPm > totalAm) ? "Evenings (PM)" : "Mornings (AM)";
            insights.append("🔥 Peak Hours: Over the last 5 days, your gym is significantly busier in the ")
                    .append(busyTime).append(".\n");
            
            // Expiry warning
            long expiringCount = masterSubscriptionList.stream().filter(s -> "Expiring Soon".equals(s.getStatus())).count();
            if (expiringCount > 0) {
                insights.append("⚠️ Action Required: You have ").append(expiringCount).append(" members expiring within the next 7 days. Check the Dashboard to follow up with them!");
            } else {
                insights.append("✅ Retention: No members are expiring in the next 7 days. Great job!");
            }

            insightsTextArea.setText(insights.toString());
        }
    }
    // ==========================================
    // ACTION BUTTONS (EDIT & DELETE)
    // ==========================================
    private void setupActionButtons() {
        if (colActions == null) return;
        
        javafx.util.Callback<TableColumn<MemberModel, Void>, javafx.scene.control.TableCell<MemberModel, Void>> cellFactory = param -> {
            return new javafx.scene.control.TableCell<MemberModel, Void>() {
                private final Button btnEdit = new Button("✎ Edit");
                private final Button btnDelete = new Button("🗑 Delete");
                private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(10, btnEdit, btnDelete);

                {
                    btnEdit.setStyle("-fx-background-color: transparent; -fx-text-fill: #007BFF; -fx-cursor: hand; -fx-font-weight: bold;");
                    btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: #D13438; -fx-cursor: hand; -fx-font-weight: bold;");

                    // EDIT CLICK
                    btnEdit.setOnAction(event -> {
                        MemberModel data = getTableView().getItems().get(getIndex());
                        handleEditMember(data);
                    });

                    // DELETE CLICK
                    btnDelete.setOnAction(event -> {
                        MemberModel data = getTableView().getItems().get(getIndex());
                        pendingDeleteId = data.getMemberId(); // Remember the ID
                        
                        if (deleteModalMemberName != null) {
                            deleteModalMemberName.setText(data.getFullName() + " (" + data.getMemberId() + ")");
                        }
                        showModal(deleteModalOverlay); // Show the modern popup!
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : pane);
                }
            };
        };
        colActions.setCellFactory(cellFactory);
    }

    private void handleEditMember(MemberModel m) {
        editingMemberId = m.getMemberId(); // Locks the form into EDIT mode
        
        fullNameField.setText(m.getFullName());
        phoneField.setText(m.getPhone());
        emailField.setText(m.getEmail());
        genderCombo.setValue(m.getGender());
        ageField.setText(String.valueOf(m.getAge()));
        heightField.setText(m.getHeight());
        weightField.setText(m.getWeight());
        
        // Disable subscription fields since they are just editing the profile
        addDurationCombo.setDisable(true);
        addStartDatePicker.setDisable(true);
        addExpiryLabel.setText("Editing Profile Only");
        
        showModal(modalOverlay); // Opens the popup
    }
    
    @FXML 
    private void openAddMemberPopup(ActionEvent event) { 
        editingMemberId = null; // Resets the form back to ADD mode
        
        // Re-enable subscription fields
        addDurationCombo.setDisable(false);
        addStartDatePicker.setDisable(false);
        
        // Clear all fields clean
        fullNameField.clear(); phoneField.clear(); emailField.clear(); 
        ageField.clear(); heightField.clear(); weightField.clear();
        genderCombo.getSelectionModel().clearSelection();
        addDurationCombo.getSelectionModel().clearSelection();
        addStartDatePicker.setValue(null);
        addExpiryLabel.setText("Select Date & Plan");
        
        showModal(modalOverlay); 
    }
    @FXML
    private void closeDeletePopup(ActionEvent event) {
        hideModal(deleteModalOverlay);
        pendingDeleteId = null; // Clear memory
    }
    
    @FXML
    private void confirmDeleteAction(ActionEvent event) {
        if (pendingDeleteId != null) {
            try {
                md.delete(pendingDeleteId); // Wipe from database
                refreshTables(); // Update UI
                closeDeletePopup(event); // Hide popup
                System.out.println("✅ Member successfully deleted.");
            } catch (Exception e) {
                System.out.println("❌ Delete failed: " + e.getMessage());
            }
        }
    }
    @FXML
    private void navigateToMembersTab(ActionEvent event) {
        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().select(6); // Index 6 is the Members Tab
        }
    }

    @FXML
    private void navigateToMembershipTab(ActionEvent event) {
        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().select(8); // Index 8 is the Membership Tab
        }
    }
     
    // ==========================================
    // WHATSAPP AUTOMATION LOGIC
    // ==========================================
    private void setupSubscriptionActions() {
        if (colSubAction == null) return;
        
        javafx.util.Callback<TableColumn<SubscriptionModel, Void>, javafx.scene.control.TableCell<SubscriptionModel, Void>> cellFactory = param -> {
            return new javafx.scene.control.TableCell<SubscriptionModel, Void>() {
                private final Button btnWhatsApp = new Button("💬 Send Reminder");
                
                {
                    // Style the button in WhatsApp Green
                    btnWhatsApp.setStyle("-fx-background-color: #25D366; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 6px;");
                    
                   btnWhatsApp.setOnAction(event -> {
                        SubscriptionModel sub = getTableView().getItems().get(getIndex());
                        
                        String phone = "";
                        for (MemberModel m : masterMemberList) {
                            if (m.getMemberId().equals(sub.getMemberId())) {
                                phone = m.getPhone();
                                break;
                            }
                        }
                        
                        if (!phone.isEmpty()) {
                            System.out.println("⏳ Testing API: Sending message to " + phone + "...");
                            // CALLING THE NEW API INSTEAD OF THE WEB BROWSER
                            WhatsAppService.sendReminder(phone, sub.getMemberName(), sub.getExpiryDate());
                        } else {
                            System.out.println("⚠️ No phone number found for this member!");
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        SubscriptionModel sub = getTableView().getItems().get(getIndex());
                        // ONLY show the button if they are expiring in <= 7 days or already expired
                        if (sub.getStatus() != null && (sub.getStatus().equals("Expiring Soon") || sub.getStatus().equals("Expired"))) {
                            setGraphic(btnWhatsApp);
                        } else {
                            setGraphic(null); // Hide button for active members
                        }
                    }
                }
            };
        };
        colSubAction.setCellFactory(cellFactory);
    }

    private void sendWhatsAppReminder(String memberName, String phone, String expiryDate) {
        try {
            // 1. Strip spaces/dashes from the phone number
            String formattedPhone = phone.replaceAll("[^0-9]", "");
            
            // 2. Auto-append India country code (+91) if they only typed 10 digits
            if (formattedPhone.length() == 10) {
                formattedPhone = "91" + formattedPhone; 
            }

            // 3. Draft the automated message (You can customize this text!)
            String message = "Hi " + memberName + ", this is a friendly reminder from AIM Fitness Park! 🏋️‍♂️\n\n"
                           + "Your gym membership is expiring on *" + expiryDate + "*. "
                           + "Please renew soon to keep your fitness streak going! 💪";

            // 4. Encode the text so the URL doesn't break
            String encodedMessage = URLEncoder.encode(message, "UTF-8").replace("+", "%20");

            // 5. Open WhatsApp Desktop App or WhatsApp Web in the default browser
            String waLink = "https://wa.me/" + formattedPhone + "?text=" + encodedMessage;
            Desktop.getDesktop().browse(new URI(waLink));

        } catch (Exception e) {
            System.out.println("❌ Failed to open WhatsApp: " + e.getMessage());
        }
    }
    
    // =========================================================
    // AUTOMATED 7-DAY EXPIRY WHATSAPP DISPATCHER
    // =========================================================
    private void triggerAutomatedWhatsAppScan() {
        // Run on a background thread so the UI never freezes
        new Thread(() -> {
            try {
                java.util.ArrayList<String[]> pendingList = subDao.getPendingExpiriesForReminder();
                
                if (pendingList.isEmpty()) {
                    System.out.println("🤖 WhatsApp Automation: No members need 7-day reminders today.");
                    return;
                }

                System.out.println("🤖 WhatsApp Automation: Found " + pendingList.size() + " member(s) expiring within 7 days.");

                for (String[] record : pendingList) {
                    int subId = Integer.parseInt(record[0]);
                    String name = record[1];
                    String phone = record[2];
                    String expiry = record[3];

                    if (phone != null && !phone.trim().isEmpty()) {
                        boolean sent = WhatsAppService.sendReminder(phone, name, expiry);
                        if (sent) {
                            subDao.markReminderSent(subId);
                        }
                    }
                    // Small delay to prevent bursting API requests
                    Thread.sleep(1000);
                }

            } catch (Exception e) {
                System.out.println("❌ Automation Scan Error: " + e.getMessage());
            }
        }).start();
    }
    @FXML
private void handleImportCSV(javafx.event.ActionEvent event) {
    javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
    fileChooser.setTitle("Select CSV File to Import");
    fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
    
    // Open File Browser
    Stage stage = (Stage) rootPane.getScene().getWindow();
    java.io.File selectedFile = fileChooser.showOpenDialog(stage);
    
    if (selectedFile != null) {
        try {
            md.importFromCSV(selectedFile.getAbsolutePath());
            refreshTables(); // Instantly update dashboard and tables
            
            // Show Success Popup
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Import Successful");
            alert.setHeaderText(null);
            alert.setContentText("✅ Data successfully imported from " + selectedFile.getName());
            alert.showAndWait();
            
        } catch (Exception e) {
            // Show Error Popup
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Import Failed");
            alert.setHeaderText(null);
            alert.setContentText("❌ Error importing data: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
    @FXML private void closeAddMemberPopup(ActionEvent event) { hideModal(modalOverlay); }
    @FXML private void openRenewPopup(ActionEvent event) { showModal(renewModalOverlay); }
    @FXML private void closeRenewPopup(ActionEvent event) { hideModal(renewModalOverlay); }
    @FXML private void openAttendancePopup(ActionEvent event) { showModal(attendanceModalOverlay); }
    @FXML private void closeAttendancePopup(ActionEvent event) { hideModal(attendanceModalOverlay); }


    // ==========================================
    // EXPORT MEMBERS TO CSV
    // ==========================================
    @FXML
    private void handleExportCSV(ActionEvent event) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Export Gym Members to CSV");
        fileChooser.setInitialFileName("AIM_Members_" + java.time.LocalDate.now().toString() + ".csv");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Spreadsheet (*.csv)", "*.csv"));

        Stage stage = (Stage) rootPane.getScene().getWindow();
        java.io.File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                int count = md.exportToCSV(file);
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("CSV Export Successful");
                alert.setHeaderText("Members Database Exported");
                alert.setContentText("✅ Successfully exported " + count + " member records to:\n" + file.getAbsolutePath());
                alert.showAndWait();
            } catch (Exception e) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Export Failed");
                alert.setHeaderText("Could not write CSV");
                alert.setContentText("❌ Error: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    // ==========================================
    // BILLING & TRANSACTIONS LOGIC
    // ==========================================
    private void setupBillingTable() {
        if (paymentsTable == null) return;
        colPayReceiptId.setCellValueFactory(new PropertyValueFactory<>("receiptId"));
        colPayMemberId.setCellValueFactory(new PropertyValueFactory<>("memberId"));
        colPayMemberName.setCellValueFactory(new PropertyValueFactory<>("memberName"));
        colPayPlan.setCellValueFactory(new PropertyValueFactory<>("durationPlan"));
        colPayMode.setCellValueFactory(new PropertyValueFactory<>("paymentMode"));
        colPayAmount.setCellValueFactory(new PropertyValueFactory<>("formattedAmount"));
        colPayDate.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        colPayStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        if (billingModeFilterCombo != null) {
            billingModeFilterCombo.setItems(FXCollections.observableArrayList("All", "Cash", "UPI", "Credit / Debit Card"));
            billingModeFilterCombo.setValue("All");
            billingModeFilterCombo.valueProperty().addListener((obs, oldV, newV) -> filterBillingData());
        }

        if (billingSearchField != null) {
            billingSearchField.textProperty().addListener((obs, oldV, newV) -> filterBillingData());
        }
    }

    private void loadBillingData() {
        try {
            java.util.ArrayList<PaymentModel> list = paymentDao.getAllPayments();
            masterPaymentsList.setAll(list);
            filterBillingData();

            double[] stats = paymentDao.getPaymentStats();
            if (dashTotalRevenue != null) dashTotalRevenue.setText(String.format("₹ %,.2f", stats[0]));
            if (dashCashCollections != null) dashCashCollections.setText(String.format("₹ %,.2f", stats[1]));
            if (dashDigitalCollections != null) dashDigitalCollections.setText(String.format("₹ %,.2f", stats[2]));
            if (dashTotalTransactions != null) dashTotalTransactions.setText(String.valueOf((int) stats[3]));
        } catch (Exception e) {
            System.err.println("Error loading billing data: " + e.getMessage());
        }
    }

    private void filterBillingData() {
        if (paymentsTable == null) return;
        String query = billingSearchField != null && billingSearchField.getText() != null
                ? billingSearchField.getText().trim().toLowerCase() : "";
        String modeFilter = billingModeFilterCombo != null && billingModeFilterCombo.getValue() != null
                ? billingModeFilterCombo.getValue() : "All";

        ObservableList<PaymentModel> filtered = FXCollections.observableArrayList();
        for (PaymentModel p : masterPaymentsList) {
            boolean matchesQuery = query.isEmpty()
                    || p.getReceiptId().toLowerCase().contains(query)
                    || p.getMemberId().toLowerCase().contains(query)
                    || p.getMemberName().toLowerCase().contains(query);

            boolean matchesMode = modeFilter.equals("All")
                    || (modeFilter.equalsIgnoreCase("Cash") && p.getPaymentMode().equalsIgnoreCase("Cash"))
                    || (modeFilter.equalsIgnoreCase("UPI") && p.getPaymentMode().equalsIgnoreCase("UPI"))
                    || (modeFilter.contains("Card") && p.getPaymentMode().toLowerCase().contains("card"));

            if (matchesQuery && matchesMode) {
                filtered.add(p);
            }
        }
        paymentsTable.setItems(filtered);
    }

    @FXML
    private void handleExportBillingCSV(ActionEvent event) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Export Payments & Receipts Ledger to CSV");
        fileChooser.setInitialFileName("AIM_Billing_Ledger_" + java.time.LocalDate.now().toString() + ".csv");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Spreadsheet (*.csv)", "*.csv"));

        Stage stage = (Stage) rootPane.getScene().getWindow();
        java.io.File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                int count = paymentDao.exportPaymentsToCSV(file);
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("CSV Export Successful");
                alert.setHeaderText("Billing Ledger Exported");
                alert.setContentText("✅ Successfully exported " + count + " payment receipts to:\n" + file.getAbsolutePath());
                alert.showAndWait();
            } catch (Exception e) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Export Failed");
                alert.setHeaderText("Could not write CSV");
                alert.setContentText("❌ Error: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void handleExportBillingCSVFromSettings(ActionEvent event) {
        handleExportBillingCSV(event);
    }

    // ==========================================
    // WHATSAPP HUB & NOTIFICATIONS LOGIC
    // ==========================================
    private void setupWhatsAppHubTable() {
        if (whatsAppTable == null) return;
        colWaMemberId.setCellValueFactory(new PropertyValueFactory<>("memberId"));
        colWaName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colWaPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colWaExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        colWaDays.setCellValueFactory(new PropertyValueFactory<>("daysRemaining"));
        colWaStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colWaAction.setCellFactory(param -> new javafx.scene.control.TableCell<>() {
            private final Button sendBtn = new Button("💬 Send");
            {
                sendBtn.setStyle("-fx-background-color: #25D366; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4px; -fx-cursor: hand;");
                sendBtn.setOnAction(evt -> {
                    WhatsAppReminderModel item = getTableView().getItems().get(getIndex());
                    sendSingleWhatsAppReminder(item);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    WhatsAppReminderModel current = getTableView().getItems().get(getIndex());
                    if ("Sent".equalsIgnoreCase(current.getStatus())) {
                        sendBtn.setText("✓ Sent");
                        sendBtn.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4px;");
                    } else {
                        sendBtn.setText("💬 Send");
                        sendBtn.setStyle("-fx-background-color: #25D366; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4px; -fx-cursor: hand;");
                    }
                    setGraphic(sendBtn);
                }
            }
        });
    }

    private void loadWhatsAppHubData() {
        try {
            java.util.ArrayList<WhatsAppReminderModel> list = subDao.getAllUpcomingExpiries();
            masterWhatsAppList.setAll(list);
            if (whatsAppTable != null) whatsAppTable.setItems(masterWhatsAppList);

            int pendingCount = 0;
            int sentCount = 0;
            for (WhatsAppReminderModel m : list) {
                if ("Sent".equalsIgnoreCase(m.getStatus())) sentCount++;
                else pendingCount++;
            }

            if (waExpiringSoonCount != null) waExpiringSoonCount.setText(String.valueOf(pendingCount));
            if (waSentTodayCount != null) waSentTodayCount.setText(String.valueOf(sentCount));
            if (waGatewayStatus != null) waGatewayStatus.setText("🟢 Online / Ready");
        } catch (Exception e) {
            System.err.println("Error loading WhatsApp hub: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefreshWhatsAppHub(ActionEvent event) {
        loadWhatsAppHubData();
    }

    private void sendSingleWhatsAppReminder(WhatsAppReminderModel item) {
        if (item == null) return;
        new Thread(() -> {
            boolean success = WhatsAppService.sendReminder(item.getPhone(), item.getFullName(), item.getExpiryDate());
            if (!success) {
                // Open WhatsApp Web chat fallback
                String customMsg = "Hello " + item.getFullName() + ", friendly reminder from AIM Fitness Park: Your gym membership expires on "
                        + item.getExpiryDate() + ". Please visit reception to renew your plan. Thank you!";
                WhatsAppService.openWhatsAppChat(item.getPhone(), customMsg);
            }
            try {
                subDao.markReminderSent(item.getSubscriptionId());
            } catch (Exception ignored) {}

            javafx.application.Platform.runLater(() -> {
                item.setStatus("Sent");
                if (whatsAppTable != null) whatsAppTable.refresh();
                loadWhatsAppHubData();
            });
        }).start();
    }

    @FXML
    private void handleSendAllWhatsAppReminders(ActionEvent event) {
        new Thread(() -> {
            int sentCount = 0;
            for (WhatsAppReminderModel m : masterWhatsAppList) {
                if (!"Sent".equalsIgnoreCase(m.getStatus())) {
                    boolean ok = WhatsAppService.sendReminder(m.getPhone(), m.getFullName(), m.getExpiryDate());
                    try {
                        subDao.markReminderSent(m.getSubscriptionId());
                        m.setStatus("Sent");
                        sentCount++;
                    } catch (Exception ignored) {}
                    try { Thread.sleep(600); } catch (Exception ignored) {}
                }
            }

            final int totalDispatched = sentCount;
            javafx.application.Platform.runLater(() -> {
                loadWhatsAppHubData();
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("WhatsApp Alerts Dispatched");
                alert.setHeaderText("Batch Reminder Delivery Complete");
                alert.setContentText("✅ Dispatched " + totalDispatched + " automated expiry reminders to upcoming members.");
                alert.showAndWait();
            });
        }).start();
    }

    @FXML
    private void handleSendDirectWhatsApp(ActionEvent event) {
        String phone = waDirectPhoneField != null ? waDirectPhoneField.getText() : "";
        String msg = waDirectMsgField != null ? waDirectMsgField.getText() : "";
        if (phone == null || phone.trim().isEmpty()) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Recipient Required");
            alert.setHeaderText(null);
            alert.setContentText("Please provide a recipient phone number.");
            alert.showAndWait();
            return;
        }
        WhatsAppService.openWhatsAppChat(phone, msg);
    }

    // ==========================================
    // FITNESS & BMI CALCULATOR LOGIC
    // ==========================================
    private void setupFitnessTools() {
        if (calcGenderCombo != null) {
            calcGenderCombo.setItems(FXCollections.observableArrayList("Male", "Female"));
            calcGenderCombo.setValue("Male");
        }
        if (calcActivityCombo != null) {
            calcActivityCombo.setItems(FXCollections.observableArrayList(
                    "Sedentary (office job / little exercise)",
                    "Lightly Active (light exercise 1-3 days/wk)",
                    "Moderately Active (exercise 3-5 days/wk)",
                    "Very Active (hard exercise 6-7 days/wk)"
            ));
            calcActivityCombo.setValue("Moderately Active (exercise 3-5 days/wk)");
        }
        handleCalculateFitness(null);
    }

    @FXML
    private void handleCalculateFitness(ActionEvent event) {
        try {
            double heightCm = calcHeightField != null ? Double.parseDouble(calcHeightField.getText().trim()) : 175.0;
            double weightKg = calcWeightField != null ? Double.parseDouble(calcWeightField.getText().trim()) : 72.0;
            int age = calcAgeField != null ? Integer.parseInt(calcAgeField.getText().trim()) : 26;
            String gender = calcGenderCombo != null && calcGenderCombo.getValue() != null ? calcGenderCombo.getValue() : "Male";
            String activity = calcActivityCombo != null && calcActivityCombo.getValue() != null ? calcActivityCombo.getValue() : "";

            if (heightCm <= 0 || weightKg <= 0 || age <= 0) return;

            double heightM = heightCm / 100.0;
            double bmi = weightKg / (heightM * heightM);

            if (calcBmiScoreLabel != null) {
                calcBmiScoreLabel.setText(String.format("%.1f", bmi));
            }

            String category;
            String badgeStyle;
            if (bmi < 18.5) {
                category = "Underweight";
                badgeStyle = "-fx-background-color: #78350F; -fx-text-fill: #FBBF24; -fx-font-weight: bold; -fx-padding: 5 14; -fx-background-radius: 20px;";
            } else if (bmi < 25.0) {
                category = "Normal / Healthy Weight";
                badgeStyle = "-fx-background-color: #064E3B; -fx-text-fill: #34D399; -fx-font-weight: bold; -fx-padding: 5 14; -fx-background-radius: 20px;";
            } else if (bmi < 30.0) {
                category = "Overweight";
                badgeStyle = "-fx-background-color: #7C2D12; -fx-text-fill: #F97316; -fx-font-weight: bold; -fx-padding: 5 14; -fx-background-radius: 20px;";
            } else {
                category = "Obese";
                badgeStyle = "-fx-background-color: #7F1D1D; -fx-text-fill: #FCA5A5; -fx-font-weight: bold; -fx-padding: 5 14; -fx-background-radius: 20px;";
            }

            if (calcBmiCategoryLabel != null) {
                calcBmiCategoryLabel.setText(category);
                calcBmiCategoryLabel.setStyle(badgeStyle);
            }

            double minIdeal = 18.5 * (heightM * heightM);
            double maxIdeal = 24.9 * (heightM * heightM);
            if (calcIdealWeightLabel != null) {
                calcIdealWeightLabel.setText(String.format("Healthy weight target for height: %.1f kg - %.1f kg", minIdeal, maxIdeal));
            }

            // BMR (Mifflin-St Jeor)
            double bmr;
            if ("Female".equalsIgnoreCase(gender)) {
                bmr = (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161;
            } else {
                bmr = (10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5;
            }
            if (calcBmrLabel != null) {
                calcBmrLabel.setText(String.format("%,d kcal/day", (int) Math.round(bmr)));
            }

            // Multiplier
            double multiplier = 1.55;
            if (activity.contains("Sedentary")) multiplier = 1.2;
            else if (activity.contains("Lightly")) multiplier = 1.375;
            else if (activity.contains("Moderately")) multiplier = 1.55;
            else if (activity.contains("Very")) multiplier = 1.725;

            double calories = bmr * multiplier;
            if (calcCaloriesLabel != null) {
                calcCaloriesLabel.setText(String.format("%,d kcal/day", (int) Math.round(calories)));
            }

            double waterLiters = weightKg * 0.035;
            if (calcWaterLabel != null) {
                calcWaterLabel.setText(String.format("%.1f Liters / day", waterLiters));
            }

        } catch (Exception ignored) {}
    }

    @FXML
    private void handleResetFitness(ActionEvent event) {
        if (calcHeightField != null) calcHeightField.setText("175");
        if (calcWeightField != null) calcWeightField.setText("72");
        if (calcAgeField != null) calcAgeField.setText("26");
        if (calcGenderCombo != null) calcGenderCombo.setValue("Male");
        handleCalculateFitness(null);
    }

    // ==========================================
    // SETTINGS TAB SUB-PANEL NAVIGATION & LOGIC
    // ==========================================
    private void setupSettingsPanels() {
        showSettingsGym(null);
        loadGymProfile();
        loadLicenseAndSystemInfo();
    }

    @FXML
    private void showSettingsGym(ActionEvent event) {
        setSettingsPanelVisible(paneSettingsGym);
        updateSettingsNavSelection(btnSettingsGym);
    }

    @FXML
    private void showSettingsSecurity(ActionEvent event) {
        setSettingsPanelVisible(paneSettingsSecurity);
        updateSettingsNavSelection(btnSettingsSecurity);
    }

    @FXML
    private void showSettingsLicense(ActionEvent event) {
        setSettingsPanelVisible(paneSettingsLicense);
        updateSettingsNavSelection(btnSettingsLicense);
    }

    @FXML
    private void showSettingsData(ActionEvent event) {
        setSettingsPanelVisible(paneSettingsData);
        updateSettingsNavSelection(btnSettingsData);
    }

    private void setSettingsPanelVisible(VBox activePane) {
        VBox[] panes = {paneSettingsGym, paneSettingsSecurity, paneSettingsLicense, paneSettingsData};
        for (VBox p : panes) {
            if (p != null) {
                boolean isCurrent = (p == activePane);
                p.setVisible(isCurrent);
                p.setManaged(isCurrent);
            }
        }
    }

    private void updateSettingsNavSelection(Button activeBtn) {
        Button[] btns = {btnSettingsGym, btnSettingsSecurity, btnSettingsLicense, btnSettingsData};
        for (Button b : btns) {
            if (b != null) {
                if (b == activeBtn) {
                    b.setStyle("-fx-background-color: #FFF8E6; -fx-text-fill: #FF7A00; -fx-font-weight: bold; -fx-alignment: BASELINE_LEFT;");
                } else {
                    b.setStyle("-fx-alignment: BASELINE_LEFT; -fx-border-color: transparent; -fx-background-color: transparent; -fx-text-fill: #475569;");
                }
            }
        }
    }

    private void loadGymProfile() {
        try (java.sql.Connection c = DatabaseSetup.getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement("SELECT * FROM gym_profile WHERE id = 1");
             java.sql.ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                if (cfgGymName != null) cfgGymName.setText(rs.getString("gym_name"));
                if (cfgGymPhone != null) cfgGymPhone.setText(rs.getString("contact_phone"));
                if (cfgGymAddress != null) cfgGymAddress.setText(rs.getString("address"));
                if (cfgGymHours != null) cfgGymHours.setText(rs.getString("operating_hours"));
                if (cfgGymCurrency != null) cfgGymCurrency.setText(rs.getString("currency"));
            }
        } catch (Exception e) {
            System.err.println("Could not load gym profile: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveGymProfile(ActionEvent event) {
        String name = cfgGymName != null ? cfgGymName.getText().trim() : "AIM Fitness Park";
        String phone = cfgGymPhone != null ? cfgGymPhone.getText().trim() : "+91 98765 12345";
        String address = cfgGymAddress != null ? cfgGymAddress.getText().trim() : "";
        String hours = cfgGymHours != null ? cfgGymHours.getText().trim() : "";
        String currency = cfgGymCurrency != null ? cfgGymCurrency.getText().trim() : "₹ INR";

        String sql = "INSERT OR REPLACE INTO gym_profile (id, gym_name, contact_phone, address, operating_hours, currency) "
                + "VALUES (1, ?, ?, ?, ?, ?)";
        try (java.sql.Connection c = DatabaseSetup.getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, address);
            ps.setString(4, hours);
            ps.setString(5, currency);
            ps.executeUpdate();

            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Settings Saved");
            alert.setHeaderText("Gym Profile Updated");
            alert.setContentText("✅ Facility settings and branding preferences saved successfully.");
            alert.showAndWait();
        } catch (Exception e) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Save Failed");
            alert.setHeaderText(null);
            alert.setContentText("❌ Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleUpdateAdminPassword(ActionEvent event) {
        String currentPass = cfgCurrentPass != null ? cfgCurrentPass.getText() : "";
        String newPass = cfgNewPass != null ? cfgNewPass.getText() : "";
        String confirmPass = cfgConfirmPass != null ? cfgConfirmPass.getText() : "";

        if (currentPass == null || currentPass.isBlank()) {
            showErrorAlert("Current Password Required", "Please enter your current administrator password to authorize modifications.");
            return;
        }

        try {
            boolean validCurrent = AdminDao.authenticate("admin", currentPass.toCharArray());
            if (!validCurrent) {
                showErrorAlert("Authentication Failed", "The current password entered is incorrect. Please try again.");
                return;
            }

            if (newPass == null || newPass.length() < 4) {
                showErrorAlert("Invalid Password", "New password must contain at least 4 characters.");
                return;
            }

            if (!newPass.equals(confirmPass)) {
                showErrorAlert("Password Mismatch", "The new password and confirmation password do not match.");
                return;
            }

            AdminDao.resetAdminPassword("admin", newPass);

            if (cfgCurrentPass != null) cfgCurrentPass.clear();
            if (cfgNewPass != null) cfgNewPass.clear();
            if (cfgConfirmPass != null) cfgConfirmPass.clear();

            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Password Updated");
            alert.setHeaderText("Administrator Credentials Modified");
            alert.setContentText("✅ Your admin credentials have been successfully updated with high-security PBKDF2 cryptography.");
            alert.showAndWait();

        } catch (Exception e) {
            showErrorAlert("Update Error", "Could not update credentials: " + e.getMessage());
        }
    }

    private void loadLicenseAndSystemInfo() {
        if (cfgLicenseStatus != null) cfgLicenseStatus.setText("LIFETIME COMMERCIAL OFFLINE");
        if (cfgAppVersion != null) cfgAppVersion.setText("v1.0.0 Production Enterprise");
        if (cfgWorkstationHost != null) {
            String os = System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")";
            String user = System.getProperty("user.name");
            cfgWorkstationHost.setText(user + " @ " + os);
        }
        if (cfgDbPath != null) {
            java.nio.file.Path p = DatabaseSetup.getActiveDatabasePath();
            cfgDbPath.setText(p != null ? p.toString() : "~/AimFitnessData/aimfitness.db");
        }
    }

    private void showErrorAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
