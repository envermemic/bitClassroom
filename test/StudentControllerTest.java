import java.util.*;

import controllers.routes;
import com.fasterxml.jackson.databind.JsonNode;

import helpers.SessionHelper;
import models.Post;
import models.course.Course;
import models.course.CourseUser;
import models.report.DailyReport;
import models.user.Role;
import models.user.User;
import org.joda.time.DateTime;
import org.junit.*;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import play.api.routing.Router;
import play.Logger;
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
import static org.fest.assertions.Assertions.*;

import org.openqa.selenium.Cookie;

/**
 * Created by becir.omerbasic on 23/09/15.
 */
public class StudentControllerTest {

    @Before
    public void configureDatabase() {
        fakeApplication(inMemoryDatabase());
    }


    @Test
    public void testJoin() {
        running(testServer(9000, fakeApplication(inMemoryDatabase())), new HtmlUnitDriver(), new Callback<TestBrowser>() {
            @Override
            public void invoke(TestBrowser testBrowser) throws Throwable {
                testBrowser.goTo("http://localhost:9000/login");

                assertFalse(testBrowser.pageSource().contains("successfully logged in"));

                testBrowser.fill("#inputEmail").with("becir.omerbasic@bitcamp.ba");
                testBrowser.fill("#inputPassword").with("Becir123");
                testBrowser.click("#login");
                testBrowser.goTo("http://localhost:9000/user/courses");

                String disabled = testBrowser.find("#courseSubmit1").getAttribute("disabled");
                testBrowser.submit("#courseSumbit1");
                assertEquals("true", disabled);

            }
        });
    }

}