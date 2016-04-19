package ch.uzh.campus.data;

import javax.persistence.*;

@Entity
@IdClass(CourseLecturerPK.class)
@Table(name = "ck_lecturer_course")
public class CourseLecturer {

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "lecturer_id")
    private Lecturer lecturer;

    public Course getCourse() {
        return course;
    }

    public Lecturer getLecturer() {
        return lecturer;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public void setLecturer(Lecturer lecturer) {
        this.lecturer = lecturer;
    }

}
