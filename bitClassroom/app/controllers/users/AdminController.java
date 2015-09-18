package controllers.users;

import com.avaje.ebean.Ebean;
import helpers.Authorization;
import helpers.SessionHelper;
import models.ErrorLog;
import models.course.Course;
import models.course.CourseUser;
import models.report.DailyReport;
import models.report.Field;
import models.user.Role;
import models.user.User;
import models.report.Field;
import org.joda.time.DateTime;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utility.MD5Hash;
import utility.UserConstants;
import views.html.admins.adduser;
import views.html.admins.adminindex;
import views.html.admins.allerrors;
import views.html.admins.userlist;

import views.html.admins.setingsdailyraport;

import views.html.courses.fillClassDetails;




import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by boris on 9/12/15.
 */
@Security.Authenticated(Authorization.Admin.class)
public class AdminController extends Controller {

    private final Form<User> userForm = Form.form(User.class);

    private final Form<Field> fieldForm = Form.form(Field.class);



    /**
     * Admin index page.
     *
     * @return
     */
    public Result index() {
        User temp = SessionHelper.currentUser(ctx());
        return ok(adminindex.render(temp));
    }

    /**
     * Renders page with user information for registration.
     * TODO roles as checkboxes
     *
     * @return
     */
    public Result addNewUser() {
        return ok(adduser.render(userForm));
    }

    public Result listOfUser(){
        return ok(userlist.render(User.findAll()));
    }

    /**
     * Registers new user to the site.
     *
     * @return
     */
    public Result saveNewUser() {
        Form<User> boundForm = userForm.bindFromRequest();

        if (boundForm.hasErrors()) {
            flash("warning", "Please correct the form.");
            return redirect("register");
        }

        String fname = boundForm.bindFromRequest().field("firstname").value();
        String lname = boundForm.bindFromRequest().field("lastname").value();
        String email = boundForm.bindFromRequest().field("email").value();
        String password = boundForm.bindFromRequest().field("password").value();
        String tmpRole = boundForm.bindFromRequest().field("type").value();
        String passwordHashed = MD5Hash.getEncriptedPasswordMD5(password);

        Long role = 1L;
        if (tmpRole != null) {
            if ("2".equals(tmpRole)) {
                role = 2L;
            } else if ("3".equals(tmpRole)) {
                role = 3L;
            } else if ("4".equals(tmpRole)) {
                role = 4L;
            }
            try {
                //role = Long.parseLong(tmpRole);
            } catch (NumberFormatException e) {
                Ebean.save(new ErrorLog(e.getMessage()));
            }
        }


        Role r = new Role(role, UserConstants.NAME_ADMIN);
        List<Role> roles = new ArrayList<>();
        roles.add(r);
        User u = new User();
        u.setFirstName(fname);
        u.setLastName(lname);
        u.setEmail(email);
        u.setPassword(passwordHashed);
        u.setRoles(roles);
        u.setCreationDate(new DateTime());
        u.setCreatedBy(SessionHelper.currentUser(ctx()).getFirstName());

        if (u != null) {
            try {
                u.save();
            } catch (PersistenceException e) {
                Ebean.save(new ErrorLog(e.getMessage()));
                flash("warning", "Something went wrong, user could not be saved to database.");
                return redirect("/admin");
            }
            flash("success", String.format("User %s successfully added to database", u.getFirstName()));
            return redirect("/admin/adduser");
        }
        flash("warning", "Something went wrong, user could not be saved to database.");
        Ebean.save(new ErrorLog("Could not save user: " + u.getEmail()));
        return redirect("/admin");
    }

    /**
     * Lists all errors caught in the code with exception getMessage message,
     * or custom message, And time of occurence.
     *
     * @return
     */
    public Result seeErrors() {
        return ok(allerrors.render(ErrorLog.findAllErrors()));
    }


    public Result test() {
        return ok("you are admin");
    }


    public Result deleteUser(Long id) {

        if (User.deleteUser(id))
            return redirect("/admin/allusers");
        else
            return badRequest("Can't delete last admin");

    }

    public Result deleteError(Long id){

        ErrorLog.findErrorById(id).delete();
        return redirect("/admin/errors");
    }

    public Result deleteReport(Long id){

        DailyReport.findDailyReportById(id).delete();
        return redirect("/listReport");
    }
    public Result genField() {
        return ok(setingsdailyraport.render());
    }

    public Result saveField() {

        Form<Field> fieldModelForm = Form.form(Field.class);
        Field model = new Field();
        String name = fieldModelForm.bindFromRequest().field("scriptName").value();
        //if (model != null && name.length() > 4) {
            model.setName(name);
            model.save();
        //}
        return ok(setingsdailyraport.render());
       // return ok("ad");

    }

    private final Form<Course> courseForm = Form.form(Course.class);

    public Result addCourse() {
        return ok(fillClassDetails.render(User.getFinder().all(), courseForm));
    }

    public Result saveCourse() {
        Form<Course> boundForm = courseForm.bindFromRequest();

        String name = boundForm.bindFromRequest().field("name").value();
        String description = boundForm.bindFromRequest().field("description").value();
        String teacher = boundForm.bindFromRequest().field("type").value();

        Course course = new Course(name, description, teacher);
        course.setCreatedBy(SessionHelper.currentUser(ctx()).getFirstName());
        course.setUpdateDate(new DateTime());
        try{
        course.save();
        } catch (PersistenceException e) {
            Ebean.save(new ErrorLog(e.getMessage()));
            flash("warning", "Something went wrong, course could not be saved to data base");
            return redirect("/admin/createcourse");
        }
        flash("success", "You successfully added new course.");
        return redirect("/admin/createcourse");
    }

    public Result approveUser() {
        DynamicForm dynamicForm = Form.form().bindFromRequest();
        dynamicForm.bindFromRequest(request());

        CourseUser courseUser = new CourseUser();
        courseUser.setCourse(Course.findById(Long.valueOf(dynamicForm.get("course.getId"))));

        Logger.info("vrijednost " + dynamicForm.get("course.getId"));
        return ok("radi");
    }



}