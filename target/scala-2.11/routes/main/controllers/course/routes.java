
// @GENERATOR:play-routes-compiler
// @SOURCE:/home/boris/Desktop/backupProject-2760526d3667371690d592920007b0ee39f1bccd/bitClassroom/conf/routes
// @DATE:Wed Sep 23 19:50:14 CEST 2015

package controllers.course;

import router.RoutesPrefix;

public class routes {
  
  public static final controllers.course.ReverseCourseController CourseController = new controllers.course.ReverseCourseController(RoutesPrefix.byNamePrefix());

  public static class javascript {
    
    public static final controllers.course.javascript.ReverseCourseController CourseController = new controllers.course.javascript.ReverseCourseController(RoutesPrefix.byNamePrefix());
  }

}