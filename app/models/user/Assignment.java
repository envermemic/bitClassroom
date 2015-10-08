package models.user;

import com.avaje.ebean.Model;
import models.Post;

import javax.persistence.*;

/**
 * Created by S on 8.10.2015.
 */
@Entity
public class Assignment extends Model {

    private static Finder<Long, Assignment> finder = new Finder<>(Assignment.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", insertable = false)
    private Long id;

    @ManyToOne
    private User student;

    @ManyToOne
    private Post post;

    @Column(name = "homework_post_status", length = 1)
    private Integer homeworkPostStatus;

    public static Finder<Long, Assignment> getFinder() {
        return finder;
    }

    public Assignment(){

    }

    public Assignment(User student, Post post, Integer homeworkPostStatus) {
        this.student = student;
        this.post = post;
        this.homeworkPostStatus = homeworkPostStatus;
    }

    public static Assignment findAssigmentByPost(Long id, User student){
        Post post = Post.findPostById(id);
        return finder.where().eq("post", post).eq("student", student).findUnique();
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public Integer getHomeworkPostStatus() {
        return homeworkPostStatus;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public void setHomeworkPostStatus(Integer homeworkPostStatus) {
        this.homeworkPostStatus = homeworkPostStatus;
    }


}
