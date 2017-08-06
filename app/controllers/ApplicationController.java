package controllers;

import models.Timeline;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

public class ApplicationController extends Controller {

    public static Result index() {
        return ok(index.render(new Timeline()));
    }

}
