package calculate;

/**
 * ゼロ除算など、計算の論理的エラーが発生した場合の例外
 */
public class ArithmeticLogicException extends CalculatorException {
    public ArithmeticLogicException(String message) {
        super(message);
    }
}
