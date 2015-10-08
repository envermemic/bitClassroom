package controllers.users;

import helpers.Authorization;
import helpers.SessionHelper;
import models.Post;
import models.PrivateMessage;
import models.course.Course;
import models.course.CourseUser;
import models.user.Assignment;
import models.user.Mentorship;
import models.user.User;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utility.UserConstants;

import java.util.List;

/**
 * Created by boris on 9/12/15.
 */
@Security.Authenticated(Authorization.Student.class)
public class StudentController extends Controller {


    /**
     * Check if user is joined to the course
     * Get from request course id
     * Get user from session
     * If user is already joined then error will be show and he will be redirect to /user/courses
     * otherwise it will be show success message and alse he will be redirected to /user/courses
     */
    public Result joinCourse() {
        DynamicForm dynamicForm = Form.form().bindFromRequest();
        dynamicForm.bindFromRequest(request());
        User u = SessionHelper.currentUser(ctx());
        Course c = Course.findById(Long.valueOf(dynamicForm.get("course-id")));
        List<Course> courses = CourseUser.allUserCourses(u);
        for(Course course: courses){
            if(course.getId() == c.getId()) {
                flash("error", "You already join to " + c.getName());
                return redirect("/user/courses");
            }
        }

        CourseUser courseUser = new CourseUser();
        courseUser.setCourse(c);
        courseUser.setUser(u);
        courseUser.setStatus(0);
        if (courseUser != null) {
            courseUser.save();
        }
        flash("success", "You successfuly applied to " + c.getName());
        return redirect("/user/courses");
    }


    /**
     *  Check if active user have access to join specified course
     *  If method returns "1" then access is denied otherwise user have possibility to join to the course
     */
    public Result haveCourseAccess() {
        DynamicForm form = Form.form().bindFromRequest();
        Long courseId = Long.parseLong(form.data().get("courseId"));
        Course c = Course.findById(courseId);
        List<Course> courses = CourseUser.allUserCourses(SessionHelper.currentUser(ctx()));
        for(Course course: courses){
            if(course.getId() == c.getId()) {
                return ok("1");
            }
        }
        return ok("2");
    }

    public Result sendStartMessage() {

        DynamicForm form = Form.form().bindFromRequest();
        String postId = form.data().get("postId");
        String courseId = form.data().get("courseId");
        Course c = Course.findById(Long.parseLong(courseId));
        Post p = Post.findPostById(Long.parseLong(postId));
        User sender = SessionHelper.currentUser(ctx());
        User receiver = Mentorship.findMentorByUser(sender);
        User receiverTeacher = User.findByName(c.getTeacher().substring(0, c.getTeacher().indexOf(' ')+ 1));

              String subject = "Announcement";
              String content = "I'm started task " + p.getTitle() + " on the course " + c.getName();
        if(receiver != null) {
              PrivateMessage privMessage = PrivateMessage.create(subject, content, sender, receiver);
              privMessage.setStatus(0);
              receiver.getMessages().add(privMessage);
              receiver.save();
          }
        PrivateMessage teacherMessage = PrivateMessage.create(subject,content,sender,receiverTeacher);
        teacherMessage.setStatus(0);
        receiverTeacher.getMessages().add(teacherMessage);
        receiverTeacher.save();

        sender.setHomeworkStatus(1);
       // p.setHomeworkPostStatus(1);

        Assignment as = new Assignment();
        as.setPost(p);
        as.setStudent(sender);
        as.setHomeworkPostStatus(1);
        as.save();

       // p.save();
        sender.save();


        return redirect("/user/class/"+courseId);
    }

    public Result sendFinishMessage() {

        DynamicForm form = Form.form().bindFromRequest();
        String postId = form.data().get("postId");
        String courseId = form.data().get("courseId");
        Course c = Course.findById(Long.parseLong(courseId));
        // Logger.info(c.getName());
        // Logger.info(c.getTeacher());
        Post p = Post.findPostById(Long.parseLong(postId));
        //flash("succes", c.getName() + "  " + c.getTeacher() +  "  " + p.getTitle());
        User sender = SessionHelper.currentUser(ctx());
        User receiver = Mentorship.findMentorByUser(sender);
        User receiverTeacher = User.findByName(c.getTeacher().substring(0, c.getTeacher().indexOf(' ')+ 1));
        String subject = "Announcement";
        String content = "I'm finished task " + p.getTitle() + " on the course " + c.getName();

        if ( receiver != null) {

            PrivateMessage privMessage = PrivateMessage.create(subject, content, sender, receiver);
            privMessage.setStatus(0);
            receiver.getMessages().add(privMessage);
            receiver.save();
        }
        PrivateMessage teacherMessage = PrivateMessage.create(subject,content,sender,receiverTeacher);
        teacherMessage.setStatus(0);
        receiverTeacher.getMessages().add(teacherMessage);
        receiverTeacher.save();

        sender.setHomeworkStatus(2);
        sender.save();

        Assignment as = new Assignment();
        as.setPost(p);
        as.setStudent(sender);
        as.setHomeworkPostStatus(1);
        as.save();
     //   p.setHomeworkPostStatus(2);
     //   p.save();


        return redirect("/user/class/"+courseId);
    }
    
    public Result test() {
        return ok("you are student");
    }
}