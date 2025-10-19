package graphcalc;
import java.util.ArrayList;

//A class to handle all definite integral operations
public class Integral extends Formula {
    
    private ArrayList<Object> function;
    private Function equation;
    private double lowerBound;
    private double higherBound;

    public Integral(ArrayList<Object> function, double lowerBound, double higherBound) {
        this.function = function;
        this.lowerBound = lowerBound;
        this.higherBound = higherBound;
        equation = new Function(function);
    }

    //Calls integrate to evaluate the integral 
    public double evaluate(){ 
        double originalValue = findIntegral(lowerBound, higherBound);
        return integrate(lowerBound, higherBound, originalValue, 1);
    }
    
    //Uses a recursive call of findIntegral. Divides the region in two and goes deeper until the error is within a certain value or max depth 
    public double integrate(double lower, double higher, double originalValue, int depth) {
        double midpoint = (lower + higher)/2;
        double s1 = findIntegral(lower, midpoint);
        double s2 = findIntegral(midpoint, higher);
        if((s1 + "").equals("Infinity") | (s2 + "").equals("Infinity")) {
            return originalValue;
        }
        if(Math.abs(s1 + s2 - originalValue) < 0.001 | depth > 15) {
            return s1 + s2;
        } else {
            return integrate(lower, midpoint, s1, depth + 1) + integrate(midpoint, higher, s2, depth + 1);
        } 
    }

    //Uses simpson's rule to approximate the integral within an interval
    private double findIntegral(double lower, double higher) {
        double deltaX = (higher - lower)/6;
        double midpoint = (higher + lower)/2;
        double fa = equation.evaluate(lower, new ArrayList<Object>(function), 0);
        double fmidpoint = 4 * equation.evaluate(midpoint, new ArrayList<Object>(function), 0);
        double fb = equation.evaluate(higher, new ArrayList<Object>(function), 0);
        return deltaX * (fa + fmidpoint + fb);
    }
}
