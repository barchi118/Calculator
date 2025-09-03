package calculate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class cul2 {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("\n計算式を入力してください: ");
        String expression = reader.readLine();

        try {
            int result = calculate(expression);
            System.out.println("計算結果: " + result);
        } catch (Exception e) {
            System.err.println("エラー: 式の形式が正しくないか、計算できませんでした。");
        }
    }

    /**
     * 計算のメイン処理
     */
    private static int calculate(String expression) {
        // [正規表現を使わない空白除去]
        StringBuilder expressionWithoutSpaces = new StringBuilder();
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (!Character.isWhitespace(c)) {
                expressionWithoutSpaces.append(c);
            }
        }

        // 1. 式をトークンに分解する
        List<String> tokens = store(expressionWithoutSpaces.toString());

        // 2. 逆ポーランド記法(RPN)に変換する
        List<String> rpn = convertToRPN(tokens);

        // 3. RPNを計算する
        return calculateRPN(rpn);
    }

    /**
     * [ステップ1] 数字と演算子に分ける（負の数に対応）
     */
    private static List<String> store(String expression) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentNumber = new StringBuilder();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (isNumber(c)) {
                currentNumber.append(c);
            } else {
                if (currentNumber.length() > 0) {
                    tokens.add(currentNumber.toString());
                    currentNumber.setLength(0);
                }
                if (c == '-') {
                    if (tokens.isEmpty() || isOperator(tokens.get(tokens.size() - 1))) {
                        currentNumber.append('-');
                    } else {
                        tokens.add(String.valueOf(c));
                    }
                } else {
                    tokens.add(String.valueOf(c));
                }
            }
        }
        if (currentNumber.length() > 0) {
            tokens.add(currentNumber.toString());
        }
        return tokens;
    }

    /**
     * ★★★ ここがRPN関連のメソッドです (1/2) ★★★ [ステップ2] トークンリストを逆ポーランド記法(RPN)に変換する
     */
    private static List<String> convertToRPN(List<String> tokens) {
        List<String> outputQueue = new ArrayList<>();
        Deque<String> operatorStack = new ArrayDeque<>();

        for (String token : tokens) {
            if (isNumber(token)) {
                outputQueue.add(token);
            } else if (isOperator(token)) {
                while (!operatorStack.isEmpty()
                        && priority(operatorStack.peek()) >= priority(token)) {
                    outputQueue.add(operatorStack.pop());
                }
                operatorStack.push(token);
            }
        }
        while (!operatorStack.isEmpty()) {
            outputQueue.add(operatorStack.pop());
        }
        return outputQueue;
    }

    /**
     * ★★★ ここがRPN関連のメソッドです (2/2) ★★★ [ステップ3] 逆ポーランド記法のリストを計算する
     */
    private static int calculateRPN(List<String> rpn) {
        Deque<Integer> calculationStack = new ArrayDeque<>();

        for (String token : rpn) {
            if (isNumber(token)) {
                calculationStack.push(Integer.parseInt(token));
            } else if (isOperator(token)) {
                int val2 = calculationStack.pop();
                int val1 = calculationStack.pop();
                int result = 0;

                switch (token) {
                    case "+":
                        result = val1 + val2;
                        break;
                    case "-":
                        result = val1 - val2;
                        break;
                    case "*":
                        result = val1 * val2;
                        break;
                    case "/":
                        result = val1 / val2;
                        break;
                }
                calculationStack.push(result);
            }
        }
        return calculationStack.pop();
    }

    // --- 以下はヘルパーメソッド群 ---

    private static int priority(String operator) {
        switch (operator) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
                return 2;
            default:
                return 0;
        }
    }

    private static boolean isNumber(Character number) {
        return Character.isDigit(number);
    }

    private static boolean isNumber(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isOperator(String str) {
        return str.equals("+") || str.equals("-") || str.equals("*") || str.equals("/");
    }
}
