package com.orbitrondev.Controller;

import com.orbitrondev.Abstract.Controller;
import com.orbitrondev.Model.DashboardModel;
import com.orbitrondev.View.DashboardView;

public class DashboardController extends Controller<DashboardModel, DashboardView> {
    protected DashboardController(DashboardModel model, DashboardView view) {
        super(model, view);
    }
}
