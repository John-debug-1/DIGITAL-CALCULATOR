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

    @FXML private void onClear() { setDisplay("0"); }

    /**
     * Εμφανίζει αποτέλεσμα
     * Εμφανίζει μύνημα Εrror
     */
    @FXML private void onEquals() {
        String expr = getDisplay();
        try {
            BigDecimal result = evaluate(expr);
            show(result);
        } catch (ArithmeticException ae) {
            if (ae.getMessage()!=null && ae.getMessage().toLowerCase().contains("zero")) {
                showErrorAlert("Σφάλμα: Διαίρεση με μηδέν δεν επιτρέπεται.");
            } else {
                showErrorAlert("Μαθηματικό σφάλμα στην έκφραση.");
            }
        } catch (Exception ex) {
            showErrorAlert("Μη έγκυρη έκφραση.");
        }
    }

    // ---------------- Μνήμη ----------------

    /**
     * M+ : προσθέτει στην μνήμη την τιμή της οθόνης.
     */
    @FXML private void onMPlus() {
        String t = getDisplay();
        BigDecimal add;
        try {
            add = new BigDecimal(t);
        } catch (Exception e) {
            try { add = evaluate(t); }
            catch (Exception ex) { return; }
        }
        mem = mem.add(add, MC);
        updateMemLabel();
    }

    /** M- : καθαρίζει τη μνήμη (μηδενισμός). */
    @FXML private void onMC() {
        mem = BigDecimal.ZERO;
        updateMemLabel();
    }

    // ----------------Λειτουργία OFF ----------------

    /** OFF : κλείνει το κύριο παράθυρο (Stage). */
    @FXML private void onOff() {
        Stage st = (Stage) display.getScene().getWindow();
        st.close();
    }

    /** Αρχικοποίηση controller: ενημερώνει την ένδειξη μνήμης. */
    @FXML private void initialize() { updateMemLabel(); }

    // ---------------- Εργαλεία parser / evaluator (για προτεραιότητα πράξεων, σωστή λειτουργία παρενθέσεων και λειτουργία δυνάμεων ----------------

    private char lastChar(String s) { return s.charAt(s.length()-1); }

    private boolean isOpChar(char c) {
        return c=='+' || c=='-' || c=='*' || c=='/' || c=='^';
    }

    private String normalizeExpr(String s) {
        return s.replace('×','*').replace('÷','/').replace('−','-')
                .replaceAll("\\s+",""); // χωρίς κενά
    }
    /**
     * Χαρτογραφεί το κείμενο κουμπιών
     */
    private String normalizeOp(String sign) {
        return switch (sign) {
            case "＋", "+" -> "+";
            case "−", "-" -> "-";
            case "×", "x", "*" -> "*";
            case "÷", "/" -> "/";
            case "^" -> "^";
            default -> sign;
        };
    }
    private BigDecimal evaluate(String expr) {
        String s = normalizeExpr(expr);
        if (s.isEmpty() || s.equals("-")) return BigDecimal.ZERO; // Κενή ή μόνο "-" -> 0
        List<String> rpn = toRPN(tokenize(s));
        return evalRPN(rpn);
    }
    /**
     * Χωρίζει μια έκφραση σε tokens (αριθμοί, παρενθέσεις, τελεστές, unary "-").
     */
    private List<String> tokenize(String s) {
        List<String> tokens = new ArrayList<>();
        int i = 0;
        String prev = null;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (Character.isDigit(c) || c=='.') {
                int j = i+1;
                while (j < s.length() && (Character.isDigit(s.charAt(j)) || s.charAt(j)=='.')) j++;
                tokens.add(s.substring(i,j));
                prev = "num";
                i = j;
            } else if (c=='(' || c==')') {
                tokens.add(String.valueOf(c));
                prev = String.valueOf(c);
                i++;
            } else if (isOpChar(c)) {
                if (c=='-' && (prev==null || "op".equals(prev) || "(".equals(prev))) {
                    tokens.add("u-");
                } else {
                    tokens.add(String.valueOf(c));
                }
                prev = "op";
                i++;
            } else {
                throw new IllegalArgumentException("Bad char: " + c);
            }
        }
        return tokens;
    }
    /** Προτεραιότητα τελεστών. */
    private int prec(String op) {
        // Προτεραιότητες: u- (υψηλή), ^, μετά * /, μετά + -
        return switch (op) {
            case "u-" -> 4;
            case "^"  -> 3;
            case "*", "/" -> 2;
            case "+", "-" -> 1;
            default -> 0;
        };
    }
    private boolean rightAssoc(String op) {
        return op.equals("^") || op.equals("u-");
    }
    private List<String> toRPN(List<String> tokens) {
        List<String> out = new ArrayList<>();
        Deque<String> ops = new ArrayDeque<>();
        for (String t : tokens) {
            switch (t) {
                case "+" , "-" , "*" , "/" , "^" , "u-" -> {
                    while (!ops.isEmpty()) {
                        String top = ops.peek();
                        if (top.equals("(")) break;
                        int pTop = prec(top), pCur = prec(t);
                        if (pTop > pCur || (pTop == pCur && !rightAssoc(t))) {
                            out.add(ops.pop());
                        } else break;
                    }
                    ops.push(t);
                }
                case "(" -> ops.push(t);
                case ")" -> {
                    while (!ops.isEmpty() && !ops.peek().equals("(")) {
                        out.add(ops.pop());
                    }
                    if (ops.isEmpty() || !ops.peek().equals("(")) {
                        throw new IllegalArgumentException("Unbalanced ')'");
                    }
                    ops.pop();
                }
                default -> {
                    out.add(t);
                }
            }
        }
        while (!ops.isEmpty()) {
            String op = ops.pop();
            if (op.equals("(") || op.equals(")"))
                throw new IllegalArgumentException("Unbalanced '('");
            out.add(op);
        }
        return out;
    }
    /**
     * Υπολογίζει δύναμη x^y:
     */
    private BigDecimal pow(BigDecimal base, BigDecimal exp) {
        try {
            if (exp.stripTrailingZeros().scale() <= 0) {
                int n = exp.intValue();
                if (n >= 0) return base.pow(n, MC);
                BigDecimal p = base.pow(-n, MC);
                if (p.compareTo(BigDecimal.ZERO)==0)
                    throw new ArithmeticException("division by zero");
                return BigDecimal.ONE.divide(p, MC);
            } else {
                double r = Math.pow(base.doubleValue(), exp.doubleValue());
                if (Double.isNaN(r) || Double.isInfinite(r))
                    throw new ArithmeticException("invalid pow");
                return BigDecimal.valueOf(r);
            }
        } catch (ArithmeticException ex) { throw ex; }
        catch (Exception ex) { throw new ArithmeticException("invalid pow"); }
    }

    /**
     * Λειτουργία πράξεων
     */
    private BigDecimal evalRPN(List<String> rpn) {
        Deque<BigDecimal> st = new ArrayDeque<>();
        for (String t : rpn) {
            switch (t) {
                case "+" -> st.push(st.pop().add(st.pop(), MC));          // a+b
                case "-" -> {
                    BigDecimal b = st.pop(), a = st.pop();                // a-b
                    st.push(a.subtract(b, MC));
                }
                case "*" -> st.push(st.pop().multiply(st.pop(), MC));     // a*b
                case "/" -> {
                    BigDecimal b = st.pop(), a = st.pop();                // a/b
                    if (b.compareTo(BigDecimal.ZERO)==0)                  // Έλεγχος διαίρεσης με μηδέν
                        throw new ArithmeticException("division by zero");
                    st.push(a.divide(b, MC));
                }
                case "^" -> {
                    BigDecimal b = st.pop(), a = st.pop();                // a^b
                    st.push(pow(a, b));
                }
                case "u-" -> st.push(st.pop().negate());
                default -> st.push(new BigDecimal(t));
            }
        }
        if (st.size()!=1)
            throw new IllegalArgumentException("Bad expression");
        return st.pop();                                                 // Επιστρέφει το τελικό αποτέλεσμα
    }
}
