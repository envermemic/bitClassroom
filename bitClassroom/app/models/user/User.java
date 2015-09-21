package models.user;

import com.avaje.ebean.Model;
import models.Post;
import models.PrivateMessage;
import models.course.Course;
import models.course.CourseUser;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import utility.UserConstants;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by boris on 9/12/15.
 */
@Entity
@Table(name = "user")
public final class User extends Model {

    private static Finder<Long, User> finder = new Finder<>(User.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", insertable = false)
    private Long id;
    @Column(name = "email", unique = true, updatable = false, length = 100)
    private String email;
    @Column(name = "password", length = 100)
    private String password;
    @Column(name = "first_name", updatable = false, length = 100)
    private String firstName;
    @Column(name = "last_name", updatable = false, length = 100)
    private String lastName;
    @Column(name = "nick_name", length = 50)
    private String nickName;
    @Column(name = "birth_date")
    private String birthDate;
    @Column(name = "gender", length = 1)
    private Integer gender;
    @Column(name = "location", length = 150)
    private String location;
    @Column(name = "home_phone", length = 15)
    private String homePhone;
    @Column(name = "cell_phone", length = 15)
    private String cellPhone;
    @Column(name = "website")
    private String website;
    @Column(name = "skype")
    private String skype;
    @Column(name = "facebook")
    private String facebook;
    @Column(name = "twitter")
    private String twitter;
    @Column(name = "youtube")
    private String youtube;
    @Column(name = "picture")
    private String profilePicture = "default.jpg";
    @Column(name = "status", length = 1)
    private Integer status = UserConstants.INACTIVE;
    @Column(name = "create_date", updatable = false, columnDefinition = "datetime")
    private DateTime creationDate = new DateTime();
    @Column(name = "created_by", updatable = false)
    private String createdBy;
    @Column(name = "update_date", columnDefinition = "datetime")
    private DateTime updateDate;
    @Column(name = "updated_by")
    private String updatedBy;
    @ManyToMany
    private List<Role> roles = new ArrayList<>();
    @OneToMany
    private List<Post> posts = new ArrayList<>();
    @OneToMany(mappedBy="sender", cascade=CascadeType.ALL)
    private List<PrivateMessage> privateMessage;


    /**
     * Default empty constructor for Ebean
     */
    public User() {
    }

    /**
     * Consturctor used to create User by admin.
     *
     * @param email    <code>String</code> type value of email
     * @param password <code>String</code> type value of password
     */
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    /**
     * Returns all users from database as List.
     *
     * @return <code>List</code> type value of all users from database
     */
    public static List<User> findAll() {
        return finder.all();
    }

    /**
     * Returns User from database for inputed email and password.
     *
     * @param email    <code>String</code> type value of User email
     * @param password <code>String</code> type value of User password
     * @return <code>User</code> type object
     */
    public static User findByEmailAndPassword(String email, String password) {
        return finder.where().eq("email", email.toLowerCase()).eq("password", password).findUnique();
    }

    /**
     * Returns User from database for inputed id
     *
     * @param id <code>Long</code> type value of User id
     * @return <code>User</code> type object
     */
    public static User findById(Long id) {
        return finder.byId(id);
    }

    /**
     * Returns User from database for inputed email
     *
     * @param email <code>String</code> type value
     * @return <code>User</code> type object
     */
    public static User findByEmail(String email) {
        return finder.where().eq("email", email).findUnique();
    }

    /**
     * Returns finder on User model
     *
     * @return
     */
    public static Finder<Long, User> getFinder() {
        return finder;
    }

    /**
     * Overrided toString method returns User first and last name, birth date,
     * gender, location, cell phone and website.
     *
     * @return <code>String</code> type value of User information
     */
    @Override
    public String toString() {
        String g = "female";
        if (gender.equals(1) && gender != null) {
            g = "male";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(firstName).append(" ");
        sb.append(lastName).append(" ");
        sb.append("Born ").append(birthDate).append(" ");
        sb.append("Gender: ").append(g).append(" ");
        sb.append("Location: ").append(location).append(" ");
        sb.append("Phone: ").append(cellPhone).append(" ");
        sb.append("Web: ").append(website);
        sb.append("STATUS ").append(status);
        return sb.toString();
    }


    /* ONLY GETTERS AND SETTERS FOR USER CLASS BELLOW */

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getHomePhone() {
        return homePhone;
    }

    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }

    public String getCellPhone() {
        return cellPhone;
    }

    public void setCellPhone(String cellPhone) {
        this.cellPhone = cellPhone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getFormattedCreationDate() {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("HH:mm (dd.MM.yyyy)");
        return dtf.print(creationDate);
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getFormattedUpdateDate() {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("HH:mm (dd.MM.yyyy)");
        return dtf.print(updateDate);
    }

    public void setCreationDate(DateTime creationDate) {
        this.creationDate = creationDate;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public void setUpdateDate(DateTime updateDate) {
        this.updateDate = updateDate;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getSkype() {
        return skype;
    }

    public void setSkype(String skype) {
        this.skype = skype;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getYoutube() {
        return youtube;
    }

    public void setYoutube(String youtube) {
        this.youtube = youtube;
    }

    public List<PrivateMessage> getPrivateMessage() {
        return privateMessage;
    }

    public void setPrivateMessage(List<PrivateMessage> privateMessage) {
        this.privateMessage = privateMessage;
    }

    public static boolean deleteUser(Long id) {

        finder.deleteById(id);
        return true;
    }

}