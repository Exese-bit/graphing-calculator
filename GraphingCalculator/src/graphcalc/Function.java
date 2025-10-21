package graphcalc;

import java.util.ArrayList;
import javax.swing.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class Function {
	private ArrayList<Object> equation;
	public Function(ArrayList<Object> function) {
		equation = function;
	}

    //Evaluates the expression at a given x (for regular functions) and n (for products and sums)
	public double evaluate(double x, ArrayList<Object> formula, double n)  {
		String tempval;
		for(int i = 0; i < formula.size(); i++) {

            //If the term is a constant/value 
			if((formula.get(i) + "").equals("x")) {
				formula.set(i, (x));
			}
            if((formula.get(i) + "").equals("n")) {
                formula.set(i, (n));
            }
			if((formula.get(i) + "").equals("pi")) {
				formula.set(i, Math.PI);
			}
			if((formula.get(i) + "").equals("e")) {
				formula.set(i, Math.E);
			}
			tempval = "" + formula.get(i);
			char c = tempval.charAt(0);
			if((c == 's' && (tempval.charAt(1) == 'i' | tempval.charAt(1) == 'e')) | c == 'c' | c == 't' | c == 'a' | (c == 'l' && tempval.charAt(1) == 'n')) { //if a function like sin(x), arccot(x), abs(x), ln(x). Only one inpit for the function 
				double temporaryResult = Operations(tempval, 0, evaluate(x, new ArrayList<Object>((ArrayList<Object>)formula.get(i + 1)), n));
				formula.set(i, temporaryResult);
				formula.remove(i + 1);
			}
            //If the term is another arrayList, recursively call evaluate for the expression within the arrayList 
			if(tempval.length() > 1 && c == '[') {
				double tempres = evaluate(x, new ArrayList<Object>((ArrayList<Object>)formula.get(i)), n);
				formula.set(i, tempres);
			}

            //If the term is a more complex function/operation with multiple inputs
            if(tempval.equals("log") | tempval.equals("der") | tempval.equals("sum") | tempval.equals("pro") | tempval.equals("fac") | tempval.equals("int")) {
                double xVal;
                double base;
                double lowerBound;
                double higherBound;
                switch (tempval) {
                    case("log"):
                        base = evaluate(x, new ArrayList<Object>((ArrayList<Object>)formula.get(i + 1)), n);
                        xVal = evaluate(x, new ArrayList<Object>((ArrayList<Object>)formula.get(i + 2)), n);
                        formula.set(i, Math.log(xVal)/Math.log(base));
                        formula.remove(i + 1);
                        break;
                    case("der"): //Derivatives 
                        xVal = evaluate(x, new ArrayList<Object>((ArrayList<Object>)formula.get(i + 1)), n);
                        Derivative tempdev = new Derivative(xVal, new ArrayList<Object>((ArrayList<Object>)formula.get(i + 2)));
                        formula.set(i, tempdev.evaluate());
                        formula.remove(i + 1);
                        break;
                    case("sum"): //Sums (Sigma notation)
                        xVal = x;
                        lowerBound = evaluate(x, new ArrayList<Object>((ArrayList<Object>)formula.get(i + 1)), n);
                        higherBound = evaluate(x, new ArrayList<Object>((ArrayList<Object>)formula.get(i + 2)), n);
                        Sum tempsum = new Sum(xVal, new ArrayList<Object>((ArrayList<Object>)formula.get(i + 3)), lowerBound, higherBound);
                        formula.set(i, tempsum.evaluate());
                        formula.remove(i + 1);
                        formula.remove(i + 1);
                        break;
                    case("pro"): //Products (Pi notation)
                        xVal = x;
                        lowerBound = evaluate(x, new ArrayList<Object>((ArrayList<Object>)formula.get(i + 1)), n);
                        higherBound = evaluate(x, new ArrayList<Object>((ArrayList<Object>)formula.get(i + 2)), n);
                        Product temppro = new Product(xVal, new ArrayList<Object>((ArrayList<Object>)formula.get(i + 3)), lowerBound, higherBound);
                        formula.set(i, temppro.evaluate());
                        formula.remove(i + 1);
                        formula.remove(i + 1);
                        break;
                    case("fac"): //Factorial
                        Factorial tempfac = new Factorial(new ArrayList<Object>((ArrayList<Object>)formula.get(i + 1)), x);
                        formula.set(i, tempfac.evaluate());
                        break;
                    case("int"): //Integral 
                        lowerBound = evaluate(x, new ArrayList<Object>((ArrayList<Object>)formula.get(i + 1)), n);
                        higherBound = evaluate(x, new ArrayList<Object>((ArrayList<Object>)formula.get(i + 2)), n);
                        Integral tempintegral = new Integral(new ArrayList<Object>((ArrayList<Object>)formula.get(i + 3)), lowerBound, higherBound);
                        formula.set(i, tempintegral.evaluate());
                        formula.remove(i + 1);
                        formula.remove(i + 1);
                        break;
                    default:
                        break;
                } 
                formula.remove(i + 1);
            }
		}
        //To handle negative numbers 
		for(int i = 0; i < formula.size(); i++) {
			tempval = "" + formula.get(i);
			char c = tempval.charAt(0);
			if(c == '-') {
				try {
					double isNumberTest = (double)formula.get(i);
				} catch(ClassCastException e) { //If not a double 
					boolean isNegative = false; 
					if(i == 0) { //If first value is a minus sign, the next value must be negative 
						isNegative = true;
					} else {
						try { 
							double isNumberTest = (double)formula.get(i - 1);
						} catch (ClassCastException f) { //If value before is not a double, that means the negative sign is in front of a double 
							isNegative = true;
						}
					}
					if(isNegative && formula.size() > 1) { //Set double to negative and remove minus sign
						formula.set(i, -(double)formula.get(i + 1));
						formula.remove(i + 1);
					}
				}
			}
		}

        /*
         *
         * Represents the order of operations (PEMDAS), where:
         *
         * 0 = exponents
         * 1 = divide/multiply
         * 2 = add/subtract
         *
         */
		int operationOrder = 0; 

        //Uses operationOrder to determine value of final arrayList, which is now only composed of doubles and operations 
		while(formula.size() > 1) {
			for(int i = 1; i < formula.size(); i += 2) {
				tempval = "" + formula.get(i);
				char c = tempval.charAt(0);
				double tempresult = 0;
				boolean changeArray = false;
				if(operationOrder == 0 && c == '^') {
					tempresult = Operations(tempval, (double)formula.get(i - 1), (double)formula.get(i + 1));
					changeArray = true;
				}
				if(operationOrder == 1 && (c == '/' | c == '*')) {
					tempresult = Operations(tempval, (double)formula.get(i - 1), (double)formula.get(i + 1));
					changeArray = true;
				}
				if(operationOrder == 2 && (c == '+' | c == '-')) {
					tempresult = Operations(tempval, (double)formula.get(i - 1), (double)formula.get(i + 1));
					changeArray = true;
				}
				if(changeArray) { //If that operation has been completed, replace the two doubles and sign with one value 
					formula.set(i - 1, tempresult);
					formula.remove(i);
					formula.remove(i);
					i -= 2;
				}
			}
			operationOrder++;
		}
		return (double)formula.get(0);
	}
	
    /* 
     * Finds all 601 y values associated with the 601 x values within the range of [lowerX, higherX] (Overloaded version of original findYValues method)
     *
     *  As each y value has been computed, it will increment the global progress and update the JLabel's text to show the new progress 
     *  Each call of findYValues creates a new thread 
     *  Each thread will eventually return a CompletableFuture as each y value has been computed
     *     
     */
    public CompletableFuture<double[]> findYValues(double lowerX, double higherX, JLabel console, boolean showProgress, AtomicInteger progress, int totalWork) {
        CompletableFuture<double[]> futureValues = new CompletableFuture<>();

		double[] YValues = new double[601];
		double range = higherX - lowerX;
        Thread t = new Thread(() -> {
            double xVal = lowerX;
            for(int x = 0; x <= 600; x += 1) {
                if(equation.size() > 0) {
                    YValues[x] = evaluate(xVal, new ArrayList<Object>(equation), 0);
                } else {
                    YValues[x] = Double.NaN;
                }
                xVal += range/600;

                int currentProgress = progress.incrementAndGet();
                if(showProgress) {
                    SwingUtilities.invokeLater(() -> { 
                        console.setText("Creating graph: (" + currentProgress + "/" + totalWork + ")");
                    });
                }
            }
            futureValues.complete(YValues);
        });

        t.start();
        return futureValues;
	}
    
    //Finds all 601 y values associated with the 601 x values within the range of [lowerX, higherX] (original findYValues method)
	public double[] findYValues(double lowerX, double higherX) {
		double[] YValues = new double[601];
		double range = higherX - lowerX;
		double xVal = lowerX;

        //Calculate each yValue 
		for(int x = 0; x <= 600; x += 1) {
            if(equation.size() > 0) {
			    YValues[x] = evaluate(xVal, new ArrayList<Object>(equation), 0);
            } else {
                YValues[x] = Double.NaN;
            }
			xVal += range/600;
		}
		return YValues;
	}

    //Defines how to calculate the value of an operation and returns the value of that operation 
	private double Operations(String operator, double result, double operand) {
		double res = result;
	    
        switch (operator) {
            case("+"):
		    	res += operand;
                break;
		    case("-"): 
                res -= operand;
                break;
	        case("*"): 
			    res *= operand;
                break;
		    case("/"):
                res /= operand;
                break;
		    case("^"):
                res = Math.pow(res, operand);
                break;
            case("sin"):
                res = Math.sin(operand);
                break;
            case("cos"):
                res = Math.cos(operand);
                break;
            case("tan"):    
                res = Math.tan(operand);
                break;
            case("arcsin"):    
                res = Math.asin(operand);
                break;
            case("arccos"):    
                res = Math.acos(operand);
                break;
            case("arctan"):    
                res = Math.atan(operand);
                break;
            case("arcsec"):    
                res = Math.acos(1/operand);
                break;
            case("arccsc"):    
                res = Math.asin(1/operand);
                break;
            case("arccot"):    
                res = Math.atan(1/operand);
                break;
            case("csc"):   
                res = 1/Math.sin(operand);
                break;
            case("sec"):	
                res = 1/Math.cos(operand);
                break;
            case("cot"):    
                res = 1/Math.tan(operand);
                break;
            case("abs"):    
                res = Math.abs(operand);
                break;
            case("ln"):
                res = Math.log(operand);
                break;
            default:
                res = 0;
		}
		
		return res;
	}
    
    //Returns an ArrayList representing a parsed version of the reflection formula for the gamma function (Used by Factorial class) 
    public static ArrayList<Object> getReflectionFormula(double equationValue) {
        ArrayList<Object> reflectionFormula = new ArrayList<Object>();
        reflectionFormula.add("pi");
        reflectionFormula.add("/");

        ArrayList<Object> denominator = new ArrayList<Object>();
        denominator.add("sin");
        
        ArrayList<Object> sin = new ArrayList<Object>();
        sin.add("pi");
        sin.add("*");
        if(equationValue < 0) {
            sin.add("-");
        }
        sin.add(Math.abs(equationValue));

        denominator.add(sin);
        reflectionFormula.add(denominator);
        return reflectionFormula;
    }

    //Returns an ArrayList representing a parsed version of the gamma function in its standard form (used by Factorial class)
    public static ArrayList<Object> getGammaIntegral(double nVal) {
        ArrayList<Object> returnArr = new ArrayList<Object>();
        returnArr.add("x");
        returnArr.add("^");
        if(nVal < 0) {
            nVal = 1 - nVal;
        }
        if(nVal - 1 < 0) {
            returnArr.add("-");
        }
        returnArr.add(Math.abs(nVal - 1));
        returnArr.add("*");
        returnArr.add("e");
        returnArr.add("^");
        ArrayList<Object> minusX = new ArrayList<Object>();
        minusX.add("-");
        minusX.add("x");
        returnArr.add(minusX);
        return returnArr;
    }
}
