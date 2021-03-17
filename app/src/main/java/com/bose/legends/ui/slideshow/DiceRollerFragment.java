package com.bose.legends.ui.slideshow;

import android.content.res.Resources;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bose.legends.R;

import java.util.ArrayList;
import java.util.List;

public class DiceRollerFragment extends Fragment implements View.OnClickListener
{
    private List<Button> functButtons, dice, operators;
    private Button zero, dc;
    private TextView diceEquation;
    private List<StringBuilder> equation;
    private int enabledTextColor, disabledTextColor;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_dice_roller, container, false);

        equation = new ArrayList<>();

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

            }
        });

        diceEquation.setMovementMethod(new ScrollingMovementMethod());

        // ImageView configs
        clearAll.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                diceEquation.setText("");
                equation.clear();
                setButtonsEnabled(false, functButtons);
                setButtonsEnabled(true, dice);
                setButtonsEnabled(false, operators);
                setButtonEnabled(false, zero);
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

    private void deleteChar()
    {
        String lastInp = equation.remove(equation.size() - 1).toString(); // get the last input
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
                equation.add(new StringBuilder(String.valueOf(number / 10)));
        }
        catch (NumberFormatException e) // if lastInp is not a number
        {
            if (lastInp.charAt(0) == 'd') // if lastInp is a dice
            {
                if (!checkIfDiceExists())
                    setButtonsEnabled(false, functButtons);

                try // if dice had a number before it
                {
                    Integer.parseInt(equation.get(equation.size() - 1).toString());
                    setButtonEnabled(true, zero);
                }
                catch (NumberFormatException e1) // if it didn't
                {
                    checkForOperator = true;
                }
            }
            else if (lastInp.equals("+") || lastInp.equals("-")) // if lastInp is an operator
            {

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
        }

        setEquation();
    }

    @Override
    public void onClick(View v)
    {
        String buttonText = ((TextView) v).getText().toString();
        boolean checkForDice = true;

        if (equation.size() == 0) // when nothing has been inputted
        {
            if (!(buttonText.equals("+") || buttonText.equals("-"))) // make sure no operator can be set
            {
                equation.add(new StringBuilder(buttonText));

                if (buttonText.charAt(0) == 'd') // if dice pressed, enable funct buttons
                    setButtonsEnabled(true, functButtons);
                else
                    setButtonEnabled(true, zero);

                setButtonsEnabled(true, operators);
            }
        }
        else
        {
            StringBuilder lastInput = equation.get(equation.size() - 1);

            try // check if input is a number
            {
                Integer.parseInt(buttonText);

                try
                {
                    Integer.parseInt(lastInput.toString()); // check if the last input was a number

                    // if here, then last input was a number, so we just add to it
                    equation.get(equation.size() - 1).append(buttonText);
                }
                catch (NumberFormatException e2)
                {
                    // if here, then last input wasn't a number, so we add new number
                    // if last input was not DC or an operator, add number to it
                    if (!(lastInput.toString().equals("DC") || lastInput.toString().equals("+") || lastInput.toString().equals("-")))
                        equation.add(new StringBuilder("+"));
                    // otherwise, just input number
                    equation.add(new StringBuilder(buttonText));
                }

                setButtonEnabled(true, zero);
            }
            catch (NumberFormatException e) // if input is not a number
            {
                // if input is an operator
                if (buttonText.equals("+") || buttonText.equals("-"))
                {
                    setButtonsEnabled(false, functButtons);
                    setButtonEnabled(false, zero);;

                    if (!(lastInput.toString().equals("+") || lastInput.toString().equals("-"))) // don't input operator if operator was inputted last
                        equation.add(new StringBuilder(buttonText));

                    checkForDice = false;
                }
                else if (buttonText.charAt(0) == 'd') // if input is a dice
                {
                    setButtonsEnabled(true, functButtons);
                    setButtonEnabled(false, zero);

                    // if input dice is the same as the last inputted dice
                    if (lastInput.toString().equals(buttonText))
                    {
                        equation.remove(equation.size() - 1); // remove last input

                        if (equation.size() == 0 || equation.get(equation.size() - 1).toString().equals("+")
                                || equation.get(equation.size() - 1).toString().equals("-")) // if there is only one dice
                        {
                            equation.add(new StringBuilder("2"));
                        }
                        else
                        {
                            int diceNo = Integer.parseInt(equation.remove(equation.size() - 1).toString()) + 1; // removing and getting the dice number
                            equation.add(new StringBuilder(String.valueOf(diceNo)));
                        }
                    }
                    // if last input was a dice or a function (except for DC)
                    else if (lastInput.toString().charAt(0) == 'd' || lastInput.toString().equals("K") || lastInput.toString().equals("k")
                            || lastInput.toString().equals("X") || lastInput.toString().equals("x") || lastInput.toString().equals("R"))
                    {
                        equation.add(new StringBuilder("+"));
                    }

                    equation.add(new StringBuilder(buttonText));
                }
                else // if input is a function
                {
                    equation.add(new StringBuilder(buttonText));

                    setButtonsEnabled(false, functButtons);
                    setButtonEnabled(false, zero);

                    if (buttonText.equals("DC"))
                    {
                        setButtonsEnabled(false, operators);
                        setButtonsEnabled(false, dice);

                        checkForDice = false;
                    }
                }
            }
        }

        if (checkIfDiceExists() && checkForDice)
            setButtonEnabled(true, dc);

        setEquation();
    }

    private boolean checkIfDiceExists()
    {
        for (StringBuilder inp : equation)
            if (inp.charAt(0) == 'd')
                return true;
        return false;
    }

    private void setEquation()
    {
        StringBuilder eq = new StringBuilder();

        for (StringBuilder word : equation)
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
}