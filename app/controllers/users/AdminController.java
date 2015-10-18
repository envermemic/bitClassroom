package controllers.users;

import com.avaje.ebean.Ebean;
import helpers.Authorization;
import helpers.SessionHelper;
import it.innove.play.pdf.PdfGenerator;
import models.*;
import models.course.Course;
import models.course.CourseUser;
import models.report.*;
import models.user.Assignment;
import models.user.Mentorship;
import models.user.Role;
import models.user.User;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.Configuration;
import play.Logger;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import utility.CourseConstants;
import utility.MD5Hash;
import utility.MailHelper;
import utility.UserConstants;
import utility.database.Roles;
import views.html.admins.*;
import views.html.admins.adduser;
import views.html.admins.newTableWeekly;
import views.html.admins.weeklypdf;
import views.html.admins.openReports;
import views.html.calendar.calendar;
import views.html.calendar.formEdit;
import views.html.calendar.formNew;
import views.html.calendar.list;

import views.html.admins.setingsweeklyreport;
import views.html.admins.dailypdf;
import views.html.admins.pdfopenreport;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import play.data.Form;


/**
 * Created by boris on 9/12/15.
 */
@Security.Authenticated(Authorization.Admin.class)
public class AdminController extends Controller {

    @Inject
    public PdfGenerator pdfGenerator;

    private final Form<User> userForm = Form.form(User.class);

    final static Form<Event> eventForm = Form.form(Event.class);

    private final Form<Field> fieldForm = Form.form(Field.class);

    private final Form<CourseUser> courseUserForm = Form.form(CourseUser.class);
    private final Form<Course> courseForm = Form.form(Course.class);
    private List<String> imageList = new ArrayList<>();
    private static String url = Play.application().configuration().getString("url");
    /**
     * Admin index page.
     *
     * @return
     */
    public Result index() {
        User temp = SessionHelper.currentUser(ctx());
        return ok(views.html.admins.adminindex.render(temp));
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

    public Result listOfUser() {
        return ok(views.html.admins.userlist.render(User.findAll()));
    }


    /**
     * Method for set users new type of role.
     * @param id - Long user id, used for find selected User
     */
    public Result editUserRole(Long id){
        User user = User.findById(id);
        return ok(views.html.admins.editUserRole.render(userForm, user));
    }

    /**
     * Registers new user to the site.
     *
     * @return
     */
    public Result saveNewUser(String type) {
        Form<User> boundForm = userForm.bindFromRequest();

        String email = "";
        String passwordHashed = "";
        User u = null;

        if (boundForm.hasErrors()) {
            flash("warning", "Please correct the form.");
            return redirect("register");
        }

        String fname = boundForm.field("firstname").value();
        String lname = boundForm.field("lastname").value();
        if (type.equals("save")) {
            email = boundForm.field("email").value();
            String password = boundForm.field("password").value();
            passwordHashed = MD5Hash.getEncriptedPasswordMD5(password);
        }
        String admin = boundForm.field("admin").value();
        String teacher = boundForm.field("teacher").value();
        String mentor = boundForm.field("mentor").value();
        String student = boundForm.field("student").value();
        String tmpRole = boundForm.field("type").value();

            List<Role> roles = new ArrayList<>();
            Long role = null;
            if (tmpRole != null) {
                if ("1".equals(tmpRole)) {
                    role = 1L;
                    Role r = new Role(role, UserConstants.NAME_ADMIN);
                    roles.add(r);
                } else if ("2".equals(tmpRole)) {
                    role = 2L;
                    Role r = new Role(role, UserConstants.NAME_TEACHER);
                    roles.add(r);
                } else if ("3".equals(tmpRole)) {
                    role = 3L;
                    Role r = new Role(role, UserConstants.NAME_MENTOR);
                    roles.add(r);
                } else if ("4".equals(tmpRole)) {
                    role = 4L;
                    Role r = new Role(role, UserConstants.NAME_STUDENT);
                    roles.add(r);
                    if(type.equals("save")) {
                        u = new User();
                        u.setStudentStatus(UserConstants.DONT_HAVE_MENTOR);
                    }else {
                        u = User.findByNick(type);
                        u.setStudentStatus(UserConstants.DONT_HAVE_MENTOR);
                    }
                } else if ("5".equals(tmpRole)) {
                    roles.add(Roles.ADMIN);
                    roles.add(Roles.TEACHER);
                }
                try {
                    //role = Long.parseLong(tmpRole);
                } catch (NumberFormatException e) {
                    Ebean.save(new ErrorLog(e.getMessage()));
                }
            }

        if(type.equals("save")){
            u = new User();
            u.setFirstName(fname);
            u.setLastName(lname);
            u.setEmail(email);
            u.setPassword(passwordHashed);
            u.setRoles(roles);
            u.setCreationDate(new DateTime());
            u.setCreatedBy(SessionHelper.currentUser(ctx()).getFirstName());
            u.setToken();
            if (u != null) {
                try {
                    u.save();
                } catch (PersistenceException e) {
                    Ebean.save(new ErrorLog(e.getLocalizedMessage()));
                    flash("warning", "Something went wrong, user could not be saved to database.");
                    return redirect("/admin");
                }
                String host = url + "login";
                String host1 = url + "remove/" + u.token;
                MailHelper.send(u.getEmail(), host, host1);
                flash("success", String.format("User %s successfully added to database", u.getFirstName()));
                return redirect("/admin/adduser");
            }
            flash("warning", "Something went wrong, user could not be saved to database.");
            Ebean.save(new ErrorLog("Could not save user: " + u.getEmail()));
            return redirect("/admin");
        }else{

            User tmp = User.findByNick(type);
            tmp.setNickName(fname);
            tmp.setRoles(roles);
            if (tmp != null) {
                try {
                    tmp.save();
                } catch (PersistenceException e) {
                    Ebean.save(new ErrorLog(e.getMessage()));
                    flash("warning", "Something went wrong, user could not be saved to database.");
                    return redirect("/admin");
                }

                flash("success", String.format("User %s is successfully edit", tmp.getFirstName()));
                return redirect("/admin/allusers");
            }
            flash("warning", "Something went wrong, user could not be saved to database.");
            Ebean.save(new ErrorLog("Could not save user: " + u.getEmail()));
            return redirect("/admin");

        }
    }

    /**
     * Lists all errors caught in the code with exception getMessage message,
     * or custom message, And time of occurence.
     *
     * @return
     */
    public Result seeErrors() {
        return ok(views.html.admins.allerrors.render(ErrorLog.findAllErrors()));
    }


    public Result test() {
        return ok("you are admin");
    }

    /**
     * Deletes user from database. When trashcan icon is pressed users id is send.
     * Id is taken as parameter in method and used to find user and delete it.
     *
     * @param id <code>Long</code> type value of User id
     * @return if user is deleted redirects to list of all users, othervise a bad request is sent
     */
    public Result deleteUser(Long id) {
        if (User.deleteUser(id))
            return redirect("/admin/allusers");
        else
            return badRequest("Can't delete last admin");
    }

    /**
     * Deletes error when trashcan icon is pressed. Id of error is send to this method
     * ErrorLog is found by id and then deleted.
     *
     * @param id <code>Long</code> type value of ErrorLog id
     * @return redirects to list of errors
     */
    public Result deleteError(Long id) {
        ErrorLog.findErrorById(id).delete();
        return redirect("/admin/errors");
    }

    /**
     * Deletes daily report from database. When delete button is pressed daily
     * report id is sent to this method. Daily report is then found and specific
     * report is deleted from database.
     *
     * @param id <code>Long</code> type value of DailyReport id
     * @return redirect to list of daily reports
     */
    public Result deleteReport(Long id) {
        DailyReport.findDailyReportById(id).delete();
        return redirect("/listReport");
    }

    /**
     * Renders single field and button for admin so he/she can create additional
     * fields for daily report. On the right side a live preview of current daily report is displayed.
     *
     * @return
     */
    public Result genField() {
        return ok(views.html.admins.setingsdailyraport.render(Field.getFinder().all()));
    }

    /**
     * Saves a field that admin has created to database. Field is later used so teacher
     * can write daily report.
     *
     * @return redirects to page for creating additional field
     */
    public Result saveField() {
        Form<Field> fieldModelForm = Form.form(Field.class);
        Field model = new Field();
        String name = fieldModelForm.bindFromRequest().field("scriptName").value();
        String name1 = AdminController.firstUpperCase(name);
        model.setName(name1);
        try {
            model.save();
        }catch (PersistenceException e){
            flash("warning", "Name field already exist");
            return redirect("/admin/createdaily");
        }
        flash("success", "You added another field.");
        return redirect("/admin/createdaily");

    }

    /**
     * Renders page for admin so he/she can create new course. Page have field for course name,
     * description, selection of teacher and option to upload course image.
     *
     * @return
     */
    public Result addCourse() {
        return ok(views.html.admins.fillClassDetails.render(User.getFinder().orderBy().asc("first_name").findList(), courseForm));
    }

    /**
     * Saves course to database.
     *
     * @return
     */
    public Result saveCourse() {
        Form<Course> boundForm = courseForm.bindFromRequest();


        String name = boundForm.field("name").value();
        String description = boundForm.field("description").value();
        String getTeacher = boundForm.field("type").value();
        String teacher = getTeacher.split(" ")[0] + " " + getTeacher.split(" ")[1];
        
        String teacherId = getTeacher.split(" ")[2];

        List<Course> courses =  Course.findAll();
        for(int i = 0; i < courses.size(); i++){
            if(courses.get(i).getName().equals(name) && courses.get(i).getDescription().equals(description) && courses.get(i).getStatus() != 2){
                flash("warning", "Course already exist");
                return redirect("/admin/createcourse");
            }
        }


        Course course = new Course(name, description, teacher);

        course.setCreatedBy(SessionHelper.currentUser(ctx()).getFirstName());
        course.setUpdateDate(new DateTime());

        Http.MultipartFormData data = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart filePart = data.getFile("image");

        try {
            course.save();
        } catch (PersistenceException e) {
            Ebean.save(new ErrorLog(e.getMessage()));
            flash("warning", "Something went wrong, course could not be saved to data base");
            return redirect("/admin/createcourse");
        }

        if (filePart != null) {
            File file = filePart.getFile();
            Image image = Image.createCourseImage(file, course.getId());
            image.save();
            course.setImage(image);
            course.update();
        }

        CourseUser cu = new CourseUser();
        cu.setCourse(course);
        cu.setStatus(User.findById(Long.parseLong(teacherId)).getStatus());
        cu.setUser(User.findById(Long.parseLong(teacherId)));

        CourseUser  adminCourse = new CourseUser();
        adminCourse.setCourse(course);
        adminCourse.setStatus(2);
        adminCourse.setUser(SessionHelper.currentUser(ctx()));
        try {
            cu.save();
            adminCourse.save();
        } catch (PersistenceException e) {
            Ebean.save(new ErrorLog(e.getMessage()));
            flash("warning", "Something went wrong, course teacher could not be saved to data base");
            return redirect("/admin/createcourse");
        }

        flash("success", "You successfully added new course.");
        return redirect("/admin/createcourse");
    }

    /**
     * Based on option pressed eg. approve or dissaprove, option value is send along with
     * CourseUser id. CourseUser is found by id passed to this method, status is set and ok
     * is send so page can be refreshed. Ajax is used in approveuser view.
     *
     * @param id <code>Long</code> type value of CourseUser id
     * @return empty ok
     */
    public Result approveStudent(Long id) {
        DynamicForm form = Form.form().bindFromRequest();

        String s = form.data().get("pressed");
        CourseUser cu = CourseUser.getFinder().byId(id);
        if (s != null) {
            cu.setStatus(Integer.parseInt(s));
            cu.save();
        }

        approvedNotification(cu.getCourse(), cu.getUser());


        Course c = cu.getCourse();
        for(int i = 0; i < c.getPosts().size(); i++){
            if(c.getPosts().get(i).getPostType() == 1){
                Assignment a = new Assignment();
                a.setUser(cu.getUser());
                a.setPost(c.getPosts().get(i));
                a.setHomeworkPostStatus(0);
                a.save();
            }

        }

        User u = Mentorship.findMentorByUser(cu.getUser());
        List<User> courseUsers = CourseUser.allUserFromCourse(cu.getCourse().getId());

        if( u != null) {
            for (int i = 0; i < courseUsers.size(); i++) {
                if (courseUsers.get(i).getId() == u.getId()) {
                    return ok("");
                }
            }
        }

        if(u != null){
            CourseUser cc = new CourseUser();
            cc.setCourse(cu.getCourse());
            cc.setStatus(Integer.parseInt(s));
            cc.setUser(u);
            cc.save();
        }


        return ok("");
    }

    /**
     * Send notification when admin approved user in the class
     * @param course
     * @param student
     */
    public void approvedNotification ( Course course, User student){

        String subject = "Course information";
        String content = "Your application for access to the course" + course.getName() + " is accepted";

        User sender = SessionHelper.currentUser(ctx());
        PrivateMessage privMessage = PrivateMessage.create(subject, content, sender, student);
        privMessage.setStatus(0);
        student.getMessages().add(privMessage);
        student.save();

    }

    /**
     * List of students that applied for courses. Course name, student name and
     * approve, dissaprove buttons are displayed in a row.
     *
     * @return
     */
    public Result awaitList() {
        return ok(views.html.admins.approveuser.render(CourseUser.getFinder().all()));
    }

    /**
     * See tables of daily reports
     *
     * @return
     */
    public Result listReport() {
        return ok(views.html.admins.newTableDaily.render(ReportField.getFinder().all(), DailyReport.getFinder().all(), Field.getFinder().all()));
    }


    /**
     * Renders page with two dropdown menus. One is for mentors and other is for
     * students that don't have mentor. User selects mentor on left side and student
     * on right side. Once buttom assign is pressed selected mentor is assigned to slected student.
     *
     * @return
     */
    public Result mentorship() {
        return ok(views.html.admins.mentorship.render(User.getFinder().orderBy().asc("first_name").findList(), User.getFinder().where().eq("student_status", UserConstants.DONT_HAVE_MENTOR).findRowCount()));
    }

    /**
     * Get's user inputed selection via DynamicForm, gets mentor.id value from mentor
     * selection in <code>String</code> type men variable and student.id value from
     * student selection in <code>String</code> type stu variable.
     * <p>
     * After that string is parsed into Long since id is Long value.
     * If everything goes well user mentor is found by mentor.id, and student by student.id.
     * <p>
     * Student's studentStatus is set to HAVE_MENTOR and student is updated.
     * Mentorship is created and status is set to ACTIVE.
     *
     * @return
     */
    public Result saveMentorship() {
        DynamicForm form = Form.form().bindFromRequest();
        String men = form.get("mentor");
        String stu = form.get("student");
        Long ment = null;
        Long stud = null;
        if (men != null && stu != null) {
            try {
                ment = Long.parseLong(men);
                stud = Long.parseLong(stu);
            } catch (NumberFormatException e) {
                Ebean.save(new ErrorLog("Could not parse users Id: " + e.getMessage()));
                flash("warning", "Something went wrong, could not assign mentor to student.");
                return redirect("/admin/mentorship");
            }
        }
        User mentor = User.findById(ment);
        mentor.setStudentStatus(UserConstants.ACTIVE_MENTOR);
        mentor.update();
        User student = User.findById(stud);
        student.setStudentStatus(UserConstants.HAVE_MENTOR);
        student.update();

        mentorshipNotification(mentor , student);

        Mentorship mentorship = new Mentorship();
        mentorship.setMentor(mentor);
        mentorship.setStudent(student);
        mentorship.setCreatedBy(SessionHelper.currentUser(ctx()).getEmail());
        mentorship.setStatus(UserConstants.ACTIVE_MENTORSHIP);
        mentorship.save();

            List<Course> courses = CourseUser.allUserCourses(student);
            for(int i = 0; i < courses.size(); i++){
                CourseUser courseUser = CourseUser.findCourseUser(mentor, courses.get(i));
                if(courseUser == null){
                    CourseUser cc = new CourseUser();
                    cc.setCourse(courses.get(i));
                    cc.setStatus(2);
                    cc.setUser(mentor);
                    cc.save();  
                }


            }


        flash("success", String.format("Successfully assigned %s %s to %s %s.", mentor.getFirstName(),mentor.getLastName(), student.getFirstName(),student.getLastName()));
        return redirect("/admin/mentorship");
    }


    /**
     * MEthod for send auto message to mentor and student,
     * @param mentor
     * @param student
     * @return
     */
    public Result mentorshipNotification(User mentor, User student) {

        User sender = SessionHelper.currentUser(ctx());
        String subject = "Mentorship";
        String content = mentor.getFirstName() + " " + mentor.getLastName() + " has been assigned as your new mentor.";
        String mentorContent = "You are assigned " + student.getFirstName() + " " + student.getLastName() + " as his new mentor";
        PrivateMessage privMessage = PrivateMessage.create(subject, content, sender, student);
        privMessage.setStatus(0);
        student.getMessages().add(privMessage);
        student.save();
        PrivateMessage mentorMessage = PrivateMessage.create(subject, mentorContent, sender, mentor);
        mentorMessage.setStatus(0);
        mentor.getMessages().add(mentorMessage);
        mentor.save();

        return redirect("admin/mentorship");
    }

    public Result teachers(){
        return ok(views.html.admins.addteacher.render(User.getFinder().orderBy().asc("first_name").findList(), Course.getFinder().where().eq("status", 1).findList()));
    }

    public Result addTeacher(){
        DynamicForm form = Form.form().bindFromRequest();
        String teacher = form.get("teacher");
        String course = form.get("course");
        Long tea = null;
        Long cou = null;
        if (teacher != null && course != null) {
            try {
                tea = Long.parseLong(teacher);
                cou = Long.parseLong(course);
            } catch (NumberFormatException e) {
                Ebean.save(new ErrorLog("Could not parse teachers Id: " + e.getMessage()));
                flash("warning", "Something went wrong, could not add teacher to course");
                return redirect("/admin/addnewteacher");
            }
        }

        List<User> courseUsers = CourseUser.allUserFromCourse(Course.findById(cou).getId());
        for(int i = 0; i < courseUsers.size(); i++){
            if(courseUsers.get(i).getId() == User.findById(tea).getId()){
                flash("warning", "Teacher is already teaching on the course");
                return redirect("/admin/addnewteacher");
            }
        }



        CourseUser cu = new CourseUser();
        cu.setCourse(Course.findById(cou));
        cu.setUser(User.findById(tea));
        cu.setStatus(2);
        cu.save();


        flash("success", String.format("Successfully added %s %s to %s %s.", User.findById(tea).getFirstName(), User.findById(tea).getLastName(), Course.findById(cou).getName(), Course.findById(cou).getDescription()));
        return redirect("/admin/addnewteacher");

    }

    /**
     * Renders table with information about current mentorship status.
     *
     * @return
     */
    public Result seeMentorsAndStudents() {
        return ok(views.html.admins.mentorshipList.render(Mentorship.getFinder().where().eq("status", UserConstants.ACTIVE_MENTORSHIP).findList()));
    }

    /**
     * Method for get list of all mentors in the classroom.
     * @return
     */
    public Result  seeAllMentors(){

        List<User> users = User.findAll();
        List<User> mentors = new ArrayList<>();

        for( int i = 0; i < users.size(); i++){
            if( users.get(i).getRoles().get(0).getName().equals(UserConstants.NAME_MENTOR)){
                mentors.add(users.get(i));
            }
        }
        return ok(allMentors.render(mentors));
    }


    /**
     * Method for get list of inactive mentors
     */
    public Result inactiveMentors(){

        Logger.info(DateTime.now().dayOfWeek().getAsShortText());
        List<User> mentors = new ArrayList<>();
        List<User> users = User.findAll();
        for ( int i = 0; i < users.size(); i++){
            if( users.get(i).getStudentStatus() != null && users.get(i).getStudentStatus().equals(UserConstants.ACTIVE_MENTOR)){
                mentors.add(users.get(i));
            }
        }
        for(int i = 0; i < mentors.size(); i++){
            if( mentors.get(i).getWeeklyReportStatus() != 2 && DateTime.now().dayOfWeek().getAsShortText().equals("Fri")){

                sendMentorNotification(mentors.get(i));

            }else if( mentors.get(i).getWeeklyReportStatus() == 2 && DateTime.now().dayOfWeek().getAsShortText().equals("Sat")){

                mentors.get(i).setWeeklyReportStatus(1);
                mentors.get(i).update();

            }
        }


        List<PrivateMessage>  message = PrivateMessage.findAllMessage();
        HashSet<User> inactiveMentors = new HashSet<>();


        for( int i = 0; i < message.size(); i++){
            if(message.get(i).getStatus() == 0 && message.get(i).getReceiver().getStudentStatus().equals(UserConstants.ACTIVE_MENTOR)){
                DateTime start = new DateTime();
                Long result = start.getMillis() - message.get(i).getCreationDate().getMillis();
                if(result > 60000) {
                    inactiveMentors.add(message.get(i).getReceiver());
                    if(!inactiveMentors.add(message.get(i).getReceiver())){
                        message.get(i).getReceiver().setUnreadMessage(message.get(i).getReceiver().getUnreadMessage()+1);
                    }
                }
            }
        }

        return ok(inactMentors.render(inactiveMentors));
    }

    /**
     * When trashcan icon is pressed, Mentorship id is sent to method. Mentorship is
     * found by id and status is set as expired. Also student is found from mentorship proces
     * and his/hers status is set as dont have mentor.
     *
     * @param id <code>Long</code> type value of Mentorship
     * @return redirects to list of currently active mentorship process
     */
    public Result deleteMentorship(Long id) {
        Mentorship men = Mentorship.getFinder().byId(id);
        men.setStatus(UserConstants.EXPIRED_MENTORSHIP);
        men.setUpdateDate(new DateTime());
        men.setUpdatedBy(SessionHelper.currentUser(ctx()).getEmail());
        men.update();

        men.getStudent().setStudentStatus(UserConstants.DONT_HAVE_MENTOR);
        men.getStudent().update();
        men.getMentor().setStudentStatus(0);
        men.getMentor().update();

        flash("success", String.format("Successfully removed %s to %s mentorship relationship.", men.getMentor().getFirstName(), men.getStudent().getFirstName()));
        return redirect("/admin/activementors");
    }

    /**
     * Renders view with table filled with all active courses and option to archive or delete course.
     *
     * @return
     */
    public Result courseList() {
        return ok(views.html.admins.coursesAll.render(Course.getFinder().where().eq("status", CourseConstants.ACTIVE_COURSE).findList()));
    }

    /**
     * Process ajax request, if pressed option 0 is passed course is automatically set as archived,
     * if option 2 is sent course is deleted. Course is found by id passed in method params.
     *
     * @param id <code>Long</code> type value of Course id
     * @return
     */
    public Result deleteOrArchiveCourse(Long id) {

        DynamicForm form = Form.form().bindFromRequest();
        Course course = Course.getFinder().byId(id);
        String s = form.data().get("pressed");
        if( Integer.parseInt(s) == 2){
            List<User> users = CourseUser.allUserFromCourse(id);
            for (int i = 0; i < users.size(); i++){
                sendAutoMessage(users.get(i).getId(), course);
            }

        }

        if (s != null) {
            course.setStatus(Integer.parseInt(s));
            course.setUpdateDate(new DateTime());
            course.setUpdatedBy(SessionHelper.currentUser(ctx()).getEmail());
            course.save();


        }
        return ok();
    }


    /**
     * Method for auto send message to User when course is deleted
     * @param id - User id.
     * @return
     */
    public Result sendAutoMessage(Long id, Course course) {

        User sender = SessionHelper.currentUser(ctx());
        User receiver = User.findById(id);
        String subject = "Information";
        String content = "Course " + course.getName()  + " is deleted by admin " + sender.getFirstName() +" " + sender.getLastName() +".";

        PrivateMessage privMessage = PrivateMessage.create(subject, content, sender, receiver);
        privMessage.setStatus(0);
        receiver.getMessages().add(privMessage);
        receiver.save();

        return redirect("admin/allusers");
    }


    /**
     * Method for send notification to mentor when due time pass for weekly report
     * @param user
     */
    public void sendMentorNotification (User user){

        User sender = SessionHelper.currentUser(ctx());
        User receiver = user;
        String subject = "Weekly report";
        String content = "Please write a weekly report";

        PrivateMessage privMessage = PrivateMessage.create(subject, content, sender, receiver);
        privMessage.setStatus(0);
        receiver.getMessages().add(privMessage);
        receiver.save();
    }

    /**
     * Renders view with table filled with all archived courses. Course name, description and email of user who
     * archived it is displayed,
     *
     * @return
     */
    public Result archivedCourses() {
        return ok(views.html.admins.coursesArchived.render(Course.getFinder().where().eq("status", CourseConstants.ARCHIVED_COURSE).findList()));
    }


    public Result deletedCourses() {
        return ok(views.html.admins.coursesDeleted.render(Course.getFinder().where().eq("status", CourseConstants.DELETED_COURSE).findList()));
    }

    /**
     * Recieves <code>Long</code> type value of Course id, find's the course and set's
     * status to active, find's course in relational table and only deletes all Users (set's them to null).
     *
     * @param id <code>Long</code> type value of Course id
     * @return
     */
    public Result activateCourse(Long id) {
        Course course = Course.getFinder().byId(id);
        course.setStatus(CourseConstants.ACTIVE_COURSE);
        course.setUpdateDate(new DateTime());
        course.setUpdatedBy(SessionHelper.currentUser(ctx()).getEmail());
        course.update();

        List<CourseUser> cu = CourseUser.getFinder().where().eq("course_id", id).findList();
        for (CourseUser courseUser : cu) {
            courseUser.setUser(null);
            courseUser.update();
        }
        return ok();
    }


    /**
     * Method for add new question and answer in FAQ view.
     * @return
     */
    public Result setupFAQ (){

        return ok(faqSetup.render());
    }

    /**
     * Method for save new question and answer to FAq page, redirect to FAQ page.
     * @return
     */
    public Result saveFAQ(){

        DynamicForm form = Form.form().bindFromRequest();
        String question = form.get("question");
        String answer = form.get("answer");

        Faq newFAQ = new Faq(question, answer);
        newFAQ.save();

        return redirect("/faq");
    }

    /**
     * Checks if events ends the same day which starts
     * @param start Date
     * @param end Date
     * @return Boolean: True if ends same day
     */
    private static Boolean endsSameDay(Date start, Date end){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(start).equals(dateFormat.format(end));
    }


    /**
     * Returns list of events for calendar view
     * @param start Long Timestamp of current view start
     * @param end Long Timestamp of current view end
     * @return Result
     */
    public static Result json(Long start, Long end) {

        Date startDate = new Date(start*1000);
        Date endDate = new Date(end*1000);

        List<Event> resultList = Event.findInDateRange(startDate, endDate);
        ArrayList<Map<Object, Serializable>> allEvents = new ArrayList<Map<Object, Serializable>>();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (Event event : resultList) {
            Map<Object, Serializable> eventRemapped = new HashMap<Object, Serializable>();
            eventRemapped.put("id", event.id);
            eventRemapped.put("title", event.title);
            eventRemapped.put("start", df.format(event.start));
            eventRemapped.put("end", df.format(event.end));
            eventRemapped.put("allDay", event.allDay);
            eventRemapped.put("url", controllers.users.routes.AdminController.edit(event.id).toString());

            allEvents.add(eventRemapped);
        }
        return ok(play.libs.Json.toJson(allEvents));
    }


    /**
     * Displays full calendar
     * @return Result
     */
    public static Result calendar() {
        return ok(calendar.render("Title of this calendar..."));
    }


    /**
     * List of events in table view
     * @return Result
     */
    public static Result listEvents() {
        List<Event> events = Event.find.order().desc("start").findList();
        return ok(list.render(events));
    }


    /**
     * Displays blank form
     * @return Result
     */
    public static Result blank() {
        return ok(formNew.render(eventForm));
    }


    /**
     * Save new event in DB (a.k.a. submit action in other examples)
     * @return Result
     */
    public static Result add() {
        Form<Event> eventForm = Form.form(Event.class).bindFromRequest();
        if (eventForm.hasErrors()) {
            return badRequest(formNew.render(eventForm));
        }

        Event newEvent = eventForm.get();

        newEvent.allDay = newEvent.allDay != null;
        if (newEvent.end == null) {
            newEvent.end = new DateTime(newEvent.start).plusMinutes(30).toDate();
        }
        newEvent.endsSameDay = endsSameDay(newEvent.start, newEvent.end);
        newEvent.save();
        return redirect("/events");
    }


    /**
     * Dislays form for editing existing event
     * @param id Long
     * @return Result
     */
    public static Result edit(Long id) {
        Event event = Event.find.byId(id);
        Form<Event> eventForm = Form.form(Event.class).fill(event);
        return ok(formEdit.render(id, eventForm, event));
    }


    /**
     * Save new event in DB (a.k.a. submit action in other examples)
     * @param id Long
     * @return Result
     */
    public static Result update(Long id) {
        Form<Event> eventForm = Form.form(Event.class).bindFromRequest();
        if (eventForm.hasErrors()) {
            return badRequest(formEdit.render(id, eventForm, Event.find.byId(id)));
        }
        Event updatedEvent = eventForm.get();
        updatedEvent.allDay = updatedEvent.allDay != null;
        if (updatedEvent.end == null) {
            updatedEvent.end = new DateTime(updatedEvent.start).plusMinutes(30).toDate();
        }
        updatedEvent.endsSameDay = endsSameDay(updatedEvent.start, updatedEvent.end);
        updatedEvent.update(id);
        return redirect("/events");
    }


    /**
     * Deletes event
     * @param id Long
     * @return Result
     */
    public static Result delete(Long id) {
        Event.find.ref(id).delete();
        return redirect("/events");
    }


    /**
     * Adds event after clicking on calendar
     * @return Result
     */
    public static Result addByAjax() {
        Form<Event> eventForm = Form.form(Event.class).bindFromRequest();
        Event newEvent = eventForm.get();
        newEvent.endsSameDay = endsSameDay(newEvent.start, newEvent.end);
        newEvent.save();

        if (eventForm.hasErrors()){
            return badRequest("There was some errors in form");
        }

        Map<String, String> result = new HashMap<String, String>();
        result.put("id", newEvent.id.toString());
        result.put("url", routes.AdminController.edit(newEvent.id).toString());

        return ok(play.libs.Json.toJson(result));
    }


    /**
     * Saves in DB date changed by event drag
     * @return Result
     */
    public static Result move() {

        Long id = Long.valueOf(Form.form().bindFromRequest().get("id"));
        int dayDelta = Integer.parseInt(Form.form().bindFromRequest().get("dayDelta"));
        int minuteDelta = Integer.parseInt(Form.form().bindFromRequest().get("minuteDelta"));

        Event event = Event.find.byId(id);
        event.start = new DateTime(event.start).plusDays(dayDelta).plusMinutes(minuteDelta).toDate();
        event.end = new DateTime(event.end).plusDays(dayDelta).plusMinutes(minuteDelta).toDate();
        event.allDay = Boolean.valueOf(Form.form().bindFromRequest().get("allDay"));
        event.endsSameDay = endsSameDay(event.start, event.end);
        event.update();

//        if (thereIsSomeError){
//            return badRequest("You can not move this event!");
//        }

        return ok("changed");
    }

    /**
     * Saves in DB end date changed by event resize
     * @return Result
     */
    public static Result resize()
    {

        Long id = Long.valueOf(Form.form().bindFromRequest().get("id"));
        int dayDelta = Integer.parseInt(Form.form().bindFromRequest().get("dayDelta"));
        int minuteDelta = Integer.parseInt(Form.form().bindFromRequest().get("minuteDelta"));

        Event event = Event.find.byId(id);
        event.end = new DateTime(event.end).plusDays(dayDelta).plusMinutes(minuteDelta).toDate();
        event.endsSameDay = endsSameDay(event.start, event.end);
        event.update();

//        if (thereIsSomeError){
//            return badRequest("You can not resize this event!");
//        }

        return ok("changed");
    }


//======================================================================================================================
// Refactoring reports


    public Result deleteWeeklyReport(Long id) {
        WeeklyReport.findWeeklyReportById(id).delete();
        return redirect("/listWeeklyReport");
    }

    public Result deleteField(Long id) {
        Field.findFieldById(id).delete();
        return redirect("/admin/createdaily");
    }

    public Result deleteWeeklyField(Long id) {
        WeeklyField.findFielWeeklydById(id).delete();
        return redirect("/admin/createweekly");
    }

    public Result genWeeklyField() {
        return ok(views.html.admins.setingsweeklyreport.render(WeeklyField.getFinder().all()));
    }

    public Result saveWeeklyField() {
        Form<WeeklyField> fieldWeeklyForm = Form.form(WeeklyField.class);
        WeeklyField wf = new WeeklyField();
        String n = fieldWeeklyForm.bindFromRequest().field("scriptWeeklyName").value();
        String nameWF = AdminController.firstUpperCase(n);
        wf.setName(nameWF);
        try {
            wf.save();
        }catch (PersistenceException e){
            flash("warning", "Name field already exist");
        }
        return redirect("/admin/createweekly");
    }

    public static String firstUpperCase(String name) {
        String s = name.substring(0, 1);
        String s1 = s.toUpperCase();
        String s2 = name.substring(1, name.length());
        String name1 = s1 + s2;
        return name1;
    }

    /**
     * See tables of weekly reports
     *
     * @return
     */
    public Result listWeeklyReport() {
        return ok(newTableWeekly.render(ReportWeeklyField.getFinderReportWeeklyField().all(), WeeklyReport.getFinder().all(), WeeklyField.getFinder().all()));
    }

    public Result openReport(Long id) {
        return ok(openReports.render(DailyReport.findDailyReportById(id), ReportField.getFinder().all()));
    }

    public Result openNextReport(Long l) {
        Long id;
        l++;
        id = l;
        if(DailyReport.findDailyReportById(id) == null){
            flash("success", "The last report from the table");
            l--;
            id = l;
            return ok(openReports.render(DailyReport.findDailyReportById(id), ReportField.getFinder().all()));

        }
        return ok(openReports.render(DailyReport.findDailyReportById(id), ReportField.getFinder().all()));
    }

    public Result openPreviousReport(Long l) {
        Long id;
        l--;
        id = l;
        if(DailyReport.findDailyReportById(id) == null){
            flash("success", "The first report of the table");
            l++;
            id = l;
            return ok(openReports.render(DailyReport.findDailyReportById(id), ReportField.getFinder().all()));

        }
        return ok(openReports.render(DailyReport.findDailyReportById(id), ReportField.getFinder().all()));
    }

    public static String cutText(String text, int length){
        if (text.length() > length) {
            text = text.substring(0, length);
            return text + "..." ;
        }
        return text;
    }

    public Result pdfWeeklyTable() {
        return pdfGenerator.ok(weeklypdf.render(ReportWeeklyField.getFinderReportWeeklyField().all(), WeeklyReport.getFinder().all(), WeeklyField.getFinder().all()), Configuration.root().getString("application.host"));
    }

    public Result pdfDailyTable() {
        return pdfGenerator.ok(dailypdf.render(ReportField.getFinder().all(), DailyReport.getFinder().all(), Field.getFinder().all()), Configuration.root().getString("application.host"));
    }

    public Result pdfDailyReports(Long id) {
        return pdfGenerator.ok(pdfopenreport.render(DailyReport.findDailyReportById(id), ReportField.getFinder().all()), Configuration.root().getString("application.host"));
    }



//    Radnja u pripremi
//    public Result pdfWeeklyReports() {
//        return pdfGenerator.ok();
//    }

}
