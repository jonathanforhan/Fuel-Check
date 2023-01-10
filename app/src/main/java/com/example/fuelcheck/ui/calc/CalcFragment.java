package com.example.fuelcheck.ui.calc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.fuelcheck.databinding.FragmentCalcBinding;
import com.google.android.material.button.MaterialButton;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class CalcFragment extends Fragment implements View.OnClickListener {

    private FragmentCalcBinding binding;

    // Calculator Data Containers
    private String buttonVal = "";
    private String buttonText = "";
    private String inputData = "";
    private String displayText = "0";
    // repeat is the indicator that a repeatable operation
    // is desired like hitting the "=" 3 times to do *2*2*2 to the result
    private boolean repeat = false;
    // sqrd is like repeat but tells us the number has been squared
    private boolean sqrd = false;
    // calc buffer holds the operation that repeat will perform like the *2 example
    private String calcBuffer = "";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle("Calculator");

        binding = FragmentCalcBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if(savedInstanceState != null) {
            resetSavedData(savedInstanceState);
        }

        binding.calcResult.setText(displayText);
        massAssign();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("buttonVal", buttonVal);
        outState.putString("buttonText", buttonText);
        outState.putString("inputData", inputData);
        outState.putString("displayText", displayText);
        outState.putBoolean("repeat", repeat);
        outState.putBoolean("sqrd", sqrd);
        outState.putString("calcBuffer", calcBuffer);
    }

    @Override
    public void onClick(View view) {
        // Get Button and Respond to it using the parser
        MaterialButton button = (MaterialButton)view;
        parseButtonData(button.getText().toString());
    }
    /*
    * Series of switches to respond to each input and respond accordingly
    * Edge cases are dealt with at each statement to account for negatives, decimals etc
    * The String of user input is then shipped to JavaScript Plugin in to be calculated
    */
    private void parseButtonData(@NonNull String s) {
        // clear data result if finished computation
        if(!s.equals("=") &&
                !s.equals("+/-") &&
                !s.equals("x²") &&
                !s.equals("÷") &&
                !s.equals("×") &&
                !s.equals("-") &&
                !s.equals("+") &&
                (repeat || sqrd)
        ) {
            buttonVal = "";
            buttonText = "";
            inputData = "";
            calcBuffer = "";
        }
        // reset everytime after check above
        sqrd = false;
        // Ensure that the calc buffer has valid operation to loop
        if(!s.equals("=")
                || (!calcBuffer.contains(" / ")
                && !calcBuffer.contains(" * ")
                && !calcBuffer.contains(" - ")
                && !calcBuffer.contains(" + "))
        ) { repeat = false; }

        switch(s) {
            case "C":
                // clear every variable set display back to zero
                //
                buttonVal = "";
                buttonText = "";
                inputData = "";
                displayText = "0";
                calcBuffer = "";
                binding.calcResult.setText(displayText);
                return;
            case "+/-":
                // change the sign of the current number in queue
                //
                if(buttonVal.isEmpty()) { return; }
                if(displayText.equals("0") || displayText.equals("Error")) { return; }
                // Determine if starting val is already negative
                if(displayText.charAt(0) == '-') {
                    buttonText = displayText.substring(1);
                } else {
                    buttonText = "-" + displayText;
                }
                // enclose in () so no errors
                buttonVal = "(" + buttonText + ")";
                displayText = buttonText;
                // add to calc buffer careful to remove previous value
                if(calcBuffer.length() > 3) {
                    calcBuffer = calcBuffer.substring(0, 3) + buttonVal;
                }
                binding.calcResult.setText(displayText);
                return;
            case "x²":
                if(displayText.equals("0")) { return; }
                buttonVal = calculate("(" + displayText + "*" + displayText + ")");
                // drop extra .0
                if(buttonVal.endsWith(".0")) {
                    buttonVal = buttonVal.substring(0, buttonVal.length() -2);
                }
                // add to calc buffer careful to remove previous value
                if(calcBuffer.length() > 3) {
                    calcBuffer = calcBuffer.substring(0, 3) + buttonVal;
                }
                buttonText = buttonVal;
                displayText = buttonText;
                // cap answers that are repeating
                if(displayText.length() > 9) {
                    if(displayText.contains("E")) {
                        String holder = displayText.substring(0, 6);
                        displayText = displayText.substring(displayText.indexOf("E"));
                        displayText = holder + displayText;
                    }
                    else {
                        displayText = displayText.substring(0, 9);
                    }
                }
                binding.calcResult.setText(displayText);
                sqrd = true;
                return;
            case ".": // check for max size
                if(buttonVal.length() > 14) {
                    return;
                }
                // ensure no double decimals
                if(!displayText.contains(".")) {
                    buttonVal += ".";
                    if(buttonVal.equals(".")) {
                        buttonVal = "0.";
                    }
                    // put the decimal inside of negative number's parenthesis
                    else if(buttonVal.endsWith(").")) {
                        buttonVal = buttonVal.substring(0, buttonVal.length() -2);
                        buttonVal += s + ")";
                    }
                    buttonText = buttonVal;
                    // get rid of the parenthesis for the display
                    if(buttonText.endsWith(")")) {
                        buttonText = buttonText.substring(1, buttonText.length() -1);
                    }
                    calcBuffer += buttonVal;
                    displayText= buttonText;

                    binding.calcResult.setText(displayText);
                }
                return;
            case "-":
            case "+":
                // insert the signs into the input, a space is added to parse between
                // negative and minus signs
                inputData = inputData + buttonVal + s;
                buttonVal = "";
                calcBuffer = "";
                calcBuffer += " " + s + " ";
                return;
            case "÷":
                // same as last but use correct symbol (not custom symbol)
                inputData += buttonVal + "/";
                buttonVal = "";
                calcBuffer = "";
                calcBuffer += " / ";
                return;
            case "×":
                inputData += buttonVal + "*";
                buttonVal = "";
                calcBuffer = "";
                calcBuffer += " * ";
                return;
            case "=":
                // get result
                //
                // functionality for repeat operations on hitting = is supported
                if(repeat) {
                    inputData = displayText;
                    buttonVal = calcBuffer;
                }
                if(buttonVal.equals("")) {
                    return;
                }
                buttonVal = calculate(inputData + buttonVal);
                // the js plugin adds .0 to every output we must remove
                if(buttonVal.endsWith(".0")) {
                    buttonVal = buttonVal.substring(0, buttonVal.length() -2);
                }
                displayText = buttonVal;
                buttonText = buttonVal;
                // cap answers that are repeating
                if(displayText.length() > 9) {
                    if(displayText.contains("E")) {
                        String holder = displayText.substring(0, 6);
                        displayText = displayText.substring(displayText.indexOf("E"));
                        displayText = holder + displayText;
                    }
                    else {
                        displayText = displayText.substring(0, 9);
                    }
                }
                binding.calcResult.setText(displayText);
                inputData = "";
                // repeat operations enable
                repeat = true;
                return;
            default:
                // default number append
                //
                // check for max size
                if(buttonVal.length() > 14) {
                    return;
                }
                // insert number within parenthesis
                if(buttonVal.endsWith(")")) {
                    buttonVal = buttonVal.substring(0, buttonVal.length() -1);
                    buttonVal += s + ")";
                    buttonText = buttonVal.substring(1, buttonVal.length() -1);
                } else {
                    buttonVal += s;
                    buttonText = buttonVal;
                }
                displayText = buttonText;
                binding.calcResult.setText(displayText);
                calcBuffer += s;
        }
    }

    private String calculate(String s) {
        // javascript plugin V1.6.0
        try {
            Context context = Context.enter();
            context.setOptimizationLevel(-1);
            Scriptable scriptable = context.initStandardObjects();
            return context.evaluateString(
                    scriptable, s, "Javascript", 1, null).toString();
        }
        catch(Exception e) {
            return "Error";
        }
    }

    private void massAssign() {
        binding.buttonClear.setOnClickListener(this);
        binding.buttonSign.setOnClickListener(this);
        binding.buttonSqr.setOnClickListener(this);
        binding.buttonDivide.setOnClickListener(this);
        binding.buttonSeven.setOnClickListener(this);
        binding.buttonEight.setOnClickListener(this);
        binding.buttonNine.setOnClickListener(this);
        binding.buttonTimes.setOnClickListener(this);
        binding.buttonFour.setOnClickListener(this);
        binding.buttonFive.setOnClickListener(this);
        binding.buttonSix.setOnClickListener(this);
        binding.buttonMinus.setOnClickListener(this);
        binding.buttonOne.setOnClickListener(this);
        binding.buttonTwo.setOnClickListener(this);
        binding.buttonThree.setOnClickListener(this);
        binding.buttonPlus.setOnClickListener(this);
        binding.buttonZero.setOnClickListener(this);
        binding.buttonPoint.setOnClickListener(this);
        binding.buttonEquals.setOnClickListener(this);
    }

    private void resetSavedData(Bundle save) {
        buttonVal = save.getString("buttonVal");
        buttonText = save.getString("buttonText");
        inputData = save.getString("inputData");
        displayText = save.getString("displayText");
        repeat = save.getBoolean("repeat");
        sqrd = save.getBoolean("sqrd");
        calcBuffer = save.getString("calcBuffer");
    }
}