package calculate;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Scanner;

public class Cul5{

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("計算式を入力してください（'exit'で終了）");
            try {
                String expression = scanner.nextLine();
                if ("exit".equalsIgnoreCase(expression)) { break; }
                if (expression.trim().isEmpty()) { continue; }
                System.out.println("計算結果: " + calculate(expression));
            } catch (Exception e) {
                System.err.println("エラー: " + e.getMessage());
            }
        }
        scanner.close();
    }

    public static int calculate(String expression) throws MyException {
        List<String> tokens = splitFormula(expression);
        Deque<String> rpn = arithmetic(tokens);
        return arithmeticResult(rpn);
    }

    // ★ 変更点
    private static List<String> splitFormula(String expression) {
        List<String> tokens = new ArrayList<>();
        StringBuilder numBuffer = new StringBuilder();
        expression = expression.replaceAll("\\s+", "");

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            boolean isUnaryContext = (i == 0) || "+-*/(".contains(String.valueOf(expression.charAt(i - 1)));

            if (Character.isDigit(c)) {
                numBuffer.append(c);
            } else {
                if (numBuffer.length() > 0) {
                    tokens.add(numBuffer.toString());
                    numBuffer.setLength(0);
                }

                if (c == '-' && isUnaryContext) {
                    // 単項マイナスは特別な演算子 "u-" として追加
                    tokens.add("u-");
                } else if (!(c == '+' && isUnaryContext)) {
                    tokens.add(String.valueOf(c));
                }
            }
        }

        if (numBuffer.length() > 0) {
            tokens.add(numBuffer.toString());
        }
        return tokens;
    }

    private static Deque<String> arithmetic(List<String> stockList) {
        Deque<String> operatorsDeque = new ArrayDeque<>();
        Deque<String> rpnDeque = new ArrayDeque<>();

        for (String element : stockList) {
            if (isNumeric(element)) {
                rpnDeque.add(element);
            } else if ("(".equals(element)) {
                operatorsDeque.push(element);
            } else if (")".equals(element)) {
                while (!operatorsDeque.isEmpty() && !"(".equals(operatorsDeque.peek())) {
                    rpnDeque.add(operatorsDeque.pop());
                }
                if (!operatorsDeque.isEmpty()) { operatorsDeque.pop(); }
            } else {
                while (!operatorsDeque.isEmpty() && priority(operatorsDeque.peek()) >= priority(element)) {
                    rpnDeque.add(operatorsDeque.pop());
                }
                operatorsDeque.push(element);
            }
        }

        while (!operatorsDeque.isEmpty()) {
            rpnDeque.add(operatorsDeque.pop());
        }
        return rpnDeque;
    }

    // ★ 変更点
    private static int arithmeticResult(Deque<String> rpnDeque) throws MyException {
        Deque<String> processDeque = new ArrayDeque<>();
        for (String element : rpnDeque) {
            if (isOperator(element)) {
                // "u-" (単項演算子) の場合
                if ("u-".equals(element)) {
                    if (processDeque.isEmpty()) throw new MyException("式の構成が不正です。");
                    int operand = Integer.parseInt(processDeque.pop());
                    processDeque.push(String.valueOf(-operand)); // 数値を1つだけ取り出し、符号を反転させる
                } else {
                    // それ以外の二項演算子の場合
                    if (processDeque.size() < 2) throw new MyException("式の構成が不正です。");
                    int right = Integer.parseInt(processDeque.pop());
                    int left = Integer.parseInt(processDeque.pop());
                    processDeque.push(calculating(left, right, element));
                }
            } else {
                processDeque.push(element);
            }
        }
        if (processDeque.size() != 1) throw new MyException("式の構成が不正です。");
        return Integer.parseInt(processDeque.pop());
    }

    private static String calculating(int left, int right, String operator) throws MyException {
        long result = 0;
        switch (operator) {
            case "+": result = (long) left + right; break;
            case "-": result = (long) left - right; break;
            case "*": result = (long) left * right; break;
            case "/":
                if (right == 0) throw new MyException("0による除算はできません。");
                result = (long) left / right;
                break;
        }
        if (result > Integer.MAX_VALUE || result < Integer.MIN_VALUE) {
            throw new MyException("計算結果がオーバーフローしました。");
        }
        return String.valueOf(result);
    }

    // ★ 変更点
    private static int priority(String operator) {
        switch (operator) {
            case "u-": return 3; // 'u-' は乗除算より優先度が高い
            case "*": case "/": return 2;
            case "+": case "-": return 1;
            default: return 0;
        }
    }

    private static boolean isNumeric(String str) {
        try { Integer.parseInt(str); return true; } catch (NumberFormatException e) { return false; }
    }

    // ★ 変更点
    private static boolean isOperator(String operator) {
        // "u-"も演算子として認識させる
        return "+-*/u-".contains(operator);
    }
}

class MyException extends Exception {
    public MyException(String message) { super(message); }
}
