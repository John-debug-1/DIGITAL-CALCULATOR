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

 /**
     * '.' → προσθέτει δεκαδικό στο τρέχον αριθμητικό token (μία φορά ανά token).
     * Αν δεν υπάρχει κάτι, ξεκινά "0.".
     */
    @FXML private void onDot() {
        String t = getDisplay();
        if ("Error".equals(t) || t.isEmpty()) {
            setDisplay("0.");
            return;
        }
        int i = t.length() - 1;
        while (i >= 0 && (Character.isDigit(t.charAt(i)))) i--;
        if (i >= 0 && t.charAt(i)=='.') return;
        if (i >= 0 && t.charAt(i)=='.') return;
        String lastToken = t.substring(i + 1);
        if (lastToken.contains(".")) return;
        if (t.endsWith(")")) {
            setDisplay(t + "*0.");
        } else {
            setDisplay(t + (lastToken.isEmpty()? "0." : "."));
        }
    }

    /**
     *  Τελεστές (+, −, ×, ÷, ^) → μπαίνουν στο input.
     * Αν ήδη τελειώνει σε τελεστή, τον αντικαθιστά.
     */
    @FXML private void onOp(javafx.event.ActionEvent e) {
        String sign = ((Button)e.getSource()).getText();
        String op = normalizeOp(sign);
        String t = getDisplay();
        if ("Error".equals(t) || t.isEmpty()) t = "0";
        if (!t.isEmpty() && isOpChar(lastChar(t))) {
            t = t.substring(0, t.length()-1);
        }
        setDisplay(t + op);
    }

    /** "(" → προσθέτει παρένθεση. */
    @FXML private void onParenOpen() {
        String t = getDisplay();
        if ("Error".equals(t) || t.equals("0")) t = "";
        if (!t.isEmpty() && (Character.isDigit(lastChar(t)) || lastChar(t)==')')) {
            t += "*";
        }
        setDisplay(t + "(");
    }

    /**  ")" → προσθέτει παρένθεση. */
    @FXML private void onParenClose() {
        String t = getDisplay();
        if ("Error".equals(t) || t.isEmpty()) return;
        setDisplay(t + ")");
    }

  /**
     +/- → αλλάζει πρόσημο.
     */
    @FXML private void onToggleSign() {
        String t = getDisplay();
        if ("Error".equals(t) || t.isEmpty() || t.equals("0")) {
            setDisplay("-");
            return;
        }
        try {
            BigDecimal val = new BigDecimal(t);
            show(val.negate());
            return;
        } catch (Exception ignore) { /* not a pure number */ }

        int end = t.length()-1;
        while (end>=0 && Character.isWhitespace(t.charAt(end))) end--;
        if (end<0 || isOpChar(t.charAt(end)) || t.charAt(end)=='(') {
            setDisplay(t + "-");
            return;
        }
        int start = end;
        while (start>=0 && (Character.isDigit(t.charAt(start)) || t.charAt(start)=='.')) start--;
        start++;
        if (start<=end) {
            String num = t.substring(start, end+1);
            if (start>0 && t.charAt(start-1)=='-' &&
                    (start-1==0 || t.charAt(start-2)=='(' || isOpChar(t.charAt(start-2)))) {
                setDisplay(t.substring(0, start-1) + t.substring(start));
            } else {
                String replaced = t.substring(0, start) + "(0-" + num + ")" + t.substring(end+1);
                setDisplay(replaced);
            }
        }
    }

    /** Backspace -> αφαιρεί τον τελευταίο χαρακτήρα του input στην οθόνη (ή επιστρέφει σε "0"). */
    @FXML private void onBackspace() {
        String t = getDisplay();
        if (t == null || t.equals("Error") || t.isEmpty()) {
            setDisplay("0");
            return;
        }
        t = t.substring(0, t.length()-1);
        if (t.isEmpty()) t = "0";
        setDisplay(t);
    }
