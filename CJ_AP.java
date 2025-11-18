package com.digitalcalculator;

/** βιβλιοθήκες */
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

public class CalculatorController {

    // ---------------- FXML references ----------------
    @FXML private TextField display;           // “οθόνη” του calculator (TextField από FXML)
    @FXML private Label memLabel;              // Ετικέτα ένδειξης μνήμης

    // ---------------- Κατάσταση μνήμης ----------------
    private BigDecimal mem = BigDecimal.ZERO;

    // ---------------- Ρυθμίσεις αριθμητικών πράξεων ----------------
    private final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);
    // Ακρίβεια 10 σημαντικών ψηφίων

    // ---------------- Βοηθητικά οθόνης/μνήμης ----------------

    private void setDisplay(String s) { display.setText(s); }

    private String getDisplay() { return Optional.ofNullable(display.getText()).orElse(""); }

    private void show(BigDecimal v) {
        String s = v.stripTrailingZeros().toPlainString();
        if (s.equals("-0")) s = "0";
        setDisplay(s);
    }

    /** Εμφανίζει "Error" στην οθόνη. */
    private void showErrorAlert(String message) {
        setDisplay("Error");
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Σφάλμα");
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    /** Ενημερώνει την μνήμη "M=..." */
    private void updateMemLabel() {
        String s = mem.stripTrailingZeros().toPlainString();
        if (s.equals("-0")) s = "0";
        memLabel.setText("M=" + s);
    }

    // ---------------- Κουμπιά εισόδου ----------------

    /**
     * Ψηφία από 0 έως 9
     * Όταν βγάζει "Error" ή "0", ξεκινάει νέο αριθμό
     */
    @FXML private void onDigit(javafx.event.ActionEvent e) {
        String d = ((Button)e.getSource()).getText();
        String t = getDisplay();
        if ("Error".equals(t) || "0".equals(t)) t = "";
        setDisplay(t + d);
    }

}
