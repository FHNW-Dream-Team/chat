package com.orbitrondev.Controller;

import com.orbitrondev.Abstract.Controller;
import com.orbitrondev.Model.LoginsModel;
import com.orbitrondev.View.LoginView;

public class LoginController extends Controller<LoginsModel, LoginView> {
    protected LoginController(LoginsModel model, LoginView view) {
        super(model, view);
    }
}
