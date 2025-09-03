package calculate;


/**
 * 電卓で発生するすべてのカスタム例外の親となるクラス
 */
public class CalculatException extends Exception {
    public CalculatException(String message) {
        super(message); // 親クラス(Exception)のコンストラクタを呼び出す
    }


}
