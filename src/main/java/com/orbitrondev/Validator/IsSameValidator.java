package com.orbitrondev.Validator;

import com.jfoenix.validation.base.ValidatorBase;
import javafx.scene.control.TextInputControl;

public class IsSameValidator extends ValidatorBase {
    TextInputControl validateTo;

    public IsSameValidator(TextInputControl validateTo) {
        this.setMessage("Values are not the same");
        this.validateTo = validateTo;
    }

    public IsSameValidator(TextInputControl validateTo, String message) {
        super(message);
        this.validateTo = validateTo;
    }

    protected void eval() {
        if (this.srcControl.get() instanceof TextInputControl) {
            this.evalTextInputField();
        }
    }

    private void evalTextInputField() {
        TextInputControl textField = (TextInputControl) this.srcControl.get();
        String text = textField.getText();
        String validateToText = validateTo.getText();

        try {
            this.hasErrors.set(false);
            if (!text.isEmpty() && !validateToText.isEmpty()) {
                if (!text.equals(validateToText)) {
                    this.hasErrors.set(true);
                }
            }
        } catch (Exception var4) {
            this.hasErrors.set(true);
        }

    }
}
