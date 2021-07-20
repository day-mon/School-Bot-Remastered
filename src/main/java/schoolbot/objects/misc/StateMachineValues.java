package schoolbot.objects.misc;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.Nullable;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.misc.interfaces.Paginatable;
import schoolbot.objects.misc.interfaces.StateMachine;
import schoolbot.objects.school.Assignment;
import schoolbot.objects.school.Classroom;
import schoolbot.objects.school.Professor;
import schoolbot.objects.school.School;

import java.util.List;
import java.util.stream.Collectors;

public class StateMachineValues
{
      private GuildMessageReceivedEvent messageReceivedEvent;
      private final long authorId;
      private final long channelId;
      private String updateColumn;
      private final JDA jda;
      private StateMachine machine;
      private CommandEvent event;
      private List<School> schoolList;
      private List<School> pittClass;
      private List<Classroom> classroomList;
      private List<Professor> professorList;
      private List<Assignment> assignmentList;
      private School school;
      private Classroom classroom;
      private Professor professor;
      private Assignment assignment;
      private int state = 1;

      public StateMachineValues(CommandEvent event, StateMachine machine)
      {
            this.event = event;
            this.schoolList = event.getGuildSchools();
            this.pittClass = event.getGuildSchools()
                    .stream()
                    .filter(School::isPittSchool)
                    .collect(Collectors.toList());
            this.classroomList = event.getGuildClasses();
            this.authorId = event.getUser().getIdLong();
            this.channelId = event.getChannel().getIdLong();
            this.jda = event.getJDA();
            this.school = new School();
            this.classroom = new Classroom();
            this.assignment = new Assignment();
            this.professor = new Professor();
            this.professorList = null;
            this.assignmentList = null;
            this.machine = machine;

      }

      public StateMachineValues(CommandEvent event)
      {
            this.event = event;
            this.schoolList = event.getGuildSchools();
            this.pittClass = event.getGuildSchools()
                    .stream()
                    .filter(School::isPittSchool)
                    .collect(Collectors.toList());
            this.classroomList = event.getGuildClasses();
            this.authorId = event.getUser().getIdLong();
            this.channelId = event.getChannel().getIdLong();
            this.jda = event.getJDA();
            this.messageReceivedEvent = this.event.getEvent();
            this.school = new School();
            this.classroom = new Classroom();
            this.assignment = new Assignment();
            this.professor = new Professor();
            this.professorList = null;
            this.assignmentList = null;
            this.machine = null;
      }

      public GuildMessageReceivedEvent getMessageReceivedEvent()
      {
            return messageReceivedEvent;
      }

      public Professor getProfessor()
      {
            return professor;
      }

      public void setProfessor(Professor professor)
      {
            this.professor = professor;
      }

      public CommandEvent getCommandEvent()
      {
            return event;
      }

      public void setEvent(CommandEvent event)
      {
            this.event = event;
      }

      public List<School> getSchoolList()
      {
            return schoolList;
      }

      public void setSchoolList(List<School> schoolList)
      {
            this.schoolList = schoolList;
      }

      public List<Assignment> getAssignmentList()
      {
            return assignmentList;
      }


      public List<Professor> getProfessorList()
      {
            return professorList;
      }

      public void setProfessorList(List<Professor> professorList)
      {
            this.professorList = professorList;
      }

      public List<Classroom> getClassroomList()
      {
            return classroomList;
      }

      public void setClassroomList(List<Classroom> classroomList)
      {
            this.classroomList = classroomList;
      }

      public void setMessageReceivedEvent(GuildMessageReceivedEvent messageReceivedEvent)
      {
            this.messageReceivedEvent = messageReceivedEvent;
      }

      public void setMachine(StateMachine machine)
      {
            this.machine = machine;
      }

      @Nullable
      public Assignment getAssignment()
      {
            return assignment;
      }

      public void setAssignment(Assignment assignment)
      {
            this.assignment = assignment;
      }

      @Nullable
      public Classroom getClassroom()
      {
            return classroom;
      }

      public void setClassroom(Classroom classroom)
      {
            if (school == null)
            {
                  this.school = classroom.getSchool();
            }
            this.classroom = classroom;
            this.assignmentList = classroom.getAssignments();
      }

      public JDA getJda()
      {
            return jda;
      }

      @Nullable
      public School getSchool()
      {
            return school;
      }

      public void setSchool(School school)
      {
            this.school = school;
      }


      public List<School> getPittClass()
      {
            return pittClass;
      }

      public void setPittClass(List<School> pittClass)
      {
            this.pittClass = pittClass;
      }

      @Nullable
      public StateMachine getMachine()
      {
            return machine;
      }

      public <T extends Paginatable> void setValue(T obj)
      {
            setAllValues(obj);
      }

      private <T extends Paginatable> void setAllValues(T obj)
      {
            String className = obj.getClass().getSimpleName();

            switch (className)
            {
                  case "School" -> {
                        var school = (School) obj;
                        classroomList = school.getClassroomList();
                        professorList = school.getProfessorList();
                        this.school = school;
                  }

                  case "Classroom" -> {
                        var classroom = (Classroom) obj;

                        var school = classroom.getSchool();

                        this.school = school;
                        this.classroom = classroom;
                        this.professorList = school.getProfessorList();
                        this.assignmentList = classroom.getAssignments();
                  }

                  case "Assignment" -> {
                        var assignment = (Assignment) obj;
                        var school = assignment.getClassroom().getSchool();

                        this.school = school;
                        setClassroom(assignment.getClassroom());
                        this.assignment = assignment;

                        professorList = school.getProfessorList();
                        classroomList = school.getClassroomList();
                  }

                  case "Professor" -> {
                        var professor = (Professor) obj;
                        var school = professor.getProfessorsSchool();

                        this.school = school;
                        this.professor = professor;

                        classroomList = school.getClassroomList();
                        professorList = school.getProfessorList();

                  }

                  default -> throw new IllegalStateException(String.format("%s is not supported", className));

            }
      }

      public void incrementMachineState()
      {
            state += 1;
      }

      public int getState()
      {
            return state;
      }

      public void setState(int state)
      {
            this.state = state;
      }

      public String getUpdateColumn()
      {
            return updateColumn;
      }

      public void setUpdateColumn(String updateColumn)
      {
            this.updateColumn = updateColumn;
      }

      @SuppressWarnings("unchecked")
      public <T extends Paginatable> void setList(List<T> list)
      {
            String className = list.get(0).getClass().getSimpleName();

            switch (className)
            {
                  case "School" -> schoolList = (List<School>) list;

                  case "Professor" -> professorList = (List<Professor>) list;

                  case "Classroom" -> classroomList = (List<Classroom>) list;

                  case "Assignment" -> assignmentList = (List<Assignment>) list;

                  default -> throw new IllegalStateException(String.format("%s is not supported", className));


            }
      }


}


