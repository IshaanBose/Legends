package com.bose.legends.ui.slideshow;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bose.legends.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class DiceRollerFragment extends Fragment implements View.OnClickListener
{
    private List<Button> functButtons, dice, operators;
    private Button zero, dc;
    private TextView diceEquation;
    private List<String> equation;
    private int enabledTextColor, disabledTextColor;
    private boolean diceExists, dcEnabled;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_dice_roller, container, false);

        equation = new ArrayList<>();
        diceExists = false; dcEnabled = false;

        // TextViews
        diceEquation = root.findViewById(R.id.dice_equation);
        // ImageView
        ImageView clearAll = root.findViewById(R.id.clear_all), deleteChar = root.findViewById(R.id.delete_char);

        // Buttons config
        Button roll = root.findViewById(R.id.roll);
        zero = root.findViewById(R.id._0);

        List<Button> numbers = new ArrayList<>(), buttons = new ArrayList<>();
        functButtons = new ArrayList<>(); dice = new ArrayList<>(); operators = new ArrayList<>();
        dc = root.findViewById(R.id.dc);

        dice.add(root.findViewById(R.id.d100)); dice.add(root.findViewById(R.id.d20)); dice.add(root.findViewById(R.id.d12));
        dice.add(root.findViewById(R.id.d10)); dice.add(root.findViewById(R.id.d8)); dice.add(root.findViewById(R.id.d6));
        dice.add(root.findViewById(R.id.d4)); dice.add(root.findViewById(R.id.d2));
        operators.add(root.findViewById(R.id.subtract)); operators.add(root.findViewById(R.id.add));
        functButtons.add(root.findViewById(R.id.keep_highest)); functButtons.add(root.findViewById(R.id.keep_lowest));
        functButtons.add(root.findViewById(R.id.drop_highest)); functButtons.add(root.findViewById(R.id.drop_lowest));
        functButtons.add(root.findViewById(R.id.reroll_ones)); functButtons.add(dc);

        Resources res = getResources();
        enabledTextColor = dice.get(0).getCurrentTextColor();
        disabledTextColor = functButtons.get(0).getCurrentTextColor();

        for (int i = 1; i < 10; i++)
            numbers.add(root.findViewById(res.getIdentifier("_" + i, "id", getContext().getPackageName())));

        numbers.add(zero);
        buttons.addAll(dice); buttons.addAll(operators); buttons.addAll(functButtons); buttons.addAll(numbers);

        for (Button button : buttons)
            button.setOnClickListener(this);

        roll.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (equation.size() != 0)
                    equate();
            }
        });

        diceEquation.setMovementMethod(new ScrollingMovementMethod());

        // ImageView configs
        clearAll.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (equation.size() != 0)
                {
                    diceEquation.setText("");
                    equation.clear();
                    setButtonsEnabled(false, functButtons);
                    setButtonsEnabled(true, dice);
                    setButtonsEnabled(false, operators);
                    setButtonEnabled(false, zero);
                    dcEnabled = false;
                    diceExists = false;
                }
            }
        });

        deleteChar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (equation.size() != 0)
                    deleteChar();
            }
        });

        return root;
    }

    private void equate()
    {
        // don't equate if last input is an operator or the DC function
        if (!(equation.get(equation.size() - 1).equals("+") || equation.get(equation.size() - 1).equals("-") || equation.get(equation.size() - 1).equals("DC")))
        {
            List<String> queryString = convertToQueryString();
            Log.d("dice", queryString.toString());
            String finalResult = "", rolls = "";

            if (queryString.size() == 1)
            {
                List<String> result = calculateOperand(queryString.get(0));
                finalResult = result.get(0);

                if (result.size() == 2)
                    rolls = result.get(1);

                Log.d("dice", "final result: " + finalResult);
                Log.d("dice", "rolls: " + rolls);
            }
            else
            {
                Stack<String> stack = new Stack<>();
            }

            buildAlertDiceResult(finalResult, rolls);
        }
    }

    private List<String> calculateOperand(String operand)
    {
        List<String> results = new ArrayList<>();

        if (!operand.contains("d"))
            results.add(operand);
        else
        {
            String [] split1 = operand.split("d");
            int noOfDice = 1;
            int diceN = 1;
            String funct = "";

            if (!split1[0].equals("")) // if dice has a number modifier
                noOfDice = Integer.parseInt(split1[0]);

            try
            {
                diceN = Integer.parseInt(split1[1]);
            }
            catch (NumberFormatException e) // dice has a function
            {
                diceN = Integer.parseInt(split1[1].substring(0, split1[1].length() - 1)); // getting the dice
                funct = String.valueOf(split1[1].charAt(split1[1].length() - 1));
            }

            results = rollDice(noOfDice, diceN, funct);
        }

        return results;
    }

    private List<String> rollDice(int noOfDice, int diceN, String funct)
    {
        Random rand = new Random();
        List<String> result = new ArrayList<>();
        List<String> rolls = new ArrayList<>();

        int sum = 0, highest = 0, lowest = diceN;

        for (int i = 0; i < noOfDice; i++)
        {
            boolean mark = false;
            int roll = rand.nextInt(diceN) + 1;

            if (funct.equals("R"))
                if (roll == 1)
                {
                    roll = rand.nextInt(diceN) + 1;
                    mark = true;
                }

            if (mark)
            {
                if (i == 0)
                    rolls.add("*" + roll + "*");
                else
                    rolls.add(" *" + roll + "*");
            }
            else
            {
                if (i == 0)
                    rolls.add(String.valueOf(roll));
                else
                    rolls.add(" " + roll);
            }

            sum += roll;

            if (highest < roll)
                highest = roll;
            if (lowest > roll)
                lowest = roll;
        }

        switch (funct)
        {
            case "":
            case "R":
                result.add(String.valueOf(sum));
                break;
            case "K":
                result.add(String.valueOf(highest));
                break;
            case "k":
                result.add(String.valueOf(lowest));
                break;
            case "X":
                result.add(String.valueOf(sum - highest));
                break;
            case "x":
                result.add(String.valueOf(sum - lowest));
                break;
        }

        String dice = (noOfDice == 1 ? "" : noOfDice) + "d" + diceN + funct;

        result.add(dice + " : " + rolls.toString());

        return result;
    }

    private List<String> convertToQueryString()
    {
        String temp = "";
        boolean operator = false;
        List<String> queryString = new ArrayList<>();

        for (String inp : equation)
        {
            if (inp.equals("+") || inp.equals("-") || inp.equals("DC"))
            {
                if (!temp.equals(""))
                    queryString.add(temp);

                temp = inp;
                operator = true;
            }
            else
            {
                if (queryString.size() == 0)
                    queryString.add(inp);
                else
                {
                    if (!operator)
                    {
                        String newOperand = queryString.get(queryString.size() - 1) + inp;
                        queryString.set(queryString.size() - 1, newOperand);
                    }
                    else
                    {
                        queryString.add(inp);
                        operator = false;
                    }
                }
            }
        }

        if (!temp.equals(""))
            queryString.add(temp);

        return queryString;
    }

    private void deleteChar()
    {
        String lastInp = equation.remove(equation.size() - 1); // get the last input
        boolean checkForOperator = false;

        try // if lastInp is a number
        {
            int number = Integer.parseInt(lastInp);

            if (number / 10 == 0) // if after removing last digit, we get 0, just remove the entire number and disable 0
            {
                setButtonEnabled(false, zero);
                checkForOperator = true;
            }
            else
                equation.add(String.valueOf(number / 10));
        }
        catch (NumberFormatException e) // if lastInp is not a number
        {
            if (lastInp.charAt(0) == 'd') // if lastInp is a dice
            {
                if (!checkIfDiceExists())
                    setButtonsEnabled(false, functButtons);

                if (equation.size() != 0)
                {
                    try // if dice had a number before it
                    {
                        Integer.parseInt(equation.get(equation.size() - 1));
                        setButtonEnabled(true, zero);
                    }
                    catch (NumberFormatException e1) // if it didn't
                    {
                        checkForOperator = true;
                    }
                }
            }
            else if (lastInp.equals("+") || lastInp.equals("-")) // if lastInp is an operator
            {
                String secondLast = equation.get(equation.size() - 1);

                if (secondLast.equals("K") || secondLast.equals("k") || secondLast.equals("X") || secondLast.equals("x")
                    || secondLast.equals("R")) // if input before op was a function
                {
                    setButtonsEnabled(false, functButtons);
                    setButtonEnabled(true, dc);
                }
                else
                {
                    setButtonsEnabled(diceExists, functButtons);
                }
            }
            else // if lastInp was a function
            {
                setButtonsEnabled(true, functButtons);
                setButtonsEnabled(true, operators);

                String secondLast = equation.get(equation.size() - 1);

                if (lastInp.equals("DC"))
                {
                    dcEnabled = false;
                    setButtonsEnabled(true, dice);

                    try // if second last input was a number
                    {
                        Integer.parseInt(secondLast);
                        setButtonEnabled(true, zero);
                    }
                    catch (NumberFormatException e2) // if second last input was not a number
                    {
                        if (!secondLast.contains("d")) // if second last input was a function
                        {
                            setButtonsEnabled(false, functButtons);
                            setButtonEnabled(true, dc);
                        }
                    }
                }
            }
        }

        if (checkForOperator)
        {
            setButtonsEnabled(false, functButtons);
            setButtonEnabled(false, zero);
        }

        if (equation.size() == 0)
        {
            setButtonsEnabled(false, functButtons);
            setButtonsEnabled(true, dice);
            setButtonsEnabled(false, operators);
            setButtonEnabled(false, zero);
            dcEnabled = false;
            diceExists = false;
        }

        setEquation();
    }

    @Override
    public void onClick(View v)
    {
        String buttonText = String.valueOf(((TextView) v).getText());
        boolean checkForDice = true;

        if (equation.size() == 0) // when nothing has been inputted
        {
            if (!(buttonText.equals("+") || buttonText.equals("-"))) // make sure no operator can be set
            {
                equation.add(buttonText);

                if (buttonText.charAt(0) == 'd') // if dice pressed, enable funct buttons
                {
                    setButtonsEnabled(true, functButtons);
                    diceExists = true;
                }
                else
                    setButtonEnabled(true, zero);

                setButtonsEnabled(true, operators);
            }
        }
        else
        {
            String lastInput = equation.get(equation.size() - 1);

            try // check if input is a number
            {
                Integer.parseInt(buttonText);

                try
                {
                    Integer.parseInt(lastInput.toString()); // check if the last input was a number

                    // if here, then last input was a number, so we just add to it
                    String newNumber = equation.get(equation.size() - 1) + buttonText;
                    equation.set(equation.size() - 1, newNumber);
                }
                catch (NumberFormatException e2)
                {
                    // if here, then last input wasn't a number, so we add new number
                    // if last input was not DC or an operator, add number to it
                    if (!(lastInput.equals("DC") || lastInput.equals("+") || lastInput.equals("-")))
                        equation.add("+");
                    // otherwise, just input number
                    equation.add(buttonText);
                }

                setButtonEnabled(true, zero);
            }
            catch (NumberFormatException e) // if input is not a number
            {
                // if input is an operator
                if (buttonText.equals("+") || buttonText.equals("-"))
                {
                    setButtonsEnabled(false, functButtons);
                    setButtonEnabled(false, zero);

                    if (!(lastInput.equals("+") || lastInput.equals("-"))) // don't input operator if operator was inputted last
                        equation.add(buttonText);

                    checkForDice = false;
                }
                else if (buttonText.charAt(0) == 'd') // if input is a dice
                {
                    setButtonsEnabled(true, functButtons);
                    setButtonEnabled(false, zero);

                    // if input dice is the same as the last inputted dice
                    if (lastInput.equals(buttonText))
                    {
                        equation.remove(equation.size() - 1); // remove last input

                        if (equation.size() == 0 || equation.get(equation.size() - 1).equals("+")
                                || equation.get(equation.size() - 1).equals("-")) // if there is only one dice
                        {
                            equation.add("2");
                        }
                        else
                        {
                            int diceNo = Integer.parseInt(equation.remove(equation.size() - 1).toString()) + 1; // removing and getting the dice number
                            equation.add(String.valueOf(diceNo));
                        }
                    }
                    // if last input was a dice or a function (except for DC)
                    else if (lastInput.charAt(0) == 'd' || lastInput.equals("K") || lastInput.equals("k")
                            || lastInput.equals("X") || lastInput.equals("x") || lastInput.equals("R"))
                    {
                        equation.add("+");
                    }

                    equation.add(buttonText);

                    diceExists = true;
                }
                else // if input is a function
                {
                    equation.add(buttonText);

                    setButtonsEnabled(false, functButtons);
                    setButtonEnabled(false, zero);

                    if (buttonText.equals("DC"))
                    {
                        setButtonsEnabled(false, operators);
                        setButtonsEnabled(false, dice);
                        checkForDice = false;
                        dcEnabled = true;
                    }
                }
            }
        }

        if (checkForDice && diceExists && !dcEnabled)
            setButtonEnabled(true, dc);

        setEquation();
    }

    private boolean checkIfDiceExists()
    {
        for (String inp : equation)
            if (inp.charAt(0) == 'd')
                return true;

        diceExists = false;
        return false;
    }

    private void setEquation()
    {
        StringBuilder eq = new StringBuilder();

        for (String word : equation)
            eq.append(word);

        diceEquation.setText(eq);
    }

    private void setButtonsEnabled(boolean enabled, List<Button> buttonGroup)
    {
        if (enabled)
        {
            for (Button button : buttonGroup)
            {
                button.setEnabled(enabled);
                button.setTextColor(enabledTextColor);
            }
        }
        else
        {
            for (Button button : buttonGroup)
            {
                button.setEnabled(enabled);
                button.setTextColor(disabledTextColor);
            }
        }
    }

    private void setButtonEnabled(boolean enabled, Button button)
    {
        if (enabled)
        {
            button.setEnabled(enabled);
            button.setTextColor(enabledTextColor);
        }
        else
        {
            button.setEnabled(enabled);
            button.setTextColor(disabledTextColor);
        }
    }

    private void buildAlertDiceResult(String finalResult, String roll)
    {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View alertView = inflater.inflate(R.layout.alert_dice_result, null);

        TextView tFinalResult = alertView.findViewById(R.id.final_result);
        TextView tRoll = alertView.findViewById(R.id.rolls);

        tFinalResult.setText(finalResult);
        tRoll.setText(roll);

        new AlertDialog.Builder(getContext())
                .setView(alertView)
                .setTitle("Results")
                .show();
    }
}