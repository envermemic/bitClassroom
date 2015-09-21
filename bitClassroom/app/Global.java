import models.user.User;
import play.Application;
import play.GlobalSettings;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import utility.database.Courses;
import utility.database.Roles;
import utility.database.Users;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.notFound;

/**
 * Created by boris.tomic on 21/09/15.
 */
public class Global extends GlobalSettings {

    @Override
    public void onStart(Application application) {
        //Roles.saveAllRoles();
        //Users.saveUsers();
        //Courses.saveCourses();

    }

    @Override
    public F.Promise<Result> onHandlerNotFound(Http.RequestHeader requestHeader) {
        return F.Promise.<Result>pure(notFound(views.html.notfound.render()));
    }

    @Override
    public F.Promise<Result> onBadRequest(Http.RequestHeader requestHeader, String s) {
        return F.Promise.<Result>pure(badRequest(views.html.notfound.render()));
    }
}
