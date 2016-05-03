package ch.uzh.campus.data;

import javax.persistence.*;

@Deprecated //deprecated because it maps to the same DB table as StudentCourse
@Entity
@IdClass(CourseStudentPK.class)
@Table(name = "ck_student_course")
public class CourseStudent {

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    public Course getCourse() {
        return course;
    }

    public Student getStudent() {
        return student;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

}

