package com.bose.legends.ui.slideshow;

import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.bose.legends.BuildAlertMessage;
import com.bose.legends.R;

import java.util.ArrayList;
import java.util.List;

public class DiceRollerFragment extends Fragment implements View.OnClickListener
{
    private List<Button> buttons, functButtons;
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
        buttons = new ArrayList<>(); functButtons = new ArrayList<>();

        buttons.add(root.findViewById(R.id.d100)); buttons.add(root.findViewById(R.id.d20)); buttons.add(root.findViewById(R.id.d12));
        buttons.add(root.findViewById(R.id.d10)); buttons.add(root.findViewById(R.id.d8)); buttons.add(root.findViewById(R.id.d6));
        buttons.add(root.findViewById(R.id.d4)); buttons.add(root.findViewById(R.id.d2)); buttons.add(root.findViewById(R.id.subtract));
        buttons.add(root.findViewById(R.id.add));
        functButtons.add(root.findViewById(R.id.keep_highest)); functButtons.add(root.findViewById(R.id.keep_lowest));
        functButtons.add(root.findViewById(R.id.drop_highest)); functButtons.add(root.findViewById(R.id.drop_lowest));
        functButtons.add(root.findViewById(R.id.reroll_ones)); functButtons.add(root.findViewById(R.id.dc));

        Resources res = getResources();
        enabledTextColor = buttons.get(0).getCurrentTextColor();
        disabledTextColor = functButtons.get(0).getCurrentTextColor();

        buttons.addAll(functButtons);

        for (int i = 0; i < 10; i++)
        {
            buttons.add(root.findViewById(res.getIdentifier("_" + i, "id", getContext().getPackageName())));
        }

        for (Button button : buttons)
            button.setOnClickListener(this);

        roll.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });

        // TextView configs
        diceEquation.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {}

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });

        // ImageView configs
        clearAll.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                diceEquation.setText("");
                equation.clear();
                setFunctButtonsEnabled(false);
            }
        });

        return root;
    }

    @Override
    public void onClick(View v)
    {
        String buttonText = ((TextView) v).getText().toString();

        if (equation.size() == 0) // when nothing has been inputted
        {
            if (!(buttonText.equals("+") || buttonText.equals("-"))) // make sure no operator can be set
            {
                equation.add(new StringBuilder(buttonText));

                if (buttonText.charAt(0) == 'd') // if dice pressed, enable funct buttons
                    setFunctButtonsEnabled(true);

                setEquation();
            }
        }
        else
        {
            StringBuilder lastInput = equation.get(equation.size() - 1);

            if (buttonText.charAt(0) != 'd') // if button pressed is not a dice
            {
                if (!(buttonText.equals("+") || buttonText.equals("-"))) // if button pressed is not an operator
                {
                    try
                    {
                        Integer.parseInt(lastInput.toString()); // check if the last input was a number

                        // if here, then last input was a number, so we just add to it
                        equation.get(equation.size() - 1).append(buttonText);
                    }
                    catch (NumberFormatException e)
                    {
                        // if here, then last input wasn't a number, so we add new number
                        // if last input was a dice, add number to it
                        if (lastInput.charAt(0) == 'd')
                            equation.add(new StringBuilder("+"));
                        // otherwise, just input number
                        equation.add(new StringBuilder(buttonText));
                    }

                    setEquation();
                }
                else // if button pressed is an operator
                {
                    if (!(lastInput.toString().equals("+") || lastInput.toString().equals("-"))) // don't input operator if operator was inputted last
                    {
                        equation.add(new StringBuilder(buttonText));
                        setEquation();
                    }
                }
            }
            else
            {

            }
        }
    }

    private void setEquation()
    {
        StringBuilder eq = new StringBuilder();

        for (StringBuilder word : equation)
            eq.append(word);

        diceEquation.setText(eq);
    }

    private void setFunctButtonsEnabled(boolean enabled)
    {
        if (enabled)
        {
            for (Button button : functButtons)
            {
                button.setEnabled(enabled);
                button.setTextColor(enabledTextColor);
            }
        }
        else
        {
            for (Button button : functButtons)
            {
                button.setEnabled(enabled);
                button.setTextColor(disabledTextColor);
            }
        }
    }
}