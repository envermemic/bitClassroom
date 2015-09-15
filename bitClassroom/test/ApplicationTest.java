import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;
import models.Post;
import models.user.User;
import org.junit.*;

import play.mvc.*;
import play.test.*;
import play.data.DynamicForm;
import play.data.validation.ValidationError;
import play.data.validation.Constraints.RequiredValidator;
import play.i18n.Lang;
import play.libs.F;
import play.libs.F.*;
import play.twirl.api.Content;

import static play.test.Helpers.*;
import static org.junit.Assert.*;


/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class ApplicationTest {

    @Before
    public void configureDatabase() {
        fakeApplication(inMemoryDatabase());
    }

    /**
     * Test application if it works or not.
     */
    @Test
    public void simpleCheck() {
        int a = 1 + 1;
        assertEquals(2, a);
    }

    @Test
    public void testNullPointer() {
        String s = "Not null";
        assertNotNull(s);
    }

    @Test
    public void testDatabase() {
        List<User> users = User.findAll();
        assertNotNull(users);
    }

    /**
     * Test saving user in database.
     */
    @Test
    public void testSavingInDatabase() {
        User u = new User();
        u.setEmail("test@test.com");
        u.setPassword("passwordtest");
        u.setFirstName("name");
        u.save();
    }

    /**
     * Test if user saved in testSavingInDatabase
     * method can be loaded from database.
     */
    @Test
    public void testSavingAndLoadingFromDatabase() {
        User temp = new User();
        temp.setEmail("email@email.com");
        temp.setPassword("password");
        temp.setFirstName("username");

        User u = User.findByEmail("email@email.com");
        assertNull(u);
    }

    /**
     * Expected not to find user with this id
     */
    @Test
    public void testNonexistentUser() {
        User u = User.findById(90000000000L);
        assertNull(u);
    }

    @Test
    public void testFindPostByID(){
        User u = new User();
        u.setEmail("email@email.com");
        u.setPassword("password");
        u.setFirstName("username");
        u.save();

        Post p = new Post();
        p.setTitle("title");
        p.setContent("content");
        p.setUser(u);
        p.setVisibleToMentors(true);
        p.save();

        Post post = Post.findPostById(1L);

        assertEquals(post, p);
    }

    @Test
    public void testFindPostByUser(){
        User u = new User();
        u.setEmail("email@email.com");
        u.setPassword("password");
        u.setFirstName("username");
        u.save();

        Post p = new Post();
        p.setTitle("title");
        p.setContent("content");
        p.setUser(u);
        p.setVisibleToMentors(true);
        p.save();

        List<Post> posts = Post.findPostsByUser(u);

        assertNotNull(posts);
        assertEquals(p, posts.get(0));
    }

    @Test
    public void testFindAllPosts(){
        User u = new User();
        u.setEmail("email@email.com");
        u.setPassword("password");
        u.setFirstName("username");
        u.save();

        Post p = new Post();
        p.setTitle("title");
        p.setContent("content");
        p.setUser(u);
        p.setVisibleToMentors(true);
        p.save();

        User u1 = new User();
        u1.setEmail("email1@email.com");
        u1.setPassword("password1");
        u1.setFirstName("username1");
        u1.save();

        Post p1 = new Post();
        p1.setTitle("title1");
        p1.setContent("content1");
        p1.setUser(u1);
        p1.setVisibleToMentors(false);
        p1.save();

        List<Post> posts = Post.findAllPosts();

        assertEquals(2, posts.size());
    }

    @Test
    public void testGetDate(){
        User u = new User();
        u.setEmail("email@email.com");
        u.setPassword("password");
        u.setFirstName("username");
        u.save();

        Post p = new Post();
        p.setTitle("title");
        p.setContent("content");
        p.setUser(u);
        p.setVisibleToMentors(true);
        p.save();
    }


}
