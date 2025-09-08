package calculate;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import java.io.IOException;
import java.util.*;

public class Cul4{

    /**
     * 実行メソッド
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("計算式を入力してください（'exit'で終了）");
            try {
                String expression = scanner.nextLine();
                if ("exit".equalsIgnoreCase(expression)) {
                    System.out.println("電卓を終了します。");
                    break;
                }
                if (expression.trim().isEmpty()) {
                    continue;
                }
                System.out.println("計算結果: " + calculate(expression));
            } catch (MyException e) {
                System.err.println("エラー: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("予期せぬエラーが発生しました: " + e.getMessage());
            }
        }
        scanner.close();
    }

    /**
     * 計算のメイン処理
     */
    private static int calculate(String expression) throws MyException {
        List<String> storeFormulaList = splitFormula(expression);
        Deque<String> rpnDeque = arithmetic(storeFormulaList);
        return arithmeticResult(rpnDeque);
    }

    /**
     * 数式をトークン（数値や演算子）に分解するメソッド（バグ修正・簡易化版）
     */
    private static List<String> splitFormula(String expression) throws MyException {
        List<String> tokens = new ArrayList<>();
        StringBuilder numBuffer = new StringBuilder();
        expression = expression.replaceAll("\\s+", "");

        if (expression.isEmpty()) {
            throw new MyException("式が入力されていません。");
        }

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c)) {
                numBuffer.append(c);
            } else if (c == '-' && i == 0) {
                // 式の先頭の'-'は負の符号として扱う
                numBuffer.append(c);
            } else if (isOperator(String.valueOf(c))) {
                // 演算子が見つかった
                if (numBuffer.length() == 0 || "-".equals(numBuffer.toString())) {
                    // 「5*+3」のように演算子が連続した場合や、「*5」のように不正に始まる場合
                    throw new MyException("演算子が連続しているか、式の開始文字が不正です。");
                }
                tokens.add(numBuffer.toString()); //溜めていた数字をリストに追加
                numBuffer.setLength(0);           //バッファをクリア
                tokens.add(String.valueOf(c));  //演算子をリストに追加
            } else {
                // 数字でも演算子でもない不正な文字
                throw new MyException("式に不正な文字が含まれています: " + c);
            }
        }

        // ループ終了後にバッファに残っている最後の数字を追加
        if (numBuffer.length() > 0) {
            if ("-".equals(numBuffer.toString())) {
                throw new MyException("式が演算子で終わっています。");
            }
            tokens.add(numBuffer.toString());
        } else if (!tokens.isEmpty()) {
            // 式が「5+」のように演算子で終わっている場合
            throw new MyException("式が演算子で終わっています。");
        }
        
        return tokens;
    }

    /**
     * 演算子の優先順位を考慮するため逆ポーランド記法に変換する
     */
    private static Deque<String> arithmetic(List<String> stockList) {
        Deque<String> stockDeque = new LinkedList<>(stockList);
        Deque<String> operatorsDeque = new ArrayDeque<>();
        Deque<String> rpnDeque = new ArrayDeque<>();

        for (String element : stockDeque) {
            if (isOperator(element)) {
                while (!operatorsDeque.isEmpty() && priority(operatorsDeque.peek()) >= priority(element)) {
                    rpnDeque.add(operatorsDeque.pop());
                }
                operatorsDeque.push(element);
            } else { // 数字の場合
                rpnDeque.add(element);
            }
        }

        while (!operatorsDeque.isEmpty()) {
            rpnDeque.add(operatorsDeque.pop());
        }
        return rpnDeque;
    }

    /**
     * 逆ポーランド記法にしたリストを計算して結果を返す
     */
    private static int arithmeticResult(Deque<String> rpnDeque) throws MyException {
        Deque<String> processDeque = new ArrayDeque<>();
        for (String element : rpnDeque) {
            if (isOperator(element)) {
                if (processDeque.size() < 2) throw new MyException("式の構成が不正です。");
                int right = Integer.parseInt(processDeque.pop());
                int left = Integer.parseInt(processDeque.pop());
                processDeque.push(calculating(left, right, element));
            } else {
                processDeque.push(element);
            }
        }
        if (processDeque.size() != 1) throw new MyException("式の構成が不正です。");
        return Integer.parseInt(processDeque.pop());
    }

    /**
     * 演算子を判定して計算する
     */
    private static String calculating(int left, int right, String operator) throws MyException {
        long result = 0;
        switch (operator) {
            case "+": result = (long) left + right; break;
            case "-": result = (long) left - right; break;
            case "*": result = (long) left * right; break; // ★順序を修正
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

    /**
     * 演算子ごとの優先順位を取得する
     */
    private static int priority(String operator) {
        switch (operator) {
            case "+": case "-": return 1;
            case "*": case "/": return 2;
            default: return 0;
        }
    }

    /**
     * 演算子判定
     */
    private static boolean isOperator(String operator) {
        return "+-*/".contains(operator) && operator.length() == 1;
    }
}

// MyExceptionクラス（元のコードのまま）
class MyException extends Exception {
    public MyException(String message) {
        super(message);
    }
}