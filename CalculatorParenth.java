package calculate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class CalculatorParenth {

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("\n計算式を入力してください (exitで終了): ");
            String expression = reader.readLine();

            if (expression == null || expression.equalsIgnoreCase("exit")) {
                System.out.println("電卓を終了します。");
                break;
            }

            try {
                int result = calculate(expression);
                System.out.println("計算結果: " + result);
            } catch (SyntaxException e) { // ★★★ 文法エラーを個別にキャッチ ★★★
                System.err.println("文法エラー: " + e.getMessage());
            } catch (CalculatorException e) {
                System.err.println("計算エラー: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("予期せぬエラーが発生しました: " + e.getMessage());
            }
        }
    }

    public static int calculate(String expression) {
        String sanitizedExpression = expression.replaceAll("\\s+", "");
        validate(sanitizedExpression);
        List<String> rpn = toRpn(sanitizedExpression);
        return evalRpn(rpn);
    }
    
    private static void validate(String expression) {
        if (expression.isEmpty()) {
            throw new SyntaxException("式が空です。");
        }
        if (!expression.matches("^[\\d\\+\\-\\*\\/()]+$")) {
            throw new CalculatorException("式に数字、演算子、括弧以外の文字が含まれています。");
        }
        if (expression.matches("^[+*/].*") || expression.endsWith("+") || expression.endsWith("-") || expression.endsWith("*") || expression.endsWith("/")) {
            throw new SyntaxException("式の先頭または末尾の記号が不正です。");
        }
        if (expression.matches(".*[+*/-]{2,}.*")) {
            throw new SyntaxException("演算子が連続しています。");
        }
        
        // ★★★ ここからが括弧に関する新しい例外チェック ★★★
        long openBrackets = expression.chars().filter(ch -> ch == '(').count();
        long closeBrackets = expression.chars().filter(ch -> ch == ')').count();
        if (openBrackets != closeBrackets) {
            throw new SyntaxException("括弧の数が一致しません。");
        }
        if (expression.contains("()")) {
            throw new SyntaxException("空の括弧が含まれています。");
        }
        if (expression.matches(".*\\([+*/].*") || expression.matches(".*[+*/-]\\).*")) {
            throw new SyntaxException("括弧の使い方が不正です（例: (*5) や (5+) ）。");
        }
    }

    private static List<String> toRpn(String expression) {
        List<String> rpnQueue = new ArrayList<>();
        Deque<String> opStack = new ArrayDeque<>();
        String[] tokens = expression.split("(?<=[-+*/()])|(?=[-+*/()])");

        List<String> mergedTokens = new ArrayList<>();
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals("-") && (i == 0 || "+-*/(".contains(tokens[i - 1]))) {
                mergedTokens.add(tokens[i] + tokens[i + 1]);
                i++;
            } else {
                mergedTokens.add(tokens[i]);
            }
        }

        for (String token : mergedTokens) {
            if (isNumeric(token)) {
                rpnQueue.add(token);
            } else if (token.equals("(")) {
                opStack.push(token);
            } else if (token.equals(")")) {
                while (!opStack.isEmpty() && !opStack.peek().equals("(")) {
                    rpnQueue.add(opStack.pop());
                }
                if (opStack.isEmpty()) { // 開き括弧が見つからなかった
                    throw new SyntaxException("対応する開き括弧がありません。");
                }
                opStack.pop(); // 開き括弧を捨てる
            } else {
                while (!opStack.isEmpty() && getPriority(opStack.peek()) >= getPriority(token)) {
                    rpnQueue.add(opStack.pop());
                }
                opStack.push(token);
            }
        }
        
        while (!opStack.isEmpty()) {
            // ★★★ 追加: スタックに開き括弧が残っていたらエラー ★★★
            if (opStack.peek().equals("(")) {
                throw new SyntaxException("対応する閉じ括弧がありません。");
            }
            rpnQueue.add(opStack.pop());
        }
        return rpnQueue;
    }

    // evalRpn, getPriority, isNumeric メソッドは変更なし
    private static int evalRpn(List<String> rpn) {
        Deque<Long> stack = new ArrayDeque<>();
        for (String token : rpn) {
            if (isNumeric(token)) {
                stack.push(Long.parseLong(token));
            } else {
                if (stack.size() < 2) throw new SyntaxException("式の形式が正しくありません。");
                long val2 = stack.pop();
                long val1 = stack.pop();
                long result;
                switch (token) {
                    case "+": result = val1 + val2; break;
                    case "-": result = val1 - val2; break;
                    case "*": result = val1 * val2; break;
                    case "/":
                        if (val2 == 0) throw new CalculatorException("0による除算は許可されていません。");
                        result = val1 / val2;
                        break;
                    default: throw new CalculatorException("不明な演算子です: " + token);
                }
                if (result > Integer.MAX_VALUE || result < Integer.MIN_VALUE) {
                    throw new CalculatorException("計算結果がint型の範囲を超えました。");
                }
                stack.push(result);
            }
        }
        if (stack.size() != 1) throw new SyntaxException("式の最終形式が正しくありません。");
        return stack.pop().intValue();
    }
    
    private static int getPriority(String op) {
        switch (op) { case "+", "-": return 1; case "*", "/": return 2; default: return 0; }
    }

    private static boolean isNumeric(String str) {
        return str.matches("-?\\d+");
    }
}