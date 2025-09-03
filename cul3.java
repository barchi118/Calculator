package calculate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class cul3 {

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("\n計算式を入力してください: ");
        String expression = reader.readLine();

        // ★★★ catchブロックをカスタム例外用に変更 ★★★
        try {
            int result = calculate(expression);
            System.out.println("計算結果: " + result);
        } catch (SyntaxException e) {
            System.err.println("構文エラー: " + e.getMessage());
        } catch (ArithmeticLogicException e) {
            System.err.println("計算エラー: " + e.getMessage());
        } catch (Exception e) {
            // 予期せぬその他のエラー
            System.err.println("予期せぬエラーが発生しました。");
            e.printStackTrace();
        }
    }

    /**
     * 計算のメイン処理 (throws句でカスタム例外を投げる可能性があることを宣言)
     */
    private static int calculate(String expression) throws CalculatorException {
        StringBuilder expressionWithoutSpaces = new StringBuilder();
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (!Character.isWhitespace(c)) {
                expressionWithoutSpaces.append(c);
            }
        }
        List<String> tokens = store(expressionWithoutSpaces.toString());
        List<String> rpn = convertToRPN(tokens);
        return calculateRPN(rpn);
    }

    /**
     * [ステップ1] トークン化 (不正な文字のチェックを追加)
     */
    private static List<String> store(String expression) throws SyntaxException {
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
                } else if (isOperator(String.valueOf(c))) { // +, *, / の場合
                    tokens.add(String.valueOf(c));
                } else {
                    // ★★★ 不正な文字を検出して例外をthrow ★★★
                    throw new SyntaxException("'" + c + "' は不正な文字です。");
                }
            }
        }
        if (currentNumber.length() > 0) {
            tokens.add(currentNumber.toString());
        }
        return tokens;
    }

    /**
     * [ステップ2] RPNへの変換 (変更なし)
     */
    private static List<String> convertToRPN(List<String> tokens) {
        // (このメソッドはエラーチェックが不要なので変更なし)
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
     * [ステップ3] RPNの計算 (ゼロ除算と式の不整合チェックを追加)
     */
    private static int calculateRPN(List<String> rpn)
            throws SyntaxException, ArithmeticLogicException {
        Deque<Integer> calculationStack = new ArrayDeque<>();

        for (String token : rpn) {
            if (isNumber(token)) {
                calculationStack.push(Integer.parseInt(token));
            } else if (isOperator(token)) {
                // ★★★ 演算子に対して数字が足りない場合のエラーチェック ★★★
                if (calculationStack.size() < 2) {
                    throw new SyntaxException("演算子に対して数字が不足しています。");
                }
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
                        // ★★★ ゼロ除算のチェック ★★★
                        if (val2 == 0) {
                            throw new ArithmeticLogicException("0による除算はできません。");
                        }
                        result = val1 / val2;
                        break;
                }
                calculationStack.push(result);
            }
        }

        // ★★★ 最後にスタックに数字が多すぎる場合のエラーチェック ★★★
        if (calculationStack.size() != 1) {
            throw new SyntaxException("数字が多すぎるか、式が正しくありません。");
        }
        return calculationStack.pop();
    }


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
