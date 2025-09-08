package calculate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * 電卓実行クラス
 */
public class Main {

    /**
     * 実行メソッド
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        while (true) {
            // 数式を入力
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("計算式を入力してください（括弧も使えます）");

            // 数式の計算
            try {
                System.out.println("計算結果: " + calculate(reader.readLine()));
            } catch (Exception e) { // MyException以外も拾えるように変更
                System.err.println("エラーが発生しました: " + e.getMessage());
            }
        }

    }

    /**
     * 計算のメイン処理
     *
     * @param expression
     * @return
     */
    private static int calculate(String expression) throws MyException {

        // 数字と演算子に分けてリストに格納
        List<String> storeFormulaList = splitFormula(expression);

        // 作ったリストを元に計算
        int result = arithmeticResult(arithmetic(storeFormulaList));
        return result;
    }

    /**
     * 【刷新】数式をトークン（構成単位）に分解するメソッド。
     * どんなに複雑な式の組み合わせでも正しく分解できるようにロジックを再設計。
     */
    private static List<String> splitFormula(String expression) {
        List<String> tokens = new ArrayList<>();
        StringBuilder numBuffer = new StringBuilder();
        expression = expression.replaceAll("\\s+", ""); // 事前に全ての空白を除去

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            // Case 1: 文字が数字の場合
            if (Character.isDigit(c)) {
                numBuffer.append(c);
                continue; // 次の文字へ（数字が続く可能性があるため）
            }

            // Case 2: 文字が数字でない場合
            // まず、それまでバッファに溜まっていた数字があればリストに確定させる
            if (numBuffer.length() > 0) {
                tokens.add(numBuffer.toString());
                numBuffer.setLength(0); // バッファをクリア
            }

            // 次に、現在の文字(c)を処理する
            // 「単項演算子」が登場する文脈かどうかを判定
            boolean isUnaryContext = tokens.isEmpty() ||
                    isOperator(tokens.get(tokens.size() - 1)) ||
                    "(".equals(tokens.get(tokens.size() - 1));

            if (c == '-' && isUnaryContext) {
                // 単項マイナスなら、「0」を追加して「0 - ...」の形にする
                tokens.add("0");
                tokens.add("-");
            } else if (c == '+' && isUnaryContext) {
                // 単項プラスは計算に影響しないので、完全に無視する
            } else {
                // 二項演算子、または括弧の場合
                tokens.add(String.valueOf(c));
            }
        }

        // ループ終了後、最後にバッファに残っている数字をリストに追加
        if (numBuffer.length() > 0) {
            tokens.add(numBuffer.toString());
        }
        return tokens;
    }

    /**
     * 演算子の優先順位を考慮するため逆ポーランド記法に変換する
     *
     * @param stockList
     * @return
     */
    private static Deque<String> arithmetic(List<String> stockList) {

        Deque<String> stockDeque = new LinkedList<>(stockList);
        Deque<String> operatorsDeque = new ArrayDeque<>();
        Deque<String> rpnDeque = new ArrayDeque<>();

        for (String element : stockDeque) {
            // ### 括弧処理：ロジック全体を変更 ###
            if (isNumeric(element)) {
                // 1. トークンが数字なら、出力キューに追加
                rpnDeque.add(element);
            } else if ("(".equals(element)) {
                // 2. トークンが左括弧なら、演算子スタックに追加
                operatorsDeque.push(element);
            } else if (")".equals(element)) {
                // 3. トークンが右括弧なら、左括弧が出てくるまでスタックからポップして出力キューに追加
                while (!operatorsDeque.isEmpty() && !"(".equals(operatorsDeque.peek())) {
                    rpnDeque.add(operatorsDeque.pop());
                }
                operatorsDeque.pop(); // 対応する左括弧をスタックからポップ（廃棄）
            } else {
                // 4. トークンが演算子の場合
                while (!operatorsDeque.isEmpty()
                        && priority(operatorsDeque.peek()) >= priority(element)) {
                    rpnDeque.add(operatorsDeque.pop());
                }
                operatorsDeque.push(element);
            }
        }

        // operatorsDequeに残ってる演算子をrpnDequeに保存
        while (!operatorsDeque.isEmpty()) {
            rpnDeque.add(operatorsDeque.pop());
        }
        return rpnDeque;
    }

    /**
     *
     * 逆ポーランド記法にしたリストを計算して結果を返す
     * 演算子の個数は数字の個数-1に必ずなるので一回のループで計算終了できる
     *
     * @param rpnDeque
     * @return
     */
    private static int arithmeticResult(Deque<String> rpnDeque) throws MyException {
        Deque<String> processDeque = new ArrayDeque<>();
        for (String element : rpnDeque) {
            if (isOperator(element)) {
                processDeque.push(calculating(
                        Integer.parseInt(processDeque.pop()), Integer.parseInt(processDeque.pop()), element));
            } else {
                processDeque.push(element);
            }
        }
        return Integer.parseInt(processDeque.pop());
    }

    /**
     * 演算子を判定して計算する
     *
     * @param first
     * @param last
     * @param operat
     * @return
     */
    private static String calculating(int first, int last, String operat) throws MyException {
        int result = 0;
        switch (operat) {
            case "+":
                result = last + first;
                break;
            case "-":
                result = last - first;
                break;
            case "*":
                long longResult = (long) last * first; // 順番を修正
                if (longResult > Integer.MAX_VALUE || longResult < Integer.MIN_VALUE) {
                    throw new MyException("オーバーフローです。");
                }
                result = (int) longResult;
                break;
            case "/":
                if (first == 0) {
                    throw new MyException("除算です。(18:0除算）");
                } else {
                    result = last / first;
                    break;
                }
        }
        return String.valueOf(result);
    }

    /**
     * 演算子ごとの優先順位を取得する
     *
     * @param operator
     * @return
     */
    private static int priority(String operator) {
        // ### 括弧処理：追加 ### 括弧は優先度を一番低くする
        switch (operator) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
                return 2;
            default: // "(" やその他の場合
                return 0;
        }
    }

    /**
     * 数字か記号かの判定
     *
     * @param number
     * @return
     */
    private static boolean isNumber(Character number) {
        return Character.isDigit(number);
    }

    // ### 括弧処理：ヘルパーメソッドを追加 ###
    /**
     * 文字列が数値（負の数を含む）かどうかを判定する
     * 
     * @param str
     * @return
     */
    private static boolean isNumeric(String str) {
        // 負の数も考慮する正規表現
        return str.matches("-?\\d+");
    }

    /**
     * 演算子判定
     *
     * @param operator
     * @return
     */
    private static boolean isOperator(String operator) {
        return "+-*/".contains(operator) && operator.length() == 1;
    }

}

// MyExceptionクラス（元のコードに含まれていないため、動作に必要なクラスを追記）
class MyException extends Exception {
    public MyException(String message) {
        super(message);
    }
}