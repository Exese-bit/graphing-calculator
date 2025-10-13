package jonah;
import java.util.ArrayList;

//A class to handle factorials for all real numbers, including negatives + decimals
public class Factorial extends Formula {
    
    private ArrayList<Object> function;
    private Function equation;
    private double xVal;

    public Factorial(ArrayList<Object> function, double xVal) {
        this.function = function;
        this.xVal = xVal;
        equation = new Function(function);
    }

    //Evaluates the factorial using the gamma function. 
    public double evaluate(){
        double equationValue = (equation.evaluate(xVal, new ArrayList<Object>((ArrayList<Object>)function), 0)) + 1;
        function = Function.getGammaIntegral(equationValue);
        Integral gammaFunction = new Integral(function, 0, 20);
        if(equationValue - 1 > 0) { //If the normal gamma function will be accurate as the exponent is positive
            return gammaFunction.evaluate();
        } else if(equationValue - 1 > -1 && equationValue != 0) { //If the normal gamma function will cause an exponent between -1 and 0, use different form
            function = Function.getGammaIntegral(equationValue + 1);
            gammaFunction = new Integral(function, 0, 20);
            return gammaFunction.evaluate()/equationValue;
        } else if(equationValue - 1 < 1 && equationValue % 1 != 0) { //If the normal gamma function won't work otherwise, use reflection formula of gamma function
            function = Function.getReflectionFormula(equationValue);
            equation = new Function(function);
            double reflectionValue = equation.evaluate(equationValue, function, 0);

            function = Function.getGammaIntegral(1 - equationValue);
            gammaFunction = new Integral(function, 0, 20);
            double difference = gammaFunction.evaluate();

            return reflectionValue/difference;
        } else if(equationValue == 0) { //If the factorial to be evaluated is 0!, return 1
            return 1;
        } else { //If the factorial is a negative whole number or equationValue is undefined, returned not a number
            return Double.NaN;
        }
    }
}
