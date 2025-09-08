package calculate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class BracketCul {

	public static void main(String[] args) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.print("\n計算式を入力してください: ");
			String expression = reader.readLine();

			try {
				int result = calculate(expression);
				System.out.println("計算結果: " + result);
			} catch (SyntaxException e) {
				System.err.println("構文エラー: " + e.getMessage());
			} catch (ArithmeticLogicException e) {
				System.err.println("計算エラー: " + e.getMessage());
			} catch (Exception e) {
				System.err.println("予期せぬエラーが発生しました。");
				e.printStackTrace();
			}
		}
	}

	// 戻り値を int に戻します
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

	private static List<String> store(String expression) throws SyntaxException {
		List<String> tokens = new ArrayList<>();
		StringBuilder currentNumber = new StringBuilder();

		for (int i = 0; i < expression.length(); i++) {
			char c = expression.charAt(i);
			// isNumber(char) を使うように戻します
			if (isNumber(c)) {
				currentNumber.append(c);
			} else {
				if (currentNumber.length() > 0) {
					tokens.add(currentNumber.toString());
					currentNumber.setLength(0);
				}
				if (c == '(' || c == ')') {
					tokens.add(String.valueOf(c));
				} else if (c == '-') {
					if (tokens.isEmpty() || isOperator(tokens.get(tokens.size() - 1))
							|| tokens.get(tokens.size() - 1).equals("(")) {
						currentNumber.append('-');
					} else {
						tokens.add(String.valueOf(c));
					}
				} else if (isOperator(String.valueOf(c))) {
					tokens.add(String.valueOf(c));
				} else {
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
	 * [ステップ2] RPNへの変換 (括弧の例外処理を明記)
	 */
	private static List<String> convertToRPN(List<String> tokens) throws SyntaxException {
		List<String> outputQueue = new ArrayList<>();
		Deque<String> operatorStack = new ArrayDeque<>();

		for (String token : tokens) {
			if (isNumber(token)) {
				outputQueue.add(token);
			} else if (token.equals("(")) {
				operatorStack.push(token);
			} else if (token.equals(")")) {
				while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
					outputQueue.add(operatorStack.pop());
				}
				// ★★★ 括弧の例外チェック① ★★★
				// スタックが空になった = 対応する '(' が見つからなかった
				if (operatorStack.isEmpty()) {
					throw new SyntaxException("閉じ括弧 ')' に対応する開き括弧 '(' がありません。");
				}
				operatorStack.pop(); // '(' をスタックから捨てる
			} else if (isOperator(token)) {
				while (!operatorStack.isEmpty()
						&& priority(operatorStack.peek()) >= priority(token)) {
					outputQueue.add(operatorStack.pop());
				}
				operatorStack.push(token);
			}
		}
		while (!operatorStack.isEmpty()) {
			String op = operatorStack.pop();
			// ★★★ 括弧の例外チェック② ★★★
			// 最後にスタックを空にする際、'(' が残っていたらエラー
			if (op.equals("(")) {
				throw new SyntaxException("開き括弧 '(' に対応する閉じ括弧 ')' がありません。");
			}
			outputQueue.add(op);
		}
		return outputQueue;
	}

	// 戻り値とスタックの型を int に戻します
	private static int calculateRPN(List<String> rpn)
			throws SyntaxException, ArithmeticLogicException {
		Deque<Integer> calculationStack = new ArrayDeque<>();
		for (String token : rpn) {
			if (isNumber(token)) {
				calculationStack.push(Integer.parseInt(token));
			} else if (isOperator(token)) {
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
						if (val2 == 0) {
							throw new ArithmeticLogicException("0による除算はできません。");
						}
						result = val1 / val2;
						break;
				}
				calculationStack.push(result);
			}
		}
		if (calculationStack.size() != 1) {
			throw new SyntaxException("数字が多すぎるか、式が正しくありません。");
		}
		return calculationStack.pop();
	}

	// --- ヘルパーメソッド群 ---

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

	// isNumber(Character) を復活させます
	private static boolean isNumber(Character number) {
		return Character.isDigit(number);
	}

	// isNumber(String) を int に戻します
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
