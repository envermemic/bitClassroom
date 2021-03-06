package controllers.users;

import helpers.Authorization;
import helpers.SessionHelper;
import models.course.CourseUser;
import models.report.ReportWeeklyField;
import models.report.WeeklyField;
import models.user.Mentorship;
import models.user.User;
import models.report.WeeklyReport;
import org.joda.time.DateTime;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utility.UserConstants;
import views.html.dailyreports.weeklyreport;
import views.html.index;
import java.util.List;

/**
 * Created by boris on 9/12/15.
 */
@Security.Authenticated(Authorization.Mentor.class)
public class MentorController extends Controller {

    private static final Form<WeeklyReport> reportForm = Form.form(WeeklyReport.class);

    public Result weeklyReport() {
        DynamicForm dynamicForm = new DynamicForm();
        dynamicForm.bindFromRequest(request());
        dynamicForm.get("1");

        List<WeeklyField> weeklyFields = WeeklyField.getFinder().findList();
        List<Mentorship> mentorship = Mentorship.getFinder().where().eq("mentor_id", SessionHelper.currentUser(ctx()).getId()).eq("status", UserConstants.ACTIVE_MENTORSHIP).findList();
        return ok(weeklyreport.render(reportForm, weeklyFields, mentorship));

    }

    public Result saveRaport() {
        User temp = SessionHelper.currentUser(ctx());
        List<WeeklyField> fields = WeeklyField.getFinder().findList();
        DynamicForm dynamicForm = Form.form().bindFromRequest();
        dynamicForm.bindFromRequest(request());

        WeeklyReport weeklyReport = new WeeklyReport();

        weeklyReport.setMentor(temp.getFirstName() + " " + temp.getLastName());
        weeklyReport.setCreateDate(new DateTime());
        weeklyReport.setDate(dynamicForm.get("date"));
        weeklyReport.setName(dynamicForm.get("title"));
        weeklyReport.setStudent(dynamicForm.get("student"));
        weeklyReport.setData(dynamicForm.get("data"));
        weeklyReport.save();

        for (WeeklyField field : fields) {
            ReportWeeklyField reportField = new ReportWeeklyField();
            reportField.setWeeklyReport(weeklyReport);
            String fieldId = dynamicForm.get(String.valueOf(field.getId()));
            reportField.setValue(fieldId);
            reportField.setWeeklyField(field);
            reportField.save();
        }
        User u = SessionHelper.currentUser(ctx());
        List<CourseUser> userc = CourseUser.getFinder().all();

        flash("success", "Your report has been sent");
        return ok(index.render(u, userc));

    }

    public Result test() {
        return ok("you are mentor");
    }

}