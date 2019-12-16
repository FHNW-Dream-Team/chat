package com.orbitrondev.Controller;

import com.orbitrondev.Abstract.Controller;
import com.orbitrondev.Model.RegisterModel;
import com.orbitrondev.View.RegisterView;

public class RegisterController extends Controller<RegisterModel, RegisterView> {
    protected RegisterController(RegisterModel model, RegisterView view) {
        super(model, view);
    }
}
