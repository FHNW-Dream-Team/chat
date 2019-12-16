package com.orbitrondev.Controller;

import com.orbitrondev.Abstract.Controller;
import com.orbitrondev.Model.DeleteAccountModel;
import com.orbitrondev.View.DeleteAccountView;

public class DeleteAccountController extends Controller<DeleteAccountModel, DeleteAccountView> {
    protected DeleteAccountController(DeleteAccountModel model, DeleteAccountView view) {
        super(model, view);
    }
}
