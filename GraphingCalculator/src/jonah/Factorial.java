package jonah;
import java.util.ArrayList;

public class Factorial extends Formula {
    
    private ArrayList<Object> function;
    private Function equation;
    private double xVal;

    public Factorial(ArrayList<Object> function, double xVal) {
        this.function = function;
        this.xVal = xVal;
        equation = new Function(function);
    }

    public double evaluate(){
        double equationValue = (equation.evaluate(xVal, new ArrayList<Object>((ArrayList<Object>)function), 0)) + 1;
        function = Function.getGammaIntegral(equationValue);
        Integral gammaFunction = new Integral(function, 0, 20);
        if(equationValue - 1 > 0) {
            return gammaFunction.evaluate();
        } else if(equationValue - 1 < 0 && equationValue % 1 != 0) {
            function = Function.getReflectionFormula(equationValue);
            equation = new Function(function);
            double reflectionValue = equation.evaluate(equationValue, function, 0); 

            function = Function.getGammaIntegral(-equationValue);
            gammaFunction = new Integral(function, 0, 20);
            double difference = gammaFunction.evaluate();

            return reflectionValue/difference;
        } else if(equationValue == 0) {
            return 1;
        } else {
            return Double.NaN;
        }
    }
}
