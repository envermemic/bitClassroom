package models;

import com.avaje.ebean.Model;
import models.course.Course;
import models.user.Assignment;
import models.user.User;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Medina Banjic
 */
@Entity
public final class Post extends Model {

    public static final int ASSIGNMENT = 1;
    public static final int ANNOUNCEMENT = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name="title")
    private String title;
    @Column(columnDefinition = "text", length = 3000)
    @Constraints.Required
    private String content;
    @Column(name="post_type")
    private Integer postType;
    @Column(name="visible_mentors")
    private Boolean visibleToMentors;
    @Column(name = "link")
    private String link;
    @Column(name="files")
    private String files;
    @Column(name = "date")
    private String date;
    @Column(name="time")
    private String time;
    @Column(name = "create_date", updatable = false, columnDefinition = "datetime")
    private DateTime createdDate = new DateTime();
    @ManyToOne
    private Course course;
    @ManyToOne
    private User user;
    @OneToMany
    private List<Assignment> assignments = new ArrayList<>();

    /**
     * Empty constructor for Ebean
     */
    public Post() {
    }

    public Post(String title, String content, Integer postType, Boolean visible, User user, String date, String time) {
        this.title = title;
        this.content = content;
        this.postType = postType;
        this.visibleToMentors = visible;
        this.user = user;
        this.date = date;
        this.time = time;

    }

    private static final Model.Finder<Long, Post> find = new Model.Finder<>(Post.class);

    /**
     * Finding all post for given user and returning them like a list
     * @param user
     * @return list of posts for given user
     */
    public static List<Post> findPostsByUser(final User user) {
        return find
                .where()
                .eq("user", user)
                .orderBy("id desc")
                .findList();
    }

    /**
     * Finding post for given id
     * @param id Long
     * @return return post
     */
    public static Post findPostById(Long id) {
        return find
                .where()
                .eq("id", id)
                .findUnique();
    }

    /**
     * Finding all posts and return them like a list
     * @return list of posts
     */
    public static List<Post> findAllPosts() {
        return find.orderBy("id desc").findList();
    }

    public static List<Post> findAllCoursePost(Long courseId) {
        return find
                .where()
                .eq("id", courseId)
                .orderBy("id asc")
                .findList();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public User getUser() {return user;}

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public Integer getPostType() {
        return postType;
    }

    public void setPostType(Integer postType) {
        this.postType = postType;
    }

    public Boolean getVisibleToMentors() {
        return visibleToMentors;
    }

    public void setVisibleToMentors(Boolean visibleToMentors) {
        this.visibleToMentors = visibleToMentors;
    }

    public String toString() {
        return "Post: " + title + " ["  +
                content + "]  " +
                " by: " + user.getFirstName();
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public void setFiles(String files) {
        this.files = files;
    }

    public String getFiles() {return files;}

    public String getCreateDate() {DateTimeFormatter dtf = DateTimeFormat.forPattern("HH:mm (dd.MM.yyyy)");
        return dtf.print(createdDate); }

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }


    public void setDate(String date) {this.date = date;}

    public String getDate() {
        return date;
    }

    public void setTime(String time){this.time = time;}

    public String getTime(){
        return time;
    }


    public List<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
    }
}