package controllers.posts;

import com.avaje.ebean.Ebean;
import com.avaje.ebeaninternal.server.lib.util.Str;
import helpers.Authorization;
import helpers.SessionHelper;
import models.Post;
import models.course.CourseUser;
import models.user.Assignment;
import models.user.Role;
import models.user.User;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.Play;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;
import play.mvc.Security;
import utility.UserConstants;
import views.html.posts.assignmentView;
import views.html.posts.newpost;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import models.course.Course;


/**
 * Created by banjich on 9/14/15.
 */
@Security.Authenticated(Authorization.FullyActiveUser.class)
public class PostController extends Controller {

    private final Form<Post> postForm = Form.form(Post.class);

    private List<String> filesList = new ArrayList<>();

    /**
     * Adding post and refresh the page with added post
     */
    public Result addPost() {
        return ok(newpost.render(postForm, SessionHelper.currentUser(ctx()), CourseUser.allUserCourses(SessionHelper.currentUser(ctx()))));
    }

    public Result assignmentDetails(Long id){

        Post post = Post.findPostById(id);

        return ok(assignmentView.render(post));

    }

    /**
     * Getting http request
     * Getting user from session
     * Saving post in database
     */
    public Result savePost() {
        Form<Post> boundForm = postForm.bindFromRequest();
        String selectedCourse = boundForm.bindFromRequest().field("course").value();
        User user = SessionHelper.currentUser(ctx());

        Post post = new Post();
        MultipartFormData data = request().body().asMultipartFormData();
        List<MultipartFormData.FilePart> files = data.getFiles();

        if (files != null) {
            String fName = null;
            for (Http.MultipartFormData.FilePart filePart : files) {
                fName = filePart.getFilename();
                File file = filePart.getFile();

                try {
                    FileUtils.moveFile(file, new File(Play.application().path() + "/public/files/users/" + user.getFirstName() + "/" + fName));
                    filesList.add(fName);
                } catch (IOException e) {
                    Logger.info("Could not move file. " + e.getMessage());
                    flash("error", "Could not move file.");
                }
            }
                if(fName != null) {
                    post.setFiles(user.getFirstName() + "/" + fName);
                }
        }





        String title = boundForm.field("title").value();
        String message = boundForm.field("content").value();
        String link = boundForm.field("link").value();

        if(user != null && user.getRoles().size() > 0 && user.getRoles() != null){
            for(Role r: user.getRoles()){
                if(r.getId().equals(UserConstants.TEACHER)){
                    String date = boundForm.field("date").value();
                    String time = boundForm.field("time").value();
                    String mentor = boundForm.field("visible").value();
                    String type = boundForm.field("post-type").value();

                    Boolean visible = false;
                    if ("on".equals(mentor)) {
                        visible = true;
                    }


                    //type 0 announcement
                    Integer typeOfPost = 0;
                    if (type.equals("1")) {
                        typeOfPost = 1; //type 1 assignment




                    }

                    post.setPostType(typeOfPost);
                    post.setVisibleToMentors(visible);
                    post.setDate(date);
                    post.setTime(time);
                    post.save();
                }
            }
        }


        if (user != null && selectedCourse!= null) {
            Course course = Course.findById(Long.parseLong(selectedCourse));
            post.setTitle(title);
            post.setContent(message);
            post.setUser(user);
            post.setLink(link);
            post.setCourse(course);
            post.save();
            if (post.getPostType() == 1) {
                List<User> students = CourseUser.allUserFromCourse(course.getId());
                for(int i=0; i < students.size();  i++ ){
                    Assignment a = new Assignment();
                    a.setStudent(students.get(i));
                    a.setPost(post);
                    a.setHomeworkPostStatus(0);
                    a.save();
                }

            }
            course.getPosts().add(post);
            flash("success", "You successfully added new post.");
            return redirect("/user/class/" + Long.parseLong(selectedCourse)); // TODO add call to route for listing posts
        }
        flash("warning", "Could not save your post.");
        return redirect("/admin");

    }

}
