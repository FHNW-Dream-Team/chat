package com.orbitrondev.Controller;

import com.orbitrondev.Abstract.Controller;
import com.orbitrondev.Model.ChangePasswordModel;
import com.orbitrondev.View.ChangePasswordView;

public class ChangePasswordController extends Controller<ChangePasswordModel, ChangePasswordView> {
    protected ChangePasswordController(ChangePasswordModel model, ChangePasswordView view) {
        super(model, view);
    }
}
