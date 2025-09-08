// package calculate;

// import java.io.BufferedReader;
// import java.io.IOException;
// import java.io.InputStreamReader;
// import java.util.ArrayList;
// import java.util.List;

// public class cul2 {
// /**
// * 実行メソッド
// *
// * @param args
// * @throws IOException
// */
// public static void main(String[] args) throws IOException {

// // 数式を入力
// BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
// System.out.print("\n計算式を入力してください");
// String expression = reader.readLine();

// // 数式の計算
// int result = calculate(expression);
// System.out.println("計算結果: " + result);

// }

// /**
// * 計算のメイン処理
// *
// * @param expression
// * @return
// */
// private static int calculate(String expression) {

// // 数字と演算子に分けてリ格納するリスト
// StringBuilder expressionWithoutSpaces = new StringBuilder();
// for (int i = 0; i < expression.length(); i++) {
// char c = expression.charAt(i);
// // Character.isWhitespace() は、文字がスペース、タブ、改行などの空白文字かどうかを判定する
// if (!Character.isWhitespace(c)) {
// expressionWithoutSpaces.append(c);
// }
// }
// return 1;
// }

// /*
// * 数字と演算子に分ける
// */
// private static List<String> store(String expression) {

// List<String> tokens = new ArrayList<>();
// StringBuilder currentNumber = new StringBuilder();

// // 数式か演算子の判定
// for (int i = 0; i < expression.length(); i++) {
// char word = expression.charAt(i);
// // 数字と記号に分ける（二桁以上を考慮する）
// if (isNumber(word)) {
// currentNumber.append(word);
// } else {
// // 文字が数字でない場合（記号の場合）
// if (currentNumber.length() > 0) {
// tokens.add(currentNumber.toString());
// currentNumber = new StringBuilder();
// }
// tokens.add(String.valueOf(word));
// }
// }

// // 最後に残った数字を追加する
// if (currentNumber.length() > 0) {
// tokens.add(currentNumber.toString());
// }
// return tokens;
// }

// /**
// * 演算子の優先順位を取得する
// */
// private static int priority(String operator) {
// switch (operator) {
// case "+":
// case "-":
// return 1;
// case "*":
// case "/":
// return 2;
// default:
// return 0;
// }
// }

// /**
// * 数字か記号かの判定
// *
// * @param str
// * @return
// */
// private static boolean isNumber(Character number) {
// if (Character.isDigit(number)) {
// return true;
// } else {
// return false;
// }
// }

// }
